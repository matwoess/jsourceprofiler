public class Switches {
  public static void main(String[] args) {
    String value = "aBcDe";
    switch (value.charAt(1)) {
      case 'a':
        System.out.println("is 'a'");
        break;
      case 'c':
        System.out.println("is 'c");
        break;
      case 'B': {
        System.out.println("is 'B'");
        break;
      }
      default:
        System.out.println("is other char.");
        break;
    }

    switch (value) {
      case "aBcDe" -> {
        if (value.toUpperCase().equals("ABCDE")) {
          System.out.println("as expected");
        }
      }
      case "other value" -> System.out.println("unexpected");
      case "" -> {
        break;
      }
      default -> throw new RuntimeException("should never happen");
    }

    int dependingOn = 5;
    int result = switch (dependingOn) {
      case 5 -> {
        yield 2;
      }
      case 1 -> 5;
      default -> {
        if (dependingOn < 10) {
          System.out.println("none of the above");
          yield 0;
        } else {
          yield -1;
        }
      }
    };
    System.out.println("result=" + result);
  }
}