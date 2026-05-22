package TokensTable.TokensTable;

import java.util.*;

public class Parser {

    private final List<Tokens> tokens;
    private int pos;
    private final List<ParseError> errors;
    private final List<Symbol> symbols = new ArrayList<>();
    

    public List<Symbol> getSymbols(){
        return symbols;
    }

    public Parser(List<Tokens> tokens) {
        this.tokens = tokens;
        this.pos    = 0;
        this.errors = new ArrayList<>();
    }

    public List<ParseError> getErrors() { return errors; }

    // Helpers

    private Tokens current() {
        return (pos < tokens.size()) ? tokens.get(pos) : null;
    }

    private boolean isEOF() { return pos >= tokens.size(); }

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
        addError("Se esperaba '" + lex + "' pero se encontró '" + lexeme() + "'");
        return false;
    }

    private Tokens consume() {
        return (!isEOF()) ? tokens.get(pos++) : null;
    }

    //Modo pánico: avanza hasta encontrar un punto de sincronización seguro. 
    private void addError(String msg) {
        errors.add(new ParseError(msg, currentLine(), currentPos(), lexeme()));
        // Avanzar hasta '}', nueva línea lógica o EOF — con límite para evitar loops
        int startPos = pos;
        int safetyLimit = tokens.size(); // nunca más iteraciones que tokens totales
        while (!isEOF() && safetyLimit-- > 0) {
            String lex = lexeme();
            TokenType type = current().getType();
            // Puntos de sincronización: inicio de nueva sentencia o cierre de bloque
            if (lex.equals("}") || lex.equals("{")
                || type == TokenType.VAR_KEYWORD
                || type == TokenType.IF_KEYWORD
                || type == TokenType.WHILE_KEYWORD
                || type == TokenType.PRINT_KEYWORD) {
                break;
            }
            pos++;
        }
        // Si no avanzamos nada (token problemático es una keyword), forzar avance de 1
        if (pos == startPos && !isEOF()) {
            pos++;
        }
    }

    // Punto de entrada 
    public void parse() {
        while (!isEOF()) {
            int before = pos;
            parseStatement();
            // Garantía anti-loop: si parseStatement no avanzó, forzar avance
            if (pos == before) pos++;
        }
    }

    // Sentencias 

    private void parseStatement() {
        if (isEOF()) return;

        TokenType type = current().getType();
        String lex     = lexeme();

        if (type == TokenType.VAR_KEYWORD)   { parseVarDecl(); }
        else if (type == TokenType.IF_KEYWORD)    { parseIfStmt(); }
        else if (type == TokenType.WHILE_KEYWORD) { parseWhileStmt(); }
        else if (type == TokenType.PRINT_KEYWORD) { parsePrintStmt(); }
        else if (type == TokenType.IDENTIFIER)    { parseAssignment(); }
        else if (lex.equals("}"))                 { /* fin de bloque, no consumir */ }
        else { addError("Sentencia no reconocida: '" + lex + "'"); }
    }

    private void parseVarDecl() {
        pos++; // consume 'var'
        if (isEOF() || current().getType() != TokenType.IDENTIFIER) {
            addError("Se esperaba un identificador después de 'var'");
            return;
        }
            String varName = current().getLexeme();
            int varLine = current().getLine();
            pos ++; 

        if (!expect("=")) return;

        if(!isEOF()){
            String varValue = current().getLexeme();
            String varType = inferType(current());
            symbols.add(new Symbol(varName, varType, varValue, varLine));
        }
        parseExpression();
    }

    private void parseAssignment() {
        pos++; // consume IDENTIFIER
        if (!isEOF() && current().getType() == TokenType.ASSIGN_OPERATOR) {
            pos++;
            parseExpression();
        } else {
            addError("Se esperaba '=' después del identificador");
        }

    }

    private void parseIfStmt() {
        pos++; // consume 'if'
        if (!expect("(")) return;
        parseCondition();
        if (!expect(")")) return;
        if (!expect("{")) return;
        parseBlock();
        if (!expect("}")) return;

        if (!isEOF() && current().getType() == TokenType.ELSE_KEYWORD) {
            pos++;
            if (!expect("{")) return;
            parseBlock();
            if (!expect("}")) return;
        }
    }

    private void parseWhileStmt() {
        pos++; // consume 'while'
        if (!expect("(")) return;
        parseCondition();
        if (!expect(")")) return;
        if (!expect("{")) return;
        parseBlock();
        if (!expect("}")) return;
    }

    private void parsePrintStmt() {
        pos++; // consume 'print'
        if (!expect("(")) return;
        parseExpression();
        if (!expect(")")) return;
    }

    private void parseBlock() {
        int limit = tokens.size(); // anti-loop
        while (!isEOF() && !lexeme().equals("}") && limit-- > 0) {
            int before = pos;
            parseStatement();
            if (pos == before) pos++; // anti-loop: forzar avance si no se movió
        }
    }

    // Expresiones 

    private void parseExpression() {
        parseTerm();
        while (!isEOF()) {
            String lex = lexeme();
            if ((lex.equals("+") || lex.equals("-"))
                && current().getType() == TokenType.ARITHMETIC_OPERATOR) {
                pos++;
                parseTerm();
            } else break;
        }
    }

    private void parseTerm() {
        parseFactor();
        while (!isEOF()) {
            String lex = lexeme();
            if ((lex.equals("*") || lex.equals("/"))
                && current().getType() == TokenType.ARITHMETIC_OPERATOR) {
                pos++;
                parseFactor();
            } else break;
        }
    }

    private void parseFactor() {
        if (isEOF()) {
            return;
        }
        TokenType type = current().getType();
        String lex     = lexeme();

        if (type == TokenType.CONSTANT) {
            pos++;
        } else if (type == TokenType.STRING_DELIMITER) {
            pos++;
            if (!isEOF() && current().getType() == TokenType.STRING_LITERAL) pos++;
            if (!isEOF() && current().getType() == TokenType.STRING_DELIMITER) pos++;
        } else if (type == TokenType.STRING_LITERAL) {
            pos++;
        } else if (type == TokenType.IDENTIFIER) {
            pos++;
        } else if (lex.equals("(")) {
            pos++;
            parseExpression();
            expect(")");
        } else {
            addError("Se esperaba un valor, se encontró '" + lex + "'");
        }
    }

    private void parseCondition() {
        parseExpression();
        if (!isEOF() && current().getType() == TokenType.RELATIONAL_OPERATOR) {
            pos++;
            parseExpression();
        }
    }

    private String inferType(Tokens t){
        if (t.getType() == TokenType.CONSTANT){
            return t.getLexeme().contains(".") ? "float" : "int"; // operador ternario, if en una sola linea
            }else if (t.getType() == TokenType.STRING_DELIMITER || t.getType() == TokenType.STRING_LITERAL){
                return "string";
            }else if(t.getType() == TokenType.IDENTIFIER){
                return "var";
            }
            return "unknown";
        }
 }



