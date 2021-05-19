public class A {
    public static void main(String[] argv) {
        A a = new B();
        a.foo();
    }

    public void foo() {
        System.out.println("A");
    }
}

class B extends A {
    public void foo() {
        System.out.println("B");
    }
}

