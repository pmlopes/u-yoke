package io.u.yoke.security;

import io.u.yoke.security.impl.KeyStoreSecurity;
import io.u.yoke.security.impl.SecretSecurity;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public interface Security {

  static Security create(String secret) {
    return new SecretSecurity(secret);
  }

  static Security create(@NotNull final String fileName, @NotNull final String keyStorePassword) {
    String storeType;
    int idx = fileName.lastIndexOf('.');

    if (idx == -1) {
      storeType = KeyStore.getDefaultType();
    } else {
      storeType = fileName.substring(idx + 1);
    }

    try {
      KeyStore ks = KeyStore.getInstance(storeType);

      try (InputStream in = new FileInputStream(fileName)) {
        ks.load(in, keyStorePassword.toCharArray());
      }

      return new KeyStoreSecurity(ks, keyStorePassword);

    } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a new Message Authentication Code
   *
   * @param alias algorithm to use e.g.: HmacSHA256
   * @return Mac implementation
   */
  Mac getMac(final @NotNull String alias);

  Signature getSignature(final @NotNull String alias);

  /**
   * Creates a new Crypto KEY
   *
   * @return Key implementation
   */
  Key getKey(final @NotNull String alias);

  /**
   * Load/Create a new Certificate
   *
   * @param alias the alias on the store
   * @return the certificate
   */
  X509Certificate getCertificate(final @NotNull String alias);

  /**
   * Creates a new Cipher
   *
   * @return Cipher implementation
   */
  static Cipher getCipher(final @NotNull Key key, int mode) {
    try {
      Cipher cipher = Cipher.getInstance(key.getAlgorithm());
      cipher.init(mode, key);
      return cipher;
    } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Signs a String value with a given MAC
   */
  static String sign(@NotNull String val, @NotNull Mac mac) {
    return val + "." + Base64.getUrlEncoder().encodeToString(mac.doFinal(val.getBytes()));
  }

  /**
   * Returns the original value is the signature is correct. Null otherwise.
   */
  static String unsign(@NotNull String val, @NotNull Mac mac) {
    int idx = val.lastIndexOf('.');

    if (idx == -1) {
      return null;
    }

    String str = val.substring(0, idx);
    if (val.equals(sign(str, mac))) {
      return str;
    }
    return null;
  }

  static String encrypt(@NotNull String val, @NotNull Cipher cipher) {
    try {
      byte[] encVal = cipher.doFinal(val.getBytes());
      return Base64.getUrlEncoder().encodeToString(encVal);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  static String decrypt(@NotNull String val, @NotNull Cipher cipher) {
    try {
      byte[] decordedValue = DatatypeConverter.parseBase64Binary(val);
      byte[] decValue = cipher.doFinal(decordedValue);
      return new String(decValue);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }
}
