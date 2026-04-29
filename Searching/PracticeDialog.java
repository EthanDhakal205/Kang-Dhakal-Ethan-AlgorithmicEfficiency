import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

class PracticeDialog extends JDialog {

    // ── Dark palette ──────────────────────────────────────────────────────────
    private static final Color[] DARK = {
        new Color(10,  11,  14),   // 0  BG
        new Color(17,  19,  24),   // 1  PANEL
        new Color(24,  27,  34),   // 2  CARD
        new Color(38,  42,  54),   // 3  BORDER
        new Color(82,  130, 255),  // 4  ACCENT
        new Color(52,  211, 153),  // 5  ACCENT2
        new Color(251, 191, 36),   // 6  WARN
        new Color(239, 68,  68),   // 7  DANGER
        new Color(220, 225, 235),  // 8  TEXT
        new Color(100, 110, 130),  // 9  TEXT_DIM
        new Color(55,  62,  78),   // 10 TEXT_HINT
        new Color(14,  16,  21),   // 11 SURFACE
    };

    // ── Light palette ─────────────────────────────────────────────────────────
    private static final Color[] LIGHT = {
        new Color(245, 246, 250),  // 0  BG
        new Color(230, 232, 240),  // 1  PANEL
        new Color(215, 218, 230),  // 2  CARD
        new Color(180, 185, 205),  // 3  BORDER
        new Color(50,  100, 220),  // 4  ACCENT
        new Color(15,  160, 100),  // 5  ACCENT2
        new Color(200, 140, 10),   // 6  WARN
        new Color(200, 50,  50),   // 7  DANGER
        new Color(15,  18,  35),   // 8  TEXT
        new Color(80,  88,  110),  // 9  TEXT_DIM
        new Color(160, 168, 190),  // 10 TEXT_HINT
        new Color(235, 237, 245),  // 11 SURFACE
    };

    // ── Live color references — swapped by applyTheme() ───────────────────────
    private static Color BG, PANEL, CARD, BORDER, ACCENT, ACCENT2,
                         WARN, DANGER, TEXT, TEXT_DIM, TEXT_HINT, SURFACE;

    static {
        assignColors(DARK);
    }

    private static void assignColors(Color[] t) {
        BG        = t[0];  PANEL    = t[1];  CARD      = t[2];  BORDER   = t[3];
        ACCENT    = t[4];  ACCENT2  = t[5];  WARN      = t[6];  DANGER   = t[7];
        TEXT      = t[8];  TEXT_DIM = t[9];  TEXT_HINT = t[10]; SURFACE  = t[11];
    }

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font MONO   = new Font("JetBrains Mono", Font.PLAIN, 13);
    private static final Font MONO_B = new Font("JetBrains Mono", Font.BOLD,  13);
    private static final Font SANS_B = new Font("Segoe UI",       Font.BOLD,  14);
    private static final Font TITLE  = new Font("Segoe UI",       Font.BOLD,  22);
    private static final Font SMALL  = new Font("Segoe UI",       Font.PLAIN, 11);

    // ── State ─────────────────────────────────────────────────────────────────
    private final PracticeJudge judge = new PracticeJudge();
    private final Map<String, String> drafts = new HashMap<>();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private Highlighter.HighlightPainter compilerHighlightPainter =
        new DefaultHighlighter.DefaultHighlightPainter(
            new Color(DANGER.getRed(), DANGER.getGreen(), DANGER.getBlue(), 70));

    // ── Components ────────────────────────────────────────────────────────────
    private final JLabel titleLabel     = new JLabel("Practice Lab");
    private final JLabel signatureLabel = new JLabel();
    private final JLabel statusLabel    = new JLabel("Ready to compile");
    private final JTextArea editorArea      = createCodeArea(true);
    private final JTextArea lineNumberArea  = createLineNumberArea();
    private final JTextArea promptArea      = createReadArea();
    private final JTextArea testsArea       = createReadArea();
    private final JTextArea resultsArea     = createReadArea();
    private final JTextArea answerArea      = createCodeArea(false);
    private final JTextArea unsupportedArea = createReadArea();
    private final JComboBox<String> answerDropdown =
        new JComboBox<>(new String[]{"Hide reference answer", "Show reference answer"});
    private final ActionButton runTestsButton  = new ActionButton("run tests",  ACCENT,  true);
    private final ActionButton resetCodeButton = new ActionButton("reset code", ACCENT2, false);
    private final JScrollPane editorScrollPane = createEditorScrollPane();
    private final JTabbedPane sideTabs         = createSideTabs();

    // Panel refs needed for theme updates
    private PracticeCatalog.Problem currentProblem;
    private JPanel supportedPanel;
    private JPanel unsupportedPanel;
    private JPanel editorToolbar;

    // ── Constructor ───────────────────────────────────────────────────────────

    PracticeDialog(JFrame owner) {
        super(owner, "Practice Lab", false);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1180, 760));
        setPreferredSize(new Dimension(1180, 760));

        cards.setBackground(BG);
        cards.add(buildSupportedPanel(),   "supported");
        cards.add(buildUnsupportedPanel(), "unsupported");
        add(cards, BorderLayout.CENTER);

        answerDropdown.setFont(MONO);
        answerDropdown.setBackground(SURFACE);
        answerDropdown.setForeground(TEXT);
        answerDropdown.addActionListener(e -> updateAnswerView());

        runTestsButton.addActionListener(e -> runTests());
        resetCodeButton.addActionListener(e -> resetEditor());

        installEditorEnhancements();
        updateLineNumbers();
        unsupportedArea.setText(
            "Select an algorithm to see whether practice mode is available.\n\n"
                + "Practice currently supports " + PracticeCatalog.supportedAlgorithmsLabel() + "."
        );
    }

    // ── Theme API ─────────────────────────────────────────────────────────────

    void applyTheme(boolean darkMode) {
        assignColors(darkMode ? DARK : LIGHT);

        compilerHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
            new Color(DANGER.getRed(), DANGER.getGreen(), DANGER.getBlue(), 70));

        // Dialog and card backgrounds
        getContentPane().setBackground(BG);
        cards.setBackground(BG);
        if (supportedPanel   != null) supportedPanel.setBackground(BG);
        if (unsupportedPanel != null) unsupportedPanel.setBackground(BG);

        // Toolbar
        if (editorToolbar != null) editorToolbar.setBackground(PANEL);

        // Button accents
        runTestsButton.setAccent(ACCENT);
        resetCodeButton.setAccent(ACCENT2);

        // Text areas
        for (JTextArea area : new JTextArea[]{
                editorArea, answerArea, promptArea, testsArea, resultsArea, unsupportedArea}) {
            area.setBackground(SURFACE);
            area.setForeground(TEXT);
            area.setCaretColor(ACCENT);
        }
        lineNumberArea.setBackground(PANEL);
        lineNumberArea.setForeground(TEXT_HINT);

        // Dropdown
        answerDropdown.setBackground(SURFACE);
        answerDropdown.setForeground(TEXT);

        // Labels
        titleLabel.setForeground(TEXT);
        signatureLabel.setForeground(ACCENT2);
        statusLabel.setForeground(TEXT_DIM);

        // Tabs
        sideTabs.setBackground(PANEL);
        sideTabs.setForeground(TEXT);

        // Scroll panes
        refreshScrollPane(editorScrollPane);
        editorScrollPane.getRowHeader().setBackground(PANEL);
        for (int i = 0; i < sideTabs.getTabCount(); i++) {
            JComponent tab = (JComponent) sideTabs.getComponentAt(i);
            refreshPanelColors(tab);
        }

        clearCompilerMarkers();
        repaint();
        revalidate();
    }

    private void refreshPanelColors(java.awt.Component c) {
        if (c instanceof JPanel panel) {
            if (panel.isOpaque()) panel.setBackground(CARD);
            if (panel.getBorder() instanceof javax.swing.border.CompoundBorder) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    new EmptyBorder(10, 10, 10, 10)));
            }
        }
        if (c instanceof JScrollPane sp) {
            refreshScrollPane(sp);
        }
        if (c instanceof java.awt.Container container) {
            for (java.awt.Component child : container.getComponents()) {
                refreshPanelColors(child);
            }
        }
    }

    private void refreshScrollPane(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(SURFACE);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    void showForAlgorithm(String algorithmName) {
        setAlgorithm(algorithmName);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        toFront();
    }

    // ── Algorithm selection ───────────────────────────────────────────────────

    private void setAlgorithm(String algorithmName) {
        if (currentProblem != null) {
            drafts.put(currentProblem.algorithmId(), editorArea.getText());
        }

        PracticeCatalog.Problem nextProblem = PracticeCatalog.findByTitle(algorithmName).orElse(null);
        currentProblem = nextProblem;
        clearCompilerMarkers();

        if (nextProblem == null) {
            setTitle("Practice Lab - " + algorithmName);
            unsupportedArea.setText(
                algorithmName + " does not have an interactive practice template yet.\n\n"
                    + "Practice currently supports " + PracticeCatalog.supportedAlgorithmsLabel() + "."
            );
            cardLayout.show(cards, "unsupported");
            return;
        }

        setTitle(nextProblem.title() + " Practice Lab");
        titleLabel.setText(nextProblem.title() + " Practice");
        signatureLabel.setText("Required signature: " + nextProblem.signatureHint());
        promptArea.setText(nextProblem.prompt().strip());
        promptArea.setCaretPosition(0);
        testsArea.setText(formatTests(nextProblem));
        testsArea.setCaretPosition(0);
        editorArea.setText(drafts.getOrDefault(nextProblem.algorithmId(), nextProblem.starterCode()));
        editorArea.setCaretPosition(0);
        resultsArea.setText("Run tests to validate your implementation.");
        resultsArea.setCaretPosition(0);
        statusLabel.setForeground(TEXT_DIM);
        statusLabel.setText("Ctrl+Enter runs tests. Compilation errors highlight the matching line.");
        answerDropdown.setSelectedIndex(0);
        updateAnswerView();
        setBusy(false);
        sideTabs.setSelectedIndex(0);
        cardLayout.show(cards, "supported");
    }

    // ── Panel builders ────────────────────────────────────────────────────────

    private JPanel buildSupportedPanel() {
        supportedPanel = new JPanel(new BorderLayout(10, 10));
        JPanel panel = supportedPanel;
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BorderLayout(0, 4));
        titleLabel.setFont(TITLE);
        titleLabel.setForeground(TEXT);
        signatureLabel.setFont(MONO_B);
        signatureLabel.setForeground(ACCENT2);
        titleGroup.add(titleLabel,     BorderLayout.NORTH);
        titleGroup.add(signatureLabel, BorderLayout.SOUTH);
        header.add(titleGroup, BorderLayout.WEST);

        JLabel hintLabel = new JLabel("Compiler workspace");
        hintLabel.setFont(SMALL);
        hintLabel.setForeground(TEXT_HINT);
        header.add(hintLabel, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, buildEditorPane(), sideTabs);
        splitPane.setBorder(null);
        splitPane.setDividerSize(1);
        splitPane.setResizeWeight(0.68);
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(BORDER);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.68));
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildEditorPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);

        // Opaque toolbar — prevents dark bleed-through from the split pane
        editorToolbar = new JPanel(new BorderLayout());
        editorToolbar.setOpaque(true);
        editorToolbar.setBackground(PANEL);
        editorToolbar.setBorder(new EmptyBorder(8, 0, 8, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(runTestsButton);
        left.add(resetCodeButton);
        editorToolbar.add(left, BorderLayout.WEST);

        statusLabel.setFont(SMALL);
        statusLabel.setForeground(TEXT_DIM);
        editorToolbar.add(statusLabel, BorderLayout.EAST);

        panel.add(editorToolbar,    BorderLayout.NORTH);
        panel.add(editorScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JTabbedPane createSideTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(MONO_B);
        tabs.setBackground(PANEL);
        tabs.setForeground(TEXT);
        tabs.addTab("Prompt",  sectionPanel("PROMPT",     createScrollPane(promptArea)));
        tabs.addTab("Tests",   sectionPanel("TEST CASES", createScrollPane(testsArea)));
        tabs.addTab("Results", sectionPanel("RESULTS",    createScrollPane(resultsArea)));
        tabs.addTab("Answer",  buildAnswerSection());
        return tabs;
    }

    private JPanel buildAnswerSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(10, 10, 10, 10)));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        JLabel label = new JLabel("REFERENCE ANSWER");
        label.setFont(SMALL);
        label.setForeground(TEXT_DIM);
        header.add(label,          BorderLayout.WEST);
        header.add(answerDropdown, BorderLayout.CENTER);

        panel.add(header,                       BorderLayout.NORTH);
        panel.add(createScrollPane(answerArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildUnsupportedPanel() {
        unsupportedPanel = new JPanel(new BorderLayout(0, 10));
        JPanel panel = unsupportedPanel;
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel heading = new JLabel("Practice Mode Unavailable");
        heading.setFont(TITLE);
        heading.setForeground(TEXT);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(createScrollPane(unsupportedArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel sectionPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(10, 10, 10, 10)));

        JLabel label = new JLabel(title);
        label.setFont(SMALL);
        label.setForeground(TEXT_DIM);
        panel.add(label,   BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // ── Editor setup ──────────────────────────────────────────────────────────

    private void installEditorEnhancements() {
        editorArea.getInputMap().put(KeyStroke.getKeyStroke("control ENTER"), "runTests");
        editorArea.getActionMap().put("runTests", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (runTestsButton.isEnabled()) runTests();
            }
        });

        editorArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { editorChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { editorChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { editorChanged(); }
        });
    }

    private void editorChanged() {
        updateLineNumbers();
        clearCompilerMarkers();
        if (currentProblem != null && runTestsButton.isEnabled()) {
            statusLabel.setForeground(TEXT_DIM);
            statusLabel.setText("Edited. Run tests again to refresh the result.");
        }
    }

    // ── Component factories ───────────────────────────────────────────────────

    private JTextArea createCodeArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setFont(new Font(Font.MONOSPACED, editable ? Font.PLAIN : Font.BOLD, 13));
        area.setBackground(SURFACE);
        area.setForeground(TEXT);
        area.setCaretColor(ACCENT);
        area.setEditable(editable);
        area.setLineWrap(false);
        area.setWrapStyleWord(false);
        area.setTabSize(4);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JTextArea createLineNumberArea() {
        JTextArea area = new JTextArea("1");
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBackground(PANEL);
        area.setForeground(TEXT_HINT);
        area.setEditable(false);
        area.setHighlighter(null);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private JTextArea createReadArea() {
        JTextArea area = new JTextArea();
        area.setFont(MONO);
        area.setBackground(SURFACE);
        area.setForeground(TEXT);
        area.setCaretColor(ACCENT);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JScrollPane createEditorScrollPane() {
        JScrollPane sp = createScrollPane(editorArea);
        sp.setRowHeaderView(lineNumberArea);
        sp.getRowHeader().setBackground(PANEL);
        return sp;
    }

    private JScrollPane createScrollPane(JComponent component) {
        JScrollPane sp = new JScrollPane(component);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(SURFACE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ── Line numbers ──────────────────────────────────────────────────────────

    private void updateLineNumbers() {
        int lineCount = Math.max(1, editorArea.getLineCount());
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            if (i > 1) builder.append(System.lineSeparator());
            builder.append(i);
        }
        lineNumberArea.setText(builder.toString());
    }

    // ── Test runner ───────────────────────────────────────────────────────────

    private void runTests() {
        if (currentProblem == null) return;

        PracticeCatalog.Problem problem = currentProblem;
        String source = editorArea.getText();
        drafts.put(problem.algorithmId(), source);
        setBusy(true);
        clearCompilerMarkers();
        statusLabel.setForeground(ACCENT2);
        statusLabel.setText("Compiling UserSolution and running tests...");
        resultsArea.setText("Compiling UserSolution and running "
            + problem.testCases().size() + " test case(s)...");
        sideTabs.setSelectedIndex(2);

        new SwingWorker<PracticeRunResult, Void>() {
            @Override
            protected PracticeRunResult doInBackground() {
                return judge.run(problem, source);
            }

            @Override
            protected void done() {
                try {
                    PracticeRunResult result = get();
                    resultsArea.setText(formatRunResult(result));
                    resultsArea.setCaretPosition(0);
                    applyCompilerIssues(result.compilerIssues());
                    updateStatus(result);
                } catch (Exception e) {
                    statusLabel.setForeground(DANGER);
                    statusLabel.setText("Practice runner failed.");
                    resultsArea.setText("Practice runner failed: " + e.getMessage());
                    resultsArea.setCaretPosition(0);
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void updateStatus(PracticeRunResult result) {
        if (!result.compilerAvailable()) {
            statusLabel.setForeground(DANGER);
            statusLabel.setText("No JDK compiler available in this runtime.");
            return;
        }
        if (!result.compiled()) {
            statusLabel.setForeground(DANGER);
            if (!result.compilerIssues().isEmpty()) {
                PracticeRunResult.CompilerIssue issue = result.compilerIssues().get(0);
                statusLabel.setText("Compilation error at line "
                    + issue.line() + ", column " + issue.column() + ".");
            } else {
                statusLabel.setText("Compilation failed.");
            }
            return;
        }
        if (result.allPassed()) {
            statusLabel.setForeground(ACCENT);
            statusLabel.setText("All test cases passed.");
        } else {
            statusLabel.setForeground(WARN);
            statusLabel.setText("Compiled successfully, but one or more test cases failed.");
        }
    }

    // ── Compiler markers ──────────────────────────────────────────────────────

    private void clearCompilerMarkers() {
        editorArea.getHighlighter().removeAllHighlights();
        editorScrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    private void applyCompilerIssues(List<PracticeRunResult.CompilerIssue> issues) {
        clearCompilerMarkers();
        if (issues.isEmpty()) return;

        editorScrollPane.setBorder(BorderFactory.createLineBorder(DANGER));
        int firstOffset = -1;
        for (PracticeRunResult.CompilerIssue issue : issues) {
            int lineIndex = (int) Math.max(0,
                Math.min(editorArea.getLineCount() - 1, issue.line() - 1));
            try {
                int lineStart = editorArea.getLineStartOffset(lineIndex);
                int lineEnd   = editorArea.getLineEndOffset(lineIndex);
                editorArea.getHighlighter().addHighlight(lineStart, lineEnd, compilerHighlightPainter);
                int columnOffset = issue.column() > 0 ? (int) issue.column() - 1 : 0;
                int candidate = Math.min(lineStart + columnOffset, Math.max(lineStart, lineEnd - 1));
                if (firstOffset < 0) firstOffset = candidate;
            } catch (BadLocationException ignored) {
            }
        }
        if (firstOffset >= 0) moveCaretTo(firstOffset);
    }

    private void moveCaretTo(int offset) {
        editorArea.requestFocusInWindow();
        editorArea.setCaretPosition(offset);
        try {
            Rectangle2D rect = editorArea.modelToView2D(offset);
            if (rect != null) editorArea.scrollRectToVisible(rect.getBounds());
        } catch (BadLocationException ignored) {
        }
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    private String formatTests(PracticeCatalog.Problem problem) {
        StringBuilder builder = new StringBuilder();
        for (PracticeCatalog.TestCase testCase : problem.testCases()) {
            if (!builder.isEmpty()) builder.append(System.lineSeparator()).append(System.lineSeparator());
            builder.append(testCase.describe());
        }
        return builder.toString();
    }

    private String formatRunResult(PracticeRunResult result) {
        StringBuilder builder = new StringBuilder(result.message());
        for (PracticeRunResult.CaseResult caseResult : result.caseResults()) {
            builder.append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(caseResult.passed() ? "PASS  " : "FAIL  ")
                .append(caseResult.label())
                .append(System.lineSeparator())
                .append(caseResult.details());
        }
        return builder.toString();
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    private void updateAnswerView() {
        if (currentProblem == null || answerDropdown.getSelectedIndex() == 0) {
            answerArea.setText("Reference answer hidden. Use the dropdown to reveal a working solution.");
        } else {
            answerArea.setText(currentProblem.answerCode());
        }
        answerArea.setCaretPosition(0);
    }

    private void resetEditor() {
        if (currentProblem == null) return;
        editorArea.setText(currentProblem.starterCode());
        editorArea.setCaretPosition(0);
        drafts.put(currentProblem.algorithmId(), currentProblem.starterCode());
        clearCompilerMarkers();
        statusLabel.setForeground(TEXT_DIM);
        statusLabel.setText("Starter template restored.");
        resultsArea.setText("Starter template restored. Run tests to validate your implementation.");
        sideTabs.setSelectedIndex(2);
    }

    private void setBusy(boolean busy) {
        boolean enabled = !busy && currentProblem != null;
        runTestsButton.setEnabled(enabled);
        resetCodeButton.setEnabled(enabled);
        answerDropdown.setEnabled(enabled);
        editorArea.setEditable(!busy && currentProblem != null);
    }

    // ── ActionButton ──────────────────────────────────────────────────────────

    private static class ActionButton extends JButton {
        private Color accent;           // not final — updated by setAccent()
        private final boolean primary;
        private boolean hovered;

        ActionButton(String text, Color accent, boolean primary) {
            super(text);
            this.accent  = accent;
            this.primary = primary;
            setFont(SANS_B);
            setForeground(primary ? Color.WHITE : accent);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(text.equals("run tests") ? 118 : 110, 34));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        void setAccent(Color newAccent) {
            this.accent = newAccent;
            setForeground(primary ? Color.WHITE : newAccent);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = primary
                ? accent
                : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hovered ? 40 : 22);
            if (!isEnabled()) fill = new Color(BORDER.getRed(), BORDER.getGreen(), BORDER.getBlue(), 180);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(isEnabled()
                ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hovered ? 220 : 130)
                : BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            super.paintComponent(g);
            g2.dispose();
        }
    }
}