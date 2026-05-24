package TokensTable.TokensTable;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

public class SyntaxTreePanel extends JPanel {
    private static final int MIN_NODE_WIDTH = 120;
    private static final int NODE_HEIGHT = 35;
    private static final int NODE_HORIZONTAL_PADDING = 22;
    private static final int HORIZONTAL_GAP = 30;
    private static final int VERTICAL_GAP = 70;
    private static final int MARGIN = 30;

    private final SyntaxTreeNode root;
    private final Map<SyntaxTreeNode, Integer> subtreeWidths = new HashMap<>();
    private final Map<SyntaxTreeNode, Integer> nodeWidths = new HashMap<>();

    public SyntaxTreePanel(SyntaxTreeNode root) {
        this.root = root;
        setBackground(Color.WHITE);
        setFont(new Font("SansSerif", Font.PLAIN, 13));
        updatePreferredSize();
    }

    private void updatePreferredSize() {
        int treeWidth = root == null ? MIN_NODE_WIDTH : calculateSubtreeWidth(root);
        int treeHeight = root == null ? NODE_HEIGHT : calculateTreeHeight(root);
        int preferredWidth = Math.max(700, treeWidth + MARGIN * 2);
        int preferredHeight = Math.max(400, treeHeight + MARGIN * 2);
        setPreferredSize(new Dimension(preferredWidth, preferredHeight));
    }

    private int calculateSubtreeWidth(SyntaxTreeNode node) {
        if (node == null) {
            return 0;
        }

        int nodeWidth = calculateNodeWidth(node);
        int width;
        if (node.getChildren().isEmpty()) {
            width = nodeWidth;
        } else {
            int childrenWidth = calculateChildrenWidth(node);
            width = Math.max(nodeWidth, childrenWidth);
        }

        nodeWidths.put(node, nodeWidth);
        subtreeWidths.put(node, width);
        return width;
    }

    private int calculateNodeWidth(SyntaxTreeNode node) {
        FontMetrics metrics = getFontMetrics(getFont());
        return Math.max(MIN_NODE_WIDTH, metrics.stringWidth(node.getLabel()) + NODE_HORIZONTAL_PADDING);
    }

    private int calculateChildrenWidth(SyntaxTreeNode node) {
        int childrenWidth = 0;
        for (SyntaxTreeNode child : node.getChildren()) {
            childrenWidth += calculateSubtreeWidth(child);
        }
        childrenWidth += HORIZONTAL_GAP * (node.getChildren().size() - 1);
        return childrenWidth;
    }

    private int calculateTreeHeight(SyntaxTreeNode node) {
        if (node == null) {
            return 0;
        }
        int maxChildHeight = 0;
        for (SyntaxTreeNode child : node.getChildren()) {
            maxChildHeight = Math.max(maxChildHeight, calculateTreeHeight(child));
        }
        if (maxChildHeight == 0) {
            return NODE_HEIGHT;
        }
        return NODE_HEIGHT + VERTICAL_GAP + maxChildHeight;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setFont(getFont());

        if (root == null) {
            drawEmptyMessage(g2);
        } else {
            subtreeWidths.clear();
            nodeWidths.clear();
            int treeWidth = calculateSubtreeWidth(root);
            int startX = Math.max(MARGIN, (getWidth() - treeWidth) / 2);
            drawNode(g2, root, startX, MARGIN, treeWidth);
        }

        g2.dispose();
    }

    private void drawEmptyMessage(Graphics2D g2) {
        g2.setColor(new Color(90, 90, 90));
        String message = "No syntax tree available";
        FontMetrics metrics = g2.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(message)) / 2;
        int y = getHeight() / 2;
        g2.drawString(message, Math.max(MARGIN, x), y);
    }

    private void drawNode(Graphics2D g2, SyntaxTreeNode node, int x, int y, int subtreeWidth) {
        int nodeWidth = nodeWidths.getOrDefault(node, MIN_NODE_WIDTH);
        int centerX = x + subtreeWidth / 2;
        int nodeX = centerX - nodeWidth / 2;
        int nodeY = y;

        int childrenWidth = getCachedChildrenWidth(node);
        int childX = x + Math.max(0, (subtreeWidth - childrenWidth) / 2);
        int childY = y + NODE_HEIGHT + VERTICAL_GAP;
        for (SyntaxTreeNode child : node.getChildren()) {
            int childSubtreeWidth = subtreeWidths.getOrDefault(child, MIN_NODE_WIDTH);
            int childCenterX = childX + childSubtreeWidth / 2;

            g2.setColor(new Color(120, 120, 120));
            g2.drawLine(centerX, nodeY + NODE_HEIGHT, childCenterX, childY);
            drawNode(g2, child, childX, childY, childSubtreeWidth);

            childX += childSubtreeWidth + HORIZONTAL_GAP;
        }

        drawNodeBox(g2, node.getLabel(), nodeX, nodeY, nodeWidth);
    }

    private int getCachedChildrenWidth(SyntaxTreeNode node) {
        if (node.getChildren().isEmpty()) {
            return 0;
        }

        int width = 0;
        for (SyntaxTreeNode child : node.getChildren()) {
            width += subtreeWidths.getOrDefault(child, MIN_NODE_WIDTH);
        }
        width += HORIZONTAL_GAP * (node.getChildren().size() - 1);
        return width;
    }

    private void drawNodeBox(Graphics2D g2, String label, int x, int y, int width) {
        g2.setColor(new Color(235, 243, 255));
        g2.fillRoundRect(x, y, width, NODE_HEIGHT, 16, 16);
        g2.setColor(new Color(70, 105, 160));
        g2.drawRoundRect(x, y, width, NODE_HEIGHT, 16, 16);

        g2.setColor(new Color(30, 30, 30));
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(label)) / 2;
        int textY = y + (NODE_HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(label, textX, textY);
    }
}
