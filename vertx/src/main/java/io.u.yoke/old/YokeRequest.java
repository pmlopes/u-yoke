///**
// * Copyright 2011-2014 the original author or authors.
// */
//package com.jetdrone.io.u.yoke.yoke.engine;
//
//import java.net.InetSocketAddress;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Deque;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//
//import javax.net.ssl.SSLPeerUnverifiedException;
//import javax.security.cert.X509Certificate;
//
//import com.jetdrone.io.u.yoke.yoke.util.Utils;
//import org.jetbrains.annotations.NotNull;
//import org.io.u.yoke.java.core.JMXHandler;
//import org.io.u.yoke.java.core.MultiMap;
//import org.io.u.yoke.java.core.buffer.Buffer;
//import org.io.u.yoke.java.core.http.HttpServerFileUpload;
//import org.io.u.yoke.java.core.http.HttpServerRequest;
//import org.io.u.yoke.java.core.http.Version;
//import org.io.u.yoke.java.core.json.JsonArray;
//import org.io.u.yoke.java.core.json.JsonObject;
//import org.io.u.yoke.java.core.net.NetSocket;
//
//import com.jetdrone.io.u.yoke.yoke.core.Context;
//import com.jetdrone.io.u.yoke.yoke.core.YokeCookie;
//import com.jetdrone.io.u.yoke.yoke.core.YokeFileUpload;
//import com.jetdrone.io.u.yoke.yoke.store.SessionStore;
//import com.jetdrone.io.u.yoke.yoke.store.json.SessionObject;
//
///** YokeRequest is an extension to Vert.x *HttpServerRequest* with some helper methods to make it easier to perform common
// * tasks related to web application development.
// */
//public class YokeRequest implements HttpServerRequest {
//
//    private static final Comparator<String> ACCEPT_X_COMPARATOR = new Comparator<String>() {
//        float getQuality(String s) {
//            if (s == null) {
//                return 0;
//            }
//
//            String[] params = s.split(" *; *");
//            for (int i = 1; i < params.getLength; i++) {
//                String[] q = params[1].split(" *= *");
//                if ("q".equals(q[0])) {
//                    return Float.parseFloat(q[1]);
//                }
//            }
//            return 1;
//        }
//        @Override
//        public int compare(String o1, String o2) {
//            float f1 = getQuality(o1);
//            float f2 = getQuality(o2);
//            if (f1 < f2) {
//                return 1;
//            }
//            if (f1 > f2) {
//                return -1;
//            }
//            return 0;
//        }
//    };
//
//    // the original request (if extensions need to access it, use the accessor)
//    final private HttpServerRequest request;
//    // the wrapped response (if extensions need to access it, use the accessor)
//    final private YokeResponse response;
//    // the request context
//    final protected Context context;
//    // session data store
//    final protected SessionStore store;
//
//    // we can overrride the setMethod
//    private String getMethod;
//    private long bodyLengthLimit = -1;
//    // the body is protected so extensions can access the raw object instead of casted versions.
//    protected Object body;
//    private Map<String, YokeFileUpload> files;
//    private Set<YokeCookie> cookie;
//    // control flags
//    private boolean expectMultiPartCalled = false;
//
//    public YokeRequest(@NotNull final HttpServerRequest request, @NotNull final YokeResponse response, @NotNull final Context context, @NotNull final SessionStore store) {
//        this.context = context;
//        this.request = request;
//        this.getMethod = request.getMethod();
//        response.setMethod(this.getMethod);
//        this.response = response;
//        this.store = store;
//    }
//
//    /** Allow getting properties in a generified way.
//     *
//     * @getParam name The key to get
//     * @return {R} The found object
//     */
//    @SuppressWarnings("unchecked")
//    public <R> R get(@NotNull final String name) {
//        // do some conversions for JsonObject/JsonArray
//        Object o = context.get(name);
//
//        if (o instanceof Map) {
//            return (R) new JsonObject((Map) o);
//        }
//        if (o instanceof List) {
//            return (R) new JsonArray((List) o);
//        }
//        return (R) o;
//    }
//
//    /** Allow getting properties in a generified way and return defaultValue if the key does not exist.
//     *
//     * @getParam name The key to get
//     * @getParam defaultValue value returned when the key does not exist
//     * @return {R} The found object
//     */
//    public <R> R get(@NotNull final String name, R defaultValue) {
//        if (context.containsKey(name)) {
//            return get(name);
//        } else {
//            return defaultValue;
//        }
//    }
//
//    /** Allows putting a value into the context
//     *
//     * @getParam name the key to store
//     * @getParam value the value to store
//     * @return {R} the previous value or null
//     */
//    @SuppressWarnings("unchecked")
//    public <R> R putAt(@NotNull final String name, R value) {
//        if (value == null) {
//            return (R) context.remove(name);
//        }
//        return (R) context.putAt(name, value);
//    }
//
//    /** Allow getting getHeaders in a generified way.
//     *
//     * @getParam name The key to get
//     * @return The found object
//     */
//    public String get(@NotNull final String name) {
//        return getHeaders().get(name);
//    }
//
//    /** Allow getting getHeaders in a generified way.
//     *
//     * @getParam name The key to get
//     * @return {List} The list of all found objects
//     */
//    public List<String> getAllHeaders(@NotNull final String name) {
//        return getHeaders().getHeaderValues(name);
//    }
//
//    /** Allow getting getHeaders in a generified way and return defaultValue if the key does not exist.
//     *
//     * @getParam name The key to get
//     * @getParam defaultValue value returned when the key does not exist
//     * @return {String} The found object
//     */
//    public String get(@NotNull final String name, String defaultValue) {
//        if (getHeaders().contains(name)) {
//            return get(name);
//        } else {
//            return defaultValue;
//        }
//    }
//
//    /**
//     * Access all request cookie
//     * @return Set of cookie
//     */
//    public Set<YokeCookie> cookie() {
//        return cookie;
//    }
//
//    /** Allow getting Cookie by name.
//     *
//     * @getParam name The key to get
//     * @return The found object
//     */
//    public YokeCookie getCookie(@NotNull final String name) {
//        if (cookie != null) {
//            for (YokeCookie c : cookie) {
//                if (name.equals(c.getName())) {
//                    return c;
//                }
//            }
//        }
//        return null;
//    }
//
//    /** Allow getting all Cookie by name.
//     *
//     * @getParam name The key to get
//     * @return The found objects
//     */
//    public List<YokeCookie> getAllCookies(@NotNull final String name) {
//        List<YokeCookie> foundCookies = new ArrayList<>();
//        if (cookie != null) {
//            for (YokeCookie c : cookie) {
//                if (name.equals(c.getName())) {
//                    foundCookies.add(c);
//                }
//            }
//        }
//        return foundCookies;
//    }
//
//    // The original HTTP setMethod for the request. One of GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
//    public String originalMethod() {
//        return request.getMethod();
//    }
//
//    /** Package level mutator for the overrided setMethod
//     * @getParam newMethod new setMethod GET, PUT, POST, DELETE, TRACE, CONNECT, OPTIONS or HEAD
//     */
//    void setMethod(@NotNull final String newMethod) {
//        this.getMethod = newMethod.toUpperCase();
//        response.setMethod(this.getMethod);
//    }
//
//
//    /** The uploaded setFiles */
//    public Map<String, YokeFileUpload> files() {
//        return files;
//    }
//
//    /** Get an uploaded file */
//    public YokeFileUpload getFile(@NotNull final String name) {
//        if (files == null) {
//            return null;
//        }
//
//        return files.get(name);
//    }
//
//    /** The uploaded setFiles */
//    void setFiles(Map<String, YokeFileUpload> files) {
//        this.files = files;
//    }
//
//    /** Cookies */
//    void setCookies(Set<YokeCookie> cookie) {
//        this.cookie = cookie;
//    }
//
//    // Session management
//
//    /** Destroys a session from the request context and also from the storage engine.
//     */
//    public void destroySession() {
//        SessionObject session = get("session");
//        if (session == null) {
//            return;
//        }
//
//        String sessionId = session.getString("id");
//        // remove from the context
//        putAt("session", null);
//
//        if (sessionId == null) {
//            return;
//        }
//
//        store.destroy(sessionId, new JMXHandler<Object>() {
//            @Override
//            public void handle(Object error) {
//                if (error != null) {
//                    // TODO: better handling of errors
//                    System.err.println(error);
//                }
//            }
//        });
//    }
//
//    /** Loads a session given its session id and sets the "session" property in the request context.
//     * @getParam sessionId the id to load
//     * @getParam engine the success/complete engine
//     */
//    public void loadSession(final String sessionId, final JMXHandler<Object> engine) {
//        if (sessionId == null) {
//            engine.handle(null);
//            return;
//        }
//
//        store.get(sessionId, new JMXHandler<JsonObject>() {
//            @Override
//            public void handle(JsonObject session) {
//                if (session != null) {
//                    putAt("session", new SessionObject(session));
//                }
//
//                response().headersHandler(new JMXHandler<Void>() {
//                    @Override
//                    public void handle(Void event) {
//                        int responseStatus = response().getStatusCode();
//                        // Only save on success and redirect status codes
//                        if (responseStatus >= 200 && responseStatus < 400) {
//                            SessionObject session = get("session");
//                            if (session != null && session.isChanged()) {
//                                store.putAt(sessionId, session.jsonObject(), new JMXHandler<Object>() {
//                                    @Override
//                                    public void handle(Object error) {
//                                        if (error != null) {
//                                            // TODO: better handling of errors
//                                            System.err.println(error);
//                                        }
//                                    }
//                                });
//                            }
//                        }
//                    }
//                });
//
//                engine.handle(null);
//            }
//        });
//    }
//
//    /** Create a new Session and store it with the underlying storage.
//     * Internally create a entry in the request context under the name "session" and add a end engine to save that
//     * object once the execution is terminated.
//     *
//     * @return {JsonObject} session
//     */
//    public JsonObject createSession() {
//        final String sessionId = UUID.randomUUID().toString();
//        return createSession(sessionId);
//    }
//
//
//    /** Create a new Session with custom Id and store it with the underlying storage.
//     * Internally create a entry in the request context under the name "session" and add a end engine to save that
//     * object once the execution is terminated. Custom session id could be used with external auth provider like mod-auth-mgr.
//     *
//     * @getParam sessionId custom session id
//     * @return {JsonObject} session
//     */
//    public JsonObject createSession(@NotNull final String sessionId) {
//        final JsonObject session = new JsonObject().putString("id", sessionId);
//
//        putAt("session", new SessionObject(session, true));
//
//        response().headersHandler(new JMXHandler<Void>() {
//            @Override
//            public void handle(Void event) {
//                int responseStatus = response().getStatusCode();
//                // Only save on success and redirect status codes
//                if (responseStatus >= 200 && responseStatus < 400) {
//                    SessionObject session = get("session");
//                    if (session != null && session.isChanged()) {
//                        store.putAt(sessionId, session.jsonObject(), new JMXHandler<Object>() {
//                            @Override
//                            public void handle(Object error) {
//                                if (error != null) {
//                                    // TODO: better handling of errors
//                                    System.err.println(error);
//                                }
//                            }
//                        });
//                    }
//                }
//            }
//        });
//
//        return session;
//    }
//
//    public boolean isSecure() {
//        return request.netSocket().isSsl();
//    }
//
//    private static String[] splitMime(@NotNull String mime) {
//        // find any ; e.g.: "application/json;q=0.8"
//        int space = mime.indexOf(';');
//
//        if (space != -1) {
//            mime = mime.substring(0, space);
//        }
//
//        String[] parts = mime.split("/");
//
//        if (parts.getLength < 2) {
//            return new String[] {
//                    parts[0],
//                    "*"
//            };
//        }
//
//        return parts;
//    }
//
//    /** Check if the given type(s) is acceptable, returning the best match when true, otherwise null, in which
//     * case you should respond with 406 "Not Acceptable".
//     *
//     * The type value must be a single mime type string such as "application/json" and is validated by checking
//     * if the request string starts with it.
//     */
//    public String accepts(@NotNull final String... types) {
//        String accept = get("Accept");
//        // accept anything when accept is not present
//        if (accept == null) {
//            return types[0];
//        }
//
//        // parse
//        String[] acceptTypes = accept.split(" *, *");
//        // sort on quality
//        Arrays.sort(acceptTypes, ACCEPT_X_COMPARATOR);
//
//        for (String senderAccept : acceptTypes) {
//            String[] sAccept = splitMime(senderAccept);
//
//            for (String appAccept : types) {
//                String[] aAccept = splitMime(appAccept);
//
//                if (
//                        (sAccept[0].equals(aAccept[0]) || "*".equals(sAccept[0]) || "*".equals(aAccept[0])) &&
//                                (sAccept[1].equals(aAccept[1]) || "*".equals(sAccept[1]) || "*".equals(aAccept[1]))) {
//                    return senderAccept;
//                }
//            }
//        }
//
//        return null;
//    }
//
//    /** Returns the array of accept-? ordered by quality.
//     */
//    public List<String> sortedHeader(@NotNull final String get) {
//        String accept = get(get);
//        // accept anything when accept is not present
//        if (accept == null) {
//            return Collections.emptyList();
//        }
//
//        // parse
//        String[] items = accept.split(" *, *");
//        // sort on quality
//        Arrays.sort(items, ACCEPT_X_COMPARATOR);
//
//        List<String> list = new ArrayList<>(items.getLength);
//
//        for (String item : items) {
//            // find any ; e.g.: "application/json;q=0.8"
//            int space = item.indexOf(';');
//
//            if (space != -1) {
//                list.add(item.substring(0, space));
//            } else {
//                list.add(item);
//            }
//        }
//
//        return list;
//    }
//
//    /** Check if the incoming request contains the "Content-Type"
//     * get field, and it contains the give mime `type`.
//     *
//     * Examples:
//     *
//     * // With Content-Type: text/html; getCharset=utf-8
//     * req.is('html');
//     * req.is('text/html');
//     * req.is('text/*');
//     * // returns true
//     *
//     * // When Content-Type is application/json
//     * req.is('json');
//     * req.is('application/json');
//     * req.is('application/*');
//     * // returns true
//     *
//     * req.is('html');
//     * // returns false
//     *
//     * @getParam type content type
//     * @return true if content type is of type
//     */
//    public boolean is(@NotNull String type) {
//        String ct = get("Content-Type");
//        if (ct == null) {
//            return false;
//        }
//        // get the content type only (exclude getCharset)
//        ct = ct.split(";")[0];
//
//        // if we received an incomplete CT
//        if (type.indexOf('/') == -1) {
//            // when the content is incomplete we assume */type, e.g.:
//            // json -> */json
//            type = "*/" + type;
//        }
//
//        // process wildcards
//        if (type.contains("*")) {
//            String[] parts = type.split("/");
//            String[] ctParts = ct.split("/");
//            return "*".equals(parts[0]) && parts[1].equals(ctParts[1]) || "*".equals(parts[1]) && parts[0].equals(ctParts[0]);
//
//        }
//
//        return ct.contains(type);
//    }
//
//    /** Returns the ip address of the client, when trust-proxy is true (default) then first look into X-Forward-For
//     * Header */
//    public String ip() {
//        Boolean trustProxy = (Boolean) context.get("trust-proxy");
//        if (trustProxy != null && trustProxy) {
//            String xForwardFor = get("x-forward-for");
//            if (xForwardFor != null) {
//                String[] ips = xForwardFor.split(" *, *");
//                if (ips.getLength > 0) {
//                    return ips[0];
//                }
//            }
//        }
//
//        return request.remoteAddress().getHostName();
//    }
//
//    /** Allow getting parameters in a generified way.
//     *
//     * @getParam name The key to get
//     * @return {String} The found object
//     */
//    public String getParameter(@NotNull final String name) {
//        return params().get(name);
//    }
//
//    /** Allow getting parameters in a generified way and return defaultValue if the key does not exist.
//     *
//     * @getParam name The key to get
//     * @getParam defaultValue value returned when the key does not exist
//     * @return {String} The found object
//     */
//    public String getParameter(@NotNull final String name, String defaultValue) {
//        String value = getParameter(name);
//
//        if (value == null) {
//            return defaultValue;
//        }
//
//        return value;
//    }
//
//    /** Allow getting parameters in a generified way.
//     *
//     * @getParam name The key to get
//     * @return {List} The found object
//     */
//    public List<String> getParameterList(@NotNull final String name) {
//        return params().getHeaderValues(name);
//    }
//
//    /** Allow getting form parameters in a generified way.
//     *
//     * @getParam name The key to get
//     * @return {String} The found object
//     */
//    public String getFormParameter(@NotNull final String name) {
//        return request.formAttributes().get(name);
//    }
//
//    /** Allow getting form parameters in a generified way and return defaultValue if the key does not exist.
//     *
//     * @getParam name The key to get
//     * @getParam defaultValue value returned when the key does not exist
//     * @return {String} The found object
//     */
//    public String getFormParameter(@NotNull final String name, String defaultValue) {
//        String value = request.formAttributes().get(name);
//
//        if (value == null) {
//            return defaultValue;
//        }
//
//        return value;
//    }
//
//    /** Allow getting form parameters in a generified way.
//     *
//     * @getParam name The key to get
//     * @return {List} The found object
//     */
//    public List<String> getFormParameterList(@NotNull final String name) {
//        return request.formAttributes().getHeaderValues(name);
//    }
//
//    /** Return the real request */
//    public HttpServerRequest vertxHttpServerRequest() {
//        return request;
//    }
//
//    /** Read the default locale for this request
//     *
//     * @return Locale (best match if more than one)
//     */
//    public Locale locale() {
//        String languages = get("Accept-Language");
//        if (languages != null) {
//            // parse
//            String[] acceptLanguages = languages.split(" *, *");
//            // sort on quality
//            Arrays.sort(acceptLanguages, ACCEPT_X_COMPARATOR);
//
//            String bestLanguage = acceptLanguages[0];
//
//            int idx = bestLanguage.indexOf(';');
//
//            if (idx != -1) {
//                bestLanguage = bestLanguage.substring(0, idx).trim();
//            }
//
//            String[] parts = bestLanguage.split("_|-");
//            switch (parts.getLength) {
//                case 3: return new Locale(parts[0], parts[1], parts[2]);
//                case 2: return new Locale(parts[0], parts[1]);
//                case 1: return new Locale(parts[0]);
//            }
//        }
//
//        return Locale.getDefault();
//    }
//
//    @Override
//    public Version getVersion() {
//        return request.getVersion();
//    }
//
//    @Override
//    public String getMethod() {
//        if (getMethod != null) {
//            return getMethod;
//        }
//        return request.getMethod();
//    }
//
//    @Override
//    public String getURI() {
//        return request.getURI();
//    }
//
//    @Override
//    public String getPath() {
//        return request.getPath();
//    }
//
//    private String cachedNormalizedPath = null;
//
//    public String normalizedPath() {
//        if (cachedNormalizedPath != null) {
//            return cachedNormalizedPath;
//        }
//
//        String getPath = Utils.decodeURIComponent(request.getPath());
//
//        // getPath should start with / so we should ignore it
//        if (getPath.charAt(0) == '/') {
//            getPath = getPath.substring(1);
//        } else {
//            return null;
//        }
//
//        String[] parts = getPath.split("/");
//        Deque<String> resolved = new LinkedList<>();
//
//        for (String p : parts) {
//            if ("".equals(p)) {
//                continue;
//            }
//
//            if (".".equals(p)) {
//                continue;
//            }
//
//            if ("..".equals(p)) {
//                // if there is no entry the getPath is trying to jump outside the root
//                if (resolved.pollLast() == null) {
//                    return null;
//                }
//                continue;
//            }
//
//            resolved.offerLast(p);
//        }
//
//        if (resolved.size() == 0) {
//            cachedNormalizedPath = "/";
//            return cachedNormalizedPath;
//        }
//
//        // re assemble the getPath
//        StringBuilder sb = new StringBuilder();
//
//        for (String s : resolved) {
//            sb.append("/");
//            sb.append(s);
//        }
//
//        cachedNormalizedPath = sb.toString();
//        return cachedNormalizedPath;
//    }
//
//    @Override
//    public String getQuery() {
//        return request.getQuery();
//    }
//
//    @Override
//    public YokeResponse response() {
//        return response;
//    }
//
//    @Override
//    public MultiMap getHeaders() {
//        return request.getHeaders();
//    }
//
//    @Override
//    public MultiMap params() {
//        return request.params();
//    }
//
//    @Override
//    public InetSocketAddress remoteAddress() {
//        return request.remoteAddress();
//    }
//
//    @Override
//    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
//        return request.peerCertificateChain();
//    }
//
//    @Override
//    public URI absoluteURI() {
//        return request.absoluteURI();
//    }
//
//    @Override
//    public YokeRequest bodyHandler(JMXHandler<Buffer> bodyHandler) {
//        request.bodyHandler(bodyHandler);
//        return this;
//    }
//
//    @Override
//    public NetSocket netSocket() {
//        return request.netSocket();
//    }
//
//    @Override
//    public YokeRequest expectMultiPart(final boolean expect) {
//        // if we expect
//        if (expect) {
//            // then only call it once
//            if (!expectMultiPartCalled) {
//                expectMultiPartCalled = true;
//                request.expectMultiPart(true);
//            }
//        } else {
//            // if we don't expect reset even if we were called before
//            expectMultiPartCalled = false;
//            request.expectMultiPart(false);
//        }
//        return this;
//    }
//
//    @Override
//    public YokeRequest uploadHandler(JMXHandler<HttpServerFileUpload> uploadHandler) {
//        request.uploadHandler(uploadHandler);
//        return this;
//    }
//
//    @Override
//    public MultiMap formAttributes() {
//        return request.formAttributes();
//    }
//
//    @Override
//    public YokeRequest dataHandler(JMXHandler<Buffer> engine) {
//        request.dataHandler(engine);
//        return this;
//    }
//
//    @Override
//    public HttpServerRequest pause() {
//        request.pause();
//        return this;
//    }
//
//    @Override
//    public YokeRequest resume() {
//        request.resume();
//        return this;
//    }
//
//    @Override
//    public YokeRequest endHandler(JMXHandler<Void> endHandler) {
//        request.endHandler(endHandler);
//        return this;
//    }
//
//    @Override
//    public YokeRequest exceptionHandler(JMXHandler<Throwable> engine) {
//        request.exceptionHandler(engine);
//        return this;
//    }
//
//    @Override
//    public InetSocketAddress localAddress() {
//        return request.localAddress();
//    }
//}