package TokensTable.TokensTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TokenTableGUI extends JFrame {

    private JTextArea inputArea;
    private JTable tokenTable;
    private DefaultTableModel tokenModel;
    private JTable errorTable;
    private DefaultTableModel errorModel;
    private JLabel statusLabel;
    private JTabbedPane tabs;
   // private JPanel outputPanel;

    public TokenTableGUI() {
        setTitle("Lexical Analyzer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input area 
        inputArea = new JTextArea();
        inputArea.setText(buildSampleCode());
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Source Code"));

        // Tabs con las dos tablas 
        tabs = new JTabbedPane();
        String[] tokenCols = {"Lexeme", "Token Type", "Position", "Line"};
        tokenModel = new DefaultTableModel(tokenCols, 0);
        tokenTable = new JTable(tokenModel);
        tabs.addTab("Token Table", new JScrollPane(tokenTable));

        String[] errorCols = {"Line", "Position", "Error", "Context"};
        errorModel = new DefaultTableModel(errorCols, 0);
        errorTable = new JTable(errorModel);
        tabs.addTab("Syntax Errors", new JScrollPane(errorTable));

        // SplitPane 
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputScroll, tabs);
        splitPane.setDividerLocation(180);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);

        // Botón 
        JButton analyzeBtn = new JButton("Analyze");
        add(analyzeBtn, BorderLayout.SOUTH);
        analyzeBtn.addActionListener(e -> analyzeText());

       
    }

    private void analyzeText() {
        String input = inputArea.getText();

        Lexer lexer = new Lexer();
        List<Tokens> tokens = lexer.analyze(input);

        Parser parser = new Parser(tokens);
        parser.parse();
        List<ParseError> errors = parser.getErrors();

        loadTokens(tokens);
        loadErrors(errors);

        if(errors.isEmpty()){
          
        }else {
            JOptionPane.showMessageDialog(this, errors.size() + "Syntax error(s) found. ", "Error", JOptionPane.ERROR_MESSAGE );
            tabs.setSelectedIndex(1);
        }
    }

    private void loadTokens(List<Tokens> tokens) {
        tokenModel.setRowCount(0);
        for (Tokens t : tokens) {
            tokenModel.addRow(new Object[]{
                t.getLexeme(), t.getType(), t.getPosition(), t.getLine()
            });
        }
    }

    private void loadErrors(List<ParseError> errors) {
        errorModel.setRowCount(0);
        for (ParseError e : errors) {
            errorModel.addRow(new Object[]{
                e.getLine(), e.getPosition(), e.getMessage(), e.getContext()
            });
        }
    }

    private String buildSampleCode() {
        return "var x = 10\nvar y = 3\nvar resultado = x + y * 2\nif (resultado > 15) {\n    print(\"Mayor que 15\")\n} else {\n    print(resultado)\n}\nwhile (x > 0) {\n    x = x - 1\n}\n";
    }
}