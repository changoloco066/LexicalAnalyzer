package TokensTable.src.minilang.parser;

import java.util.ArrayList;
import java.util.List;

public class SyntaxTreeNode {
    private final String label;
    private final List<SyntaxTreeNode> children;

    // Cada nodo tiene una etiqueta y una lista de hijos para formar el arbol.
    public SyntaxTreeNode(String label) {
        this.label = label;
        this.children = new ArrayList<>();
    }

    public void addChild(SyntaxTreeNode child) {
        // Evita agregar hijos nulos cuando algun analisis no produce nodo.
        if (child != null) {
            children.add(child);
        }
    }

    public String getLabel() {
        return label;
    }

    public List<SyntaxTreeNode> getChildren() {
        return children;
    }

    public String toTextTree() {
        // Convierte el arbol a texto para mostrarlo o exportarlo.
        StringBuilder text = new StringBuilder();
        buildTextTree(text, "", true, true);
        return text.toString();
    }

    private void buildTextTree(StringBuilder text, String prefix, boolean isLast, boolean isRoot) {
        // La raiz se imprime sin ramas; los demas nodos usan prefijos visuales.
        if (isRoot) {
            text.append(label).append(System.lineSeparator());
        } else {
            text.append(prefix)
                    .append(isLast ? "\u2514\u2500\u2500 " : "\u251c\u2500\u2500 ")
                    .append(label)
                    .append(System.lineSeparator());
        }

        String childPrefix = isRoot ? "" : prefix + (isLast ? "    " : "\u2502   ");
        // Recorre los hijos manteniendo el prefijo correcto para cada nivel.
        for (int i = 0; i < children.size(); i++) {
            children.get(i).buildTextTree(text, childPrefix, i == children.size() - 1, false);
        }
    }
}
