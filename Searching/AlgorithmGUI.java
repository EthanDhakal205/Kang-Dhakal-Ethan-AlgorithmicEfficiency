import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class AlgorithmGUI extends JFrame implements AlgoRunner.Callbacks {

    private static final Color BG      = new Color(10,  11,  14);
    private static final Color PANEL   = new Color(17,  19,  24);
    private static final Color CARD    = new Color(24,  27,  34);
    private static final Color BORDER  = new Color(38,  42,  54);
    private static final Color ACCENT  = new Color(82, 130, 255);
    private static final Color ACCENT2 = new Color(52, 211, 153);
    private static final Color TEXT    = new Color(220, 225, 235);
    private static final Color TEXT_DIM= new Color(100, 110, 130);
    private static final Color TEXT_HINT=new Color(55,  62,  78);

    private static final Font MONO   = new Font("JetBrains Mono", Font.PLAIN, 13);
    private static final Font SANS   = new Font("Segoe UI",       Font.PLAIN, 13);
    private static final Font SANS_B = new Font("Segoe UI",       Font.BOLD,  14);
    private static final Font TITLE  = new Font("Segoe UI",       Font.BOLD,  22);
    private static final Font SMALL  = new Font("Segoe UI",       Font.PLAIN, 11);

    private final AnimState      state  = new AnimState();
    private final VisualizerPanel viz;
    private final AlgoRunner      runner;
    private javax.swing.Timer     animLoop;

    private JScrollPane  vizScroll;
    private JLabel       statusLabel, cmpLabel, timeLabel, resultLabel, algoLabel;
    private JSlider      speedSlider;
    private JButton      runBtn, resetBtn;
    private JPanel       inputPanel;
    private CardLayout   inputCards;
    private JCheckBox    captionToggle;

    public AlgorithmGUI() {
        buildTreeGraph();
        viz    = new VisualizerPanel(state);
        runner = new AlgoRunner(state, this);

        setTitle("Search Algorithm Visualizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 760));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        startAnimLoop();
        setVisible(true);
        refreshInputPanel();
    }

    private void startAnimLoop() {
        animLoop = new javax.swing.Timer(16, e -> tickAnimations());
        animLoop.start();
    }

    private void tickAnimations() {
        boolean dirty = false;

        if (state.cellAnim != null && state.cellAnimState != null) {
            for (int i = 0; i < state.cellAnim.length; i++) {
                float target = (state.cellAnimState[i] == 2 || state.cellAnimState[i] == 3) ? 1f : 0f;
                float diff   = target - state.cellAnim[i];
                if (Math.abs(diff) > 0.002f) { state.cellAnim[i] += diff * 0.18f; dirty = true; }
                else if (state.cellAnim[i] != target) { state.cellAnim[i] = target; dirty = true; }
            }
        }

        for (String k : new ArrayList<>(state.nodeAnim.keySet())) {
            float v = state.nodeAnim.get(k);
            if (v < 1f) { state.nodeAnim.put(k, Math.min(1f, v + 0.14f)); dirty = true; }
        }

        if (state.captionPhase == 1) {
            state.captionProgress += 0.07f;
            if (state.captionProgress >= 1f) { state.captionProgress = 1f; state.captionPhase = 2; }
            dirty = true;
        } else if (state.captionPhase == 2) {
            state.captionHoldTicks++;
            if (state.captionHoldTicks >= state.captionHoldMax) { state.captionPhase = 3; state.captionProgress = 1f; }
            dirty = true;
        } else if (state.captionPhase == 3) {
            state.captionProgress -= 0.05f;
            if (state.captionProgress <= 0f) {
                state.captionProgress = 0f; state.captionPhase = 0;
                if (state.captionQueued != null) { pushCaption(state.captionQueued); state.captionQueued = null; }
            }
            dirty = true;
        }

        if (dirty) viz.repaint();
    }

    private void pushCaption(String text) {
        state.captionText      = text;
        state.captionPhase     = 1;
        state.captionProgress  = 0f;
        state.captionHoldTicks = 0;
        state.captionHoldMax   = Math.max(6, state.stepDelay / 20);
    }

    @Override public void showCaption(String text) {
        if (!state.showCaptions) return;
        if (state.captionPhase == 0 || state.captionPhase == 3) {
            state.captionQueued = null;
            SwingUtilities.invokeLater(() -> pushCaption(text));
        } else {
            state.captionQueued = text;
            SwingUtilities.invokeLater(() -> { if (state.captionPhase == 2) { state.captionPhase = 3; state.captionProgress = 1f; } });
        }
    }

    @Override public void setCellActive(int i) {
        if (state.cellStates == null || i < 0 || i >= state.cellStates.length) return;
        state.cellStates[i] = 2;
        if (state.cellAnimState != null && i < state.cellAnimState.length) state.cellAnimState[i] = 2;
    }
    @Override public void setCellFound(int i) {
        if (state.cellStates == null || i < 0 || i >= state.cellStates.length) return;
        state.cellStates[i] = 3;
        if (state.cellAnimState != null && i < state.cellAnimState.length) state.cellAnimState[i] = 3;
    }
    @Override public void setCellScanned(int i) {
        if (state.cellStates == null || i < 0 || i >= state.cellStates.length) return;
        state.cellStates[i] = 1;
        if (state.cellAnimState != null && i < state.cellAnimState.length) state.cellAnimState[i] = 1;
    }
    @Override public void markScanned(int from, int to) {
        if (state.cellStates == null) return;
        for (int i = Math.max(0,from); i <= Math.min(to, state.cellStates.length-1); i++) {
            if (state.cellStates[i] != 3) setCellScanned(i);
        }
    }
    @Override public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }
    @Override public void repaint() {
        viz.repaint();
    }
    @Override public void sleep() throws InterruptedException {
        Thread.sleep(state.stepDelay);
    }
    @Override public int compareValues(Object a, Object b) {
        if (a instanceof Number && b instanceof Number)
            return Double.compare(((Number)a).doubleValue(), ((Number)b).doubleValue());
        if (a instanceof String && b instanceof String)
            return state.ignoreCase ? ((String)a).compareToIgnoreCase((String)b) : ((String)a).compareTo((String)b);
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(PANEL);
        side.setPreferredSize(new Dimension(232, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JLabel title = new JLabel("  searchviz");
        title.setFont(TITLE); title.setForeground(ACCENT);
        title.setBorder(new EmptyBorder(24, 16, 20, 16));
        side.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setBackground(PANEL);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(0, 0, 16, 0));
        addCategory(list, "ARRAY BASED",   AnimState.ARRAY_ALGOS);
        addCategory(list, "GRAPH / TREE",  AnimState.GRAPH_ALGOS);
        addCategory(list, "STRING / TEXT", AnimState.STRING_ALGOS);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null); scroll.getViewport().setBackground(PANEL);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        side.add(scroll, BorderLayout.CENTER);
        return side;
    }

    private void addCategory(JPanel parent, String label, String[] algos) {
        JLabel cat = new JLabel(label);
        cat.setFont(SMALL); cat.setForeground(TEXT_HINT);
        cat.setBorder(new EmptyBorder(14, 18, 6, 0));
        cat.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(cat);
        for (String algo : algos) {
            AlgoButton btn = new AlgoButton(algo);
            btn.setAlignmentX(LEFT_ALIGNMENT);
            btn.addActionListener(e -> { state.selectedAlgo = algo; algoLabel.setText(algo); refreshInputPanel(); });
            parent.add(btn);
        }
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(buildTopBar(),    BorderLayout.NORTH);
        main.add(buildCenter(),    BorderLayout.CENTER);
        main.add(buildBottomBar(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(12, 24, 12, 24)));

        algoLabel = new JLabel(state.selectedAlgo);
        algoLabel.setFont(SANS_B); algoLabel.setForeground(TEXT);
        bar.add(algoLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        captionToggle = styledCheckbox("captions");
        captionToggle.setSelected(true);
        captionToggle.addActionListener(e -> state.showCaptions = captionToggle.isSelected());

        JButton codeBtn = actionButton("code", false);
        codeBtn.addActionListener(e -> showCodeDialog());

        JButton appsBtn = actionButton("applications", false);
        appsBtn.addActionListener(e -> showApplicationsDialog());

        JLabel sl = new JLabel("speed"); sl.setFont(SMALL); sl.setForeground(TEXT_DIM);
        speedSlider = new JSlider(1, 5, 3);
        speedSlider.setBackground(PANEL); speedSlider.setPreferredSize(new Dimension(100, 24));
        speedSlider.addChangeListener(e -> {
            int[] d = {700, 450, 280, 130, 50};
            state.stepDelay = d[speedSlider.getValue() - 1];
        });

        right.add(captionToggle); right.add(codeBtn); right.add(appsBtn);
        right.add(sl); right.add(speedSlider);
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
        inputPanel.add(buildArrayInput(),  "array");
        inputPanel.add(buildGraphInput(),  "graph");
        inputPanel.add(buildStringInput(), "string");
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
        JPanel p = roundPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JTextField arrField = styledField(22); arrField.setText("2, 5, 8, 11, 14, 19, 27, 33, 45");
        JTextField tgtField = styledField(6);  tgtField.setText("19");
        JCheckBox icBox = styledCheckbox("ignore case");

        Runnable apply = () -> parseArrayInput(arrField.getText(), tgtField.getText(), icBox.isSelected());
        addLiveListener(arrField, apply); addLiveListener(tgtField, apply);
        icBox.addActionListener(e -> apply.run());

        p.add(dimLabel("array:")); p.add(arrField);
        p.add(dimLabel("target:")); p.add(tgtField);
        p.add(icBox);
        return p;
    }

    private JPanel buildGraphInput() {
        JPanel p = roundPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JTextField sField = styledField(3); sField.setText("A");
        JTextField tField = styledField(3); tField.setText("C");
        JSpinner depthSpin  = styledSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        JSpinner branchSpin = styledSpinner(new SpinnerNumberModel(2, 2, 4, 1));

        Runnable apply = () -> {
            state.graphStart  = sField.getText().trim().isEmpty() ? "A" : sField.getText().trim().toUpperCase();
            state.graphTarget = tField.getText().trim().isEmpty() ? "C" : tField.getText().trim().toUpperCase();
            state.graphDepth  = (Integer) depthSpin.getValue();
            state.graphBranch = (Integer) branchSpin.getValue();
            buildTreeGraph(); resetVisuals();
        };
        addLiveListener(sField, apply); addLiveListener(tField, apply);
        depthSpin.addChangeListener(e -> apply.run());
        branchSpin.addChangeListener(e -> apply.run());

        p.add(dimLabel("start:")); p.add(sField);
        p.add(dimLabel("target:")); p.add(tField);
        p.add(dimLabel("depth:")); p.add(depthSpin);
        p.add(dimLabel("branches:")); p.add(branchSpin);
        return p;
    }

    private JPanel buildStringInput() {
        JPanel p = roundPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JTextField txtField = styledField(28); txtField.setText("the cat sat on the caterpillar");
        JTextField patField = styledField(10); patField.setText("cat");
        JCheckBox icBox = styledCheckbox("ignore case");

        Runnable apply = () -> {
            state.currentText    = txtField.getText();
            state.currentPattern = patField.getText();
            state.ignoreCase     = icBox.isSelected();
            resetVisuals();
        };
        addLiveListener(txtField, apply); addLiveListener(patField, apply);
        icBox.addActionListener(e -> apply.run());

        p.add(dimLabel("text:")); p.add(txtField);
        p.add(dimLabel("pattern:")); p.add(patField);
        p.add(icBox);
        return p;
    }

    private void addLiveListener(JTextField field, Runnable onChange) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(onChange); }
            public void removeUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(onChange); }
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(onChange); }
        });
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(12, 24, 12, 24)));

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        stats.setOpaque(false);
        cmpLabel    = monoLabel("comparisons: —");
        timeLabel   = monoLabel("time: —");
        resultLabel = monoLabel("result: —");
        statusLabel = monoLabel("configure and press run");
        stats.add(cmpLabel); stats.add(timeLabel); stats.add(resultLabel); stats.add(statusLabel);
        bar.add(stats, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        resetBtn = actionButton("reset", false);
        runBtn   = actionButton("run",   true);
        resetBtn.addActionListener(e -> resetVisuals());
        runBtn.addActionListener(e -> { if (!state.running) runAlgorithm(); });
        btns.add(resetBtn); btns.add(runBtn);
        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    private void runAlgorithm() {
        state.running = true;
        runBtn.setEnabled(false);
        state.resetStats();

        new Thread(() -> {
            try {
                if      (state.isArrayAlgo())  { runner.runArrayAlgo();  updateStats(); }
                else if (state.isGraphAlgo())  { runner.runGraphAlgo();  updateStats(); }
                else                           { runner.runStringAlgo(); updateStats(); }
            } catch (InterruptedException ignored) {
            } catch (Exception ex) { ex.printStackTrace(); }
            finally {
                state.running = false;
                SwingUtilities.invokeLater(() -> runBtn.setEnabled(true));
            }
        }).start();
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            cmpLabel.setText("comparisons: " + state.comparisons);
            timeLabel.setText("time: " + (state.elapsedNs > 0 ? state.elapsedNs + " ns" : "—"));
            resultLabel.setText(state.resultIndex == -1 ? "result: not found"
                              : state.resultIndex >= 0  ? "result: index " + state.resultIndex : "result: —");
        });
    }

    private void resetVisuals() {
        if (state.running) return;
        if (viz == null) return;
        state.resetStats();
        state.resetCaption();
        if (state.isArrayAlgo())       state.resetArrayState();
        else if (state.isStringAlgo()) state.resetStringState();
        else                           state.resetGraphState();
        if (statusLabel != null) statusLabel.setText("ready — press run");
        if (cmpLabel    != null) cmpLabel.setText("comparisons: —");
        if (timeLabel   != null) timeLabel.setText("time: —");
        if (resultLabel != null) resultLabel.setText("result: —");
        viz.revalidate(); viz.repaint();
    }

    private void refreshInputPanel() {
        if (state.isArrayAlgo())      inputCards.show(inputPanel, "array");
        else if (state.isGraphAlgo()) inputCards.show(inputPanel, "graph");
        else                          inputCards.show(inputPanel, "string");
        resetVisuals();
    }

    private void parseArrayInput(String arrText, String tgtText, boolean ic) {
        if (state.running) return;
        try {
            String[] parts = arrText.split(",");
            if (parts.length == 0) return;
            Object[] arr = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String s = parts[i].trim(); if (s.isEmpty()) return;
                try { arr[i] = Integer.parseInt(s); }
                catch (NumberFormatException e1) {
                    try { arr[i] = Double.parseDouble(s); }
                    catch (NumberFormatException e2) { arr[i] = s; }
                }
            }
            state.currentArray = arr;
            String tgt = tgtText.trim();
            if (!tgt.isEmpty()) {
                try { state.currentTarget = Integer.parseInt(tgt); }
                catch (NumberFormatException e1) {
                    try { state.currentTarget = Double.parseDouble(tgt); }
                    catch (NumberFormatException e2) { state.currentTarget = tgt; }
                }
            }
            state.ignoreCase = ic;
            resetVisuals();
        } catch (Exception ignored) {}
    }

    private void buildTreeGraph() {
        state.currentGraph = new LinkedHashMap<>();
        int[] counter = {0};
        buildNode(null, state.graphDepth, state.graphBranch, counter);
        List<String> nodes = new ArrayList<>(state.currentGraph.keySet());
        if (!nodes.isEmpty() && !state.currentGraph.containsKey(state.graphStart))  state.graphStart  = nodes.get(0);
        if (nodes.size() > 1 && !state.currentGraph.containsKey(state.graphTarget)) state.graphTarget = nodes.get(nodes.size()-1);
    }

    private String buildNode(String parent, int depth, int branch, int[] counter) {
        String name = nodeLabel(counter[0]++);
        state.currentGraph.put(name, new ArrayList<>());
        if (parent != null) state.currentGraph.get(parent).add(name);
        if (depth > 1) for (int i = 0; i < branch; i++) buildNode(name, depth-1, branch, counter);
        return name;
    }

    private String nodeLabel(int n) {
        StringBuilder sb = new StringBuilder();
        do { sb.insert(0, (char)('A' + n % 26)); n = n/26 - 1; } while (n >= 0);
        return sb.toString();
    }

    private void showCodeDialog() {
        JDialog dlg = new JDialog(this, state.selectedAlgo + " — source code", false);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        JTextArea area = new JTextArea(AppData.CODE_MAP.getOrDefault(state.selectedAlgo, "// not available"));
        area.setFont(MONO);
        area.setBackground(new Color(13, 15, 20)); area.setForeground(new Color(190, 205, 225));
        area.setCaretColor(ACCENT); area.setEditable(false); area.setLineWrap(false);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));
        area.setSelectionColor(new Color(50, 80, 140));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(new Color(13, 15, 20));
        scroll.setPreferredSize(new Dimension(660, 400));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(PANEL);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(12, 20, 12, 20)));
        JLabel ht = new JLabel(state.selectedAlgo); ht.setFont(SANS_B); ht.setForeground(TEXT);
        JLabel hc = new JLabel("time: " + timeComplexity() + "   space: " + spaceComplexity());
        hc.setFont(SMALL); hc.setForeground(TEXT_DIM);
        hdr.add(ht, BorderLayout.WEST); hdr.add(hc, BorderLayout.EAST);
        dlg.add(hdr, BorderLayout.NORTH); dlg.add(scroll, BorderLayout.CENTER);
        dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private void showApplicationsDialog() {
        JDialog dlg = new JDialog(this, "Search algorithms — real-world applications", false);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());
        dlg.setPreferredSize(new Dimension(880, 700));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(PANEL);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(14, 24, 14, 24)));
        JLabel ht = new JLabel("When to use each algorithm"); ht.setFont(SANS_B); ht.setForeground(TEXT);
        JLabel hs = new JLabel("time  |  space  |  best use cases  |  real-world examples");
        hs.setFont(SMALL); hs.setForeground(TEXT_DIM);
        hdr.add(ht, BorderLayout.WEST); hdr.add(hs, BorderLayout.EAST);
        dlg.add(hdr, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setBackground(BG); grid.setBorder(new EmptyBorder(16, 20, 16, 20));
        for (String[] row : AppData.APP_DATA) grid.add(buildAppCard(row));
        if (AppData.APP_DATA.length % 2 != 0) { JPanel empty = new JPanel(); empty.setOpaque(false); grid.add(empty); }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null); scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    private JPanel buildAppCard(String[] row) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(14, 16, 14, 16)));

        JPanel top = new JPanel(new BorderLayout(8, 0)); top.setOpaque(false);
        JLabel name = new JLabel(row[0]); name.setFont(SANS_B); name.setForeground(TEXT);
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); badges.setOpaque(false);
        badges.add(badge("T: " + row[1], ACCENT, new Color(14,22,44)));
        badges.add(badge("S: " + row[2], ACCENT2, new Color(10,30,22)));
        top.add(name, BorderLayout.WEST); top.add(badges, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel body = new JPanel(); body.setOpaque(false); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(htmlLabel("<b>Best for:</b> " + row[3], TEXT));
        body.add(Box.createVerticalStrut(4));
        body.add(htmlLabel(row[4], TEXT_DIM));
        body.add(Box.createVerticalStrut(4));
        body.add(htmlLabel("<i>" + row[5] + "</i>", new Color(90, 100, 120)));
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JLabel htmlLabel(String text, Color color) {
        JLabel l = new JLabel("<html><body style='width:360px'>" + text + "</body></html>");
        l.setFont(SMALL); l.setForeground(color); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel badge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        l.setForeground(fg); l.setOpaque(false); l.setBorder(new EmptyBorder(2, 7, 2, 7));
        return l;
    }

    private String timeComplexity() {
        if (state.selectedAlgo.equals("Linear Search"))        return "O(n)";
        if (state.selectedAlgo.equals("Binary Search"))        return "O(log n)";
        if (state.selectedAlgo.equals("Ternary Search"))       return "O(log3 n)";
        if (state.selectedAlgo.equals("Jump Search"))          return "O(sqrt n)";
        if (state.selectedAlgo.equals("Interpolation Search")) return "O(log log n)";
        if (state.selectedAlgo.equals("Exponential Search"))   return "O(log n)";
        if (state.selectedAlgo.equals("Fibonacci Search"))     return "O(log n)";
        if (state.selectedAlgo.equals("Breadth-First Search")) return "O(V + E)";
        if (state.selectedAlgo.equals("Depth-First Search"))   return "O(V + E)";
        if (state.selectedAlgo.equals("KMP Search"))           return "O(n + m)";
        if (state.selectedAlgo.equals("Rabin-Karp Search"))    return "O(n + m)";
        return "-";
    }
    private String spaceComplexity() {
        if (state.selectedAlgo.equals("Breadth-First Search") || state.selectedAlgo.equals("Depth-First Search")) return "O(V)";
        if (state.selectedAlgo.equals("KMP Search")) return "O(m)";
        return "O(1)";
    }

    private JPanel roundPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        p.setOpaque(false); return p;
    }

    private JLabel dimLabel(String text)  { JLabel l = new JLabel(text); l.setFont(SMALL); l.setForeground(TEXT_DIM); return l; }
    private JLabel monoLabel(String text) { JLabel l = new JLabel(text); l.setFont(MONO);  l.setForeground(TEXT_DIM); return l; }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(new Color(14,16,21)); f.setForeground(TEXT);
        f.setCaretColor(ACCENT); f.setFont(MONO);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(4,8,4,8)));
        return f;
    }

    private JSpinner styledSpinner(SpinnerNumberModel model) {
        JSpinner s = new JSpinner(model);
        s.setPreferredSize(new Dimension(54, 28));
        JComponent ed = s.getEditor();
        if (ed instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)ed).getTextField();
            tf.setBackground(new Color(14,16,21)); tf.setForeground(TEXT);
            tf.setFont(MONO); tf.setCaretColor(ACCENT); tf.setBorder(new EmptyBorder(2,4,2,4));
        }
        return s;
    }

    private JCheckBox styledCheckbox(String label) {
        JCheckBox cb = new JCheckBox(label);
        cb.setFont(SMALL); cb.setForeground(TEXT_DIM); cb.setOpaque(false); cb.setFocusPainted(false);
        return cb;
    }

    private JButton actionButton(String label, boolean primary) {
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primary ? ACCENT : CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        b.setFont(SANS_B); b.setForeground(primary ? Color.WHITE : TEXT_DIM);
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int w = label.length() >= 10 ? 120 : label.length() >= 6 ? 100 : 80;
        b.setPreferredSize(new Dimension(w, 34));
        return b;
    }

    class AlgoButton extends JButton {
        private boolean hovered = false;
        AlgoButton(String name) {
            super("  " + name);
            setFont(SANS); setForeground(TEXT_DIM);
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(232, 32)); setPreferredSize(new Dimension(232, 32));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered=true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered=false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean active = getText().trim().equals(state.selectedAlgo);
            if (active) {
                g2.setColor(new Color(28,38,70)); g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,6,6);
                g2.setColor(ACCENT); g2.fillRoundRect(8,2,3,getHeight()-4,2,2);
            } else if (hovered) {
                g2.setColor(new Color(24,27,38)); g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,6,6);
            }
            setForeground(active ? ACCENT : hovered ? TEXT : TEXT_DIM);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AlgorithmGUI::new);
    }
}