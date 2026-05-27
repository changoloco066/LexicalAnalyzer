package TokensTable.src.minilang.gui;

import javax.swing.JPanel;

import TokensTable.src.minilang.parser.SyntaxTreeNode;

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
    // Medidas base para dibujar el arbol de forma legible.
    private static final int MIN_NODE_WIDTH = 120;
    private static final int NODE_HEIGHT = 35;
    private static final int NODE_HORIZONTAL_PADDING = 22;
    private static final int HORIZONTAL_GAP = 30;
    private static final int VERTICAL_GAP = 70;
    private static final int MARGIN = 30;
    private static final double MIN_ZOOM = 0.4;
    private static final double MAX_ZOOM = 2.5;

    private final SyntaxTreeNode root;
    private final Map<SyntaxTreeNode, Integer> subtreeWidths = new HashMap<>();
    private final Map<SyntaxTreeNode, Integer> nodeWidths = new HashMap<>();
    private int basePreferredWidth;
    private int basePreferredHeight;
    private double zoom = 1.0;

    // Recibe la raiz del arbol y prepara el panel para pintarlo.
    public SyntaxTreePanel(SyntaxTreeNode root) {
        this.root = root;
        setBackground(Color.WHITE);
        setFont(new Font("SansSerif", Font.PLAIN, 13));
        updatePreferredSize();
    }

    public void zoomIn() {
        setZoom(zoom + 0.1);
    }

    public void zoomOut() {
        setZoom(zoom - 0.1);
    }

    public void resetZoom() {
        setZoom(1.0);
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        // El zoom se limita para evitar que el arbol sea demasiado pequeno o grande.
        this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        applyZoomedPreferredSize();
        revalidate();
        repaint();
    }

    private void updatePreferredSize() {
        // Calcula el espacio base que necesita el arbol antes de aplicar zoom.
        int treeWidth = root == null ? MIN_NODE_WIDTH : calculateSubtreeWidth(root);
        int treeHeight = root == null ? NODE_HEIGHT : calculateTreeHeight(root);
        basePreferredWidth = Math.max(700, treeWidth + MARGIN * 2);
        basePreferredHeight = Math.max(400, treeHeight + MARGIN * 2);
        applyZoomedPreferredSize();
    }

    private void applyZoomedPreferredSize() {
        int scaledWidth = (int) Math.ceil(basePreferredWidth * zoom);
        int scaledHeight = (int) Math.ceil(basePreferredHeight * zoom);
        setPreferredSize(new Dimension(scaledWidth, scaledHeight));
    }

    private int calculateSubtreeWidth(SyntaxTreeNode node) {
        // El ancho del subarbol depende del nodo actual y de todos sus hijos.
        if (node == null) {
            return 0;
        }

        int nodeWidth = calculateNodeWidth(node);
        int width;
        if (node.getChildren().isEmpty()) {
            // Un nodo hoja solo necesita su propio ancho.
            width = nodeWidth;
        } else {
            // Un nodo con hijos usa el ancho mayor entre su caja y sus ramas.
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
        // Suma el ancho de cada hijo para saber cuanto ocupa el siguiente nivel.
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
        // Se toma la rama mas alta para definir la altura total del arbol.
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
        g2.scale(zoom, zoom);

        if (root == null) {
            drawEmptyMessage(g2);
        } else {
            // Se recalculan medidas antes de pintar por si cambio el tamano o la fuente.
            subtreeWidths.clear();
            nodeWidths.clear();
            int treeWidth = calculateSubtreeWidth(root);
            int logicalWidth = (int) Math.round(getWidth() / zoom);
            int startX = Math.max(MARGIN, (logicalWidth - treeWidth) / 2);
            drawNode(g2, root, startX, MARGIN, treeWidth);
        }

        g2.dispose();
    }

    private void drawEmptyMessage(Graphics2D g2) {
        g2.setColor(new Color(90, 90, 90));
        String message = "No syntax tree available";
        FontMetrics metrics = g2.getFontMetrics();
        int logicalWidth = (int) Math.round(getWidth() / zoom);
        int logicalHeight = (int) Math.round(getHeight() / zoom);
        int x = (logicalWidth - metrics.stringWidth(message)) / 2;
        int y = logicalHeight / 2;
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
            // Primero se dibuja la linea hacia el hijo y luego el subarbol hijo.
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
        // Dibuja la caja del nodo y centra su texto.
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
