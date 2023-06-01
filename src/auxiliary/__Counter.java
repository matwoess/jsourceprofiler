package auxiliary;

import java.io.*;

public class __Counter {
  static {
    init("../metadata.dat");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> save(("../counts.dat"))));
  }

  private static int[] blockCounts;

  public static synchronized void inc(int n) {
    blockCounts[n]++;
  }

  public static synchronized void init(String fileName) {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
      int nBlocks = ois.readInt(); // number of blocks is the first value of the metadata file
      blockCounts = new int[nBlocks];
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized void save(String fileName) {
    try (DataOutputStream dis = new DataOutputStream(new FileOutputStream(fileName))) {
      dis.writeInt(blockCounts.length);
      for (int blockCount : blockCounts) {
        dis.writeInt(blockCount);
      }
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }
}
