# EBNF expression evaluator for .NET

This .NET 8 implementation loads `Evaluator/expression.ebnf` directly with
[Eto.Parse](https://github.com/picoe/Eto.Parse) 1.6.0 and evaluates the resulting
named match tree. The EBNF is embedded in the library; no working-directory-dependent
file lookup or separate parser-generation step is needed. Editing the grammar
requires rebuilding, and new operations also require matching evaluation logic.

## Build and run

From the repository root, with the .NET 8 SDK installed:

```sh
dotnet build evaluator-dotnet/Evaluator.sln -c Release
dotnet run --project evaluator-dotnet/Evaluator.Cli -- "3*(2+1)"
# 9
dotnet run --project evaluator-dotnet/Evaluator.Cli -- "a*(b+1)" "a=3" "b=2*a"
# 21
```

The CLI returns exit code 0 on success, 1 for an invalid expression or variable,
and 2 when no expression is supplied. Quote expressions to protect operators
from shell interpretation.

## Library API

Reference `Evaluator/Evaluator.csproj`, then:

```csharp
using Zeh.Expressions;

var evaluator = new ExpressionEvaluator(new Dictionary<string, string>
{
    ["a"] = "3",
    ["b"] = "2*a"
});
double result = evaluator.Evaluate("a*(b+1)"); // 21
evaluator.Check("unknown+1");                 // syntax only; succeeds
```

Variables are case-sensitive and contain expressions. The supplied dictionary is
copied; evaluation does not mutate it. Cycles throw `InvalidOperationException`,
unknown variables throw `KeyNotFoundException`, and syntax errors throw
`FormatException`. Unselected conditional branches and short-circuited boolean
operands are not evaluated, so they can contain unresolved variables.

## Syntax and precedence

Highest precedence first, following the unusual Java `TreeFactory` priorities:

| Level | Operators |
| --- | --- |
| 1 | Parentheses, postfix percent, unary signs |
| 2 | Comparisons `<`, `>`, `=`, and combinations |
| 3 | Logical AND `&` |
| 4 | Logical OR `\|` |
| 5 | Conditional `? :` |
| 6 | `*`, `/` |
| 7 | `+`, `-` |

Binary operators and chained conditionals associate to the left. The true
conditional branch accepts a full expression, matching Java's implicit grouping
between `?` and `:`. Use parentheses to make nested conditions explicit.
Examples: `1+2>3` is `1+(2>3)` and returns 1; `1?0:1?2:3` returns 3.

`%` divides the preceding operand by 100, rather than computing modulo.
Comparisons and boolean operations return 0 or 1; any nonzero value is true.
Comparison character combinations are ORed: `<>` means unequal, `<=` means
less-or-equal, and `==` is equivalent to `=`. `!=`, `&&`, and `||` are unsupported.
Numbers use an invariant decimal point (`.5` and `1.` are allowed); exponent
notation is unsupported. IEEE double arithmetic is used, including infinity and
NaN on division by zero.

## Deliberate differences from the legacy parser

This is a documented expression language, not bug-for-bug emulation of Java's
`StreamTokenizer` and state machine:

- Extra operands, malformed decimals, and trailing input are rejected.
- Chained conditionals have explicit left-associative semantics; Java rejects
  some unparenthesized chains such as `1?0:1?2:3`.
- Repeated unary signs have their mathematical meaning (`--2` returns 2).
- Multi-character comparisons work within larger expressions, including
  `1+(2<=3)`; the Java implementation can reject these because it inspects the root.
- Names begin with a Unicode letter, followed by letters, ASCII digits, or dots;
  Java's wider token-character rules are not reproduced.
- Comments are not supported in input expressions; `(* ... *)` comments in the
  grammar describe the grammar itself.
- Spaces are permitted between tokens but never within a number, name, or
  multi-character comparison operator.

The original Java implementation is unchanged.

## Tests

The dependency-light test executable exits unsuccessfully on the first failed
assertion (use `dotnet run`, not `dotnet test`):

```sh
dotnet run --project evaluator-dotnet/Evaluator.Tests
```

With Java 17+ including the `jdk.compiler` module, compare the same expression
cases against the original Java source as well:

```sh
dotnet run --project evaluator-dotnet/Evaluator.Tests -- .
```

The final argument is the repository root. Java compilation uses ISO-8859-1 for
the original sources and writes classes only to a temporary directory.
