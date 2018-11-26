package blockchain;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class Blockchain implements Iterable<Block> {

  private final LinkedList<Block> blocks;
  private final Random random;
  private final String prefix;
  private final Persister persister;

  public Blockchain(int N) {
    persister = new Persister("blockchain.ser");
    random = new Random();
    prefix = Stream.iterate("0", x -> "0").limit(N).reduce("", (x, y) -> x + y);
    if (persister.existsChain()) {
      LinkedList<Block> blocks = (LinkedList<Block>) persister.load();
      if (validate(blocks)) {
        this.blocks = blocks;
      } else {
        Block firstBlock = initialBlockHead();
        this.blocks = new LinkedList<>(Collections.singleton(firstBlock));
      }
    } else {
      Block firstBlock = initialBlockHead();
      blocks = new LinkedList<>(Collections.singleton(firstBlock));
    }
    persister.save(blocks);
  }


  public void generate() {
    Long timestamp = new Date().getTime();
    Block tailBlock = blocks.getLast();
    Integer magicNumber = random.nextInt();
    String hash = StringUtil.applySha256(tailBlock.getHash() + "hello" + magicNumber);
    while (!hash.startsWith(prefix)) {
      magicNumber = random.nextInt();
      hash = StringUtil.applySha256(tailBlock.getHash() + "hello" + magicNumber);
    }

    Block newBlock = new Block(tailBlock.getId() + 1, tailBlock.getHash(), hash, "hello", timestamp,
        magicNumber);
    blocks.add(newBlock);
    persister.save(blocks);
  }

  public boolean validate() {
    return validate(blocks);
  }

  @Override
  public Iterator<Block> iterator() {
    return blocks.iterator();
  }

  private boolean validate(List<Block> blocks) {
    for (int i = 1; i < blocks.size(); i++) {
      Block prev = blocks.get(i - 1);
      Block cur = blocks.get(i);
      if (!cur.getHashPreviousBlock().equals(prev.getHash())) {
        return false;
      }
    }
    return true;
  }

  private Block initialBlockHead() {
    Integer magicNumber = random.nextInt();
    String hash = StringUtil.applySha256("0" + magicNumber);
    while (!hash.startsWith(prefix)) {
      magicNumber = random.nextInt();
      hash = StringUtil.applySha256("0" + magicNumber);
    }
    return new Block(1,
        "0",
        hash,
        "empty",
        new Date().getTime(), magicNumber);
  }
}
