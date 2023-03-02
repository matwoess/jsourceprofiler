import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Lambdas {
  public static void main(String[] args) {
    "xyz".chars().forEach(ch -> {
      System.out.println(ch);
    });

    Function<Integer, Integer> doubleFn = (num) -> { return num*2; };
    int result = doubleFn.apply(5);
    System.out.println(result);

    Supplier<String> getHello = () -> {
      return "Hello";
    };
    System.out.println(getHello.get());

    Function<Integer, Double> divideBy3 = num -> num / 3.0;
    double number = divideBy3.apply(7);
    System.out.println(number);

    BiFunction<String, Predicate<String>, Integer> parseIntIf
        = (num, pred) -> pred.test(num) ? Integer.parseInt(num) : -1;
    Predicate<String> isNotBlank = (String str) -> !str.isBlank();
    result = parseIntIf.apply("234", isNotBlank);
    System.out.println(result);

    int[] array = new int[]{1, 2, 3, 4, 5, 6};
    Arrays.stream(array)
        .map(x -> x*2)
        .peek(x -> System.out.printf("%d ", x))
        .filter(x -> (x > 5))
        .reduce((acc, x) -> ((acc) + (x)))
        .ifPresent(possibleResult -> System.out.println("\nRes: " + possibleResult));
  }
}