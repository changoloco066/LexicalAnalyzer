package TokensTable.src.minilang;

import TokensTable.src.minilang.gui.TokenTableGUI;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TokenTableGUI gui = new TokenTableGUI();
            gui.setVisible(true);
        });
    }

}