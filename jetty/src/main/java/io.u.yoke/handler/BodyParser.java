/**
 * Copyright 2011-2014 the original author or authors.
 */
package io.u.yoke.handler;

import io.u.yoke.Context;
import io.u.yoke.Handler;
import io.u.yoke.http.form.Form;
import io.u.yoke.http.Method;
import io.u.yoke.http.Status;
import io.u.yoke.http.impl.AbstractRequest;
import org.eclipse.jetty.server.Request;
import org.jetbrains.annotations.NotNull;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.*;
import java.util.*;

/**
 * # BodyParser
 * <p>
 * Parse getRequest bodies, supports *application/json*, *application/x-www-form-urlencoded*, and *multipart/form-data*.
 * <p>
 * Once data has been parsed the result is visible in the field `body` of the getRequest.
 * <p>
 * If the content type was *multipart/form-data* and there were uploaded files the files are ```files()``` returns
 * `Map&lt;String, HttpServerFileUpload&gt;`.
 * <p>
 * ### Limitations
 * <p>
 * Currently when parsing *multipart/form-data* if there are several files uploaded under the same name, only the last
 * is preserved.
 */
public class BodyParser implements Handler<Context> {

  /**
   * Location on the file system to store the uploaded files.
   */
  private final String uploadDir;

  /**
   * Instantiates a Body parser with a configurable upload directory.
   * <p>
   * <pre>
   *      Yoke yoke = new Yoke(...);
   *      yoke.use(new BodyParser("/upload"));
   * </pre>
   *
   * @param uploadDir upload directory path
   */
  public BodyParser(@NotNull String uploadDir) {
    this.uploadDir = uploadDir;
  }

  /**
   * Instantiates a Body parser using the system default temp directory.
   * <p>
   * <pre>
   *      Yoke yoke = new Yoke(...);
   *      yoke.use(new BodyParser());
   * </pre>
   */
  public BodyParser() {
    this(System.getProperty("java.io.tmpdir"));
  }

  /**
   * JMXHandler for the parser. When the getRequest method is GET or HEAD this is a Noop engine.
   * If not the engine verifies if there is a body and according to its header tries to
   * parse it as JSON, form data or multi part upload.
   *
   * @param ctx http yoke context
   */
  @Override
  public void handle(@NotNull final Context ctx) {
    final Method method = ctx.getRequest().getMethod();

    // GET and HEAD have no setBody
    if (method == Method.GET || method == Method.HEAD || !ctx.getRequest().hasBody()) {
      ctx.next();
    } else {

      final String contentType = ctx.get("Content-Type");

      final boolean isJSON = contentType != null && contentType.contains("application/json");
      final boolean isMULTIPART = contentType != null && contentType.contains("multipart/form-data");
      final boolean isURLENCODEC = contentType != null && contentType.contains("application/x-www-form-urlencoded");
      final ByteArrayOutputStream buffer = (!isMULTIPART && !isURLENCODEC) ? new ByteArrayOutputStream() : null;

      // enable the parsing at Jetty level
      final AbstractRequest abstractRequest = (AbstractRequest) ctx.getRequest();
      final HttpServletRequest nativeRequest = ctx.getRequest().getNativeRequest();

      if (isMULTIPART) {
        final MultipartConfigElement cfg = new MultipartConfigElement(uploadDir);
        nativeRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, cfg);

        try {
          final long limit = ctx.getRequest().getMaxLength();
          Collection<Part> parts = nativeRequest.getParts();
          int size = 0;

          if (parts != null) {
            for (Part part : parts) {
              size += part.getSize();
              if (limit != -1 && size >= limit) {
                ctx.putAt("canceled", true);
                ctx.fail(Status.PAYLOAD_TOO_LARGE);
                return;
              }

              File upload = File.createTempFile("yoke-upload-", "", new File(uploadDir));
              try (InputStream in = part.getInputStream()) {

                try (OutputStream out = new FileOutputStream(upload)) {
                  byte[] chunk = new byte[4096];
                  int bytesRead;
                  while ((bytesRead = in.read(chunk)) > 0) {
                    out.write(chunk, 0, bytesRead);
                  }

                  abstractRequest.putFile(part.getName(), upload);
                  ctx.getResponse().endHandler(v -> {
                    if (!upload.delete()) {
                      // TODO: log it
                    }
                  });
                }
              }
            }
          }
        } catch (IOException | ServletException e) {
          ctx.fail(e);
        }
      }

      if (buffer != null) {
        try (InputStream in = nativeRequest.getInputStream()) {
          long size = 0;
          final long limit = ctx.getRequest().getMaxLength();

          byte[] chunk = new byte[4096];
          int bytesRead;
          while ((bytesRead = in.read(chunk)) > 0) {
            if (limit != -1) {
              size += bytesRead;
              if (size < limit) {
                buffer.write(chunk, 0, bytesRead);
              } else {
                ctx.putAt("canceled", true);
                ctx.fail(Status.PAYLOAD_TOO_LARGE);
              }
            } else {
              buffer.write(chunk, 0, bytesRead);
            }
          }
        } catch (IOException e) {
          ctx.fail(e);
        }
      }

      if (isJSON) {
        if (buffer != null && buffer.size() > 0) {
          ((AbstractRequest) ctx.getRequest()).setBody(buffer.toByteArray());
          ctx.next();
        } else if (buffer != null && buffer.size() == 0) {
          // special case for IE and Safari than even for 0 content length, send content type header
          if (ctx.getRequest().getLength() == 0) {
            ((AbstractRequest) ctx.getRequest()).setBody(null);
            ctx.next();
          } else {
            ctx.fail(Status.BAD_REQUEST);
          }
        } else {
          ctx.fail(Status.BAD_REQUEST);
        }
      } else {
        if (buffer != null) {
          ((AbstractRequest) ctx.getRequest()).setBody(buffer.toByteArray());
        } else {
          ((AbstractRequest) ctx.getRequest()).setBody(new Form() {
            @Override
            public String getParam(String parameter) {
              return nativeRequest.getParameter(parameter);
            }

            @Override
            public Iterable<String> getParamValues(String parameter) {
              return Arrays.asList(nativeRequest.getParameterValues(parameter));
            }

            @Override
            public Iterable<String> getParams() {
              return () -> {
                final Enumeration<String> enumeration = nativeRequest.getParameterNames();
                return new Iterator<String>() {
                  @Override
                  public boolean hasNext() {
                    return enumeration.hasMoreElements();
                  }

                  @Override
                  public String next() {
                    return enumeration.nextElement();
                  }
                };
              };
            }
          });
        }
        if (!ctx.getAt("canceled", false)) {
          ctx.next();
        }
      }
    }
  }
}
