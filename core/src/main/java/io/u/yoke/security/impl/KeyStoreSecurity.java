package io.u.yoke.security.impl;

import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

public class KeyStoreSecurity implements io.u.yoke.security.Security {

  private final KeyStore keyStore;
  private final Map<String, Key> keys;

  public KeyStoreSecurity(@NotNull final KeyStore keyStore, @NotNull final Map<String, Object> keyPasswords) {
    this.keyStore = keyStore;

    Map<String, Key> tmp = new HashMap<>();

    for (Map.Entry<String, Object> entry : keyPasswords.entrySet()) {
      try {
        if (keyStore.containsAlias(entry.getKey())) {
          tmp.put(entry.getKey(), keyStore.getKey(entry.getKey(), ((String) entry.getValue()).toCharArray()));
        }
      } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
        throw new RuntimeException(e);
      }
    }

    keys = Collections.unmodifiableMap(tmp);
  }

  public KeyStoreSecurity(@NotNull final KeyStore keyStore, @NotNull final String keyPassword) {
    this.keyStore = keyStore;

    Map<String, Key> tmp = new HashMap<>();

    try {
      Enumeration<String> aliases = keyStore.aliases();

      while (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();

        try {
          tmp.put(alias, keyStore.getKey(alias, keyPassword.toCharArray()));
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
          throw new RuntimeException(e);
        }
      }

    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    }

    keys = Collections.unmodifiableMap(tmp);
  }

  /**
   * Creates a new Message Authentication Code
   *
   * @param alias algorithm to use e.g.: HmacSHA256
   * @return Mac implementation
   */
  public Mac getMac(final @NotNull String alias) {
    try {
      final Key secretKey = keys.get(alias);

      Mac mac = Mac.getInstance(secretKey.getAlgorithm());
      mac.init(secretKey);

      return mac;
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  public Signature getSignature(final @NotNull String alias) {
    try {
      final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

      return Signature.getInstance(certificate.getSigAlgName());
    } catch (NoSuchAlgorithmException | KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a new Crypto KEY
   *
   * @return Key implementation
   */
  public Key getKey(final @NotNull String alias) {
    return keys.get(alias);
  }

  public X509Certificate getCertificate(final @NotNull String alias) {
    try {
      return (X509Certificate) keyStore.getCertificate(alias);
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }
}
