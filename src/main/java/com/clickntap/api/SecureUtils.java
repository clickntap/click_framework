package com.clickntap.api;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECCurve;

import com.clickntap.utils.ConstUtils;
import com.clickntap.utils.SecurityUtils;

public class SecureUtils {

  public static KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDH", "BC");
    keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
    return keyGen.generateKeyPair();
  }

  public static ECPublicKey importClientPublicKey(String base64key) throws Exception {
    return importClientPublicKey(SecureUtils.base64dec(base64key));
  }

  public static ECPublicKey importPublicKey(String base64key) throws Exception {
    return importPublicKey(SecureUtils.base64dec(base64key));
  }

  public static ECPrivateKey importPrivateKey(String base64key) throws Exception {
    return importPrivateKey(SecureUtils.base64dec(base64key));
  }

  public static ECPublicKey importClientPublicKey(byte[] rawKey) throws Exception {
    ECNamedCurveParameterSpec namedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
    ECCurve curve = namedCurveParameterSpec.getCurve();
    EllipticCurve ellipticCurve = EC5Util.convertCurve(curve, namedCurveParameterSpec.getSeed());
    ECPoint point = ECPointUtil.decodePoint(ellipticCurve, rawKey);
    ECParameterSpec parameterSpec = EC5Util.convertSpec(ellipticCurve, namedCurveParameterSpec);
    ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(point, parameterSpec);
    return (ECPublicKey) KeyFactory.getInstance("ECDH", "BC").generatePublic(publicKeySpec);
  }

  public static ECPublicKey importPublicKey(byte[] rawKey) throws Exception {
    KeySpec keySpec = new X509EncodedKeySpec(rawKey);
    return (ECPublicKey) KeyFactory.getInstance("ECDH", "BC").generatePublic(keySpec);
  }

  public static ECPrivateKey importPrivateKey(byte[] rawKey) throws Exception {
    KeySpec keySpec = new PKCS8EncodedKeySpec(rawKey);
    return (ECPrivateKey) KeyFactory.getInstance("ECDH", "BC").generatePrivate(keySpec);
  }

  public static SecretKeySpec generateSecret(ECPublicKey publicKey, ECPrivateKey privateKey) throws Exception {
    KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
    keyAgreement.init(privateKey);
    keyAgreement.doPhase(publicKey, true);
    return new SecretKeySpec(keyAgreement.generateSecret(), "AES");
  }

  public static String base64enc(byte[] data) throws Exception {
    return SecurityUtils.base64enc(data);
  }

  public static byte[] base64dec(String data) throws Exception {
    return SecurityUtils.base64dec(data);
  }

  public static String decrypt(SecretKey key, byte[] encoded) throws Exception {
    Key decryptionKey = new SecretKeySpec(key.getEncoded(), key.getAlgorithm());
    byte[] iv = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
    byte[] plainText;
    cipher.init(Cipher.DECRYPT_MODE, decryptionKey, ivSpec);
    plainText = new byte[cipher.getOutputSize(encoded.length)];
    int decryptLength = cipher.update(encoded, 0, encoded.length, plainText, 0);
    decryptLength += cipher.doFinal(plainText, decryptLength);
    return new String(plainText, ConstUtils.UTF_8);
  }

  public static byte[] encrypt(SecretKey key, String plainText) throws Exception {
    byte[] iv = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
    return cipher.doFinal(plainText.getBytes(ConstUtils.UTF_8));
  }

}
