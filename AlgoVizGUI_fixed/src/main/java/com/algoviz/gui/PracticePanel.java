package com.algoviz.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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

import com.algoviz.practice.PracticeCatalog;
import com.algoviz.practice.PracticeJudge;
import com.algoviz.practice.PracticeRunResult;

public class PracticePanel extends JPanel {

    private final PracticeJudge judge = new PracticeJudge();
    private final Map<String, String> drafts = new HashMap<>();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final JLabel titleLabel = new JLabel("Practice Lab");
    private final JLabel signatureLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Ready to compile");
    private final JTextArea editorArea = createCodeArea(true);
    private final JTextArea lineNumberArea = createLineNumberArea();
    private final JTextArea promptArea = createReadArea();
    private final JTextArea testsArea = createReadArea();
    private final JTextArea resultsArea = createReadArea();
    private final JTextArea answerArea = createCodeArea(false);
    private final JComboBox<String> answerDropdown = new JComboBox<>(new String[]{"Hide reference answer", "Show reference answer"});
    private final ControlsPanel.StyledButton runTestsButton = new ControlsPanel.StyledButton("RUN TESTS", Theme.ACCENT, Theme.BG);
    private final ControlsPanel.StyledButton resetCodeButton = new ControlsPanel.StyledButton("RESET CODE", Theme.ACCENT4, Theme.BG3);
    private final JTextArea unsupportedArea = createReadArea();
    private final Highlighter.HighlightPainter compilerHighlightPainter =
            new DefaultHighlighter.DefaultHighlightPainter(Theme.withAlpha(Theme.ACCENT3, 50));

    private final JScrollPane editorScrollPane = createEditorScrollPane();
    private final JTabbedPane sideTabs = createSideTabs();

    private PracticeCatalog.Problem currentProblem;

    public PracticePanel() {
        setBackground(Theme.BG3);
        setLayout(new BorderLayout());

        runTestsButton.setForeground(Theme.BG);
        runTestsButton.setPreferredSize(new Dimension(122, 32));
        resetCodeButton.setPreferredSize(new Dimension(118, 32));

        answerDropdown.setFont(Theme.FONT_MONO);
        answerDropdown.setBackground(Theme.SURFACE);
        answerDropdown.setForeground(Theme.TEXT);
        answerDropdown.addActionListener(e -> updateAnswerView());

        statusLabel.setFont(Theme.FONT_LABEL);
        statusLabel.setForeground(Theme.TEXT2);

        runTestsButton.addActionListener(e -> runTests());
        resetCodeButton.addActionListener(e -> resetEditor());

        installEditorEnhancements();

        cards.setBackground(Theme.BG3);
        cards.add(buildSupportedPanel(), "supported");
        cards.add(buildUnsupportedPanel(), "unsupported");
        add(cards, BorderLayout.CENTER);

        resultsArea.setText("Run tests to validate your implementation.");
        unsupportedArea.setText("Select an algorithm to see whether practice mode is available.");
        updateLineNumbers();
    }

    public void setAlgorithm(String algorithmId) {
        if (currentProblem != null) {
            drafts.put(currentProblem.algorithmId(), editorArea.getText());
        }

        PracticeCatalog.Problem nextProblem = PracticeCatalog.find(algorithmId).orElse(null);
        currentProblem = nextProblem;
        clearCompilerMarkers();

        if (nextProblem == null) {
            unsupportedArea.setText("Practice mode is currently enabled for "
                    + PracticeCatalog.supportedAlgorithmsLabel()
                    + ".\n\nThis algorithm remains available in the visualizer, but it does not have an assessment compiler yet.");
            cardLayout.show(cards, "unsupported");
            return;
        }

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
        statusLabel.setForeground(Theme.TEXT2);
        statusLabel.setText("Ctrl+Enter runs tests. Compiler errors jump to the right line.");
        answerDropdown.setSelectedIndex(0);
        updateAnswerView();
        setBusy(false);
        sideTabs.setSelectedIndex(0);
        cardLayout.show(cards, "supported");
    }

    public void focusEditor() {
        if (currentProblem != null) {
            SwingUtilities.invokeLater(editorArea::requestFocusInWindow);
        }
    }

    private JPanel buildSupportedPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Theme.BG3);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT);
        signatureLabel.setFont(Theme.FONT_MONO_BOLD);
        signatureLabel.setForeground(Theme.ACCENT5);
        header.add(titleLabel);
        header.add(signatureLabel);
        panel.add(header, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildEditorPane(), sideTabs);
        splitPane.setBorder(null);
        splitPane.setDividerSize(1);
        splitPane.setResizeWeight(0.72);
        splitPane.setContinuousLayout(true);
        splitPane.setBackground(Theme.BORDER);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.72));
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildEditorPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonGroup.setOpaque(false);
        buttonGroup.add(runTestsButton);
        buttonGroup.add(resetCodeButton);
        toolbar.add(buttonGroup, BorderLayout.WEST);

        JLabel note = new JLabel("Compiler workspace");
        note.setFont(Theme.FONT_LABEL);
        note.setForeground(Theme.TEXT2);
        toolbar.add(note, BorderLayout.EAST);

        JPanel statusStrip = new JPanel(new BorderLayout());
        statusStrip.setOpaque(false);
        statusStrip.add(statusLabel, BorderLayout.WEST);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(editorScrollPane, BorderLayout.CENTER);
        panel.add(statusStrip, BorderLayout.SOUTH);
        return panel;
    }

    private JTabbedPane createSideTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_MONO_BOLD);
        tabs.setBackground(Theme.BG2);
        tabs.setForeground(Theme.TEXT);
        tabs.addTab("Prompt", sectionPanel("PROMPT", createScrollPane(promptArea)));
        tabs.addTab("Tests", sectionPanel("TEST CASES", createScrollPane(testsArea)));
        tabs.addTab("Results", sectionPanel("RESULTS", createScrollPane(resultsArea)));
        tabs.addTab("Answer", buildAnswerSection());
        return tabs;
    }

    private JPanel buildAnswerSection() {
        JPanel answerSection = new JPanel(new BorderLayout(0, 8));
        answerSection.setBackground(Theme.BG2);
        answerSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(10, 10, 10, 10)));

        JPanel answerHeader = new JPanel(new BorderLayout(8, 0));
        answerHeader.setOpaque(false);
        JLabel answerLabel = new JLabel("REFERENCE ANSWER");
        answerLabel.setFont(Theme.FONT_LABEL);
        answerLabel.setForeground(Theme.TEXT2);
        answerHeader.add(answerLabel, BorderLayout.WEST);
        answerHeader.add(answerDropdown, BorderLayout.CENTER);
        answerSection.add(answerHeader, BorderLayout.NORTH);
        answerSection.add(createScrollPane(answerArea), BorderLayout.CENTER);
        return answerSection;
    }

    private JPanel buildUnsupportedPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.BG3);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Practice Mode Unavailable");
        heading.setFont(Theme.FONT_TITLE);
        heading.setForeground(Theme.TEXT);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(createScrollPane(unsupportedArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel sectionPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Theme.BG2);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel label = new JLabel(title);
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT2);
        panel.add(label, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void installEditorEnhancements() {
        editorArea.getInputMap().put(KeyStroke.getKeyStroke("control ENTER"), "runTests");
        editorArea.getActionMap().put("runTests", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (runTestsButton.isEnabled()) {
                    runTests();
                }
            }
        });

        editorArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                editorChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                editorChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                editorChanged();
            }
        });
    }

    private void editorChanged() {
        updateLineNumbers();
        clearCompilerMarkers();
        if (currentProblem != null && runTestsButton.isEnabled()) {
            statusLabel.setForeground(Theme.TEXT2);
            statusLabel.setText("Edited. Run tests again to refresh the result.");
        }
    }

    private JTextArea createCodeArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setFont(new Font(Font.MONOSPACED, editable ? Font.PLAIN : Font.BOLD, 13));
        area.setBackground(Theme.SURFACE);
        area.setForeground(Theme.TEXT);
        area.setCaretColor(Theme.ACCENT);
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
        area.setBackground(Theme.BG2);
        area.setForeground(Theme.TEXT3);
        area.setEditable(false);
        area.setHighlighter(null);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private JTextArea createReadArea() {
        JTextArea area = new JTextArea();
        area.setFont(Theme.FONT_MONO);
        area.setBackground(Theme.SURFACE);
        area.setForeground(Theme.TEXT);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JScrollPane createEditorScrollPane() {
        JScrollPane scrollPane = createScrollPane(editorArea);
        scrollPane.setRowHeaderView(lineNumberArea);
        scrollPane.getRowHeader().setBackground(Theme.BG2);
        return scrollPane;
    }

    private JScrollPane createScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.SURFACE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void updateLineNumbers() {
        int lineCount = Math.max(1, editorArea.getLineCount());
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            if (i > 1) {
                builder.append(System.lineSeparator());
            }
            builder.append(i);
        }
        lineNumberArea.setText(builder.toString());
    }

    private String formatTests(PracticeCatalog.Problem problem) {
        StringBuilder builder = new StringBuilder();
        for (PracticeCatalog.TestCase testCase : problem.testCases()) {
            if (!builder.isEmpty()) {
                builder.append(System.lineSeparator()).append(System.lineSeparator());
            }
            builder.append(testCase.describe());
        }
        return builder.toString();
    }

    private void updateAnswerView() {
        if (currentProblem == null || answerDropdown.getSelectedIndex() == 0) {
            answerArea.setText("Reference answer hidden. Use the dropdown to reveal a working solution for this algorithm.");
        } else {
            answerArea.setText(currentProblem.answerCode());
        }
        answerArea.setCaretPosition(0);
    }

    private void resetEditor() {
        if (currentProblem == null) {
            return;
        }
        editorArea.setText(currentProblem.starterCode());
        editorArea.setCaretPosition(0);
        drafts.put(currentProblem.algorithmId(), currentProblem.starterCode());
        clearCompilerMarkers();
        statusLabel.setForeground(Theme.TEXT2);
        statusLabel.setText("Starter template restored.");
        resultsArea.setText("Starter template restored. Run tests to validate your implementation.");
        sideTabs.setSelectedIndex(2);
    }

    private void runTests() {
        if (currentProblem == null) {
            return;
        }

        PracticeCatalog.Problem problem = currentProblem;
        String source = editorArea.getText();
        drafts.put(problem.algorithmId(), source);
        setBusy(true);
        clearCompilerMarkers();
        statusLabel.setForeground(Theme.ACCENT5);
        statusLabel.setText("Compiling UserSolution and running tests...");
        resultsArea.setText("Compiling UserSolution and running " + problem.testCases().size() + " test cases...");
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
                    statusLabel.setForeground(Theme.ACCENT3);
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
            statusLabel.setForeground(Theme.ACCENT3);
            statusLabel.setText("No JDK compiler available in this runtime.");
            return;
        }
        if (!result.compiled()) {
            statusLabel.setForeground(Theme.ACCENT3);
            if (!result.compilerIssues().isEmpty()) {
                PracticeRunResult.CompilerIssue issue = result.compilerIssues().get(0);
                statusLabel.setText("Compilation error at line " + issue.line() + ", column " + issue.column() + ".");
            } else {
                statusLabel.setText("Compilation failed.");
            }
            return;
        }
        if (result.allPassed()) {
            statusLabel.setForeground(Theme.ACCENT);
            statusLabel.setText("All test cases passed.");
        } else {
            statusLabel.setForeground(Theme.ACCENT4);
            statusLabel.setText("Compiled successfully, but one or more test cases failed.");
        }
    }

    private void clearCompilerMarkers() {
        editorArea.getHighlighter().removeAllHighlights();
        editorScrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
    }

    private void applyCompilerIssues(List<PracticeRunResult.CompilerIssue> issues) {
        clearCompilerMarkers();
        if (issues.isEmpty()) {
            return;
        }

        editorScrollPane.setBorder(BorderFactory.createLineBorder(Theme.ACCENT3));
        int firstOffset = -1;
        for (PracticeRunResult.CompilerIssue issue : issues) {
            int lineIndex = (int) Math.max(0, Math.min(editorArea.getLineCount() - 1, issue.line() - 1));
            try {
                int lineStart = editorArea.getLineStartOffset(lineIndex);
                int lineEnd = editorArea.getLineEndOffset(lineIndex);
                editorArea.getHighlighter().addHighlight(lineStart, lineEnd, compilerHighlightPainter);
                int columnOffset = issue.column() > 0 ? (int) issue.column() - 1 : 0;
                int candidate = Math.min(lineStart + columnOffset, Math.max(lineStart, lineEnd - 1));
                if (firstOffset < 0) {
                    firstOffset = candidate;
                }
            } catch (BadLocationException ignored) {
            }
        }

        if (firstOffset >= 0) {
            moveCaretTo(firstOffset);
        }
    }

    private void moveCaretTo(int offset) {
        editorArea.requestFocusInWindow();
        editorArea.setCaretPosition(offset);
        try {
            Rectangle2D rect = editorArea.modelToView2D(offset);
            if (rect != null) {
                editorArea.scrollRectToVisible(rect.getBounds());
            }
        } catch (BadLocationException ignored) {
        }
    }

    private String formatRunResult(PracticeRunResult result) {
        StringBuilder builder = new StringBuilder(result.message());
        for (PracticeRunResult.CaseResult caseResult : result.caseResults()) {
            builder.append(System.lineSeparator()).append(System.lineSeparator())
                    .append(caseResult.passed() ? "PASS  " : "FAIL  ")
                    .append(caseResult.label()).append(System.lineSeparator())
                    .append(caseResult.details());
        }
        return builder.toString();
    }

    private void setBusy(boolean busy) {
        boolean enabled = !busy && currentProblem != null;
        runTestsButton.setEnabled(enabled);
        resetCodeButton.setEnabled(enabled);
        answerDropdown.setEnabled(enabled);
        editorArea.setEditable(!busy && currentProblem != null);
    }
}