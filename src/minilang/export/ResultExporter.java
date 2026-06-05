package TokensTable.src.minilang.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import TokensTable.src.minilang.lexer.Tokens;
import TokensTable.src.minilang.parser.ParseError;
import TokensTable.src.minilang.parser.SyntaxTreeNode;
import TokensTable.src.minilang.semantic.Symbol;

public class ResultExporter {
    private static final String SEPARATOR = "==================================================";

    // Clase de utilidad: no necesita crear objetos, solo exportar resultados.
    private ResultExporter() {
    }

    // Genera un archivo de texto con todas las partes del analisis.
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
            // Si no hay errores, se deja una marca clara en el reporte.
            if (errors == null || errors.isEmpty()) {
                writer.write("No syntax errors found.");
                writer.newLine();
            } else {
                // Cada error se escribe con su linea, posicion y contexto.
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
            // El arbol puede no existir si el analisis no lo genero.
            if (syntaxTree == null) {
                writer.write("(No syntax tree available)");
                writer.newLine();
            } else {
                writer.write(syntaxTree.toTextTree());
            }
        }
    }

    // Escribe la tabla de tokens ajustando el ancho de columnas al contenido.
    private static void writeTokensTable(BufferedWriter writer, List<Tokens> tokens) throws IOException {
        int lexemeWidth = "LEXEME".length();
        int typeWidth = "TYPE".length();
        int positionWidth = "POSITION".length();
        int lineWidth = "LINE".length();

        if (tokens != null) {
            // Primero se calcula el ancho necesario para que la tabla quede alineada.
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
            // Despues se imprimen las filas usando el formato calculado.
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

    // Escribe la tabla de simbolos con el mismo formato alineado.
    private static void writeSymbolsTable(BufferedWriter writer, List<Symbol> symbols) throws IOException {
        int nameWidth = "NAME".length();
        int typeWidth = "TYPE".length();
        int valueWidth = "VALUE".length();
        int lineWidth = "LINE".length();

        if (symbols != null) {
            // Se revisan todos los simbolos para conocer el ancho real de cada columna.
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
            // Con los anchos listos, se escribe cada simbolo encontrado.
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

    // Imprime el encabezado de cada bloque del reporte.
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
