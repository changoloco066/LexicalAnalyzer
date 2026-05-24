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
}
