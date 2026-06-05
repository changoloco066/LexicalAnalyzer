package TokensTable.src.minilang.parser;

public class ParseError {
    private final String message;
    private final int line;
    private final int position;
    private final String context;
 
    public ParseError(String message, int line, int position, String context) {
        this.message = message;
        this.line = line;
        this.position = position;
        this.context = context;
    }
 
    public String getMessage(){
         return message; 
        }

    public int getLine(){ 
        return line; 
    }

    public int getPosition(){ 
        return position;
     }

    public String getContext(){ 
        return context;
     }
 
    @Override
    public String toString() {
        return "[Línea " + line + ", Pos " + position + "] " + message + " → near: \"" + context + "\"";
    }
}
