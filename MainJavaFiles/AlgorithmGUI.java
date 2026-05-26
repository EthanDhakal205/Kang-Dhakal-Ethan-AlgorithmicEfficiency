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
import javax.swing.JScrollBar;
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
import javax.swing.plaf.basic.BasicScrollBarUI;

public class AlgorithmGUI extends JFrame implements AlgoRunner.Callbacks {

    // Dark palette
    private static final Color[] DARK = {
        new Color(8, 13, 26),      // BG
        new Color(13, 20, 35),     // PANEL
        new Color(20, 30, 48),     // CARD
        new Color(42, 54, 74),     // BORDER
        new Color(96, 165, 250),   // ACCENT
        new Color(52, 211, 153),   // ACCENT2
        new Color(241, 245, 249),  // TEXT
        new Color(180, 190, 205),  // TEXT_DIM
        new Color(105, 118, 139),  // TEXT_HINT
        new Color(28, 55, 99),     // ALGO_ACTIVE_BG
        new Color(28, 39, 59),     // ALGO_HOVER_BG
    };

    // Light palette
    private static final Color[] LIGHT = {
        new Color(248, 250, 252), // BG
        new Color(255, 255, 255), // PANEL
        new Color(255, 255, 255), // CARD
        new Color(211, 219, 232), // BORDER
        new Color(37, 99, 235),   // ACCENT
        new Color(5, 150, 105),   // ACCENT2
        new Color(15, 23, 42),    // TEXT
        new Color(71, 85, 105),   // TEXT_DIM
        new Color(148, 163, 184), // TEXT_HINT
        new Color(219, 234, 254), // ALGO_ACTIVE_BG
        new Color(241, 245, 249), // ALGO_HOVER_BG
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
    private JLabel algoMetaLabel;
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
        themeBtn.setText(darkMode ? "light" : "dark");
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
        JPanel side = buildRailPanel("Search", ACCENT, false);
        JPanel list = buildRailList();
        addCategory(list, "SEARCH / ARRAY", AnimState.ARRAY_SEARCH_ALGOS, ACCENT, false);
        addCategory(list, "SEARCH / GRAPH", AnimState.GRAPH_ALGOS, ACCENT, false);
        addCategory(list, "SEARCH / STRING", AnimState.STRING_ALGOS, ACCENT, false);
        side.add(buildRailScroll(list), BorderLayout.CENTER);
        return side;
    }

    private JPanel buildSortSidebar() {
        JPanel side = buildRailPanel("Sort", ACCENT2, true);
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
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollPane(scroll, PANEL);
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
        if (algoMetaLabel != null) {
            algoMetaLabel.setText(selectedAlgorithmMeta());
        }
        refreshInputPanel();
    }

    private String selectedAlgorithmMeta() {
        String family;
        if (state.isSortAlgo()) {
            family = "sorting";
        } else if (state.isGraphAlgo()) {
            family = "graph search";
        } else if (state.isStringAlgo()) {
            family = "string search";
        } else {
            family = "array search";
        }
        return family + " | time " + AppData.getTimeComplexity(state.selectedAlgo)
            + " | space " + AppData.getSpaceComplexity(state.selectedAlgo);
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
        algoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        algoLabel.setForeground(TEXT);
        algoMetaLabel = new JLabel(selectedAlgorithmMeta());
        algoMetaLabel.setFont(SMALL);
        algoMetaLabel.setForeground(TEXT_DIM);
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.add(algoLabel);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(algoMetaLabel);
        themeBtn = actionButton(darkMode ? "light" : "dark", false);
        themeBtn.addActionListener(e -> {
            darkMode = !darkMode;
            rebuildUI();
        });
        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        westPanel.setOpaque(false);
        westPanel.add(themeBtn);
        westPanel.add(titleStack);
        bar.add(westPanel, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        captionToggle = styledCheckbox("captions");
        captionToggle.setSelected(true);
        captionToggle.addActionListener(e -> state.showCaptions = captionToggle.isSelected());
        JButton codeBtn = actionButton("code", false);
        codeBtn.addActionListener(e -> showCodeDialog());
        JButton aiBtn = actionButton("ai chat", false);
        aiBtn.addActionListener(e -> openAiChatDialog());
        JButton appsBtn = actionButton("applications", false);
        appsBtn.addActionListener(e -> showAppsMenu(appsBtn));
        JButton practiceBtn = actionButton("practice", false);
        practiceBtn.addActionListener(e -> openPracticeDialog());
        JLabel speedLabel = new JLabel("speed");
        speedLabel.setFont(SMALL);
        speedLabel.setForeground(TEXT_DIM);
        speedSlider = new JSlider(1, 5, 3);
        speedSlider.setOpaque(false);
        speedSlider.setForeground(ACCENT);
        speedSlider.setPreferredSize(new Dimension(100, 24));
        speedSlider.addChangeListener(e -> {
            int[] delays = {700, 450, 280, 130, 50};
            state.stepDelay = delays[speedSlider.getValue() - 1];
        });
        right.add(captionToggle);
        right.add(codeBtn);
        right.add(aiBtn);
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
        styleScrollPane(vizScroll, BG);
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

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stats.setOpaque(false);
        cmpLabel = monoLabel("comparisons: -");
        swapsLabel = monoLabel("swaps: -");
        timeLabel = monoLabel("time: -");
        resultLabel = monoLabel("result: -");
        statusLabel = monoLabel("configure and press run");
        stats.add(metricChip(cmpLabel));
        stats.add(metricChip(swapsLabel));
        stats.add(metricChip(timeLabel));
        stats.add(metricChip(resultLabel));
        stats.add(metricChip(statusLabel));
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

    // ── Sort-required search detection & prompt ───────────────────
    private static final java.util.Set<String> SORT_REQUIRED = new java.util.HashSet<>(java.util.Arrays.asList(
        "Binary Search", "Ternary Search", "Jump Search",
        "Interpolation Search", "Exponential Search", "Fibonacci Search"
    ));

    private boolean isSortRequired() {
        return SORT_REQUIRED.contains(state.selectedAlgo);
    }

    private boolean isSortedAscending(Object[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (compareValues(arr[i], arr[i + 1]) > 0) return false;
        }
        return true;
    }

    private void promptSortAndApply() {
        String[] sortOptions = {
            "Bubble Sort", "Insertion Sort", "Selection Sort",
            "Merge Sort", "Quick Sort", "Heap Sort"
        };

        JDialog dialog = new JDialog(this, "Array is not sorted", true);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));
        JLabel title = new JLabel(state.selectedAlgo + " requires a sorted array");
        title.setFont(SANS_B);
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Choose a sorting algorithm to sort your array first");
        subtitle.setFont(SMALL);
        subtitle.setForeground(TEXT_DIM);
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        buttons.setBackground(BG);

        for (String sort : sortOptions) {
            JButton btn = actionButton(sort, false);
            btn.setPreferredSize(new Dimension(140, 34));
            btn.addActionListener(e -> {
                dialog.dispose();
                String originalAlgo = state.selectedAlgo;

                // Temporarily become a sort algorithm so the visualizer renders correctly
                state.selectedAlgo = sort;
                parseSortInput(arrayField.getText(), false);

                // Run the sort animation on the algo thread like a normal sort
                state.running = true;
                runBtn.setEnabled(false);
                resetBtn.setText("stop");
                state.resetStats();

                algoThread = new Thread(() -> {
                    try {
                        runner.runSortAlgo();
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            // Grab the sorted result from state.currentArray
                            List<String> parts = new ArrayList<>();
                            for (Object v : state.currentArray) parts.add(String.valueOf(v));
                            String newText = String.join(", ", parts);

                            // Restore everything back to the search algorithm
                            state.selectedAlgo = originalAlgo;
                            arrayField.setText(newText);
                            parseArrayInput(newText, targetField.getText(),
                                arrayIgnoreCaseBox.isSelected(), false);
                            refreshInputPanel();

                            state.running = false;
                            runBtn.setEnabled(true);
                            resetBtn.setText("reset");
                        });
                    }
                });
                algoThread.start();
            });
            buttons.add(btn);
        }

        JButton cancelBtn = actionButton("cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttons.add(cancelBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(buttons, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ── Applications dropdown menu ────────────────────────────────
    private void showAppsMenu(JButton anchor) {
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        menu.setBackground(CARD);
        menu.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        String[] options = {
            "Real-World Applications",
            "Algorithm Comparison",
            "Compatibility Matrix",
            "Compatibility Notes",
            "Use Case Profiles"
        };

        for (String option : options) {
            javax.swing.JMenuItem item = new javax.swing.JMenuItem(option);
            item.setBackground(CARD);
            item.setForeground(TEXT);
            item.setFont(SANS);
            item.setBorder(new EmptyBorder(8, 16, 8, 16));
            item.addActionListener(e -> handleAppsMenuSelection(option));
            menu.add(item);
        }

        menu.show(anchor, 0, anchor.getHeight());
    }

    private void handleAppsMenuSelection(String option) {
        switch (option) {
            case "Real-World Applications" -> showApplicationsDialog();
            case "Algorithm Comparison"    -> showSpreadsheetSheet(0);
            case "Compatibility Matrix"    -> showSpreadsheetSheet(1);
            case "Compatibility Notes"     -> showSpreadsheetSheet(2);
            case "Use Case Profiles"       -> showSpreadsheetSheet(3);
        }
    }

    private void showSpreadsheetSheet(int sheetIndex) {
        String[] sheetTitles = {
            "Algorithm Comparison",
            "Compatibility Matrix",
            "Compatibility Notes",
            "Use Case Profiles"
        };

        String[][][] sheetColumns = {
            {{"Algorithm","160"},{"Type","100"},{"Category","110"},{"Best Case","110"},
             {"Average Case","110"},{"Worst Case","110"},{"Space","90"},{"Stable","80"},
             {"Sorted Input Req.","120"},{"In-Place","80"},{"Notes","400"}},
            null,
            {{"Sort Algorithm","140"},{"Search Algorithm","140"},{"Rating","80"},{"Notes","500"}},
            {{"Use Case / Scenario","260"},{"Recommended Pairing","220"},{"Why","500"}}
        };

        String[][][] sheetData = {
            buildComparisonData(),
            null,
            buildCompatNotesData(),
            buildUseCaseData()
        };

        JDialog dialog = new JDialog(this, sheetTitles[sheetIndex], false);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout());
        dialog.setPreferredSize(new Dimension(1100, 640));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 20, 12, 20)
        ));
        JLabel heading = new JLabel(sheetTitles[sheetIndex]);
        heading.setFont(SANS_B);
        heading.setForeground(TEXT);
        header.add(heading, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        JPanel content;
        if (sheetIndex == 1) {
            content = buildCompatibilityMatrixPanel();
        } else {
            content = buildTablePanel(sheetColumns[sheetIndex], sheetData[sheetIndex]);
        }

        JScrollPane scroll = new JScrollPane(content);
        styleScrollPane(scroll, BG);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel buildTablePanel(String[][] columns, String[][] data) {
        final Color tableBg = darkMode ? new Color(17, 19, 24) : new Color(250, 251, 255);
        final Color tableAltBg = darkMode ? new Color(20, 23, 30) : new Color(239, 242, 249);
        final Color tableHeaderBg = darkMode ? new Color(26, 31, 46) : PANEL;
        final Color tableSelectionBg = darkMode ? new Color(28, 38, 70) : ALGO_ACTIVE_BG;

        javax.swing.JTable table = new javax.swing.JTable(
            new javax.swing.table.DefaultTableModel(data,
                java.util.Arrays.stream(columns).map(c -> c[0]).toArray()) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        table.setBackground(tableBg);
        table.setForeground(TEXT);
        table.setFont(SANS);
        table.setRowHeight(26);
        table.setGridColor(BORDER);
        table.setSelectionBackground(tableSelectionBg);
        table.setSelectionForeground(TEXT);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
                );
                if (!isSelected) {
                    cell.setBackground(row % 2 == 0 ? tableBg : tableAltBg);
                    cell.setForeground(TEXT);
                }
                if (cell instanceof JComponent) {
                    ((JComponent) cell).setBorder(new EmptyBorder(0, 8, 0, 8));
                }
                return cell;
            }
        });
        table.getTableHeader().setBackground(tableHeaderBg);
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(SANS_B);
        table.getTableHeader().setOpaque(true);
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i)
                 .setPreferredWidth(Integer.parseInt(columns[i][1]));
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(tableBg);
        panel.add(table.getTableHeader(), BorderLayout.NORTH);
        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCompatibilityMatrixPanel() {
        String[] searches = {"Linear","Binary","Ternary","Jump","Interpolation",
                             "Exponential","Fibonacci","BFS","DFS","KMP","Rabin-Karp"};
        String[] sorts    = {"Bubble","Insertion","Selection","Gnome","Merge",
                             "Quick","Heap","Tim","Bogo","Radix"};
        String[][] ratings = {
            {"★★★","★☆☆","★☆☆","★☆☆","★☆☆","★☆☆","★☆☆","—","—","—","—"},
            {"★★★","★★☆","★☆☆","★★☆","★★☆","★☆☆","★☆☆","—","—","—","—"},
            {"★★☆","★☆☆","★☆☆","★☆☆","★☆☆","✗","★☆☆","—","—","—","—"},
            {"★★☆","★☆☆","★☆☆","★☆☆","✗","✗","✗","—","—","—","—"},
            {"★★☆","★★★","★★☆","★★★","★★★","★★★","★★☆","—","—","—","—"},
            {"★★☆","★★★","★★☆","★★★","★★★","★★★","★★☆","—","—","—","—"},
            {"★★☆","★★★","★★☆","★★☆","★★☆","★★★","★★☆","—","—","—","—"},
            {"★★☆","★★★","★★☆","★★★","★★★","★★★","★★☆","—","—","—","—"},
            {"★★☆","✗","✗","✗","✗","✗","✗","—","—","—","—"},
            {"★★☆","★★★","★★☆","★★★","★★★","★★★","★★☆","—","—","—","—"},
        };

        int CELL = 72, ROW_HDR = 110, HDR_H = 36, ROW_H = 28;
        int cols = searches.length, rows = sorts.length;
        int totalW = ROW_HDR + cols * CELL;
        int totalH = HDR_H + rows * ROW_H;
        Color matrixBg = darkMode ? new Color(17, 19, 24) : new Color(250, 251, 255);
        Color matrixHeaderBg = darkMode ? new Color(26, 31, 46) : PANEL;
        Color matrixBorder = darkMode ? new Color(42, 47, 61) : BORDER;

        JPanel panel = new JPanel(null);
        panel.setBackground(matrixBg);
        panel.setPreferredSize(new Dimension(totalW, totalH));

        for (int c = 0; c < cols; c++) {
            JLabel lbl = new JLabel(searches[c], SwingConstants.CENTER);
            lbl.setFont(SMALL);
            lbl.setForeground(ACCENT2);
            lbl.setOpaque(true);
            lbl.setBackground(matrixHeaderBg);
            lbl.setBounds(ROW_HDR + c * CELL, 0, CELL, HDR_H);
            panel.add(lbl);
        }

        for (int r = 0; r < rows; r++) {
            JLabel rh = new JLabel("  " + sorts[r]);
            rh.setFont(SMALL);
            rh.setForeground(ACCENT);
            rh.setOpaque(true);
            rh.setBackground(matrixHeaderBg);
            rh.setBounds(0, HDR_H + r * ROW_H, ROW_HDR, ROW_H);
            panel.add(rh);

            for (int c = 0; c < cols; c++) {
                String rating = ratings[r][c];
                Color bg, fg;
                switch (rating) {
                    case "★★★" -> { bg = new Color(13, 38, 32);  fg = new Color(52, 211, 153); }
                    case "★★☆" -> { bg = new Color(13, 33, 55);  fg = new Color(82, 130, 255); }
                    case "★☆☆" -> { bg = new Color(42, 26, 16);  fg = new Color(251, 146, 60); }
                    case "✗"   -> { bg = new Color(42, 16, 16);  fg = new Color(248, 113, 113); }
                    default    -> { bg = new Color(17, 19, 24);  fg = new Color(42, 47, 61); }
                }
                if (!darkMode) {
                    bg = lightMatrixRatingBackground(rating);
                    fg = lightMatrixRatingForeground(rating);
                }
                JLabel cell = new JLabel(rating, SwingConstants.CENTER);
                cell.setFont(SANS_B);
                cell.setForeground(fg);
                cell.setOpaque(true);
                cell.setBackground(bg);
                cell.setBorder(BorderFactory.createLineBorder(matrixBorder, 1));
                cell.setBounds(ROW_HDR + c * CELL, HDR_H + r * ROW_H, CELL, ROW_H);
                panel.add(cell);
            }
        }
        return panel;
    }

    private Color lightMatrixRatingBackground(String rating) {
        if (rating.contains("\u2717")) {
            return new Color(255, 232, 235);
        }
        int stars = countOccurrences(rating, '\u2605');
        if (stars >= 3) {
            return new Color(225, 247, 238);
        }
        if (stars == 2) {
            return new Color(228, 237, 255);
        }
        if (stars == 1) {
            return new Color(255, 240, 222);
        }
        return new Color(239, 242, 249);
    }

    private Color lightMatrixRatingForeground(String rating) {
        if (rating.contains("\u2717")) {
            return new Color(185, 28, 28);
        }
        int stars = countOccurrences(rating, '\u2605');
        if (stars >= 3) {
            return new Color(12, 116, 70);
        }
        if (stars == 2) {
            return new Color(38, 88, 190);
        }
        if (stars == 1) {
            return new Color(180, 83, 9);
        }
        return TEXT_DIM;
    }

    private int countOccurrences(String text, char needle) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    private String[][] buildComparisonData() {
        return new String[][] {
            {"Bubble Sort","Sort","Comparison","O(n)","O(n²)","O(n²)","O(1)","Yes","N/A","Yes","Early exit if no swaps. Simple but slow on large data."},
            {"Insertion Sort","Sort","Comparison","O(n)","O(n²)","O(n²)","O(1)","Yes","N/A","Yes","Very fast on nearly-sorted or small arrays. Used inside Tim Sort."},
            {"Selection Sort","Sort","Comparison","O(n²)","O(n²)","O(n²)","O(1)","No","N/A","Yes","Fewest swaps of any simple sort. Always O(n²) comparisons."},
            {"Gnome Sort","Sort","Comparison","O(n)","O(n²)","O(n²)","O(1)","Yes","N/A","Yes","Like Insertion Sort but uses swaps. Educational only."},
            {"Merge Sort","Sort","Comparison","O(n log n)","O(n log n)","O(n log n)","O(n)","Yes","N/A","No","Guaranteed O(n log n). Stable. Requires O(n) extra memory."},
            {"Quick Sort","Sort","Comparison","O(n log n)","O(n log n)","O(n²)","O(log n)","No","N/A","Yes","Fast in practice. Worst case on sorted input with bad pivot."},
            {"Heap Sort","Sort","Comparison","O(n log n)","O(n log n)","O(n log n)","O(1)","No","N/A","Yes","Guaranteed O(n log n) with O(1) space. Not cache-friendly."},
            {"Tim Sort","Sort","Hybrid","O(n)","O(n log n)","O(n log n)","O(n)","Yes","N/A","No","Used in Java and Python. Exploits natural runs in real data."},
            {"Bogo Sort","Sort","Novelty","O(n)","O((n+1)!)","O(∞)","O(1)","No","N/A","Yes","Randomly shuffles until sorted. Never use in practice."},
            {"Radix Sort","Sort","Distribution","O(nk)","O(nk)","O(nk)","O(n+k)","Yes","N/A","No","Non-comparison sort. Integers only (non-negative)."},
            {"Linear Search","Search","Array","O(1)","O(n)","O(n)","O(1)","N/A","No","N/A","No sorting required. Best for small or unsorted arrays."},
            {"Binary Search","Search","Array","O(1)","O(log n)","O(log n)","O(1)","N/A","Yes","N/A","Halves the search range each step. Array must be sorted."},
            {"Ternary Search","Search","Array","O(1)","O(log₃ n)","O(log₃ n)","O(1)","N/A","Yes","N/A","Splits into 3 parts. More comparisons per step than Binary."},
            {"Jump Search","Search","Array","O(1)","O(√n)","O(√n)","O(1)","N/A","Yes","N/A","Jumps √n steps then linear scans. Between Linear and Binary."},
            {"Interpolation Search","Search","Array","O(1)","O(log log n)","O(n)","O(1)","N/A","Yes","N/A","Best on uniformly distributed data. Degrades on skewed input."},
            {"Exponential Search","Search","Array","O(1)","O(log n)","O(log n)","O(1)","N/A","Yes","N/A","Finds range by doubling. Good for large or unbounded arrays."},
            {"Fibonacci Search","Search","Array","O(1)","O(log n)","O(log n)","O(1)","N/A","Yes","N/A","Uses Fibonacci numbers instead of division."},
            {"BFS","Search","Graph","O(1)","O(V+E)","O(V+E)","O(V)","N/A","No","N/A","Level-by-level. Guarantees shortest path in unweighted graphs."},
            {"DFS","Search","Graph","O(1)","O(V+E)","O(V+E)","O(V)","N/A","No","N/A","Goes deep before backtracking. Does NOT guarantee shortest path."},
            {"KMP Search","Search","String","O(n)","O(n+m)","O(n+m)","O(m)","N/A","No","N/A","Precomputes LPS table to avoid re-checking characters."},
            {"Rabin-Karp","Search","String","O(n)","O(n+m)","O(nm)","O(1)","N/A","No","N/A","Rolling hash. Worst case O(nm) on collisions. Good for multi-pattern."},
        };
    }

    private String[][] buildCompatNotesData() {
        return new String[][] {
            {"Bubble Sort","Linear Search","★★★","Both work on unsorted data. Consistent for small datasets."},
            {"Bubble Sort","Binary Search","★☆☆","O(n²) sort cost dwarfs Binary Search gain except on tiny arrays."},
            {"Bubble Sort","Ternary Search","★☆☆","Sort cost too high to justify Ternary's marginal benefit over Binary."},
            {"Bubble Sort","Jump Search","★☆☆","Technically works but O(n²) sort cost makes this impractical."},
            {"Bubble Sort","Interpolation Search","★☆☆","Sort cost dominates at any meaningful scale."},
            {"Bubble Sort","Exponential Search","★☆☆","Exponential Search shines on large arrays. Bubble Sort does not."},
            {"Bubble Sort","Fibonacci Search","★☆☆","Pairing a niche search with the slowest sort undermines both."},
            {"Insertion Sort","Linear Search","★★★","Both fast on small n. Ideal for embedded or educational use."},
            {"Insertion Sort","Binary Search","★★☆","Keep sorted on insert, then Binary Search for lookups."},
            {"Insertion Sort","Jump Search","★★☆","Solid pairing for small-to-medium sorted arrays."},
            {"Insertion Sort","Interpolation Search","★★☆","Good if data is numerically well-spread."},
            {"Insertion Sort","Exponential Search","★☆☆","Exponential targets large arrays. Insertion is slow on large n."},
            {"Selection Sort","Linear Search","★★☆","Selection minimises swaps — useful when writes are expensive."},
            {"Selection Sort","Exponential Search","✗","Exponential is for large arrays. Selection Sort is O(n²). At odds."},
            {"Gnome Sort","Interpolation Search","✗","Gnome Sort too slow to pair with a search requiring efficient prep."},
            {"Gnome Sort","Exponential Search","✗","Scale mismatch. Gnome is educational. Exponential is production."},
            {"Gnome Sort","Fibonacci Search","✗","Both are niche — but for completely different reasons. No synergy."},
            {"Merge Sort","Linear Search","★★☆","Merge Sort is overkill if only Linear searching, but fine."},
            {"Merge Sort","Binary Search","★★★","Classic pairing. Stable, reliable, O(n log n) + O(log n)."},
            {"Merge Sort","Jump Search","★★★","Practical, well-balanced combination for medium datasets."},
            {"Merge Sort","Interpolation Search","★★★","Merge preserves distribution. Interpolation exploits it."},
            {"Merge Sort","Exponential Search","★★★","Both handle large datasets well. Strong match."},
            {"Quick Sort","Binary Search","★★★","Most common real-world pairing. The industry standard."},
            {"Quick Sort","Jump Search","★★★","Great for medium arrays. Fast, in-place sort + simple search."},
            {"Quick Sort","Interpolation Search","★★★","Strong for numeric data. Interpolation capitalises on sorted output."},
            {"Quick Sort","Exponential Search","★★★","Both designed for large-scale use. Excellent production pairing."},
            {"Heap Sort","Binary Search","★★★","Guaranteed O(n log n) + O(log n). No worst-case surprises."},
            {"Heap Sort","Exponential Search","★★★","Both work well at scale with guaranteed bounds."},
            {"Tim Sort","Binary Search","★★★","Literally what Java's Collections.binarySearch + Arrays.sort does."},
            {"Tim Sort","Jump Search","★★★","Tim Sort handles real data well. Jump Search is efficient and practical."},
            {"Tim Sort","Interpolation Search","★★★","Real-world data tends toward uniform distribution. Strong match."},
            {"Tim Sort","Exponential Search","★★★","Both production-grade. Strong combination for large datasets."},
            {"Bogo Sort","Linear Search","★★☆","At least Linear Search is fast — partially compensates for Bogo's absurdity."},
            {"Bogo Sort","Binary Search","✗","Waiting O((n+1)!) to sort for O(log n) search is an insult to CS."},
            {"Bogo Sort","Ternary Search","✗","Bogo Sort invalidates any efficiency argument Ternary could make."},
            {"Bogo Sort","Jump Search","✗","Sort cost makes Jump Search's O(√n) completely irrelevant."},
            {"Bogo Sort","Interpolation Search","✗","You'd spend a geological epoch sorting before search begins."},
            {"Bogo Sort","Exponential Search","✗","Exponential is for large arrays. Bogo on large arrays is impossible."},
            {"Bogo Sort","Fibonacci Search","✗","No. Just no."},
            {"Radix Sort","Binary Search","★★★","Radix O(nk) + Binary O(log n). Extremely fast for large integers."},
            {"Radix Sort","Jump Search","★★★","Great combo for large integer arrays. Both fast and practical."},
            {"Radix Sort","Interpolation Search","★★★","Outstanding. Radix produces perfectly ordered integers — ideal for Interpolation."},
            {"Radix Sort","Exponential Search","★★★","Both designed for large-scale integer data. One of the best pairings."},
        };
    }

    private String[][] buildUseCaseData() {
        return new String[][] {
            {"Small dataset (n < 20), any order","Insertion Sort + Linear Search","Both O(n) or better on small n. Simple, zero overhead. Ideal for embedded or educational use."},
            {"Large dataset, many repeated searches","Quick Sort + Binary Search","Sort once O(n log n), search many times at O(log n). Amortised cost per search is effectively O(log n). Industry standard."},
            {"Large dataset, guaranteed worst-case","Merge Sort + Binary Search","Merge Sort has no O(n²) worst case. Stable and predictable. Ideal for safety-critical systems."},
            {"Large integer dataset, maximum speed","Radix Sort + Binary Search","Radix is O(nk) — faster than O(n log n) for fixed-width integers. Best raw throughput."},
            {"Uniformly distributed numeric data","Radix Sort + Interpolation Search","Radix produces perfectly ordered integers. Interpolation achieves O(log log n). Best theoretical pairing."},
            {"Data arrives in nearly-sorted order","Insertion Sort + Binary Search","Insertion Sort is O(n) on nearly-sorted data. Keep array sorted on insert, search with Binary."},
            {"Java / Python production code","Tim Sort + Binary Search","Tim Sort is Java's Arrays.sort and Python's sorted(). Java's Collections.binarySearch pairs natively."},
            {"Memory-constrained environment","Heap Sort + Fibonacci Search","Heap Sort is O(1) space. Fibonacci avoids division — useful on microcontrollers."},
            {"Unknown or unbounded array size","Quick Sort + Exponential Search","Exponential finds the range by doubling — ideal when bounds are unknown."},
            {"Graph pathfinding, shortest path","BFS (no sort needed)","BFS guarantees shortest path in unweighted graphs. Sorting is irrelevant to graph structure."},
            {"Graph exploration, cycle detection","DFS (no sort needed)","DFS is the foundation of cycle detection and topological sort."},
            {"String pattern matching, single pattern","KMP Search (no sort needed)","KMP finds all occurrences in O(n+m). Best single-pattern string search."},
            {"String matching, multiple patterns","Rabin-Karp (no sort needed)","Rolling hash extends naturally to multi-pattern search."},
            {"One-time search, no sort budget","Linear Search (no sort needed)","If searching once on unsorted data, sort cost exceeds a single O(n) scan."},
            {"Educational / visualisation purposes","Bubble Sort + Linear Search","Both are the simplest in their category. Easy to animate and understand."},
            {"Avoid at all costs","Bogo Sort + anything","Expected O((n+1)!) sort time. Pairing with any search is computational self-harm."},
        };
    }

    private void runAlgorithm() {
    if (state.running) {
        return;
    }

    boolean ready = true;
    if (state.isArrayAlgo()) {
        ready = parseArrayInput(arrayField.getText(), targetField.getText(), arrayIgnoreCaseBox.isSelected(), true);
        if (ready && isSortRequired() && !isSortedAscending(state.currentArray)) {
            promptSortAndApply();
            return;
        }
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
        styleScrollPane(scroll, new Color(13, 15, 20));
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
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
        styleScrollPane(scroll, BG);
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

    private void openAiChatDialog() {
        AiChatDialog dialog = new AiChatDialog(this, darkMode, state.selectedAlgo);
        dialog.setVisible(true);
    }

    private JPanel buildAppCard(String[] row) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);

        JLabel name = new JLabel(row[0]);
        name.setFont(SANS_B);
        name.setForeground(TEXT);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badges.setOpaque(false);
        badges.add(badge("T: " + row[1], ACCENT,
            darkMode ? new Color(14, 22, 44) : new Color(226, 235, 255)));
        badges.add(badge("S: " + row[2], ACCENT2,
            darkMode ? new Color(10, 30, 22) : new Color(222, 245, 234)));

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
        body.add(htmlLabel("<i>" + row[5] + "</i>", TEXT_DIM));
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JLabel htmlLabel(String text, Color color) {
        JLabel label = new JLabel("<html><body style='width:360px;color:" + colorToHex(color) + "'>"
            + text + "</body></html>");
        label.setFont(SMALL);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private JLabel badge(String text, Color fg, Color bg) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), darkMode ? 95 : 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                super.paintComponent(g);
            }
        };
        label.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        label.setForeground(fg);
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(2, 7, 2, 7));
        return label;
    }

    private void styleScrollPane(JScrollPane scroll, Color viewportBg) {
        scroll.setBorder(null);
        scroll.getViewport().setBackground(viewportBg);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);
        styleScrollBar(scroll.getVerticalScrollBar(), viewportBg);
        styleScrollBar(scroll.getHorizontalScrollBar(), viewportBg);
    }

    private void styleScrollBar(JScrollBar scrollBar, Color trackColor) {
        scrollBar.setPreferredSize(new Dimension(10, 10));
        scrollBar.setOpaque(false);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.trackColor = trackColor;
                this.thumbColor = TEXT_HINT;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return zeroScrollButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return zeroScrollButton();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle thumbBounds) {
                if (!scrollBar.isEnabled() || thumbBounds.width <= 0 || thumbBounds.height <= 0) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(darkMode ? new Color(75, 90, 115) : new Color(170, 184, 205));
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                    thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
                g2.dispose();
            }
        });
    }

    private JButton zeroScrollButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    private JPanel roundPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
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

    private JPanel metricChip(JLabel label) {
        JPanel chip = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(darkMode ? new Color(16, 25, 42) : new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
        };
        chip.setOpaque(false);
        chip.setBorder(new EmptyBorder(6, 10, 6, 10));
        chip.add(label, BorderLayout.CENTER);
        return chip;
    }

    private JTextField styledField(int cols) {
        JTextField field = new JTextField(cols);
        field.setBackground(darkMode ? new Color(12, 18, 32) : new Color(248, 250, 252));
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setFont(MONO);
        field.setSelectionColor(ALGO_ACTIVE_BG);
        field.setSelectedTextColor(TEXT);
        Dimension size = field.getPreferredSize();
        field.setPreferredSize(new Dimension(size.width, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private JSpinner styledSpinner(SpinnerNumberModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension(54, 28));
        JComponent editor = spinner.getEditor();
        if (editor instanceof DefaultEditor) {
            JTextField field = ((DefaultEditor) editor).getTextField();
            field.setBackground(darkMode ? new Color(12, 18, 32) : new Color(248, 250, 252));
            field.setForeground(TEXT);
            field.setFont(MONO);
            field.setCaretColor(ACCENT);
            field.setBorder(new EmptyBorder(4, 6, 4, 6));
        }
        spinner.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
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
            private boolean hovered = false;
            private boolean pressed = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        pressed = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = primary ? ACCENT : hovered ? ALGO_HOVER_BG : CARD;
                if (pressed) {
                    fill = primary ? ACCENT.darker() : ALGO_ACTIVE_BG;
                }
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(primary ? ACCENT : BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                setForeground(primary ? Color.WHITE : hovered ? TEXT : TEXT_DIM);
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
            setMaximumSize(new Dimension(232, 36));
            setPreferredSize(new Dimension(232, 36));
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
                g2.fillRoundRect(8, 3, getWidth() - 16, getHeight() - 6, 8, 8);
                int stripeX = rightAligned ? getWidth() - 13 : 8;
                g2.setColor(accent);
                g2.fillRoundRect(stripeX, 7, 5, getHeight() - 14, 4, 4);
            } else if (hovered) {
                g2.setColor(ALGO_HOVER_BG);
                g2.fillRoundRect(8, 3, getWidth() - 16, getHeight() - 6, 8, 8);
            }
            setForeground(active ? accent : hovered ? TEXT : TEXT_DIM);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AlgorithmGUI::new);
    }
}
