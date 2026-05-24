package TokensTable.TokensTable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.List;

public class TokenTableGUI extends JFrame {

    private JTextArea inputArea;
    private JTable tokenTable;
    private DefaultTableModel tokenModel;
    private JTable errorTable;
    private DefaultTableModel errorModel;
    private DefaultTableModel symbolModel;
    private JTable symbolTable;
    private JTabbedPane tabs;
    private JScrollPane syntaxTreeScroll;
    private SyntaxTreePanel syntaxTreePanel;
    private JLabel zoomLabel;
    private String currentSourceCode;
    private List<Tokens> currentTokens;
    private List<ParseError> currentErrors;
    private List<Symbol> currentSymbols;
    private SyntaxTreeNode currentTree;

    public TokenTableGUI() {
        setTitle("MiniLang Analyzer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        inputArea = new JTextArea();
        inputArea.setText(buildSampleCode());
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Source Code"));

        tabs = new JTabbedPane();
        String[] tokenCols = {"Lexeme", "Token Type", "Position", "Line"};
        tokenModel = new DefaultTableModel(tokenCols, 0);
        tokenTable = new JTable(tokenModel);
        tabs.addTab("Token Table", new JScrollPane(tokenTable));

        String[] errorCols = {"Line", "Position", "Error", "Context"};
        errorModel = new DefaultTableModel(errorCols, 0);
        errorTable = new JTable(errorModel);
        tabs.addTab("Syntax Errors", new JScrollPane(errorTable));

        String[] symbolCols = {"Name", "Type", "Value", "Line"};
        symbolModel = new DefaultTableModel(symbolCols, 0);
        symbolTable = new JTable(symbolModel);
        tabs.addTab("Symbol Table", new JScrollPane(symbolTable));

        syntaxTreePanel = new SyntaxTreePanel(null);
        syntaxTreeScroll = new JScrollPane(syntaxTreePanel);
        tabs.addTab("Syntax Tree", buildSyntaxTreeTab());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputScroll, tabs);
        splitPane.setDividerLocation(180);
        splitPane.setResizeWeight(0.3);
        add(splitPane, BorderLayout.CENTER);

        JButton analyzeBtn = new JButton("Analyze");
        JButton exportBtn = new JButton("Export Results");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(analyzeBtn);
        buttonPanel.add(exportBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        analyzeBtn.addActionListener(e -> analyzeText());
        exportBtn.addActionListener(e -> exportResults());
    }

    private void analyzeText() {
        String input = inputArea.getText();

        Lexer lexer = new Lexer();
        List<Tokens> tokens = lexer.analyze(input);

        Parser parser = new Parser(tokens);
        parser.parse();
        List<ParseError> errors = parser.getErrors();
        List<Symbol> symbols = parser.getSymbols();
        SyntaxTreeNode tree = parser.getSyntaxTree();

        currentSourceCode = input;
        currentTokens = tokens;
        currentErrors = errors;
        currentSymbols = symbols;
        currentTree = tree;

        loadTokens(tokens);
        loadErrors(errors);
        loadSymbols(symbols);
        loadSyntaxTree(tree);

        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    errors.size() + " Syntax error(s) found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
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

    private void loadSymbols(List<Symbol> symbols) {
        symbolModel.setRowCount(0);
        for (Symbol s : symbols) {
            symbolModel.addRow(new Object[]{
                    s.getName(), s.getType(), s.getValue(), s.getLine()
            });
        }
    }

    private void loadSyntaxTree(SyntaxTreeNode tree) {
        syntaxTreePanel = new SyntaxTreePanel(tree);
        syntaxTreeScroll.setViewportView(syntaxTreePanel);
        updateZoomLabel();
        syntaxTreeScroll.revalidate();
        syntaxTreeScroll.repaint();
    }

    private JPanel buildSyntaxTreeTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton zoomOutBtn = new JButton("-");
        JButton resetZoomBtn = new JButton("100%");
        JButton zoomInBtn = new JButton("+");
        zoomLabel = new JLabel("Zoom: 100%");

        zoomOutBtn.setToolTipText("Zoom out");
        resetZoomBtn.setToolTipText("Reset zoom");
        zoomInBtn.setToolTipText("Zoom in");

        zoomOutBtn.addActionListener(e -> {
            syntaxTreePanel.zoomOut();
            updateZoomLabel();
        });
        resetZoomBtn.addActionListener(e -> {
            syntaxTreePanel.resetZoom();
            updateZoomLabel();
        });
        zoomInBtn.addActionListener(e -> {
            syntaxTreePanel.zoomIn();
            updateZoomLabel();
        });

        controls.add(zoomOutBtn);
        controls.add(resetZoomBtn);
        controls.add(zoomInBtn);
        controls.add(zoomLabel);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(syntaxTreeScroll, BorderLayout.CENTER);
        return panel;
    }

    private void updateZoomLabel() {
        int percent = (int) Math.round(syntaxTreePanel.getZoom() * 100);
        zoomLabel.setText("Zoom: " + percent + "%");
    }

    private void exportResults() {
        if (currentTokens == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please analyze the source code before exporting results.",
                    "No Analysis Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Results");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        fileChooser.setSelectedFile(new File("analysis-results.txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = ensureTxtExtension(fileChooser.getSelectedFile());
        try {
            ResultExporter.exportToTxt(
                    file,
                    currentSourceCode,
                    currentTokens,
                    currentErrors,
                    currentSymbols,
                    currentTree);
            JOptionPane.showMessageDialog(
                    this,
                    "Results exported successfully.",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not export results: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private File ensureTxtExtension(File file) {
        String path = file.getAbsolutePath();
        if (path.toLowerCase().endsWith(".txt")) {
            return file;
        }
        return new File(path + ".txt");
    }

    private String buildSampleCode() {
        return "var x = 10\nvar y = 3\nvar resultado = x + y * 2\nif (resultado > 15) {\n    print(\"Mayor que 15\")\n} else {\n    print(resultado)\n}\nwhile (x > 0) {\n    x = x - 1\n}\n";
    }
}
