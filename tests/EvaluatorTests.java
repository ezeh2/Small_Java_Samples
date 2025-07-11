import ch.zeh.evaluator.*;
import java.util.*;

public class EvaluatorTests {
    private static int testsRun = 0;
    private static int testsFailed = 0;

    private interface Test {
        void execute() throws Exception;
    }

    public static void main(String[] args) {
        run("testAdd", () -> testAdd());
        run("testMulAdd", () -> testMulAdd());
        run("testNested", () -> testNested());
        run("testVariable", () -> testVariable());
        run("testUnknownVariable", () -> testUnknownVariable());
        run("testSyntaxError", () -> testSyntaxError());
        run("testSlash", () -> testSlash());
        run("testCondition", () -> testCondition());
        run("testAnd", () -> testAnd());
        run("testOr", () -> testOr());
        if (testsFailed == 0) {
            System.out.println("All " + testsRun + " tests passed.");
        } else {
            System.out.println(testsFailed + " tests failed out of " + testsRun);
            System.exit(1);
        }
    }

    private static void run(String name, Test t) {
        testsRun++;
        try {
            t.execute();
            System.out.println(name + " passed");
        } catch (Throwable ex) {
            testsFailed++;
            System.out.println(name + " failed: " + ex.getMessage());
        }
    }

    private static void assertEquals(double expected, double actual) {
        if (Math.abs(expected - actual) > 1e-9) {
            throw new AssertionError("expected " + expected + " but got " + actual);
        }
    }

    public static void testAdd() throws Exception {
        Evaluator e = new Evaluator();
        assertEquals(4.0, e.eval("2+2"));
    }

    public static void testMulAdd() throws Exception {
        Evaluator e = new Evaluator();
        assertEquals(27.0, e.eval("3*(4+5)"));
    }

    public static void testNested() throws Exception {
        Evaluator e = new Evaluator();
        assertEquals(30.0, e.eval("2*(1+2*(3+4))"));
    }

    public static void testVariable() throws Exception {
        Dictionary<String,String> d = new Hashtable<>();
        d.put("a", "2+3");
        d.put("b", "a*5");
        Evaluator e = new Evaluator(d);
        assertEquals(25.0, e.eval("b"));
    }

    public static void testUnknownVariable() throws Exception {
        Evaluator e = new Evaluator();
        try {
            e.eval("x+1");
            throw new AssertionError("Expected EvalException");
        } catch (EvalException ex) {
            // expected
        }
    }

    public static void testSyntaxError() throws Exception {
        Evaluator e = new Evaluator();
        try {
            e.eval("2**2");
            throw new AssertionError("Expected ExpressionException");
        } catch (ExpressionException ex) {
            // expected
        }
    }

    public static void testSlash() throws Exception {
        Evaluator e = new Evaluator();
        assertEquals(2.5, e.eval("5/2"));
    }

    public static void testCondition() throws Exception {
        Evaluator e = new Evaluator();
        assertEquals(2.0, e.eval("1?2:3"));
    }

    public static void testAnd() throws Exception {
        Evaluator e = new Evaluator();
        assertEquals(0.0, e.eval("1&0"));
    }

    public static void testOr() throws Exception {
        Evaluator e = new Evaluator();
        assertEquals(1.0, e.eval("1|0"));
    }
}
