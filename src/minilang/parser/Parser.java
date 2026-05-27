package TokensTable.src.minilang.parser;

import java.util.ArrayList;
import java.util.List;

import TokensTable.src.minilang.lexer.TokenType;
import TokensTable.src.minilang.lexer.Tokens;
import TokensTable.src.minilang.semantic.Symbol;

public class Parser {

    private final List<Tokens> tokens;
    private int pos;
    private final List<ParseError> errors;
    private final List<Symbol> symbols = new ArrayList<>();
    private SyntaxTreeNode root;

    public Parser(List<Tokens> tokens) {
        // El parser trabaja sobre la lista de tokens generada por el lexer.
        this.tokens = tokens;
        this.pos = 0;
        this.errors = new ArrayList<>();
    }

    public List<ParseError> getErrors() {
        return errors;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public SyntaxTreeNode getSyntaxTree() {
        return root;
    }

    private Tokens current() {
        return (pos < tokens.size()) ? tokens.get(pos) : null;
    }

    private boolean isEOF() {
        return pos >= tokens.size();
    }

    private String lexeme() {
        Tokens t = current();
        return t != null ? t.getLexeme() : "<EOF>";
    }

    private int currentLine() {
        Tokens t = current();
        return t != null ? t.getLine() : -1;
    }

    private int currentPos() {
        Tokens t = current();
        return t != null ? t.getPosition() : -1;
    }

    private boolean expect(String lex) {
        // Valida que el token actual sea el esperado y avanza si coincide.
        if (!isEOF() && lexeme().equals(lex)) {
            pos++;
            return true;
        }
        addError("Se esperaba '" + lex + "' pero se encontro '" + lexeme() + "'");
        return false;
    }

    private Tokens consume() {
        return (!isEOF()) ? tokens.get(pos++) : null;
    }

    private void addError(String msg) {
        errors.add(new ParseError(msg, currentLine(), currentPos(), lexeme()));

        // Intenta recuperarse avanzando hasta una zona donde pueda continuar el analisis.
        int startPos = pos;
        int safetyLimit = tokens.size();
        while (!isEOF() && safetyLimit-- > 0) {
            String lex = lexeme();
            TokenType type = current().getType();
            if (lex.equals("}") || lex.equals("{")
                    || type == TokenType.VAR_KEYWORD
                    || type == TokenType.IF_KEYWORD
                    || type == TokenType.WHILE_KEYWORD
                    || type == TokenType.PRINT_KEYWORD) {
                break;
            }
            pos++;
        }

        if (pos == startPos && !isEOF()) {
            pos++;
        }
    }

    public void parse() {
        // Punto de entrada: construye el nodo principal y analiza sentencia por sentencia.
        root = new SyntaxTreeNode("PROGRAMA");
        symbols.clear();

        while (!isEOF()) {
            int before = pos;
            SyntaxTreeNode statement = parseStatement();
            root.addChild(statement);

            if (pos == before) {
                pos++;
            }
        }
    }

    private SyntaxTreeNode parseStatement() {
        // Decide que tipo de sentencia leer segun el token actual.
        if (isEOF()) {
            return null;
        }

        TokenType type = current().getType();
        String lex = lexeme();

        if (type == TokenType.VAR_KEYWORD) {
            return parseVarDecl();
        } else if (type == TokenType.IF_KEYWORD) {
            return parseIfStmt();
        } else if (type == TokenType.WHILE_KEYWORD) {
            return parseWhileStmt();
        } else if (type == TokenType.PRINT_KEYWORD) {
            return parsePrintStmt();
        } else if (type == TokenType.IDENTIFIER) {
            return parseAssignment();
        } else if (lex.equals("}")) {
            return null;
        }

        addError("Sentencia no reconocida: '" + lex + "'");
        return new SyntaxTreeNode("ERROR: " + lex);
    }

    private SyntaxTreeNode parseVarDecl() {
        // Analiza declaraciones con la forma: var nombre = valor.
        SyntaxTreeNode node = new SyntaxTreeNode("DECLARACION VAR");
        pos++;

        if (isEOF() || current().getType() != TokenType.IDENTIFIER) {
            addError("Se esperaba un identificador despues de 'var'");
            return node;
        }

        String varName = current().getLexeme();
        int varLine = current().getLine();
        node.addChild(new SyntaxTreeNode("IDENTIFICADOR: " + varName));
        pos++;

        if (!expect("=")) {
            return node;
        }

        // Guarda la variable en la tabla de simbolos antes de leer la expresion completa.
        if (!isEOF()) {
            String varValue = current().getLexeme();
            String varType = inferType(current());
            symbols.add(new Symbol(varName, varType, varValue, varLine));
        }

        SyntaxTreeNode valueNode = new SyntaxTreeNode("VALOR");
        valueNode.addChild(parseExpression());
        node.addChild(valueNode);
        return node;
    }

    private SyntaxTreeNode parseAssignment() {
        // Analiza una asignacion que empieza con un identificador.
        SyntaxTreeNode node = new SyntaxTreeNode("ASIGNACION");
        Tokens identifier = consume();
        if (identifier != null) {
            node.addChild(new SyntaxTreeNode("IDENTIFICADOR: " + identifier.getLexeme()));
        }

        if (!isEOF() && current().getType() == TokenType.ASSIGN_OPERATOR) {
            pos++;
            SyntaxTreeNode valueNode = new SyntaxTreeNode("VALOR");
            valueNode.addChild(parseExpression());
            node.addChild(valueNode);
        } else {
            addError("Se esperaba '=' despues del identificador");
        }

        return node;
    }

    private SyntaxTreeNode parseIfStmt() {
        // Analiza la condicion, el bloque principal y opcionalmente el bloque else.
        SyntaxTreeNode node = new SyntaxTreeNode("IF");
        pos++;

        if (!expect("(")) {
            return node;
        }
        node.addChild(parseCondition());
        if (!expect(")")) {
            return node;
        }
        if (!expect("{")) {
            return node;
        }
        node.addChild(parseBlock("BLOQUE THEN"));
        if (!expect("}")) {
            return node;
        }

        if (!isEOF() && current().getType() == TokenType.ELSE_KEYWORD) {
            pos++;
            if (!expect("{")) {
                return node;
            }
            node.addChild(parseBlock("BLOQUE ELSE"));
            if (!expect("}")) {
                return node;
            }
        }

        return node;
    }

    private SyntaxTreeNode parseWhileStmt() {
        // Analiza un ciclo while con condicion y bloque entre llaves.
        SyntaxTreeNode node = new SyntaxTreeNode("WHILE");
        pos++;

        if (!expect("(")) {
            return node;
        }
        node.addChild(parseCondition());
        if (!expect(")")) {
            return node;
        }
        if (!expect("{")) {
            return node;
        }
        node.addChild(parseBlock());
        if (!expect("}")) {
            return node;
        }

        return node;
    }

    private SyntaxTreeNode parsePrintStmt() {
        // Analiza la llamada print(expresion).
        SyntaxTreeNode node = new SyntaxTreeNode("PRINT");
        pos++;

        if (!expect("(")) {
            return node;
        }
        node.addChild(parseExpression());
        if (!expect(")")) {
            return node;
        }

        return node;
    }

    private SyntaxTreeNode parseBlock() {
        return parseBlock("BLOQUE");
    }

    private SyntaxTreeNode parseBlock(String label) {
        SyntaxTreeNode block = new SyntaxTreeNode(label);
        int limit = tokens.size();

        // Lee sentencias hasta encontrar el cierre del bloque.
        while (!isEOF() && !lexeme().equals("}") && limit-- > 0) {
            int before = pos;
            SyntaxTreeNode statement = parseStatement();
            block.addChild(statement);

            if (pos == before) {
                pos++;
            }
        }

        return block;
    }

    private SyntaxTreeNode parseExpression() {
        // Maneja sumas y restas, apoyandose en terminos para respetar precedencia.
        SyntaxTreeNode left = parseTerm();
        if (left == null) {
            return null;
        }

        SyntaxTreeNode expression = null;
        while (!isEOF()) {
            String lex = lexeme();
            if ((lex.equals("+") || lex.equals("-"))
                    && current().getType() == TokenType.ARITHMETIC_OPERATOR) {
                if (expression == null) {
                    expression = new SyntaxTreeNode("EXPRESION");
                    expression.addChild(left);
                }
                expression.addChild(new SyntaxTreeNode("OPERADOR: " + lex));
                pos++;
                expression.addChild(parseTerm());
            } else {
                break;
            }
        }

        return expression != null ? expression : left;
    }

    private SyntaxTreeNode parseTerm() {
        // Maneja multiplicaciones y divisiones antes que la expresion principal.
        SyntaxTreeNode left = parseFactor();
        if (left == null) {
            return null;
        }

        SyntaxTreeNode term = null;
        while (!isEOF()) {
            String lex = lexeme();
            if ((lex.equals("*") || lex.equals("/"))
                    && current().getType() == TokenType.ARITHMETIC_OPERATOR) {
                if (term == null) {
                    term = new SyntaxTreeNode("TERMINO");
                    term.addChild(left);
                }
                term.addChild(new SyntaxTreeNode("OPERADOR: " + lex));
                pos++;
                term.addChild(parseFactor());
            } else {
                break;
            }
        }

        return term != null ? term : left;
    }

    private SyntaxTreeNode parseFactor() {
        // Un factor puede ser numero, texto, identificador o expresion agrupada.
        if (isEOF()) {
            return null;
        }

        TokenType type = current().getType();
        String lex = lexeme();

        if (type == TokenType.CONSTANT) {
            pos++;
            return new SyntaxTreeNode("NUMERO: " + lex);
        } else if (type == TokenType.STRING_DELIMITER) {
            String delimiter = lex;
            pos++;

            String value = "";
            if (!isEOF() && current().getType() == TokenType.STRING_LITERAL) {
                value = current().getLexeme();
                pos++;
            }
            if (!isEOF() && current().getType() == TokenType.STRING_DELIMITER) {
                pos++;
            } else {
                addError("Se esperaba cierre de cadena " + delimiter);
            }

            return new SyntaxTreeNode("STRING: " + value);
        } else if (type == TokenType.STRING_LITERAL) {
            pos++;
            return new SyntaxTreeNode("STRING: " + lex);
        } else if (type == TokenType.IDENTIFIER) {
            pos++;
            return new SyntaxTreeNode("IDENTIFICADOR: " + lex);
        } else if (lex.equals("(")) {
            pos++;
            SyntaxTreeNode expression = parseExpression();
            expect(")");
            SyntaxTreeNode group = new SyntaxTreeNode("GRUPO");
            group.addChild(expression);
            return group;
        }

        addError("Se esperaba un valor, se encontro '" + lex + "'");
        return new SyntaxTreeNode("ERROR: " + lex);
    }

    private SyntaxTreeNode parseCondition() {
        // Una condicion une dos expresiones mediante un operador relacional.
        SyntaxTreeNode condition = new SyntaxTreeNode("CONDICION");
        condition.addChild(parseExpression());

        if (!isEOF() && current().getType() == TokenType.RELATIONAL_OPERATOR) {
            condition.addChild(new SyntaxTreeNode("OPERADOR: " + lexeme()));
            pos++;
            condition.addChild(parseExpression());
        }

        return condition;
    }

    private String inferType(Tokens t) {
        // Deduce un tipo simple para mostrarlo en la tabla de simbolos.
        if (t.getType() == TokenType.CONSTANT) {
            return t.getLexeme().contains(".") ? "float" : "int";
        } else if (t.getType() == TokenType.STRING_DELIMITER || t.getType() == TokenType.STRING_LITERAL) {
            return "string";
        } else if (t.getType() == TokenType.IDENTIFIER) {
            return "var";
        }
        return "unknown";
    }
}
