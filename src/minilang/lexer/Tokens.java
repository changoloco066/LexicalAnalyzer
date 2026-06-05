package TokensTable.src.minilang.lexer;

public class Tokens {
    private String lexeme;
    private TokenType type;
    private int position;
    private int line;

    public Tokens(String lexeme, TokenType type, int position, int line) {
        this.lexeme = lexeme;
        this.type = type;
        this.position = position;
        this.line = line;
    }

    public String getLexeme() {
        return lexeme;
    }

    public TokenType getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return lexeme + " | " + type + " | " + position + " | " + line;
    }
}
