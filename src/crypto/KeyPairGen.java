package crypto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairGen {

  private KeyPairGenerator keyGen;
  private KeyPair pair;
  private PrivateKey privateKey;
  private PublicKey publicKey;

  private KeyPairGen(int keyLen) throws NoSuchAlgorithmException {
    this.keyGen = KeyPairGenerator.getInstance("RSA");
    this.keyGen.initialize(keyLen);
  }


  private void genPair() {
    pair = keyGen.generateKeyPair();
    privateKey = pair.getPrivate();
    publicKey = pair.getPublic();
  }

  private PrivateKey getPrivateKey() {
    return privateKey;
  }

  private PublicKey getPublicKey() {
    return publicKey;
  }

  public static void main(String[] args) throws Exception {
    KeyPairGen gen = new KeyPairGen(1024);

    gen.genPair();

    writeToFile("keys/publicKey", gen.getPublicKey().getEncoded());
    writeToFile("keys/privateKey", gen.getPrivateKey().getEncoded());
  }

  private static void writeToFile(String path, byte[] key) throws IOException {
    File f = new File(path);
    f.getParentFile().mkdirs();
    FileOutputStream fos = new FileOutputStream(f);
    fos.write(key);
    fos.flush();
    fos.close();
  }
}
