package io.u.yoke.security.impl;


import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

public class SecretSecurity implements io.u.yoke.security.Security {

  private static final Map<String, String> ALIAS_ALG_MAP = new HashMap<>();

  static {
    ALIAS_ALG_MAP.put("HS256", "HMacSHA256");
    ALIAS_ALG_MAP.put("HS384", "HMacSHA384");
    ALIAS_ALG_MAP.put("HS512", "HMacSHA512");
    ALIAS_ALG_MAP.put("RS256", "SHA256withRSA");
  }

  private static String getAlgorithm(String alias) {
    if (ALIAS_ALG_MAP.containsKey(alias)) {
      return ALIAS_ALG_MAP.get(alias);
    } else {
      return alias;
    }
  }

  private final byte[] secret;

  public SecretSecurity(@NotNull String string) {
    this(string.getBytes());
  }

  public SecretSecurity(@NotNull byte[] bytes) {
    secret = bytes;
  }

  @Override
  public Mac getMac(final @NotNull String alias) {
    try {
      Mac mac = Mac.getInstance(getAlgorithm(alias));
      mac.init(new SecretKeySpec(secret, mac.getAlgorithm()));

      return mac;
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Signature getSignature(final @NotNull String alias) {
    try {
      final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

      Signature signature = Signature.getInstance(getAlgorithm(alias));
      signature.initSign(keyPair.getPrivate());

      return signature;
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Key getKey(final @NotNull String alias) {
    return new SecretKeySpec(secret, getAlgorithm(alias));
  }

  @Override
  public X509Certificate getCertificate(final @NotNull String alias) {
    throw new UnsupportedOperationException();
  }

}
