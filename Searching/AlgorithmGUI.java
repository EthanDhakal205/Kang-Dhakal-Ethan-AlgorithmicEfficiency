import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AlgorithmGUI extends JFrame implements AlgoRunner.Callbacks {

    // Dark palette
    private static final Color[] DARK = {
        new Color(10, 11, 14),    // BG
        new Color(17, 19, 24),    // PANEL
        new Color(24, 27, 34),    // CARD
        new Color(38, 42, 54),    // BORDER
        new Color(82, 130, 255),  // ACCENT
        new Color(52, 211, 153),  // ACCENT2
        new Color(220, 225, 235), // TEXT
        new Color(100, 110, 130), // TEXT_DIM
        new Color(55, 62, 78),     // TEXT_HINT
        new Color(28, 38, 70),   // ALGO_ACTIVE_BG
        new Color(24, 27, 38),   // ALGO_HOVER_BG
    };

    // Light palette
    private static final Color[] LIGHT = {
        new Color(245, 246, 250), // BG
        new Color(230, 232, 240), // PANEL
        new Color(215, 218, 230), // CARD
        new Color(180, 185, 205), // BORDER
        new Color(50, 100, 220),  // ACCENT
        new Color(15, 160, 100),  // ACCENT2
        new Color(15, 18, 35),    // TEXT
        new Color(80, 88, 110),   // TEXT_DIM
        new Color(160, 168, 190),  // TEXT_HINT
        new Color(210, 220, 248), // ALGO_ACTIVE_BG
        new Color(225, 228, 240), // ALGO_HOVER_BG
    };

    // Live color references — swapped by applyTheme()
    private static Color BG, PANEL, CARD, BORDER, ACCENT, ACCENT2,
                        TEXT, TEXT_DIM, TEXT_HINT, ALGO_ACTIVE_BG, ALGO_HOVER_BG;

    private static final Font MONO = new Font("JetBrains Mono", Font.PLAIN, 13);
    private static final Font SANS = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SANS_B = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private final AnimState state = new AnimState();
    private final VisualizerPanel viz;
    private final AlgoRunner runner;
    private Timer animLoop;
    private volatile Thread algoThread;

    private JScrollPane vizScroll;
    private JLabel statusLabel;
    private JLabel cmpLabel;
    private JLabel swapsLabel;
    private JLabel timeLabel;
    private JLabel resultLabel;
    private JLabel algoLabel;
    private JSlider speedSlider;
    private JButton runBtn;
    private JButton resetBtn;
    private JPanel inputPanel;
    private CardLayout inputCards;
    private JCheckBox captionToggle;
    private PracticeDialog practiceDialog;

    private JTextField arrayField;
    private JTextField targetField;
    private JCheckBox arrayIgnoreCaseBox;
    private JTextField sortArrayField;

    private boolean darkMode = true;
    private JButton themeBtn;

    public AlgorithmGUI() {
        applyTheme();
        buildTreeGraph();
        viz = new VisualizerPanel(state);
        runner = new AlgoRunner(state, this);

        setTitle("Search and Sort Algorithm Visualizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1480, 780));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildSearchSidebar(), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);
        add(buildSortSidebar(), BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        startAnimLoop();
        setVisible(true);
        refreshInputPanel();
    }
    private void applyTheme() {
        Color[] t = darkMode ? DARK : LIGHT;
        BG = t[0];
        PANEL = t[1];
        CARD = t[2]; 
        BORDER = t[3];
        ACCENT = t[4]; 
        ACCENT2 = t[5]; 
        TEXT = t[6];
        TEXT_DIM = t[7]; 
        TEXT_HINT = t[8];
        ALGO_ACTIVE_BG = t[9]; 
        ALGO_HOVER_BG = t[10];
    }

    private void rebuildUI() {
        if (state.running) return;  // don't swap mid-animation
        applyTheme();
        viz.applyTheme(darkMode);
        if (practiceDialog != null) practiceDialog.applyTheme(darkMode);
        getContentPane().removeAll();
        getContentPane().setBackground(BG);
        add(buildSearchSidebar(), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);
        add(buildSortSidebar(), BorderLayout.EAST);
        revalidate();
        repaint();
        refreshInputPanel();
        themeBtn.setText(darkMode ? "☀ light" : "🌙 dark");
    }

    private void startAnimLoop() {
        animLoop = new Timer(16, e -> tickAnimations());
        animLoop.start();
    }

    private void tickAnimations() {
        boolean dirty = false;

        if (state.cellAnim != null && state.cellAnimState != null) {
            for (int i = 0; i < state.cellAnim.length; i++) {
                float target = (state.cellAnimState[i] == 2 || state.cellAnimState[i] == 3) ? 1f : 0f;
                float diff = target - state.cellAnim[i];
                if (Math.abs(diff) > 0.002f) {
                    state.cellAnim[i] += diff * 0.18f;
                    dirty = true;
                } else if (state.cellAnim[i] != target) {
                    state.cellAnim[i] = target;
                    dirty = true;
                }
            }
        }

        for (String key : new ArrayList<>(state.nodeAnim.keySet())) {
            float value = state.nodeAnim.get(key);
            if (value < 1f) {
                state.nodeAnim.put(key, Math.min(1f, value + 0.14f));
                dirty = true;
            }
        }

        if (state.captionPhase == 1) {
            state.captionProgress += 0.07f;
            if (state.captionProgress >= 1f) {
                state.captionProgress = 1f;
                state.captionPhase = 2;
            }
            dirty = true;
        } else if (state.captionPhase == 2) {
            state.captionHoldTicks++;
            if (state.captionHoldTicks >= state.captionHoldMax) {
                state.captionPhase = 3;
                state.captionProgress = 1f;
            }
            dirty = true;
        } else if (state.captionPhase == 3) {
            state.captionProgress -= 0.05f;
            if (state.captionProgress <= 0f) {
                state.captionProgress = 0f;
                state.captionPhase = 0;
                if (state.captionQueued != null) {
                    pushCaption(state.captionQueued);
                    state.captionQueued = null;
                }
            }
            dirty = true;
        }

        if (dirty) {
            viz.repaint();
        }
    }

    private void pushCaption(String text) {
        state.captionText = text;
        state.captionPhase = 1;
        state.captionProgress = 0f;
        state.captionHoldTicks = 0;
        state.captionHoldMax = Math.max(6, state.stepDelay / 20);
    }

    @Override
    public void showCaption(String text) {
        if (!state.showCaptions) {
            return;
        }
        if (state.captionPhase == 0 || state.captionPhase == 3) {
            state.captionQueued = null;
            SwingUtilities.invokeLater(() -> pushCaption(text));
        } else {
            state.captionQueued = text;
            SwingUtilities.invokeLater(() -> {
                if (state.captionPhase == 2) {
                    state.captionPhase = 3;
                    state.captionProgress = 1f;
                }
            });
        }
    }

    @Override
    public void setCellActive(int i) {
        if (state.cellStates == null || i < 0 || i >= state.cellStates.length) {
            return;
        }
        state.cellStates[i] = 2;
        if (state.cellAnimState != null && i < state.cellAnimState.length) {
            state.cellAnimState[i] = 2;
        }
    }

    @Override
    public void setCellFound(int i) {
        if (state.cellStates == null || i < 0 || i >= state.cellStates.length) {
            return;
        }
        state.cellStates[i] = 3;
        if (state.cellAnimState != null && i < state.cellAnimState.length) {
            state.cellAnimState[i] = 3;
        }
    }

    @Override
    public void setCellScanned(int i) {
        if (state.cellStates == null || i < 0 || i >= state.cellStates.length) {
            return;
        }
        state.cellStates[i] = 1;
        if (state.cellAnimState != null && i < state.cellAnimState.length) {
            state.cellAnimState[i] = 1;
        }
    }

    @Override
    public void markScanned(int from, int to) {
        if (state.cellStates == null) {
            return;
        }
        for (int i = Math.max(0, from); i <= Math.min(to, state.cellStates.length - 1); i++) {
            if (state.cellStates[i] != 3) {
                setCellScanned(i);
            }
        }
    }

    @Override
    public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }

    @Override
    public void repaint() {
        viz.repaint();
    }

    @Override
    public void sleep() throws InterruptedException {
        Thread.sleep(state.stepDelay);
    }

    @Override
    public int compareValues(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }
        if (a instanceof String && b instanceof String) {
            return state.ignoreCase ? ((String) a).compareToIgnoreCase((String) b) : ((String) a).compareTo((String) b);
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    private JPanel buildSearchSidebar() {
        JPanel side = buildRailPanel("searchviz", ACCENT, false);
        JPanel list = buildRailList();
        addCategory(list, "SEARCH / ARRAY", AnimState.ARRAY_SEARCH_ALGOS, ACCENT, false);
        addCategory(list, "SEARCH / GRAPH", AnimState.GRAPH_ALGOS, ACCENT, false);
        addCategory(list, "SEARCH / STRING", AnimState.STRING_ALGOS, ACCENT, false);
        side.add(buildRailScroll(list), BorderLayout.CENTER);
        return side;
    }

    private JPanel buildSortSidebar() {
        JPanel side = buildRailPanel("sortviz", ACCENT2, true);
        side.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
        JPanel list = buildRailList();
        addCategory(list, "COMPARISON SORTS", AnimState.COMPARISON_SORT_ALGOS, ACCENT2, true);
        addCategory(list, "HYBRID / ODDITIES", AnimState.HYBRID_SORT_ALGOS, ACCENT2, true);
        addCategory(list, "DIGIT BASED", AnimState.DISTRIBUTION_SORT_ALGOS, ACCENT2, true);
        side.add(buildRailScroll(list), BorderLayout.CENTER);
        return side;
    }

    private JPanel buildRailPanel(String titleText, Color accent, boolean rightAligned) {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(PANEL);
        side.setPreferredSize(new Dimension(232, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JLabel title = new JLabel(rightAligned ? titleText + "  " : "  " + titleText);
        title.setFont(TITLE);
        title.setForeground(accent);
        title.setHorizontalAlignment(rightAligned ? SwingConstants.RIGHT : SwingConstants.LEFT);
        title.setBorder(new EmptyBorder(24, 16, 20, 16));
        side.add(title, BorderLayout.NORTH);
        return side;
    }

    private JPanel buildRailList() {
        JPanel list = new JPanel();
        list.setBackground(PANEL);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(0, 0, 16, 0));
        return list;
    }

    private JScrollPane buildRailScroll(JPanel list) {
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(PANEL);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private void addCategory(JPanel parent, String label, String[] algos, Color accent, boolean rightAligned) {
        JLabel categoryLabel = new JLabel(label);
        categoryLabel.setFont(SMALL);
        categoryLabel.setForeground(TEXT_HINT);
        categoryLabel.setHorizontalAlignment(rightAligned ? SwingConstants.RIGHT : SwingConstants.LEFT);
        categoryLabel.setAlignmentX(LEFT_ALIGNMENT);
        categoryLabel.setBorder(new EmptyBorder(14, 18, 6, 18));
        parent.add(categoryLabel);

        for (String algo : algos) {
            AlgoButton button = new AlgoButton(algo, accent, rightAligned);
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.addActionListener(e -> {
                selectAlgorithm(algo);
                parent.repaint();
            });
            parent.add(button);
        }
    }

    private void selectAlgorithm(String algo) {
        state.selectedAlgo = algo;
        algoLabel.setText(algo);
        refreshInputPanel();
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(buildTopBar(), BorderLayout.NORTH);
        main.add(buildCenter(), BorderLayout.CENTER);
        main.add(buildBottomBar(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 24, 12, 24)
        ));

        algoLabel = new JLabel(state.selectedAlgo);
        algoLabel.setFont(SANS_B);
        algoLabel.setForeground(TEXT);

        themeBtn = actionButton(darkMode ? "☀ light" : "🌙 dark", false);
        themeBtn.addActionListener(e -> {
            darkMode = !darkMode;
            rebuildUI();
        });

        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        westPanel.setOpaque(false);
        westPanel.add(themeBtn);
        westPanel.add(algoLabel);
        bar.add(westPanel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        captionToggle = styledCheckbox("captions");
        captionToggle.setSelected(true);
        captionToggle.addActionListener(e -> state.showCaptions = captionToggle.isSelected());

        JButton codeBtn = actionButton("code", false);
        codeBtn.addActionListener(e -> showCodeDialog());

        JButton appsBtn = actionButton("applications", false);
        appsBtn.addActionListener(e -> showApplicationsDialog());

        JButton practiceBtn = actionButton("practice", false);
        practiceBtn.addActionListener(e -> openPracticeDialog());

        JLabel speedLabel = new JLabel("speed");
        speedLabel.setFont(SMALL);
        speedLabel.setForeground(TEXT_DIM);

        speedSlider = new JSlider(1, 5, 3);
        speedSlider.setBackground(PANEL);
        speedSlider.setPreferredSize(new Dimension(100, 24));
        speedSlider.addChangeListener(e -> {
            int[] delays = {700, 450, 280, 130, 50};
            state.stepDelay = delays[speedSlider.getValue() - 1];
        });

        right.add(captionToggle);
        right.add(codeBtn);
        right.add(appsBtn);
        right.add(practiceBtn);
        right.add(speedLabel);
        right.add(speedSlider);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(18, 24, 0, 24));

        inputCards = new CardLayout();
        inputPanel = new JPanel(inputCards);
        inputPanel.setBackground(BG);
        inputPanel.add(buildArrayInput(), "array");
        inputPanel.add(buildGraphInput(), "graph");
        inputPanel.add(buildStringInput(), "string");
        inputPanel.add(buildSortInput(), "sort");
        center.add(inputPanel, BorderLayout.NORTH);

        vizScroll = new JScrollPane(viz);
        vizScroll.setBorder(null);
        vizScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        vizScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        vizScroll.getViewport().setBackground(BG);
        vizScroll.getVerticalScrollBar().setUnitIncrement(20);
        viz.setScrollParent(vizScroll);
        center.add(vizScroll, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildArrayInput() {
        JPanel panel = roundPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        arrayField = styledField(22);
        arrayField.setText("2, 5, 8, 11, 14, 19, 27, 33, 45");

        targetField = styledField(6);
        targetField.setText("19");

        arrayIgnoreCaseBox = styledCheckbox("ignore case");
        JButton sampleBtn = actionButton("sample", false);
        sampleBtn.addActionListener(e -> applySearchSample());

        Runnable apply = () -> parseArrayInput(arrayField.getText(), targetField.getText(), arrayIgnoreCaseBox.isSelected(), false);
        addLiveListener(arrayField, apply);
        addLiveListener(targetField, apply);
        arrayIgnoreCaseBox.addActionListener(e -> apply.run());

        panel.add(dimLabel("array:"));
        panel.add(arrayField);
        panel.add(dimLabel("target:"));
        panel.add(targetField);
        panel.add(sampleBtn);
        panel.add(arrayIgnoreCaseBox);
        return panel;
    }

    private JPanel buildGraphInput() {
        JPanel panel = roundPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        JTextField startField = styledField(3);
        startField.setText("A");
        JTextField targetGraphField = styledField(3);
        targetGraphField.setText("C");
        JSpinner depthSpinner = styledSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        JSpinner branchSpinner = styledSpinner(new SpinnerNumberModel(2, 2, 4, 1));

        Runnable apply = () -> {
            state.graphStart = startField.getText().trim().isEmpty() ? "A" : startField.getText().trim().toUpperCase();
            state.graphTarget = targetGraphField.getText().trim().isEmpty() ? "C" : targetGraphField.getText().trim().toUpperCase();
            state.graphDepth = (Integer) depthSpinner.getValue();
            state.graphBranch = (Integer) branchSpinner.getValue();
            buildTreeGraph();
            resetVisuals();
        };

        addLiveListener(startField, apply);
        addLiveListener(targetGraphField, apply);
        depthSpinner.addChangeListener(e -> apply.run());
        branchSpinner.addChangeListener(e -> apply.run());

        panel.add(dimLabel("start:"));
        panel.add(startField);
        panel.add(dimLabel("target:"));
        panel.add(targetGraphField);
        panel.add(dimLabel("depth:"));
        panel.add(depthSpinner);
        panel.add(dimLabel("branches:"));
        panel.add(branchSpinner);
        return panel;
    }

    private JPanel buildStringInput() {
        JPanel panel = roundPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        JTextField textField = styledField(28);
        textField.setText("the cat sat on the caterpillar");
        JTextField patternField = styledField(10);
        patternField.setText("cat");
        JCheckBox ignoreCaseBox = styledCheckbox("ignore case");

        Runnable apply = () -> {
            state.currentText = textField.getText();
            state.currentPattern = patternField.getText();
            state.ignoreCase = ignoreCaseBox.isSelected();
            resetVisuals();
        };

        addLiveListener(textField, apply);
        addLiveListener(patternField, apply);
        ignoreCaseBox.addActionListener(e -> apply.run());

        panel.add(dimLabel("text:"));
        panel.add(textField);
        panel.add(dimLabel("pattern:"));
        panel.add(patternField);
        panel.add(ignoreCaseBox);
        return panel;
    }

    private JPanel buildSortInput() {
        JPanel panel = roundPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        sortArrayField = styledField(28);
        sortArrayField.setText("64, 34, 25, 12, 22, 11, 90, 45, 78, 3");

        Runnable apply = () -> parseSortInput(sortArrayField.getText(), false);
        addLiveListener(sortArrayField, apply);

        JButton sampleBtn = actionButton("sample", false);
        sampleBtn.addActionListener(e -> {
            sortArrayField.setText(randomSortSample());
            parseSortInput(sortArrayField.getText(), false);
        });

        panel.add(dimLabel("array:"));
        panel.add(sortArrayField);
        panel.add(sampleBtn);
        panel.add(dimLabel("integers only"));
        return panel;
    }

    private void addLiveListener(JTextField field, Runnable onChange) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(onChange);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(onChange);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(onChange);
            }
        });
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
            new EmptyBorder(12, 24, 12, 24)
        ));

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        stats.setOpaque(false);
        cmpLabel = monoLabel("comparisons: -");
        swapsLabel = monoLabel("swaps: -");
        timeLabel = monoLabel("time: -");
        resultLabel = monoLabel("result: -");
        statusLabel = monoLabel("configure and press run");
        stats.add(cmpLabel);
        stats.add(swapsLabel);
        stats.add(timeLabel);
        stats.add(resultLabel);
        stats.add(statusLabel);
        bar.add(stats, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        resetBtn = actionButton("reset", false);
        runBtn = actionButton("run", true);
        resetBtn.addActionListener(e -> {
        if (state.running && algoThread != null) {
            algoThread.interrupt();   // triggers InterruptedException in sleep()
        } else {
        resetVisuals();
        }
        });
        runBtn.addActionListener(e -> {
            if (!state.running) {
                runAlgorithm();
            }
        });
        buttons.add(resetBtn);
        buttons.add(runBtn);
        bar.add(buttons, BorderLayout.EAST);
        return bar;
    }

    private void runAlgorithm() {
    if (state.running) {
        return;
    }

    boolean ready = true;
    if (state.isArrayAlgo()) {
        ready = parseArrayInput(arrayField.getText(), targetField.getText(), arrayIgnoreCaseBox.isSelected(), true);
    } else if (state.isSortAlgo()) {
        ready = parseSortInput(sortArrayField.getText(), true);
    }
    if (!ready) {
        return;
    }

    state.running = true;
    runBtn.setEnabled(false);
    resetBtn.setText("stop");       // <-- rename to stop
    state.resetStats();

    algoThread = new Thread(() -> {         // <-- store the thread
        try {
            if (state.isArrayAlgo()) {
                runner.runArrayAlgo();
            } else if (state.isGraphAlgo()) {
                runner.runGraphAlgo();
            } else if (state.isSortAlgo()) {
                runner.runSortAlgo();
            } else {
                runner.runStringAlgo();
            }
            updateStats();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            SwingUtilities.invokeLater(() -> {   // <-- clean up on stop
                state.running = false;
                forceResetVisuals();
            });
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "Unable to run the selected algorithm." : ex.getMessage();
            SwingUtilities.invokeLater(() -> statusLabel.setText(message));
            updateStats();
        } finally {
            state.running = false;
            SwingUtilities.invokeLater(() -> {
                runBtn.setEnabled(true);
                resetBtn.setText("reset");  // <-- restore label
            });
        }
    });
    algoThread.start();
}

    private void forceResetVisuals() {
        
        state.resetStats();
        state.resetCaption();
        if (state.isArrayAlgo()) {
            state.resetArrayState();
        } else if (state.isSortAlgo()) {
            state.resetSortState();
        } else if (state.isStringAlgo()) {
            state.resetStringState();
        } else {
            state.resetGraphState();
        }
        if (statusLabel != null) statusLabel.setText("stopped - press run");
        if (cmpLabel != null) cmpLabel.setText("comparisons: -");
        if (swapsLabel != null) swapsLabel.setText("swaps: -");
        if (timeLabel != null) timeLabel.setText("time: -");
        if (resultLabel != null) resultLabel.setText("result: -");
        viz.revalidate();
        viz.repaint();
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            cmpLabel.setText("comparisons: " + state.comparisons);
            swapsLabel.setText(state.isSortAlgo() ? "swaps: " + state.swaps : "swaps: -");
            timeLabel.setText("time: " + (state.elapsedNs > 0 ? state.elapsedNs + " ns" : "-"));
            if (state.isSortAlgo()) {
                resultLabel.setText(state.sortCompleted ? "result: sorted" : "result: -");
            } else if (state.isGraphAlgo()) {
                resultLabel.setText(state.resultIndex >= 0 ? "result: path found"
                    : state.resultIndex == -1 ? "result: not found" : "result: -");
            } else if (state.isStringAlgo()) {
                resultLabel.setText(state.resultIndex >= 0 ? "result: index " + state.resultIndex
                    : state.resultIndex == -1 ? "result: no match" : "result: -");
            } else {
                resultLabel.setText(state.resultIndex >= 0 ? "result: index " + state.resultIndex
                    : state.resultIndex == -1 ? "result: not found" : "result: -");
            }
        });
    }

    private void resetVisuals() {
    if (state.running || viz == null) return;
    forceResetVisuals();
}

    private void refreshInputPanel() {
        if (state.isArrayAlgo()) {
            inputCards.show(inputPanel, "array");
            if (arrayField != null && targetField != null && arrayIgnoreCaseBox != null) {
                parseArrayInput(arrayField.getText(), targetField.getText(), arrayIgnoreCaseBox.isSelected(), false);
            } else {
                resetVisuals();
            }
        } else if (state.isGraphAlgo()) {
            inputCards.show(inputPanel, "graph");
            resetVisuals();
        } else if (state.isStringAlgo()) {
            inputCards.show(inputPanel, "string");
            resetVisuals();
        } else {
            inputCards.show(inputPanel, "sort");
            if (sortArrayField != null) {
                parseSortInput(sortArrayField.getText(), false);
            } else {
                resetVisuals();
            }
        }
    }

    private boolean parseArrayInput(String arrText, String tgtText, boolean ignoreCase, boolean showErrors) {
        if (state.running) {
            return false;
        }
        try {
            String[] parts = arrText.split(",");
            if (parts.length == 0) {
                throw new IllegalArgumentException("Provide at least one array value.");
            }
            Object[] arr = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String value = parts[i].trim();
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Array entries must be comma-separated values.");
                }
                try {
                    arr[i] = Integer.parseInt(value);
                } catch (NumberFormatException first) {
                    try {
                        arr[i] = Double.parseDouble(value);
                    } catch (NumberFormatException second) {
                        arr[i] = value;
                    }
                }
            }

            state.currentArray = arr;
            String targetText = tgtText.trim();
            if (!targetText.isEmpty()) {
                try {
                    state.currentTarget = Integer.parseInt(targetText);
                } catch (NumberFormatException first) {
                    try {
                        state.currentTarget = Double.parseDouble(targetText);
                    } catch (NumberFormatException second) {
                        state.currentTarget = targetText;
                    }
                }
            }
            state.ignoreCase = ignoreCase;
            resetVisuals();
            return true;
        } catch (Exception ex) {
            if (showErrors && statusLabel != null) {
                statusLabel.setText(ex.getMessage() == null ? "Unable to parse array input." : ex.getMessage());
            }
            return false;
        }
    }

    private boolean parseSortInput(String arrText, boolean showErrors) {
        if (state.running) {
            return false;
        }
        try {
            String[] parts = arrText.split(",");
            if (parts.length == 0) {
                throw new IllegalArgumentException("Provide at least one integer to sort.");
            }
            Object[] arr = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String value = parts[i].trim();
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Sort input must be comma-separated integers.");
                }
                arr[i] = Integer.parseInt(value);
            }
            state.currentArray = arr;
            resetVisuals();
            return true;
        } catch (Exception ex) {
            if (showErrors && statusLabel != null) {
                statusLabel.setText(ex.getMessage() == null ? "Sort input must be comma-separated integers." : ex.getMessage());
            }
            return false;
        }
    }

    private String randomSortSample() {
        Random random = new Random();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            values.add(String.valueOf(5 + random.nextInt(95)));
        }
        return String.join(", ", values);
    }

    private void applySearchSample() {
        Random random = new Random();
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            values.add(5 + random.nextInt(90));
        }
        boolean sorted = !state.selectedAlgo.equals("Linear Search");
        if (sorted) {
            Collections.sort(values);
        }
        int target;
        if (random.nextDouble() < 0.75) {
            target = values.get(random.nextInt(values.size()));
        } else {
            target = 100 + random.nextInt(50);
        }
        arrayField.setText(joinInts(values));
        targetField.setText(String.valueOf(target));
        arrayIgnoreCaseBox.setSelected(false);
        parseArrayInput(arrayField.getText(), targetField.getText(), false, false);
    }

    private String joinInts(List<Integer> values) {
        List<String> parts = new ArrayList<>();
        for (Integer value : values) {
            parts.add(String.valueOf(value));
        }
        return String.join(", ", parts);
    }

    private void buildTreeGraph() {
        state.currentGraph = new LinkedHashMap<>();
        int[] counter = {0};
        buildNode(null, state.graphDepth, state.graphBranch, counter);
        List<String> nodes = new ArrayList<>(state.currentGraph.keySet());
        if (!nodes.isEmpty() && !state.currentGraph.containsKey(state.graphStart)) {
            state.graphStart = nodes.get(0);
        }
        if (nodes.size() > 1 && !state.currentGraph.containsKey(state.graphTarget)) {
            state.graphTarget = nodes.get(nodes.size() - 1);
        }
    }

    private String buildNode(String parent, int depth, int branch, int[] counter) {
        String name = nodeLabel(counter[0]++);
        state.currentGraph.put(name, new ArrayList<>());
        if (parent != null) {
            state.currentGraph.get(parent).add(name);
        }
        if (depth > 1) {
            for (int i = 0; i < branch; i++) {
                buildNode(name, depth - 1, branch, counter);
            }
        }
        return name;
    }

    private String nodeLabel(int n) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.insert(0, (char) ('A' + n % 26));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }

    private void showCodeDialog() {
        JDialog dialog = new JDialog(this, state.selectedAlgo + " - source code", false);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout());

        JTextArea area = new JTextArea(AppData.CODE_MAP.getOrDefault(state.selectedAlgo, "// not available"));
        area.setFont(MONO);
        area.setBackground(new Color(13, 15, 20));
        area.setForeground(new Color(190, 205, 225));
        area.setCaretColor(ACCENT);
        area.setEditable(false);
        area.setLineWrap(false);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));
        area.setSelectionColor(new Color(50, 80, 140));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(new Color(13, 15, 20));
        scroll.setPreferredSize(new Dimension(720, 420));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel heading = new JLabel(state.selectedAlgo);
        heading.setFont(SANS_B);
        heading.setForeground(TEXT);

        JLabel complexity = new JLabel(
            "time: " + AppData.getTimeComplexity(state.selectedAlgo)
                + "   space: " + AppData.getSpaceComplexity(state.selectedAlgo)
        );
        complexity.setFont(SMALL);
        complexity.setForeground(TEXT_DIM);

        header.add(heading, BorderLayout.WEST);
        header.add(complexity, BorderLayout.EAST);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showApplicationsDialog() {
        JDialog dialog = new JDialog(this, "Algorithms - real-world applications", false);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout());
        dialog.setPreferredSize(new Dimension(960, 720));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel title = new JLabel("When to use each algorithm");
        title.setFont(SANS_B);
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("time  |  space  |  best use cases  |  real-world examples");
        subtitle.setFont(SMALL);
        subtitle.setForeground(TEXT_DIM);
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setBackground(BG);
        grid.setBorder(new EmptyBorder(16, 20, 16, 20));
        for (String[] row : AppData.APP_DATA) {
            grid.add(buildAppCard(row));
        }
        if (AppData.APP_DATA.size() % 2 != 0) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            grid.add(empty);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openPracticeDialog() {
        if (practiceDialog == null) {
            practiceDialog = new PracticeDialog(this);
        }
        practiceDialog.applyTheme(darkMode);
        practiceDialog.showForAlgorithm(state.selectedAlgo);
    }

    private JPanel buildAppCard(String[] row) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);

        JLabel name = new JLabel(row[0]);
        name.setFont(SANS_B);
        name.setForeground(TEXT);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badges.setOpaque(false);
        badges.add(badge("T: " + row[1], ACCENT, new Color(14, 22, 44)));
        badges.add(badge("S: " + row[2], ACCENT2, new Color(10, 30, 22)));

        top.add(name, BorderLayout.WEST);
        top.add(badges, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(htmlLabel("<b>Best for:</b> " + row[3], TEXT));
        body.add(Box.createVerticalStrut(4));
        body.add(htmlLabel(row[4], TEXT_DIM));
        body.add(Box.createVerticalStrut(4));
        body.add(htmlLabel("<i>" + row[5] + "</i>", new Color(90, 100, 120)));
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JLabel htmlLabel(String text, Color color) {
        JLabel label = new JLabel("<html><body style='width:360px'>" + text + "</body></html>");
        label.setFont(SMALL);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel badge(String text, Color fg, Color bg) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        label.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        label.setForeground(fg);
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(2, 7, 2, 7));
        return label;
    }

    private JPanel roundPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JLabel dimLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL);
        label.setForeground(TEXT_DIM);
        return label;
    }

    private JLabel monoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MONO);
        label.setForeground(TEXT_DIM);
        return label;
    }

    private JTextField styledField(int cols) {
        JTextField field = new JTextField(cols);
        field.setBackground(CARD);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setFont(MONO);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    private JSpinner styledSpinner(SpinnerNumberModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension(54, 28));
        JComponent editor = spinner.getEditor();
        if (editor instanceof DefaultEditor) {
            JTextField field = ((DefaultEditor) editor).getTextField();
            field.setBackground(CARD);
            field.setForeground(TEXT);
            field.setFont(MONO);
            field.setCaretColor(ACCENT);
            field.setBorder(new EmptyBorder(2, 4, 2, 4));
        }
        return spinner;
    }

    private JCheckBox styledCheckbox(String label) {
        JCheckBox checkBox = new JCheckBox(label);
        checkBox.setFont(SMALL);
        checkBox.setForeground(TEXT_DIM);
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        return checkBox;
    }

    private JButton actionButton(String label, boolean primary) {
        JButton button = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primary ? ACCENT : CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        button.setFont(SANS_B);
        button.setForeground(primary ? Color.WHITE : TEXT_DIM);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int width = label.length() >= 10 ? 120 : label.length() >= 6 ? 100 : 80;
        button.setPreferredSize(new Dimension(width, 34));
        return button;
    }

    class AlgoButton extends JButton {
        private final String algorithmName;
        private final Color accent;
        private final boolean rightAligned;
        private boolean hovered = false;

        AlgoButton(String algorithmName, Color accent, boolean rightAligned) {
            super(algorithmName);
            this.algorithmName = algorithmName;
            this.accent = accent;
            this.rightAligned = rightAligned;
            setFont(SANS);
            setForeground(TEXT_DIM);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(rightAligned ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(232, 32));
            setPreferredSize(new Dimension(232, 32));
            setBorder(new EmptyBorder(0, rightAligned ? 10 : 18, 0, rightAligned ? 18 : 10));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean active = algorithmName.equals(state.selectedAlgo);
            if (active) {
                g2.setColor(ALGO_ACTIVE_BG);
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 6, 6);
                int stripeX = rightAligned ? getWidth() - 11 : 8;
                g2.setColor(accent);
                g2.fillRoundRect(stripeX, 2, 3, getHeight() - 4, 2, 2);
            } else if (hovered) {
                g2.setColor(ALGO_HOVER_BG);
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 6, 6);
            }
            setForeground(active ? accent : hovered ? TEXT : TEXT_DIM);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AlgorithmGUI::new);
    }
}