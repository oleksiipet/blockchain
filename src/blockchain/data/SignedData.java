package blockchain.data;

public interface SignedData {

  Integer id();

  byte[] dataSignature();

  byte[] publicKey();
}
