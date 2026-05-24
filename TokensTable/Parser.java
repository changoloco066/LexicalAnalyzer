package TokensTable.TokensTable;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Tokens> tokens;
    private int pos;
    private final List<ParseError> errors;
    private final List<Symbol> symbols = new ArrayList<>();
    private SyntaxTreeNode root;

    public Parser(List<Tokens> tokens) {
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
