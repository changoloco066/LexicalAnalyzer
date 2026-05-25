# MiniLang Compiler

A Java-based mini compiler with a graphical user interface built using Swing. It performs lexical and syntactic analysis on a custom mini language, displaying tokens, syntax errors, a symbol table, and a visual syntax tree.

## Features

- **Lexical analysis**: Tokenizes source code and classifies each token by type
- **Syntactic analysis**: Validates the structure of the program using a recursive descent parser
- **Error reporting**: Detects and reports syntax errors with line number, position, and context
- **Symbol table**: Tracks declared variables with their inferred type and initial value
- **Visual syntax tree**: Renders the parse tree graphically with zoom in/out support
- **Multi-line input**: Analyze complete programs with multiple statements
- **Resizable interface**: Adjustable split pane between the code editor and results

## Mini Language Syntax

MiniLang supports the following constructs:

**Variable declaration**
```
var x = 10
var name = "hello"
var pi = 3.14
```

**Arithmetic expressions**
```
var result = x + y * 2
x = x - 1
```

**Conditionals**
```
if (result > 15) {
    print("Greater than 15")
} else {
    print(result)
}
```

**Loops**
```
while (x > 0) {
    x = x - 1
}
```

**Print statement**
```
print(x)
print("Hello world")
```

**Comments**
```
// This is a comment
```

## Token Types

| Token Type | Description | Examples |
|------------|-------------|---------|
| `VAR_KEYWORD` | Variable declaration keyword | `var` |
| `IF_KEYWORD` | Conditional keyword | `if` |
| `ELSE_KEYWORD` | Alternative branch keyword | `else` |
| `WHILE_KEYWORD` | Loop keyword | `while` |
| `PRINT_KEYWORD` | Print keyword | `print` |
| `IDENTIFIER` | Variable or function names | `x`, `result`, `myVar` |
| `CONSTANT` | Numeric literals | `42`, `3.14` |
| `ASSIGN_OPERATOR` | Assignment | `=` |
| `ARITHMETIC_OPERATOR` | Math operators | `+`, `-`, `*`, `/`, `%`, `^` |
| `RELATIONAL_OPERATOR` | Comparison operators | `==`, `!=`, `<`, `>`, `<=`, `>=` |
| `LOGICAL_OPERATOR` | Boolean operators | `&&`, `\|\|`, `!` |
| `STRING_DELIMITER` | Quote marks | `"`, `'` |
| `STRING_LITERAL` | String content | `hello`, `world` |
| `PUNCTUATION` | Delimiters and separators | `;`, `(`, `)`, `{`, `}` |
| `UNKNOWN` | Unrecognized characters | Any other character |

## Grammar (BNF)

```
program    → statement*
statement  → varDecl | assignment | ifStmt | whileStmt | printStmt
varDecl    → 'var' id '=' expression
assignment → id '=' expression
ifStmt     → 'if' '(' condition ')' '{' statement* '}' ('else' '{' statement* '}')?
whileStmt  → 'while' '(' condition ')' '{' statement* '}'
printStmt  → 'print' '(' expression ')'
expression → term ( ('+' | '-') term )*
term       → factor ( ('*' | '/') factor )*
factor     → num | str | id | '(' expression ')'
condition  → expression relOp expression
relOp      → '==' | '!=' | '<' | '>' | '<=' | '>='
```

## Requirements

- Java 17 or higher (uses switch expressions and text blocks)
- No external dependencies — built entirely with Java SE and Swing