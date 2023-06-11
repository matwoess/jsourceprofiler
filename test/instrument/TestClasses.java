package instrument;

import org.junit.jupiter.api.Test;

import model.Block;
import model.Method;
import model.Class;

import java.util.ArrayList;
import java.util.List;

import static model.BlockType.*;
import static instrument.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestClasses {
  @Test
  public void TestAbstractClass() {
    String abstractClass = """
        abstract class Pet {
          String name;
          int age;
          abstract String speak();  // method without block
          @Override
          public String toString() {
            return "Pet{" +
                "name='" + name + '\\'' +
                ", age=" + age +
                '}';
          }
        }""";
    List<Block> blocks = getFoundBlocks(abstractClass);
    assertEquals(1, blocks.size());
    Class clazz = new Class("Pet");
    Method meth = new Method("toString");
    Block expectedBlock = getBlock(METHOD, clazz, meth, 6, 11, 139, 234);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestInnerClassAndStaticBlocks() {
    String innerClassAndStaticBlocks = """
        public class Classes {
          static int i;
          
          static {
            i = 0;
          }
          
          public static void main(String[] args) {
            PetFarm farm = new PetFarm();
            farm.pets.add("Barky");
            farm.pets.add("Molly");
            for (String pet : farm.pets) {
              String output;
              if (pet.length() > 3) {
                output = String.format(pet + " has a long name.");
              } else {
                output = String.format(pet + " has a short name.");
              }
              System.out.println(output);
            }
          }
          
          static class PetFarm {
            static List<String> pets = new ArrayList<>();
            
            static {
              pets.add("Bob");
            }
          }
        }""";
    List<Block> blocks = getFoundBlocks(innerClassAndStaticBlocks);
    assertEquals(6, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Classes", true);
    expectedBlocks.add(getBlock(STATIC, clazz, null, 4, 6, 50, 65));
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 8, 21, 109, 471));
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 12, 20, 234, 467));
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 14, 16, 285, 352));
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 16, 18, 359, 427));
    Class innerClass = new Class("PetFarm");
    innerClass.setParentClass(clazz);
    expectedBlocks.add(getBlock(STATIC, innerClass, null, 26, 28, 561, 590));
    assertIterableEquals(expectedBlocks, blocks);
  }


  @Test
  public void TestDeepInnerClasses() {
    String deepInnerClasses = """
        public class InnerClasses {
          public static void main(String[] args) {
            Inner inner = new Inner();
            Inner.Sub innerSub = new Inner.Sub();
            Inner.Sub.SubSub innerSubSub = new Inner.Sub.SubSub();
            inner.level1();
            innerSub.level2();
            innerSubSub.level3();
          }
          static class Inner {
            void level1() {
              System.out.println("Level: 1");
            }
            static class Sub {
              void level2() {
                System.out.println("Level: 2");
              }
              static class SubSub {
                void level3() {
                  System.out.println("Level: 3");
                }
              }
            }
          }
        }""";
    List<Block> blocks = getFoundBlocks(deepInnerClasses);
    assertEquals(4, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("InnerClasses", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 2, 9, 70, 275));
    Class innerClass = new Class("Inner");
    innerClass.setParentClass(clazz);
    meth = new Method("level1");
    expectedBlocks.add(getBlock(METHOD, innerClass, meth, 11, 13, 318, 362));
    Class subInnerClass = new Class("Sub");
    subInnerClass.setParentClass(innerClass);
    meth = new Method("level2");
    expectedBlocks.add(getBlock(METHOD, subInnerClass, meth, 15, 17, 407, 455));
    clazz = new Class("InnerClasses.Inner.Sub.SubSub");
    Class subSubInnerClass = new Class("SubSub");
    subSubInnerClass.setParentClass(subInnerClass);
    meth = new Method("level3");
    expectedBlocks.add(getBlock(METHOD, subSubInnerClass, meth, 19, 21, 507, 559));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestClassWithInheritanceAndConstructors() {
    String classWithInheritance = """
        abstract class Pet {
          String name;
          int age;
          abstract String speak();  // method without block
          @Override
          public String toString() {
            return "Pet{" +
                "name='" + name + '\\'' +
                ", age=" + age +
                '}';
          }
        }
                
        class Dog extends Pet {
          public Dog(String name, int age) {
            this.name = name;
            this.age = age;
          }
          @Override String speak() { return "woof!"; }
        }
                
        class Cat extends Pet {
          public Cat(String name, int age) {
            this.name = name;
            this.age = age;
          }
          @Override String speak() { return "meow."; }
        }""";
    List<Block> blocks = getFoundBlocks(classWithInheritance);
    assertEquals(5, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Pet");
    Method meth = new Method("toString");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 6, 11, 139, 234));
    clazz = new Class("Dog");
    meth = new Method("Dog");
    expectedBlocks.add(getBlock(CONSTRUCTOR, clazz, meth, 15, 18, 298, 344));
    meth = new Method("speak");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 19, 19, 373, 391));
    clazz = new Class("Cat");
    meth = new Method("Cat");
    expectedBlocks.add(getBlock(CONSTRUCTOR, clazz, meth, 23, 26, 455, 501));
    meth = new Method("speak");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 27, 27, 530, 548));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestMembersWithInitBlocks() {
    String fileContent = """
        class InitBlocks {
          int[] ints = new int[]{0, 1, 3};
          List<Float> floats = new ArrayList<>(Arrays.asList(0.5f, 3.4f));
          String[] strings = new String[]{
              String.format("%d", ints[1]),
              floats.get(1).toString(),
              "ASDF",
          };
          void doNothing() {}
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    Class clazz = new Class("InitBlocks");
    Method meth = new Method("doNothing");
    Block expectedBlock = getBlock(METHOD, clazz, meth, 9, 9, 263, 264);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestMembersWithImplicitInitializers() {
    String fileContent = """
        class ImplicitInitBlocks {
          static String[] strings = {"String1", "String2"};
          private static final int[][] intArray = {
            {55, 44},
            {123, 456}
          };
          void doNothing() {}
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    Class clazz = new Class("ImplicitInitBlocks");
    Method meth = new Method("doNothing");
    Block expectedBlock = getBlock(METHOD, clazz, meth, 7, 7, 177, 178);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestSubClassWithFollowingMethod() {
    // testing that the classStack works
    String fileContent = """
        class A {
          class B {
            void classBMeth() {}
          }
          void classAMeth1() {}
          class C {
            class D {
              void classDMeth() {}
            }
            void classCMeth() {}
          }
          void classAMeth2() {}
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(5, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("A");
    Class innerClass1 = new Class("B");
    innerClass1.setParentClass(clazz);
    Method meth = new Method("classBMeth");
    expectedBlocks.add(getBlock(METHOD, innerClass1, meth, 3, 3, 45, 46));
    meth = new Method("classAMeth1");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 5, 5, 73, 74));
    Class innerClass2 = new Class("C");
    innerClass2.setParentClass(clazz);
    Class subInnerClass = new Class("D");
    subInnerClass.setParentClass(innerClass2);
    meth = new Method("classDMeth");
    expectedBlocks.add(getBlock(METHOD, subInnerClass, meth, 8, 8, 126, 127));
    meth = new Method("classCMeth");
    expectedBlocks.add(getBlock(METHOD, innerClass2, meth, 10, 10, 157, 158));
    meth = new Method("classAMeth2");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 12, 12, 185, 186));
    assertIterableEquals(expectedBlocks, blocks);
    assertEquals("A", clazz.getName());
    assertEquals("A$B", innerClass1.getName());
    assertEquals("A$C$D", subInnerClass.getName());
    assertEquals("A$C", innerClass2.getName());
  }

  @Test
  public void TestMethodsWithThrowsDeclarations() {
    // testing that the classStack works
    String fileContent = """
        abstract class ThrowClass {
          public abstract void method1() throws IOException;
          
          protected static int errorCode(int n) throws ArithmeticException, RuntimeException {
            return n / (n-1);
          }
          
          public static void main() throws IOException, RuntimeException {
            int err = errorCode(1);
            throw new RuntimeException(String.valueOf(err));
          }
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("ThrowClass", true);
    Method meth = new Method("errorCode");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 4, 6, 168, 194));
    meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 8, 11, 262, 347));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestInheritedClassWithSuperCallAndThisCall() {
    String classWithInheritance = """
        class Dog {
          String name;
          int age;
          public Dog(String name, int age) {
            this.name = name;
            this.age = age;
          }
          String speak() {
            return "woof";
          }
        }
        class SmallDog extends Dog {
          boolean amSmall;
          public SmallDog(String name, int age) {
            super(name, age);
            amSmall = true;
            super.speak();
          }
          public SmallDog(String name, int age, boolean small) {
            this(name, age);
            amSmall = small;
          }
          @Override
          String speak() {
            if (amSmall) {
              return "wuf!";
            } else {
              return super.speak();
            }
          }
        }""";
    List<Block> blocks = getFoundBlocks(classWithInheritance);
    assertEquals(7, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Dog");
    Method meth = new Method("Dog");
    expectedBlocks.add(getBlock(CONSTRUCTOR, clazz, meth, 4, 7, 74, 120));
    meth = new Method("speak");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 8, 10, 139, 162));
    clazz = new Class("SmallDog");
    meth = new Method("SmallDog");
    Block blockWithSuperCall = getBlock(CONSTRUCTOR, clazz, meth, 14, 18, 254, 319);
    blockWithSuperCall.incInsertPosition = blockWithSuperCall.incInsertPosition + "\n    super(name, age);".length();
    expectedBlocks.add(blockWithSuperCall);
    Block blockWithThisCall = getBlock(CONSTRUCTOR, clazz, meth, 19, 22, 376, 422);
    blockWithThisCall.incInsertPosition = blockWithThisCall.incInsertPosition + "\n    this(name, age);".length();
    expectedBlocks.add(blockWithThisCall);
    meth = new Method("speak");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 24, 30, 453, 544));
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 25, 27, 472, 499));
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 27, 29, 506, 540));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
