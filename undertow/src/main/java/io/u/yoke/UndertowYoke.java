package io.u.yoke;

import io.u.yoke.impl.AbstractYoke;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

public final class UndertowYoke extends AbstractYoke {

  private static final char[] STORE_PASSWORD = "password".toCharArray();
  private Undertow server;

  public static void main(String[] args) throws Exception {
    String bindAddress = System.getProperty("bind.address", "0.0.0.0");
    String bindPort = System.getProperty("bind.port", "8080");
    String bindSslPort = System.getProperty("bind.sslPort", "8443");

    new UndertowYoke().listen(Integer.parseInt(bindPort), Integer.parseInt(bindSslPort), bindAddress);
  }

  public void listen(int port, int sslPort, String bindAddress) {
    //final SSLContext sslContext = createSSLContext(loadKeyStore("server.keystore"), loadKeyStore("server.truststore"));

    final HttpHandler yokeHandler = new HttpHandler() {
      @Override
      public void handleRequest(HttpServerExchange exchange) throws Exception {
//        // dispatch the getRequest to a worker thread
//        if (exchange.isInIoThread()) {
          exchange.dispatch();
//          return;
//        }

        final UndertowContext ctx = new UndertowContext(locals, exchange);

        // add x-powered-by header is enabled
        Boolean poweredBy = ctx.getAt("x-powered-by");
        if (poweredBy != null && poweredBy) {
          ctx.set("X-Powered-By", "yoke");
        }

        ctx.setIterator(handlers, getErrorHandler());
        // start the handling
        ctx.next();
      }
    };

    server = Undertow.builder()
        //.setServerOption(UndertowOptions.ENABLE_HTTP2, true)
        .addHttpListener(port, bindAddress)
        //.addHttpsListener(sslPort, bindAddress, sslContext)
        .setHandler(yokeHandler).build();

    server.start();
  }

  static char[] password(String name) {
    String pw = System.getProperty(name + ".password");
    return pw != null ? pw.toCharArray() : STORE_PASSWORD;
  }

  private static KeyStore loadKeyStore(String name) {
    try {
      String storeLoc = System.getProperty(name);
      final InputStream stream;
      if(storeLoc == null) {
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
      } else {
        stream = Files.newInputStream(Paths.get(storeLoc));
      }

      try(InputStream is = stream) {
        KeyStore loadedKeystore = KeyStore.getInstance("JKS");
        loadedKeystore.load(is, password(name));
        return loadedKeystore;
      }
    } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static SSLContext createSSLContext(final KeyStore keyStore, final KeyStore trustStore) {
    try {
      KeyManager[] keyManagers;
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, password("key"));
      keyManagers = keyManagerFactory.getKeyManagers();

      TrustManager[] trustManagers;
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      trustManagers = trustManagerFactory.getTrustManagers();

      SSLContext sslContext;
      sslContext = SSLContext.getInstance("TLS");
      sslContext.init(keyManagers, trustManagers, null);

      return sslContext;
    } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void listen(int port) {
    listen(port, 8443, "0.0.0.0");
  }

  @Override
  public void close() {
    server.stop();
  }
}
