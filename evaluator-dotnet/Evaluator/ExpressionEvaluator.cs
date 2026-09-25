using System.Globalization;
using Eto.Parse;
using Eto.Parse.Grammars;

namespace Zeh.Expressions;

/// <summary>Parses EBNF with Eto.Parse and evaluates its named match tree.</summary>
public sealed class ExpressionEvaluator
{
    private readonly Grammar grammar;
    private readonly object parseLock = new();
    private readonly IReadOnlyDictionary<string, string> variables;

    public ExpressionEvaluator(IReadOnlyDictionary<string, string>? variables = null)
    {
        this.variables = variables is null
            ? new Dictionary<string, string>(StringComparer.Ordinal)
            : new Dictionary<string, string>(variables, StringComparer.Ordinal);
        using var stream = typeof(ExpressionEvaluator).Assembly
            .GetManifestResourceStream("Evaluator.expression.ebnf")
            ?? throw new InvalidOperationException("Embedded EBNF not found.");
        using var reader = new StreamReader(stream);
        grammar = new EbnfGrammar(EbnfStyle.Iso14977).Build(reader.ReadToEnd(), "Expression");
        grammar.AllowPartialMatch = false;
    }

    /// <summary>Checks syntax without resolving variables.</summary>
    public void Check(string expression) => Parse(expression);

    public double Evaluate(string expression) => EvaluateNode(Parse(expression), new HashSet<string>(StringComparer.Ordinal));

    private Match Parse(string expression)
    {
        ArgumentNullException.ThrowIfNull(expression);
        // Eto.Parse grammar instances are not shared concurrently while matching.
        lock (parseLock)
        {
            var match = grammar.Match(expression);
            if (!match.Success)
                throw new FormatException(match.ErrorMessage);
            return match;
        }
    }

    private double EvaluateNode(Match node, HashSet<string> activeVariables)
    {
        double Eval(Match child) => EvaluateNode(child, activeVariables);
        var children = node.Matches.ToArray();
        switch (node.Name)
        {
            case "Number":
                return double.Parse(node.Text, NumberStyles.AllowDecimalPoint, CultureInfo.InvariantCulture);
            case "Variable":
                var name = node.Text;
                if (!variables.TryGetValue(name, out var expression))
                    throw new KeyNotFoundException($"Unknown variable: {name}");
                if (!activeVariables.Add(name))
                    throw new InvalidOperationException($"Circular variable dependency: {name}");
                try { return Eval(Parse(expression)); }
                finally { activeVariables.Remove(name); }
            case "Unary":
                var value = Eval(children[^1]);
                foreach (var sign in children[..^1])
                    if (sign.Text == "-") value = -value;
                return value;
            case "Postfix":
                var percentValue = Eval(children[0]);
                for (var i = 1; i < children.Length; i++) percentValue /= 100;
                return percentValue;
            case "Conditional":
                var branchValue = Eval(children[0]);
                for (var i = 1; i < children.Length; i += 4)
                    branchValue = branchValue != 0
                        ? Eval(children[i + 1])
                        : Eval(children[i + 3]);
                return branchValue;
            case "Sum":
            case "Product":
            case "OrExpression":
            case "AndExpression":
            case "Comparison":
                var result = Eval(children[0]);
                for (var i = 1; i < children.Length; i += 2)
                {
                    var op = children[i].Text;
                    // Preserve Java's short-circuit behavior.
                    if (op == "&" && result == 0) { result = 0; continue; }
                    if (op == "|" && result != 0) { result = 1; continue; }
                    var right = Eval(children[i + 1]);
                    result = op switch
                    {
                        "+" => result + right,
                        "-" => result - right,
                        "*" => result * right,
                        "/" => result / right,
                        "&" => right != 0 ? 1 : 0,
                        "|" => right != 0 ? 1 : 0,
                        _ => ((op.Contains('<') && result < right)
                           || (op.Contains('>') && result > right)
                           || (op.Contains('=') && result == right)) ? 1 : 0
                    };
                }
                return result;
            case "Expression":
            case "Primary":
            case "Group":
                return Eval(children.Single());
            default:
                throw new InvalidOperationException($"Unsupported grammar node: {node.Name}");
        }
    }
}
