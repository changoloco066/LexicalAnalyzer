package TokensTable.TokensTable;

import java.util.ArrayList;
import java.util.List;

public class SyntaxTreeNode {
    private final String label;
    private final List<SyntaxTreeNode> children;

    public SyntaxTreeNode(String label) {
        this.label = label;
        this.children = new ArrayList<>();
    }

    public void addChild(SyntaxTreeNode child) {
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
        StringBuilder text = new StringBuilder();
        buildTextTree(text, "", true, true);
        return text.toString();
    }

    private void buildTextTree(StringBuilder text, String prefix, boolean isLast, boolean isRoot) {
        if (isRoot) {
            text.append(label).append(System.lineSeparator());
        } else {
            text.append(prefix)
                    .append(isLast ? "\u2514\u2500\u2500 " : "\u251c\u2500\u2500 ")
                    .append(label)
                    .append(System.lineSeparator());
        }

        String childPrefix = isRoot ? "" : prefix + (isLast ? "    " : "\u2502   ");
        for (int i = 0; i < children.size(); i++) {
            children.get(i).buildTextTree(text, childPrefix, i == children.size() - 1, false);
        }
    }
}
