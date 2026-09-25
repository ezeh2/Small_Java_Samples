import ch.zeh.evaluator.Evaluator;
import java.util.Hashtable;

/** Differential-test adapter; does not change the original Java implementation. */
public class JavaReference {
    public static void main(String[] args) throws Exception {
        Hashtable<String, String> variables = new Hashtable<>();
        variables.put("a", "3");
        variables.put("b", "2*a");
        for (String expression : args)
            System.out.println(new Evaluator(variables).eval(expression));
    }
}
