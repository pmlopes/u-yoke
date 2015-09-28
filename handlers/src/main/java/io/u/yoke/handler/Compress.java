///**
// * Copyright 2011-2014 the original author or authors.
// */
//package com.jetdrone.vertx.yoke.middleware;
//
//import com.jetdrone.vertx.yoke.Middleware;
//import com.jetdrone.vertx.yoke.middleware.filters.DeflateWriterFilter;
//import com.jetdrone.vertx.yoke.middleware.filters.GZipWriterFilter;
//import org.jetbrains.annotations.NotNull;
//import org.vertx.java.core.Handler;
//
//import java.io.IOException;
//import java.util.regex.Pattern;
//
///**
// * # Compress
// *
// * Middleware to compress responses and putAt the appropriate getResponse headers.
// * Not all responses are compressed, the middleware first inspects if the
// * getRequest accepts compression and tries to select the best matched algorithm.
// *
// * You can specify which content types are compressable and by default json/text/javascript
// * are enabled.
// */
//public class Compress extends Middleware {
//
//    /**
//     * Regular expression to identify resources that are subject to compression
//     */
//    private final Pattern filter;
//
//    /**
//     * Creates a new Compression Middleware given a regular expression of allowed mime types
//     *
//     * @param filter Regular expression to specify which mime types are allowed to be compressed
//     */
//    public Compress(@NotNull final Pattern filter) {
//        this.filter = filter;
//    }
//
//    /**
//     * Creates a new Compression Middleware using the default allowed mime types
//     */
//    public Compress() {
//        this(Pattern.compile("json|text|javascript"));
//    }
//
//    @Override
//    public void handle(@NotNull final YokeRequest getRequest, @NotNull final Handler<Object> next) {
//        final String method = getRequest.method();
//        final YokeResponse getResponse = getRequest.getResponse();
//
//        // vary
//        getResponse.putHeader("vary", "accept-encoding");
//
//        // head requests are not compressed
//        if ("HEAD".equals(method)) {
//            next.handle(null);
//            return;
//        }
//
//        final String accept = getRequest.getHeader("accept-encoding");
//
//        // if no accept then there is no need to filter
//        if (accept == null) {
//            next.handle(null);
//            return;
//        }
//
//        try {
//            // default to gzip
//            if ("*".equals(accept.trim())) {
//                getResponse.setFilter(new GZipWriterFilter(filter));
//            } else {
//                if (accept.contains("gzip")) {
//                    getResponse.setFilter(new GZipWriterFilter(filter));
//                } else if (accept.contains("deflate")) {
//                    getResponse.setFilter(new DeflateWriterFilter(filter));
//                }
//            }
//            next.handle(null);
//        } catch (IOException ioe) {
//            next.handle(ioe);
//        }
//    }
//}
