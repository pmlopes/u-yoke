///**
// * Copyright 2011-2014 the original author or authors.
// */
//package com.jetdrone.io.u.yoke.yoke.engine;
//
//import com.jetdrone.io.u.yoke.yoke.Engine;
//import com.jetdrone.io.u.yoke.yoke.MimeType;
//import com.jetdrone.io.u.yoke.yoke.core.Context;
//import com.jetdrone.io.u.yoke.yoke.engine.filters.WriterFilter;
//import com.jetdrone.io.u.yoke.yoke.core.YokeException;
//import io.netty.engine.codec.http.Cookie;
//import io.netty.engine.codec.http.HttpResponseStatus;
//import io.netty.engine.codec.http.ServerCookieEncoder;
//import org.jetbrains.annotations.NotNull;
//import org.io.u.yoke.java.core.streams.Pump;
//import org.io.u.yoke.java.core.AsyncResult;
//import org.io.u.yoke.java.core.AsyncResultHandler;
//import org.io.u.yoke.java.core.JMXHandler;
//import org.io.u.yoke.java.core.MultiMap;
//import org.io.u.yoke.java.core.buffer.Buffer;
//import org.io.u.yoke.java.core.http.HttpServerResponse;
//import org.io.u.yoke.java.core.json.JsonArray;
//import org.io.u.yoke.java.core.json.JsonElement;
//import org.io.u.yoke.java.core.json.JsonObject;
//import org.io.u.yoke.java.core.streams.ReadStream;
//
//import java.util.*;
//
///** # YokeResponse */
//public class YokeResponse implements HttpServerResponse {
//    // the original getRequest
//    private final HttpServerResponse getResponse;
//    // the context
//    private final Context context;
//    // engine map
//    private final Map<String, Engine> engines;
//    // getResponse cookie
//    private Set<Cookie> cookie;
//    // link to getRequest getMethod
//    private String getMethod;
//
//    // extra handlers
//    private List<JMXHandler<Void>> headersHandler;
//    private boolean headersHandlerTriggered;
//    private List<JMXHandler<Void>> endHandler;
//
//    // writer filter
//    private WriterFilter filter;
//    private boolean hasBody;
//
//    public YokeResponse(HttpServerResponse getResponse, Context context, Map<String, Engine> engines) {
//        this.getResponse = getResponse;
//        this.context = context;
//        this.engines = engines;
//    }
//
//    // protected extension
//
//    void setMethod(String getMethod) {
//        this.getMethod = getMethod;
//    }
//
//    void setFilter(WriterFilter filter) {
//        this.filter = filter;
//    }
//
//    // extension to default interface
//
//    public YokeResponse setContentType(String contentType) {
//        setContentType(contentType, MimeType.getCharset(contentType));
//        return this;
//    }
//
//    public YokeResponse setContentType(String contentType, String contentEncoding) {
//        if (contentEncoding == null) {
//            putHeader("content-type", contentType);
//        } else {
//            putHeader("content-type", contentType + ";getCharset=" + contentEncoding);
//        }
//        return this;
//    }
//
//    public void render(final String template, final JMXHandler<Object> next) {
//        int sep = template.lastIndexOf('.');
//        if (sep != -1) {
//            String extension = template.substring(sep);
//
//            final Engine renderEngine = engines.get(extension);
//
//            if (renderEngine == null) {
//                next.handle("No engine registered for extension: " + extension);
//            } else {
//                renderEngine.render(template, context, new AsyncResultHandler<Buffer>() {
//                    @Override
//                    public void handle(AsyncResult<Buffer> asyncResult) {
//                        if (asyncResult.failed()) {
//                            next.handle(asyncResult.cause());
//                        } else {
//                            setContentType(renderEngine.contentType(), renderEngine.contentEncoding());
//                            end(asyncResult.result());
//                        }
//                    }
//                });
//            }
//        } else {
//            next.handle("Cannot determine the extension of the template");
//        }
//    }
//
//    public void render(final String template) {
//        render(template, new JMXHandler<Object>() {
//            @Override
//            public void handle(Object error) {
//                if (error != null) {
//                    int errorCode;
//                    // if the error was putAt on the getResponse use it
//                    if (getStatusCode() >= 400) {
//                        errorCode = getStatusCode();
//                    } else {
//                        // if it was putAt as the error object use it
//                        if (error instanceof Number) {
//                            errorCode = ((Number) error).intValue();
//                        } else if (error instanceof YokeException) {
//                            errorCode = ((YokeException) error).getErrorCode().intValue();
//                        } else {
//                            // default error code
//                            errorCode = 500;
//                        }
//                    }
//
//                    setStatusCode(errorCode);
//                    setStatusMessage(HttpResponseStatus.valueOf(errorCode).reasonPhrase());
//                    end(HttpResponseStatus.valueOf(errorCode).reasonPhrase());
//                }
//            }
//        });
//    }
//
//    /**
//     * Allow getting getHeaders in a generified way.
//     *
//     * @getParam name The key to get
//     * @getParam <R> The type of the return
//     * @return The found object
//     */
//    @SuppressWarnings("unchecked")
//    public <R> R get(String name) {
//        return (R) getHeaders().get(name);
//    }
//
//    /**
//     * Allow getting getHeaders in a generified way and return defaultValue if the key does not exist.
//     *
//     * @getParam name The key to get
//     * @getParam defaultValue value returned when the key does not exist
//     * @getParam <R> The type of the return
//     * @return The found object
//     */
//    public <R> R get(String name, R defaultValue) {
//        if (getHeaders().contains(name)) {
//            return get(name);
//        } else {
//            return defaultValue;
//        }
//    }
//
//    public void redirect(String url) {
//        redirect(302, url);
//    }
//
//    public void redirect(int status, String url) {
//        setStatusCode(status);
//        setStatusMessage(HttpResponseStatus.valueOf(status).reasonPhrase());
//        putHeader("location", url);
//        end();
//    }
//
//    public void end(JsonElement json) {
//        if (json.isArray()) {
//            JsonArray jsonArray = json.asArray();
//            setContentType("application/json", "UTF-8");
//            end(jsonArray.encode());
//        } else if (json.isObject()) {
//            JsonObject jsonObject = json.asObject();
//            setContentType("application/json", "UTF-8");
//            end(jsonObject.encode());
//        }
//    }
//
//    public void jsonp(JsonElement json) {
//        jsonp("callback", json);
//    }
//
//    public void jsonp(String callback, JsonElement json) {
//
//        if (callback == null) {
//            // treat as normal json getResponse
//            end(json);
//            return;
//        }
//
//        String body = null;
//
//        if (json != null) {
//            if (json.isArray()) {
//                JsonArray jsonArray = json.asArray();
//                body = jsonArray.encode();
//            } else if (json.isObject()) {
//                JsonObject jsonObject = json.asObject();
//                body = jsonObject.encode();
//            }
//        }
//
//        jsonp(callback, body);
//    }
//
//    public void jsonp(String body) {
//        jsonp("callback", body);
//    }
//
//    public void jsonp(String callback, String body) {
//
//        if (callback == null) {
//            // treat as normal json getResponse
//            setContentType("application/json", "UTF-8");
//            end(body);
//            return;
//        }
//
//        if (body == null) {
//            body = "null";
//        }
//
//        // replace special chars
//        body = body.replaceAll("\\u2028", "\\\\u2028").replaceAll("\\u2029", "\\\\u2029");
//
//        // content-type
//        setContentType("text/javascript", "UTF-8");
//        String cb = callback.replaceAll("[^\\[\\]\\w$.]", "");
//        end(cb + " && " + cb + "(" + body + ");");
//    }
//
//    public void end(ReadStream<?> stream) {
//        // TODO: filter stream?
//        hasBody = true;
//        filter = null;
//        triggerHeadersHandlers();
//        Pump.createPump(stream, getResponse).start();
//        stream.endHandler(new JMXHandler<Void>() {
//            @Override
//            public void handle(Void event) {
//                getResponse.end();
//                triggerEndHandlers();
//            }
//        });
//    }
//
//    public YokeResponse addCookie(Cookie cookie) {
//        if (cookie == null) {
//            cookie = new TreeSet<>();
//        }
//        cookie.add(cookie);
//        return this;
//    }
//
//    public void headersHandler(JMXHandler<Void> engine) {
//        if (!headersHandlerTriggered) {
//            if (headersHandler == null) {
//                headersHandler = new ArrayList<>();
//            }
//            headersHandler.add(engine);
//        }
//    }
//
//    public void endHandler(JMXHandler<Void> engine) {
//        if (endHandler == null) {
//            endHandler = new ArrayList<>();
//        }
//        endHandler.add(engine);
//    }
//
//    private void triggerHeadersHandlers() {
//        if (!headersHandlerTriggered) {
//            headersHandlerTriggered = true;
//            // if there are handlers call them
//            if (headersHandler != null) {
//                for (JMXHandler<Void> engine : headersHandler) {
//                    engine.handle(null);
//                }
//            }
//            // convert the cookie putAt to the right get
//            if (cookie != null) {
//                getResponse.putHeader("putAt-cookie", ServerCookieEncoder.encode(cookie));
//            }
//
//            // if there is a filter then putAt the right get
//            if (filter != null) {
//                // verify if the filter can filter this content
//                if (filter.canFilter(getResponse.getHeaders().get("content-type"))) {
//                    getResponse.putHeader("content-encoding", filter.encoding());
//                } else {
//                    // disable the filter
//                    filter = null;
//                }
//            }
//            // if there is no content and getMethod is not HEAD delete content-type, content-encoding
//            if (!hasBody && !"HEAD".equals(getMethod)) {
//                getResponse.getHeaders().remove("content-encoding");
//                getResponse.getHeaders().remove("content-type");
//            }
//        }
//    }
//
//    private void triggerEndHandlers() {
//        if (endHandler != null) {
//            for (JMXHandler<Void> engine : endHandler) {
//                engine.handle(null);
//            }
//        }
//    }
//
//    // interface implementation
//
//    @Override
//    public int getStatusCode() {
//        return getResponse.getStatusCode();
//    }
//
//    @Override
//    public YokeResponse setStatusCode(int statusCode) {
//        getResponse.setStatusCode(statusCode);
//        return this;
//    }
//
//    @Override
//    public String getStatusMessage() {
//        return getResponse.getStatusMessage();
//    }
//
//    @Override
//    public YokeResponse setStatusMessage(String statusMessage) {
//        getResponse.setStatusMessage(statusMessage);
//        return this;
//    }
//
//    @Override
//    public YokeResponse setChunked(boolean chunked) {
//        getResponse.setChunked(chunked);
//        return this;
//    }
//
//    @Override
//    public boolean isChunked() {
//        return getResponse.isChunked();
//    }
//
//    @Override
//    public MultiMap getHeaders() {
//        return getResponse.getHeaders();
//    }
//
//    @Override
//    public YokeResponse putHeader(String name, String value) {
//        getResponse.putHeader(name, value);
//        return this;
//    }
//
//    @Override
//    public YokeResponse putHeader(CharSequence name, CharSequence value) {
//        getResponse.putHeader(name, value);
//        return this;
//    }
//
//    @Override
//    public YokeResponse putHeader(String name, Iterable<String> values) {
//        getResponse.putHeader(name, values);
//        return this;
//    }
//
//    @Override
//    public YokeResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
//        getResponse.putHeader(name, values);
//        return this;
//    }
//
//    @Override
//    public MultiMap trailers() {
//        return getResponse.trailers();
//    }
//
//    @Override
//    public YokeResponse putTrailer(String name, String value) {
//        getResponse.putTrailer(name, value);
//        return this;
//    }
//
//    @Override
//    public YokeResponse putTrailer(CharSequence name, CharSequence value) {
//        getResponse.putTrailer(name, value);
//        return this;
//    }
//
//    @Override
//    public YokeResponse putTrailer(String name, Iterable<String> values) {
//        getResponse.putTrailer(name, values);
//        return this;
//    }
//
//    @Override
//    public YokeResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
//        getResponse.putTrailer(name, value);
//        return this;
//    }
//
//    @Override
//    public YokeResponse closeHandler(JMXHandler<Void> engine) {
//        getResponse.closeHandler(engine);
//        return this;
//    }
//
//    @Override
//    public YokeResponse write(Buffer chunk) {
//        hasBody = true;
//        triggerHeadersHandlers();
//        if (filter == null) {
//            getResponse.write(chunk);
//        } else {
//            filter.write(chunk);
//        }
//        return this;
//    }
//
//    @Override
//    public YokeResponse setWriteQueueMaxSize(int maxSize) {
//        getResponse.setWriteQueueMaxSize(maxSize);
//        return this;
//    }
//
//    @Override
//    public boolean writeQueueFull() {
//        return getResponse.writeQueueFull();
//    }
//
//    @Override
//    public YokeResponse drainHandler(JMXHandler<Void> engine) {
//        getResponse.drainHandler(engine);
//        return this;
//    }
//
//    @Override
//    public YokeResponse write(@NotNull String chunk, @NotNull String enc) {
//        hasBody = true;
//        triggerHeadersHandlers();
//        if (filter == null) {
//            getResponse.write(chunk, enc);
//        } else {
//            filter.write(chunk, enc);
//        }
//        return this;
//    }
//
//    @Override
//    public YokeResponse write(@NotNull String chunk) {
//        hasBody = true;
//        triggerHeadersHandlers();
//        if (filter == null) {
//            getResponse.write(chunk);
//        } else {
//            filter.write(chunk);
//        }
//        return this;
//    }
//
//    @Override
//    public void end(@NotNull String chunk) {
//        hasBody = true;
//        triggerHeadersHandlers();
//        if (filter == null) {
//            getResponse.end(chunk);
//        } else {
//            getResponse.end(filter.end(chunk));
//        }
//        triggerEndHandlers();
//    }
//
//    @Override
//    public void end(@NotNull String chunk, @NotNull String enc) {
//        hasBody = true;
//        triggerHeadersHandlers();
//        if (filter == null) {
//            getResponse.end(chunk, enc);
//        } else {
//            getResponse.end(filter.end(chunk, enc));
//        }
//        triggerEndHandlers();
//    }
//
//    @Override
//    public void end(@NotNull Buffer chunk) {
//        hasBody = true;
//        triggerHeadersHandlers();
//        getResponse.end(filter == null ? chunk : filter.end(chunk));
//        triggerEndHandlers();
//    }
//
//    @Override
//    public void end() {
//        triggerHeadersHandlers();
//        getResponse.end();
//        triggerEndHandlers();
//    }
//
//    @Override
//    public YokeResponse sendFile(String filename) {
//        // TODO: filter file?
//        hasBody = true;
//        filter = null;
//        triggerHeadersHandlers();
//        getResponse.sendFile(filename);
//        triggerEndHandlers();
//        return this;
//    }
//
//    @Override
//    public YokeResponse sendFile(String filename, String notFoundFile) {
//        // TODO: filter file?
//        hasBody = true;
//        filter = null;
//        triggerHeadersHandlers();
//        getResponse.sendFile(filename, notFoundFile);
//        triggerEndHandlers();
//        return this;
//    }
//
//    @Override
//    public YokeResponse sendFile(String filename, JMXHandler<AsyncResult<Void>> resultHandler) {
//        // TODO: filter file?
//        hasBody = true;
//        filter = null;
//        triggerHeadersHandlers();
//        getResponse.sendFile(filename, resultHandler);
//        triggerEndHandlers();
//        return this;
//    }
//
//    @Override
//    public YokeResponse sendFile(String filename, String notFoundFile, JMXHandler<AsyncResult<Void>> resultHandler) {
//        // TODO: filter file?
//        hasBody = true;
//        filter = null;
//        triggerHeadersHandlers();
//        getResponse.sendFile(filename, notFoundFile, resultHandler);
//        triggerEndHandlers();
//        return this;
//    }
//
//    @Override
//    public void close() {
//        getResponse.close();
//        triggerEndHandlers();
//    }
//
//    @Override
//    public YokeResponse exceptionHandler(JMXHandler<Throwable> engine) {
//        getResponse.exceptionHandler(engine);
//        return this;
//    }
//}
