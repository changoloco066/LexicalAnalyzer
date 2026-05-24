package TokensTable.LexicalAnalyzer;
import javax.swing.JFrame;
import javax.swing.JTable;
import java.util.*;
public class Lexer {
 
    private List<Tokens> tokens;
 
    // Palabras clave del MINI LENGUAJE
    private static final Set<String> MINI_KEYWORDS = Set.of("if", "else", "while", "print", "var");
 
    // Palabras clave de lenguajes comunes (para referencia)
    private static final Set<String> GENERAL_KEYWORDS = Set.of("int", "float", "return", "for");
 
    private static final Set<Character> ARITHMETIC_OPS   = Set.of('+', '-', '*', '/', '%', '^');
    private static final Set<Character> RELATIONAL_OPS   = Set.of('<', '>');
    private static final Set<Character> LOGICAL_OPS      = Set.of('&', '|', '!');
    private static final Set<Character> PUNCTUATION      = Set.of(';', ',', '.', ':', '(', ')', '{', '}', '[', ']');
    private static final Set<Character> SPECIAL_SYMBOLS  = Set.of('@', '#', '$', '_');
 
    public Lexer() {
        tokens = new ArrayList<>();
    }
 
    public List<Tokens> analyze(String input) {
        tokens.clear();
        String[] lines = input.split("\n");
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            analyzeLine(lines[lineNum], lineNum + 1);
        }
        return tokens;
    }
 
    private void analyzeLine(String line, int lineNumber) {
        int i = 0;
 
        while (i < line.length()) {
            char c = line.charAt(i);
 
            // Espacios
            if (Character.isWhitespace(c)) { i++; continue; }
 
            int start = i;
 
            // --- Comentarios de línea: // ---
            if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                break; // ignorar resto de la línea
            }
 
            // --- Strings entre comillas dobles ---
            if (c == '"') {
                addToken(String.valueOf(c), TokenType.STRING_DELIMITER, start, lineNumber);
                i++;
                StringBuilder sb = new StringBuilder();
                int strStart = i;
                while (i < line.length() && line.charAt(i) != '"') {
                    sb.append(line.charAt(i));
                    i++;
                }
                if (sb.length() > 0) {
                    addToken(sb.toString(), TokenType.STRING_LITERAL, strStart, lineNumber);
                }
                if (i < line.length()) {
                    addToken("\"", TokenType.STRING_DELIMITER, i, lineNumber);
                    i++;
                }
                continue;
            }
 
            // --- Strings entre comillas simples ---
            if (c == '\'') {
                addToken(String.valueOf(c), TokenType.STRING_DELIMITER, start, lineNumber);
                i++;
                StringBuilder sb = new StringBuilder();
                int strStart = i;
                while (i < line.length() && line.charAt(i) != '\'') {
                    sb.append(line.charAt(i));
                    i++;
                }
                if (sb.length() > 0) {
                    addToken(sb.toString(), TokenType.STRING_LITERAL, strStart, lineNumber);
                }
                if (i < line.length()) {
                    addToken("'", TokenType.STRING_DELIMITER, i, lineNumber);
                    i++;
                }
                continue;
            }
 
            // --- Identificadores y Palabras clave ---
            if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (i < line.length() &&
                       (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                    sb.append(line.charAt(i));
                    i++;
                }
                String word = sb.toString();
 
                switch (word) {
                    case "var"   -> addToken(word, TokenType.VAR_KEYWORD,   start, lineNumber);
                    case "if"    -> addToken(word, TokenType.IF_KEYWORD,    start, lineNumber);
                    case "else"  -> addToken(word, TokenType.ELSE_KEYWORD,  start, lineNumber);
                    case "while" -> addToken(word, TokenType.WHILE_KEYWORD, start, lineNumber);
                    case "print" -> addToken(word, TokenType.PRINT_KEYWORD, start, lineNumber);
                    default -> {
                        if (GENERAL_KEYWORDS.contains(word)) {
                            addToken(word, TokenType.KEYWORD, start, lineNumber);
                        } else {
                            addToken(word, TokenType.IDENTIFIER, start, lineNumber);
                        }
                    }
                }
                continue;
            }
 
            // --- Números (enteros y flotantes) ---
            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                boolean hasDot = false;
                while (i < line.length()) {
                    char cur = line.charAt(i);
                    if (Character.isDigit(cur)) {
                        sb.append(cur);
                    } else if (cur == '.' && !hasDot) {
                        sb.append(cur);
                        hasDot = true;
                    } else {
                        break;
                    }
                    i++;
                }
                addToken(sb.toString(), TokenType.CONSTANT, start, lineNumber);
                continue;
            }
 
            // --- Operador de asignación = y == ---
            if (c == '=') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '=') {
                    addToken("==", TokenType.RELATIONAL_OPERATOR, start, lineNumber);
                    i += 2;
                } else {
                    addToken("=", TokenType.ASSIGN_OPERATOR, start, lineNumber);
                    i++;
                }
                continue;
            }
 
            // --- != ---
            if (c == '!' && i + 1 < line.length() && line.charAt(i + 1) == '=') {
                addToken("!=", TokenType.RELATIONAL_OPERATOR, start, lineNumber);
                i += 2;
                continue;
            }
 
            // --- >= y <= ---
            if ((c == '>' || c == '<') && i + 1 < line.length() && line.charAt(i + 1) == '=') {
                addToken(String.valueOf(c) + "=", TokenType.RELATIONAL_OPERATOR, start, lineNumber);
                i += 2;
                continue;
            }
 
            // --- && y || ---
            if (c == '&' && i + 1 < line.length() && line.charAt(i + 1) == '&') {
                addToken("&&", TokenType.LOGICAL_OPERATOR, start, lineNumber);
                i += 2;
                continue;
            }
            if (c == '|' && i + 1 < line.length() && line.charAt(i + 1) == '|') {
                addToken("||", TokenType.LOGICAL_OPERATOR, start, lineNumber);
                i += 2;
                continue;
            }
 
            // --- Operadores aritméticos ---
            if (ARITHMETIC_OPS.contains(c)) {
                addToken(String.valueOf(c), TokenType.ARITHMETIC_OPERATOR, start, lineNumber);
                i++;
                continue;
            }
 
            // --- Operadores relacionales simples ---
            if (RELATIONAL_OPS.contains(c)) {
                addToken(String.valueOf(c), TokenType.RELATIONAL_OPERATOR, start, lineNumber);
                i++;
                continue;
            }
 
            // --- Operadores lógicos simples ---
            if (LOGICAL_OPS.contains(c)) {
                addToken(String.valueOf(c), TokenType.LOGICAL_OPERATOR, start, lineNumber);
                i++;
                continue;
            }
 
            // --- Puntuación ---
            if (PUNCTUATION.contains(c)) {
                addToken(String.valueOf(c), TokenType.PUNCTUATION, start, lineNumber);
                i++;
                continue;
            }
 
            // --- Símbolos especiales ---
            if (SPECIAL_SYMBOLS.contains(c)) {
                addToken(String.valueOf(c), TokenType.SPECIAL_SYMBOL, start, lineNumber);
                i++;
                continue;
            }
 
            // --- Desconocido ---
            addToken(String.valueOf(c), TokenType.UNKNOWN, start, lineNumber);
            i++;
        }
    }
 
    private void addToken(String lexeme, TokenType type, int pos, int line) {
        tokens.add(new Tokens(lexeme, type, pos, line));
    }
}
 