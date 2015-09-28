///**
// * Copyright 2011-2014 the original author or authors.
// */
//package com.jetdrone.vertx.yoke.middleware;
//
//import org.jetbrains.annotations.NotNull;
//import org.vertx.java.core.Handler;
//import org.vertx.java.core.json.JsonObject;
//
//import com.jetdrone.vertx.yoke.Middleware;
//import com.jetdrone.vertx.yoke.Yoke;
//import com.jetdrone.vertx.yoke.store.json.SessionObject;
//import com.jetdrone.vertx.yoke.util.Utils;
//
//public class FormAuth extends Middleware {
//
//    private final AuthHandler authHandler;
//
//    private String loginURI;
//    private String logoutURI;
//
//    private String loginTemplate;
//    private String userLoginTemplate;
//
//    private final boolean forceSSL;
//
//    public FormAuth(@NotNull final AuthHandler authHandler) {
//        this(false, authHandler);
//    }
//
//    public FormAuth(final boolean forceSSL, @NotNull final AuthHandler authHandler) {
//        this(forceSSL, "/login", "/logout", null, authHandler);
//    }
//
//    public FormAuth(final boolean forceSSL, @NotNull final String loginURI, @NotNull final String logoutURI, final String loginTemplate, @NotNull final AuthHandler authHandler) {
//        this.authHandler = authHandler;
//        this.loginURI = loginURI;
//        this.logoutURI = logoutURI;
//        this.userLoginTemplate = loginTemplate;
//        this.forceSSL = forceSSL;
//
//        if (this.userLoginTemplate == null) {
//            this.loginTemplate = Utils.readResourceToBuffer(getClass(), "login.html").toString();
//        }
//    }
//
//    @Override
//    public Middleware init(@NotNull final Yoke yoke, @NotNull final String mount) {
//        super.init(yoke, mount);
//        // trim the initial slash
//        String correctedMount = mount;
//        if (mount.endsWith("/")) {
//            correctedMount = correctedMount.substring(0, correctedMount.length() - 1);
//        }
//        loginURI = correctedMount + loginURI;
//        logoutURI = correctedMount + logoutURI;
//        return this;
//    }
//
//    @Override
//    public void handle(@NotNull final YokeRequest getRequest, @NotNull final Handler<Object> next) {
//        if (getRequest.path().equals(loginURI)) {
//            if ("GET".equals(getRequest.method())) {
//                if (loginTemplate != null) {
//                    // render internal login
//                    getRequest.getResponse().setContentType("text/html");
//                    getRequest.getResponse().end(
//                            loginTemplate.replace("{title}", getRequest.get("title"))
//                                    .replace("{action}", loginURI + "?redirect_url=" + Utils.encodeURIComponent(getRequest.getParameter("redirect_url", "/")))
//                                    .replace("{message}", ""));
//                } else {
//                    // render login
//                    getRequest.getResponse().render(userLoginTemplate, next);
//                }
//                return;
//            }
//
//            if ("POST".equals(getRequest.method())) {
//                if (forceSSL && !getRequest.isSecure()) {
//                    // SSL is required but the post is insecure
//                    next.handle(400);
//                    return;
//                }
//
//                authHandler.handle(getRequest.getFormParameter("username"), getRequest.getFormParameter("password"), user -> {
//                    if (user != null) {
//                        JsonObject session = getRequest.createSession();
//                        session.putString("user", getRequest.getFormParameter("username"));
//
//                        // get the redirect_url parameter
//                        String redirect = getRequest.getParameter("redirect_url", "/");
//                        getRequest.getResponse().redirect(Utils.decodeURIComponent(redirect));
//                    } else {
//                        if (loginTemplate != null) {
//                            // render internal login
//                            getRequest.getResponse().setContentType("text/html");
//                            getRequest.getResponse().setStatusCode(401);
//                            getRequest.getResponse().end(
//                                    loginTemplate.replace("{title}", getRequest.get("title"))
//                                            .replace("{action}", loginURI + "?redirect_url=" + Utils.encodeURIComponent(getRequest.getParameter("redirect_url", "/")))
//                                            .replace("{message}", "Invalid username and/or password, please try again."));
//                        } else {
//                            next.handle(401);
//                        }
//                    }
//                });
//
//                return;
//            }
//        }
//
//        if (getRequest.path().equals(logoutURI)) {
//            if ("GET".equals(getRequest.method())) {
//                // remove session from storage
//                getRequest.destroySession();
//                // get the redirect_url parameter
//                String redirect = getRequest.getParameter("redirect_url", "/");
//                getRequest.getResponse().redirect(Utils.decodeURIComponent(redirect));
//                return;
//            }
//        }
//
//        // all others continue
//        next.handle(null);
//    }
//
//    public final Middleware RequiredAuth = new Middleware() {
//        @Override
//        public void handle(@NotNull final YokeRequest getRequest, @NotNull final Handler<Object> next) {
//        	SessionObject session = getRequest.get("session");
//
//            if (session != null) {
//                if (session.getString("id") != null) {
//                    next.handle(null);
//                    return;
//                }
//            }
//
//            String redirect = getRequest.getParameter("redirect_url", Utils.encodeURIComponent(getRequest.uri()));
//            getRequest.getResponse().redirect(loginURI + "?redirect_url=" + Utils.decodeURIComponent(redirect));
//        }
//    };
//}
