package TokensTable.src.minilang.lexer;

public enum TokenType{
    KEYWORD,
    IDENTIFIER,
    CONSTANT,

    ARITHMETIC_OPERATOR,
    RELATIONAL_OPERATOR,
    LOGICAL_OPERATOR,

    PUNCTUATION,
    SPECIAL_SYMBOL,
    STRING_DELIMITER,
    STRING_LITERAL, 

    UNKNOWN,

    ASSIGN_OPERATOR,
    PRINT_KEYWORD,       
    VAR_KEYWORD,         
    IF_KEYWORD,          
    ELSE_KEYWORD,        
    WHILE_KEYWORD,      
}