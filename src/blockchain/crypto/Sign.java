package blockchain.crypto;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

public class Sign {

  private PrivateKey privateKey;

  public Sign(String privateKeyPath) throws Exception {
    byte[] keyBytes = Files.readAllBytes(new File(privateKeyPath).toPath());
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    this.privateKey = kf.generatePrivate(spec);
  }

  public byte[] sign(String text) throws Exception {
    Signature rsa = Signature.getInstance("SHA1withRSA");
    rsa.initSign(privateKey);
    rsa.update(text.getBytes());
    return rsa.sign();
  }
}
