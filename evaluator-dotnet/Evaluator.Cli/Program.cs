using System.Globalization;
using Zeh.Expressions;

if (args.Length == 0)
{
    Console.Error.WriteLine("Usage: Evaluator.Cli <expression> [name=expression ...]");
    return 2;
}
try
{
    var variables = new Dictionary<string, string>(StringComparer.Ordinal);
    foreach (var argument in args.Skip(1))
    {
        var separator = argument.IndexOf('=');
        if (separator <= 0 || separator == argument.Length - 1)
            throw new FormatException($"Invalid variable definition: {argument}");
        variables.Add(argument[..separator], argument[(separator + 1)..]);
    }
    Console.WriteLine(new ExpressionEvaluator(variables).Evaluate(args[0]).ToString("R", CultureInfo.InvariantCulture));
    return 0;
}
catch (Exception ex) when (ex is FormatException or KeyNotFoundException or InvalidOperationException or ArgumentException)
{
    Console.Error.WriteLine(ex.Message);
    return 1;
}
