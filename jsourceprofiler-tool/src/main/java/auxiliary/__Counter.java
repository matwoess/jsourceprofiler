package auxiliary;

import java.io.*;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Supplier;

/**
 * This class is used to count the number of times a block of code is executed.
 */
public class __Counter {
  static {
    init(".profiler/metadata.dat");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> save((".profiler/counts.dat"))));
  }

  private static long[] blockCounts;
  private static AtomicLongArray atomicBlockCounts;

  /**
   * Increments the counter for the given block.
   *
   * @param n the block id
   */
  public static void inc(int n) {
    blockCounts[n]++;
  }

  /**
   * Increments the counter for the given block, in a synchronized way.
   *
   * @param n the block id
   */
  public static void incSync(int n) {
    atomicBlockCounts.incrementAndGet(n);
  }

  /**
   * Initializes the counter-arrays with the given number of blocks.
   * <p>
   * The number of blocks is the first value of the metadata file.
   * One array is used for regular counters and one synchronized ones,
   * incremented by {@link #incSync}.
   *
   * @param fileName the location of the metadata file
   */
  private static void init(@SuppressWarnings("SameParameterValue") String fileName) {
    File file = new File(fileName);
    if (!file.exists()) {
      throw new RuntimeException("Metadata not found at expected path: " + file.getAbsolutePath());
    }
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      int nBlocks = ois.readInt(); // number of blocks is the first value of the metadata file
      blockCounts = new long[nBlocks];
      atomicBlockCounts = new AtomicLongArray(nBlocks);
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Saves the counter-array to the given file.
   * <p>
   * The final count for each block is the sum of the regular and synchronized counters.
   * Every counter is either synchronized or not, so only ever one of the two array values is non-zero.
   *
   * @param fileName the location of the file to save the counter-array to
   */
  private static void save(@SuppressWarnings("SameParameterValue") String fileName) {
    try (DataOutputStream dis = new DataOutputStream(new FileOutputStream(fileName))) {
      dis.writeInt(blockCounts.length);
      for (int i = 0; i < blockCounts.length; i++) {
        dis.writeLong(blockCounts[i] + atomicBlockCounts.get(i));
      }
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Call {@link #inc(int)} with the block id and then execute the given lambda <code>Runnable</code>.
   *
   * @param n      the block id
   * @param method the lambda <code>Runnable</code> to execute
   */
  public static void incLambda(int n, Runnable method) {
    __Counter.inc(n);
    method.run();
  }

  /**
   * Overloaded version of {@link #incLambda(int, Runnable)} using a Supplier with a generic return type.
   */
  public static <T> T incLambda(int n, Supplier<T> function) {
    __Counter.inc(n);
    return function.get();
  }

  /**
   * Synchronized version of {@link #incLambda(int, Runnable)}.
   */
  public static void incLambdaSync(int n, Runnable method) {
    __Counter.incSync(n);
    method.run();
  }

  /**
   * Synchronized version of {@link #incLambda(int, Supplier)}.
   */
  public static <T> T incLambdaSync(int n, Supplier<T> function) {
    __Counter.incSync(n);
    return function.get();
  }
}
