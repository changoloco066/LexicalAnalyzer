package TokensTable.LexicalAnalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ResultExporter {
    private static final String SEPARATOR = "==================================================";

    private ResultExporter() {
    }

    public static void exportToTxt(
            File file,
            String sourceCode,
            List<Tokens> tokens,
            List<ParseError> errors,
            List<Symbol> symbols,
            SyntaxTreeNode syntaxTree) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writeSectionTitle(writer, "SOURCE CODE");
            writer.write(sourceCode != null ? sourceCode : "");
            writer.newLine();
            writer.newLine();

            writeSectionTitle(writer, "TOKENS");
            writeTokensTable(writer, tokens);
            writer.newLine();

            writeSectionTitle(writer, "SYNTAX ERRORS");
            if (errors == null || errors.isEmpty()) {
                writer.write("No syntax errors found.");
                writer.newLine();
            } else {
                for (ParseError error : errors) {
                    writer.write("[L\u00ednea " + error.getLine()
                            + ", Pos " + error.getPosition()
                            + "] " + error.getMessage()
                            + " \u2192 near: \"" + error.getContext() + "\"");
                    writer.newLine();
                }
            }
            writer.newLine();

            writeSectionTitle(writer, "SYMBOL TABLE");
            writeSymbolsTable(writer, symbols);
            writer.newLine();

            writeSectionTitle(writer, "SYNTAX TREE");
            if (syntaxTree == null) {
                writer.write("(No syntax tree available)");
                writer.newLine();
            } else {
                writer.write(syntaxTree.toTextTree());
            }
        }
    }

    private static void writeTokensTable(BufferedWriter writer, List<Tokens> tokens) throws IOException {
        int lexemeWidth = "LEXEME".length();
        int typeWidth = "TYPE".length();
        int positionWidth = "POSITION".length();
        int lineWidth = "LINE".length();

        if (tokens != null) {
            for (Tokens token : tokens) {
                lexemeWidth = Math.max(lexemeWidth, safe(token.getLexeme()).length());
                typeWidth = Math.max(typeWidth, String.valueOf(token.getType()).length());
                positionWidth = Math.max(positionWidth, String.valueOf(token.getPosition()).length());
                lineWidth = Math.max(lineWidth, String.valueOf(token.getLine()).length());
            }
        }

        String format = "%-" + lexemeWidth + "s | %-" + typeWidth + "s | %"
                + positionWidth + "s | %" + lineWidth + "s";
        String separator = repeat("-", lexemeWidth) + "-+-"
                + repeat("-", typeWidth) + "-+-"
                + repeat("-", positionWidth) + "-+-"
                + repeat("-", lineWidth);

        writer.write(String.format(format, "LEXEME", "TYPE", "POSITION", "LINE"));
        writer.newLine();
        writer.write(separator);
        writer.newLine();

        if (tokens != null) {
            for (Tokens token : tokens) {
                writer.write(String.format(
                        format,
                        safe(token.getLexeme()),
                        String.valueOf(token.getType()),
                        String.valueOf(token.getPosition()),
                        String.valueOf(token.getLine())));
                writer.newLine();
            }
        }
    }

    private static void writeSymbolsTable(BufferedWriter writer, List<Symbol> symbols) throws IOException {
        int nameWidth = "NAME".length();
        int typeWidth = "TYPE".length();
        int valueWidth = "VALUE".length();
        int lineWidth = "LINE".length();

        if (symbols != null) {
            for (Symbol symbol : symbols) {
                nameWidth = Math.max(nameWidth, safe(symbol.getName()).length());
                typeWidth = Math.max(typeWidth, safe(symbol.getType()).length());
                valueWidth = Math.max(valueWidth, safe(symbol.getValue()).length());
                lineWidth = Math.max(lineWidth, String.valueOf(symbol.getLine()).length());
            }
        }

        String format = "%-" + nameWidth + "s | %-" + typeWidth + "s | %-"
                + valueWidth + "s | %" + lineWidth + "s";
        String separator = repeat("-", nameWidth) + "-+-"
                + repeat("-", typeWidth) + "-+-"
                + repeat("-", valueWidth) + "-+-"
                + repeat("-", lineWidth);

        writer.write(String.format(format, "NAME", "TYPE", "VALUE", "LINE"));
        writer.newLine();
        writer.write(separator);
        writer.newLine();

        if (symbols != null) {
            for (Symbol symbol : symbols) {
                writer.write(String.format(
                        format,
                        safe(symbol.getName()),
                        safe(symbol.getType()),
                        safe(symbol.getValue()),
                        String.valueOf(symbol.getLine())));
                writer.newLine();
            }
        }
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static String repeat(String value, int count) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < count; i++) {
            text.append(value);
        }
        return text.toString();
    }

    private static void writeSectionTitle(BufferedWriter writer, String title) throws IOException {
        writer.write(SEPARATOR);
        writer.newLine();
        writer.write(title);
        writer.newLine();
        writer.write(SEPARATOR);
        writer.newLine();
        writer.newLine();
    }
}
