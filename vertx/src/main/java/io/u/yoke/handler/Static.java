///**
// * Copyright 2011-2014 the original author or authors.
// */
//package io.u.yoke.handler;
//
//import io.u.yoke.Context;
//import io.u.yoke.Handler;
//import io.u.yoke.VertxHandler;
//import io.u.yoke.http.Status;
//import io.u.yoke.util.MimeType;
//import io.vertx.core.AsyncResult;
//import io.vertx.core.AsyncResultHandler;
//import io.vertx.core.Vertx;
//import io.vertx.core.file.FileProps;
//import io.vertx.core.file.FileSystem;
//import io.vertx.core.json.JsonArray;
//import org.jetbrains.annotations.NotNull;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.TimeZone;
//
///**
// * # Static
// *
// * Static file server with the given ```root``` path. Optionaly will also generate index pages for directory listings.
// */
//public class Static implements VertxHandler<Context> {
//
//  /**
//   * SimpleDateFormat to format date objects into ISO format.
//   */
//  private final SimpleDateFormat ISODATE;
//
//  /**
//   * Cache for the HTML template of the directory listing page
//   */
//  private final String directoryTemplate;
//
//  /**
//   * Root directory where to look files from
//   */
//  private final String root;
//
//  /**
//   * Max age allowed for cache of resources
//   */
//  private final long maxAge;
//
//  /**
//   * Allow directory listing
//   */
//  private final boolean directoryListing;
//
//  /**
//   * Include hidden files (Hiden files are files start start with dot (.).
//   */
//  private final boolean includeHidden;
//
//  /**
//   * Create a new Static File Server Middleware
//   *
//   * <pre>
//   * new Yoke(...)
//   *   .use(new Static("webroot", 0, true, false));
//   * </pre>
//   *
//   * @param root             the root location of the static files in the file system (relative to the main Verticle).
//   * @param maxAge           cache-control max-age directive
//   * @param directoryListing generate index pages for directories
//   * @param includeHidden    in the directory listing show dot files
//   */
//  public Static(@NotNull String root, final long maxAge, final boolean directoryListing, final boolean includeHidden) {
//    // if the root is not empty it should end with / for convenience
//    if (!"".equals(root)) {
//      if (!root.endsWith("/")) {
//        root = root + "/";
//      }
//    }
//    this.root = root;
//    this.maxAge = maxAge;
//    this.includeHidden = includeHidden;
//    this.directoryListing = directoryListing;
//    this.directoryTemplate = Utils.readResourceToBuffer(getClass(), "directory.html").toString();
//
//    ISODATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
//    ISODATE.setTimeZone(TimeZone.getTimeZone("UTC"));
//  }
//
//  /**
//   * Create a new Static File Server Middleware that does not generate directory listings or hidden files
//   *
//   * <pre>
//   * new Yoke(...)
//   *   .use(new Static("webroot", 0));
//   * </pre>
//   *
//   * @param root   the root location of the static files in the file system (relative to the main Verticle).
//   * @param maxAge cache-control max-age directive
//   */
//  public Static(@NotNull final String root, final long maxAge) {
//    this(root, maxAge, false, false);
//  }
//
//  /**
//   * Create a new Static File Server Middleware that does not generate directory listings or hidden files and files
//   * are cache for 1 full day
//   *
//   * <pre>
//   * new Yoke(...)
//   *   .use(new Static("webroot"));
//   * </pre>
//   *
//   * @param root the root location of the static files in the file system (relative to the main Verticle).
//   */
//  public Static(@NotNull final String root) {
//    this(root, 86400000, false, false);
//  }
//
//  private Vertx vertx;
//
//  @Override
//  public void setVertx(Vertx vertx) {
//    this.vertx = vertx;
//  }
//
//  /**
//   * Create all required header so content can be cache by Caching servers or Browsers
//   *
//   * @param request
//   * @param props
//   */
//  private void writeHeaders(final YokeRequest request, final FileProps props) {
//
//    MultiMap headers = request.response().headers();
//
//    if (!headers.contains("etag")) {
//      headers.putAt("etag", "\"" + props.size() + "-" + props.lastModifiedTime().getTime() + "\"");
//    }
//
//    if (!headers.contains("date")) {
//      headers.putAt("date", ISODATE.format(new Date()));
//    }
//
//    if (!headers.contains("cache-control")) {
//      headers.putAt("cache-control", "public, max-age=" + maxAge / 1000);
//    }
//
//    if (!headers.contains("last-modified")) {
//      headers.putAt("last-modified", ISODATE.format(props.lastModifiedTime()));
//    }
//  }
//
//  /**
//   * Write a file into the response body
//   *
//   * @param ctx
//   * @param file
//   * @param props
//   */
//  private void sendFile(final Context ctx, final String file, final FileProps props) {
//    // write content type
//    String contentType = MimeType.getMime(file);
//    String charset = MimeType.getCharset(contentType);
//    request.response().setContentType(contentType, charset);
//    request.response().putHeader("Content-Length", Long.toString(props.size()));
//
//    // head support
//    if ("HEAD".equals(request.method())) {
//      request.response().end();
//    } else {
//      request.response().sendFile(file);
//    }
//  }
//
//  /**
//   * Generate Directory listing
//   *
//   * @param ctx
//   * @param dir
//   * @param next
//   */
//  private void sendDirectory(final Context ctx, final String dir, final Handler<Object> next) {
//    final FileSystem fileSystem = vertx.fileSystem();
//
//    fileSystem.readDir(dir, new AsyncResultHandler<String[]>() {
//      @Override
//      public void handle(AsyncResult<String[]> asyncResult) {
//        if (asyncResult.failed()) {
//          next.handle(asyncResult.cause());
//        } else {
//          String accept = request.getHeader("accept", "text/plain");
//
//          if (accept.contains("html")) {
//            String normalizedDir = dir.substring(root.length());
//            if (!normalizedDir.endsWith("/")) {
//              normalizedDir += "/";
//            }
//
//            String file;
//            StringBuilder files = new StringBuilder("<ul id=\"files\">");
//
//            for (String s : asyncResult.result()) {
//              file = s.substring(s.lastIndexOf('/') + 1);
//              // skip dot files
//              if (!includeHidden && file.charAt(0) == '.') {
//                continue;
//              }
//              files.append("<li><a href=\"");
//              files.append(normalizedDir);
//              files.append(file);
//              files.append("\" title=\"");
//              files.append(file);
//              files.append("\">");
//              files.append(file);
//              files.append("</a></li>");
//            }
//
//            files.append("</ul>");
//
//            StringBuilder directory = new StringBuilder();
//            // define access to root
//            directory.append("<a href=\"/\">/</a> ");
//
//            StringBuilder expandingPath = new StringBuilder();
//            String[] dirParts = normalizedDir.split("/");
//            for (int i = 1; i < dirParts.length; i++) {
//              // dynamic expansion
//              expandingPath.append("/");
//              expandingPath.append(dirParts[i]);
//              // anchor building
//              if (i > 1) {
//                directory.append(" / ");
//              }
//              directory.append("<a href=\"");
//              directory.append(expandingPath.toString());
//              directory.append("\">");
//              directory.append(dirParts[i]);
//              directory.append("</a>");
//            }
//
//            request.response().setContentType("text/html");
//            request.response().end(
//                directoryTemplate.replace("{title}", (String) request.get("title")).replace("{directory}", normalizedDir)
//                    .replace("{linked-path}", directory.toString())
//                    .replace("{files}", files.toString()));
//          } else if (accept.contains("json")) {
//            String file;
//            JsonArray json = new JsonArray();
//
//            for (String s : asyncResult.result()) {
//              file = s.substring(s.lastIndexOf('/') + 1);
//              // skip dot files
//              if (!includeHidden && file.charAt(0) == '.') {
//                continue;
//              }
//              json.addString(file);
//            }
//
//            request.response().end(json);
//          } else {
//            String file;
//            StringBuilder buffer = new StringBuilder();
//
//            for (String s : asyncResult.result()) {
//              file = s.substring(s.lastIndexOf('/') + 1);
//              // skip dot files
//              if (!includeHidden && file.charAt(0) == '.') {
//                continue;
//              }
//              buffer.append(file);
//              buffer.append('\n');
//            }
//
//            request.response().setContentType("text/plain");
//            request.response().end(buffer.toString());
//          }
//        }
//      }
//    });
//  }
//
//  /**
//   * Verify if a resource is fresh, fresh means that its cache headers are validated against the local resource and
//   * etags last-modified headers are still the same.
//   *
//   * @param ctx
//   * @return {boolean}
//   */
//  private boolean isFresh(final Context ctx) {
//    // defaults
//    boolean etagMatches = true;
//    boolean notModified = true;
//
//    // fields
//    String modifiedSince = request.getHeader("if-modified-since");
//    String noneMatch = request.getHeader("if-none-match");
//    String[] noneMatchTokens = null;
//    String lastModified = request.response().getHeader("last-modified");
//    String etag = request.response().getHeader("etag");
//
//    // unconditional request
//    if (modifiedSince == null && noneMatch == null) {
//      return false;
//    }
//
//    // parse if-none-match
//    if (noneMatch != null) {
//      noneMatchTokens = noneMatch.split(" *, *");
//    }
//
//    // if-none-match
//    if (noneMatchTokens != null) {
//      etagMatches = false;
//      for (String s : noneMatchTokens) {
//        if (etag.equals(s) || "*".equals(noneMatchTokens[0])) {
//          etagMatches = true;
//          break;
//        }
//      }
//    }
//
//    // if-modified-since
//    if (modifiedSince != null) {
//      try {
//        Date modifiedSinceDate = ISODATE.parse(modifiedSince);
//        Date lastModifiedDate = ISODATE.parse(lastModified);
//        notModified = lastModifiedDate.getTime() <= modifiedSinceDate.getTime();
//      } catch (ParseException e) {
//        notModified = false;
//      }
//    }
//
//    return etagMatches && notModified;
//  }
//
//  @Override
//  public void handle(@NotNull final Context ctx) {
//    if (!"GET".equals(request.method()) && !"HEAD".equals(request.method())) {
//      ctx.next();
//    } else {
//      String path = request.normalizedPath();
//      // if the normalized path is null it cannot be resolved
//      if (path == null) {
//        ctx.fail(Status.NOT_FOUND);
//        return;
//      }
//      // map file path from the request
//      // the final path is, root + request.path excluding mount
//      final String file = root + path.substring(mount.length());
//
//      if (!includeHidden) {
//        int idx = file.lastIndexOf('/');
//        String name = file.substring(idx + 1);
//        if (name.length() > 0 && name.charAt(0) == '.') {
//          ctx.next();
//          return;
//        }
//      }
//
//      final FileSystem fileSystem = vertx.fileSystem();
//
//      fileSystem.exists(file, asyncResult -> {
//        if (asyncResult.failed()) {
//          ctx.fail(asyncResult.cause());
//        } else {
//          if (!asyncResult.result()) {
//            // no static file found, let the next middleware handle it
//            ctx.next();
//          } else {
//            fileSystem.props(file, props -> {
//              if (props.failed()) {
//                ctx.fail(props.cause());
//              } else {
//                if (props.result().isDirectory()) {
//                  if (directoryListing) {
//                    // write cache control headers
//                    writeHeaders(request, props.result());
//                    // verify if we are still fresh
//                    if (isFresh(ctx)) {
//                      request.response().setStatusCode(304);
//                      ctx.end();
//                    } else {
//                      sendDirectory(request, file, next);
//                    }
//                  } else {
//                    // we are not listing directories
//                    ctx.next();
//                  }
//                } else {
//                  // write cache control headers
//                  writeHeaders(request, props.result());
//                  // verify if we are still fresh
//                  if (isFresh(ctx)) {
//                    request.response().setStatusCode(304);
//                    ctx.end();
//                  } else {
//                    sendFile(request, file, props.result());
//                  }
//                }
//              }
//            });
//          }
//        }
//      });
//    }
//  }
//}
