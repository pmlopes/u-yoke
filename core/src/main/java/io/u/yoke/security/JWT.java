package io.u.yoke.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.u.yoke.json.JSON;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

public final class JWT {

  private static final Base64.Encoder encoder = Base64.getUrlEncoder();
  private static final Base64.Decoder decoder = Base64.getUrlDecoder();

  private interface Crypto {
    byte[] sign(byte[] payload);
    boolean verify(byte[] signature, byte[] payload);
  }

  private static final class CryptoMac implements Crypto {
    private final Mac mac;

    private CryptoMac(final Mac mac) {
      this.mac = mac;
    }

    @Override
    public byte[] sign(byte[] payload) {
      return mac.doFinal(payload);
    }

    @Override
    public boolean verify(byte[] signature, byte[] payload) {
      return Arrays.equals(signature, mac.doFinal(payload));
    }
  }

  private static final class CryptoSignature implements Crypto {
    private final Signature sign;
    private final Signature verify;

    private CryptoSignature(@NotNull final Security security, @NotNull final String alias) throws InvalidKeyException {
      final PrivateKey key = (PrivateKey) security.getKey(alias);
      final X509Certificate certificate = security.getCertificate(alias);

      this.sign = security.getSignature(alias);
      this.sign.initSign(key);

      this.verify = security.getSignature(alias);
      this.verify.initVerify(certificate);
    }

    @Override
    public byte[] sign(byte[] payload) {
      try {
        sign.update(payload);
        return sign.sign();
      } catch (SignatureException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean verify(byte[] signature, byte[] payload) {
      try {
        verify.update(payload);
        return verify.verify(signature);
      } catch (SignatureException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private final Map<String, Crypto> CRYPTO_MAP;

  public JWT(final Security security) {

    Map<String, Crypto> tmp = new HashMap<>();

    try {
      tmp.put("HS256", new CryptoMac(security.getMac("HS256")));
    } catch (RuntimeException e) {
      // Algorithm not supported
    }
    try {
      tmp.put("HS384", new CryptoMac(security.getMac("HS384")));
    } catch (RuntimeException e) {
      // Algorithm not supported
    }
    try {
      tmp.put("HS512", new CryptoMac(security.getMac("HS512")));
    } catch (RuntimeException e) {
      // Algorithm not supported
    }
    try {
      tmp.put("RS256", new CryptoSignature(security, "RS256"));
    } catch (RuntimeException e) {
      // Algorithm not supported
    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    }

    CRYPTO_MAP = Collections.unmodifiableMap(tmp);
  }

  public Map decode(final String token) {
    return decode(token, false);
  }

  public Map decode(final String token, boolean noVerify) {
    String[] segments = token.split("\\.");
    if (segments.length != 3) {
      throw new RuntimeException("Not enough or too many segments");
    }

    // All segment should be base64
    String headerSeg = segments[0];
    String payloadSeg = segments[1];
    String signatureSeg = segments[2];

    // base64 decode and parse JSON
    Map header, payload;

    try {
      header = JSON.decode(decoder.decode(headerSeg), Map.class);
      payload = JSON.decode(decoder.decode(payloadSeg), Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!noVerify) {
      final String alg = (String) header.get("alg");
      Crypto crypto = CRYPTO_MAP.get(alg);

      if (crypto == null) {
        throw new RuntimeException("Algorithm not supported");
      }

      // verify signature. `sign` will return base64 string.
      String signingInput = headerSeg + "." + payloadSeg;

      if (!crypto.verify(decoder.decode(signatureSeg), signingInput.getBytes())) {
        throw new RuntimeException("Signature verification failed");
      }
    }

    return payload;
  }

  public String encode(Map payload) {
    return encode(payload, "HS256");
  }

  public String encode(Map payload, String algorithm) {
    Crypto crypto = CRYPTO_MAP.get(algorithm);

    if (crypto == null) {
      throw new RuntimeException("Algorithm not supported");
    }

    // header, typ is fixed value.
    final Map<String, String> header = new LinkedHashMap<>();
    header.put("typ", "JWT");
    header.put("alg", algorithm);

    // create segments, all segment should be base64 string
    try {
      String headerSegment = encoder.encodeToString(JSON.encodeToBytes(header));
      String payloadSegment = encoder.encodeToString(JSON.encodeToBytes(payload));
      String signingInput = headerSegment + "." + payloadSegment;
      String signSegment = encoder.encodeToString(crypto.sign(signingInput.getBytes()));

      return headerSegment + "." + payloadSegment + "." + signSegment;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
