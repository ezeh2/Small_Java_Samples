using System.Diagnostics;
using System.Globalization;
using Zeh.Expressions;

var evaluator = new ExpressionEvaluator(new Dictionary<string, string> { ["a"] = "3", ["b"] = "2*a" });
var cases = new (string Expression, double Expected)[]
{
    ("3*(3+2*(-1+2)+1)", 18), ("2+3*4", 14), ("(2+3)*4", 20),
    ("8/4/2", 1), ("10-3-2", 5), ("-2*-3", 6), ("-(2+3)", -5),
    (".5+1.", 1.5), (" 3 * (2 + 1) ", 9), ("50%", .5),
    ("200*10%", 20), ("(20+30)%", .5), ("50%%", .005),
    ("1+2>3", 1), ("2*3>4", 0), ("(1+2)>2", 1),
    ("1<2", 1), ("2>=2", 1), ("2<=1", 0), ("2<>3", 1), ("2==2", 1),
    ("1<2<3", 1), ("0|2&3", 1), ("0&missing", 0), ("1|missing", 1),
    ("1?10:20", 10), ("0?missing:20", 20), ("1?10:missing", 10),
    ("0?1:2+3", 5), ("1?2+3:4", 5),
    ("1?0?2:3:4", 3), ("0?(1?2:3):4", 4),
    ("a*(b+1)", 21), ("1/0", double.PositiveInfinity), ("0/0", double.NaN)
};
var checks = 0;
void Equal(double expected, double actual, string label)
{
    if (!expected.Equals(actual) && Math.Abs(expected - actual) > 1e-12
        || double.IsNaN(expected) != double.IsNaN(actual))
        throw new Exception($"{label}: expected {expected}, got {actual}");
    checks++;
}
void Throws<T>(Action action, string label) where T : Exception
{
    try { action(); }
    catch (T) { checks++; return; }
    throw new Exception($"{label}: expected {typeof(T).Name}");
}
foreach (var test in cases) Equal(test.Expected, evaluator.Evaluate(test.Expression), test.Expression);
foreach (var invalid in new[] { "", " ", "1+", "()", "(1+2", "1+2)", "1 2", "ab cd", "1..2", ".", "2**3", "1&&2", "1!=2", "1?2", "1:2", "1?2:", "2(3)", "1e3", "1 trailing", "2%3" })
    Throws<FormatException>(() => evaluator.Check(invalid), invalid);
evaluator.Check("missing+1"); checks++;
Throws<KeyNotFoundException>(() => evaluator.Evaluate("missing"), "unknown variable");
var cyclic = new ExpressionEvaluator(new Dictionary<string, string> { ["x"] = "y+1", ["y"] = "x+1" });
Throws<InvalidOperationException>(() => cyclic.Evaluate("x"), "indirect cycle");
Throws<InvalidOperationException>(() => cyclic.Evaluate("x"), "cycle state restored");
Equal(1, cyclic.Evaluate("1?1:x"), "unused cycle");
// Deliberate corrections to legacy bugs, not Java differential cases.
Equal(2, evaluator.Evaluate("1+(2<=3)"), "compound comparison inside sum");
Equal(2, evaluator.Evaluate("--2"), "repeated unary minus");
Equal(3, evaluator.Evaluate("1?0:1?2:3"), "left associative conditional chain");
Equal(3, evaluator.Evaluate("0?missing:1?3:missing"), "unselected conditional subtree");

if (args.Length == 1)
{
    var root = Path.GetFullPath(args[0]);
    var temporary = Path.Combine(Path.GetTempPath(), "evaluator-reference-" + Guid.NewGuid().ToString("N"));
    Directory.CreateDirectory(temporary);
    try
    {
        Run("java", new[] { "--module", "jdk.compiler/com.sun.tools.javac.Main", "-encoding", "ISO-8859-1", "-d", temporary }
            .Concat(Directory.GetFiles(Path.Combine(root, "evaluator", "src"), "*.java", SearchOption.AllDirectories))
            .Append(Path.Combine(AppContext.BaseDirectory, "JavaReference.java")));
        var output = Run("java", new[] { "-cp", temporary, "JavaReference" }.Concat(cases.Select(c => c.Expression)));
        var lines = output.Split('\n', StringSplitOptions.RemoveEmptyEntries);
        if (lines.Length != cases.Length) throw new Exception("Wrong Java result count.");
        for (var i = 0; i < cases.Length; i++)
            Equal(double.Parse(lines[i], CultureInfo.InvariantCulture), evaluator.Evaluate(cases[i].Expression), "Java: " + cases[i].Expression);
        Console.WriteLine($"Java comparison: {cases.Length} expressions matched.");
    }
    finally { Directory.Delete(temporary, true); }
}
Console.WriteLine($"Passed {checks} checks.");

static string Run(string executable, IEnumerable<string> arguments)
{
    var info = new ProcessStartInfo(executable) { RedirectStandardOutput = true, RedirectStandardError = true };
    foreach (var argument in arguments) info.ArgumentList.Add(argument);
    using var process = Process.Start(info) ?? throw new Exception($"Cannot start {executable}");
    var output = process.StandardOutput.ReadToEndAsync();
    var error = process.StandardError.ReadToEndAsync();
    process.WaitForExit();
    if (process.ExitCode != 0) throw new Exception(error.GetAwaiter().GetResult());
    return output.GetAwaiter().GetResult();
}
