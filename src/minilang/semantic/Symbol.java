package TokensTable.src.minilang.semantic;

public class Symbol{
    private String name;
    private String type;
    private String value;
    private int line;

    public Symbol(String name, String type, String value, int line){
        this.name = name;
        this.type = type;
        this.value = value; 
        this.line = line;
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public String getValue(){ 
        return value; 
    }

    public int getLine(){ 
        return line; 
    }

}