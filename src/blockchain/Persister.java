package blockchain;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Persister {


  private String filename;

  public Persister(String filename) {
    this.filename = filename;
  }

  public void save(List<Block> blockchain) {
    try (FileOutputStream fileOutputStream = new FileOutputStream(filename);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
      objectOutputStream.writeObject(blockchain);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public List<Block> load() {
    try (FileInputStream fileInputStream = new FileInputStream(filename);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
      return (List<Block>) objectInputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public boolean existsChain() {
    return Files.exists(Paths.get(filename));
  }
}
