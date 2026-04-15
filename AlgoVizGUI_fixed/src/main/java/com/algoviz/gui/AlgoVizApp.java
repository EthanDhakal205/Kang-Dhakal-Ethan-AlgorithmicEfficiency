package com.algoviz.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import com.algoviz.algorithms.searching.BinarySearch;
import com.algoviz.algorithms.searching.ExponentialSearch;
import com.algoviz.algorithms.searching.FibonacciSearch;
import com.algoviz.algorithms.searching.InterpolationSearch;
import com.algoviz.algorithms.searching.JumpSearch;
import com.algoviz.algorithms.searching.LinearSearch;
import com.algoviz.algorithms.searching.TernarySearch;
import com.algoviz.algorithms.sorting.BogoSort;
import com.algoviz.algorithms.sorting.BubbleSort;
import com.algoviz.algorithms.sorting.GnomeSort;
import com.algoviz.algorithms.sorting.HeapSort;
import com.algoviz.algorithms.sorting.InsertionSort;
import com.algoviz.algorithms.sorting.MergeSort;
import com.algoviz.algorithms.sorting.QuickSort;
import com.algoviz.algorithms.sorting.RadixSort;
import com.algoviz.algorithms.sorting.SelectionSort;
import com.algoviz.algorithms.sorting.TimSort;
import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;

public class AlgoVizApp extends JFrame {

    private enum WorkspaceMode {
        VISUALIZER,
        PRACTICE
    }

    private final SidebarPanel sidebar;
    private final TopbarPanel topbar;
    private final ControlsPanel controls;
    private final SpeedControlPanel speedControl;
    private final BarChartPanel barChart;
    private final StatsPanel stats;
    private final PlaybackPanel playback;
    private final InfoPanel info;
    private final PracticePanel practice;
    private final JPanel workspaceCards;
    private final ControlsPanel.StyledButton visualizerModeButton;
    private final ControlsPanel.StyledButton practiceModeButton;

    private WorkspaceMode activeMode = WorkspaceMode.VISUALIZER;
    private List<AlgoStep> steps = new ArrayList<>();
    private int currentStep = 0;
    private boolean playing = false;
    private Timer playTimer;

    private record AlgoMeta(String name, String cat, boolean stable, boolean inPlace,
                            String best, String avg, String worst, String space, String desc) {
    }

    private static final Map<String, AlgoMeta> META = new LinkedHashMap<>();

    static {
        META.put("bubble", new AlgoMeta("Bubble Sort", "sort", true, true,
                "O(n)", "O(n^2)", "O(n^2)", "O(1)",
                "Repeatedly compares adjacent values and swaps them when they are out of order."));
        META.put("insertion", new AlgoMeta("Insertion Sort", "sort", true, true,
                "O(n)", "O(n^2)", "O(n^2)", "O(1)",
                "Builds a sorted prefix one item at a time. Strong on small or nearly sorted arrays."));
        META.put("selection", new AlgoMeta("Selection Sort", "sort", false, true,
                "O(n^2)", "O(n^2)", "O(n^2)", "O(1)",
                "Finds the minimum remaining value on each pass and places it at the front."));
        META.put("gnome", new AlgoMeta("Gnome Sort", "sort", true, true,
                "O(n)", "O(n^2)", "O(n^2)", "O(1)",
                "Walks forward and backward through the array, swapping adjacent values when needed."));
        META.put("bogo", new AlgoMeta("Bogo Sort", "sort", false, true,
                "O(n)", "O((n+1)!)", "O(infinity)", "O(1)",
                "Randomly shuffles until the data happens to be sorted. Included only as a cautionary example."));
        META.put("merge", new AlgoMeta("Merge Sort", "sort", true, false,
                "O(n log n)", "O(n log n)", "O(n log n)", "O(n)",
                "Splits the array recursively and merges sorted halves back together."));
        META.put("quick", new AlgoMeta("Quick Sort", "sort", false, true,
                "O(n log n)", "O(n log n)", "O(n^2)", "O(log n)",
                "Partitions around a pivot and sorts each side recursively. Fast in practice."));
        META.put("heap", new AlgoMeta("Heap Sort", "sort", false, true,
                "O(n log n)", "O(n log n)", "O(n log n)", "O(1)",
                "Builds a heap and repeatedly removes the maximum element."));
        META.put("tim", new AlgoMeta("Tim Sort", "sort", true, false,
                "O(n)", "O(n log n)", "O(n log n)", "O(n)",
                "Hybrid of merge and insertion sort used widely in production runtimes."));
        META.put("radix", new AlgoMeta("Radix Sort", "sort", true, false,
                "O(n)", "O(nk)", "O(nk)", "O(n+k)",
                "Non-comparison sort that groups values by digit positions."));
        META.put("linear", new AlgoMeta("Linear Search", "search", true, true,
                "O(1)", "O(n)", "O(n)", "O(1)",
                "Checks every element in order. Works on unsorted input."));
        META.put("binary", new AlgoMeta("Binary Search", "search", true, true,
                "O(1)", "O(log n)", "O(log n)", "O(1)",
                "Halves the search space each comparison. Requires sorted data."));
        META.put("jump", new AlgoMeta("Jump Search", "search", true, true,
                "O(1)", "O(sqrt n)", "O(sqrt n)", "O(1)",
                "Jumps ahead in blocks and then scans the likely range."));
        META.put("interpolation", new AlgoMeta("Interpolation Search", "search", true, true,
                "O(1)", "O(log log n)", "O(n)", "O(1)",
                "Estimates the target position by value and works best on uniform data."));
        META.put("exponential", new AlgoMeta("Exponential Search", "search", true, true,
                "O(1)", "O(log n)", "O(log n)", "O(1)",
                "Expands the search window exponentially before using binary search."));
        META.put("fibonacci", new AlgoMeta("Fibonacci Search", "search", true, true,
                "O(1)", "O(log n)", "O(log n)", "O(1)",
                "Uses Fibonacci offsets instead of midpoint division."));
        META.put("ternary", new AlgoMeta("Ternary Search", "search", true, true,
                "O(1)", "O(log_3 n)", "O(log_3 n)", "O(1)",
                "Splits the search range into three sections using two midpoints."));
    }

    public AlgoVizApp() {
        super("AlgoViz - Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 750));
        setPreferredSize(new Dimension(1280, 820));
        getContentPane().setBackground(Theme.BG);

        sidebar = new SidebarPanel(this::onAlgoSelected);
        topbar = new TopbarPanel();
        controls = new ControlsPanel();
        speedControl = new SpeedControlPanel();
        barChart = new BarChartPanel();
        stats = new StatsPanel();
        playback = new PlaybackPanel(new PlaybackPanel.PlaybackListener() {
            @Override public void onFirst() { goFirst(); }
            @Override public void onPrev() { stepBack(); }
            @Override public void onPlayPause() { togglePlay(); }
            @Override public void onNext() { stepForward(); }
            @Override public void onLast() { goLast(); }
        });
        info = new InfoPanel();
        practice = new PracticePanel();
        visualizerModeButton = createModeButton("VISUALIZER", Theme.ACCENT, WorkspaceMode.VISUALIZER);
        practiceModeButton = createModeButton("PRACTICE LAB", Theme.ACCENT5, WorkspaceMode.PRACTICE);
        workspaceCards = buildWorkspaceCards();

        controls.setListener(new ControlsPanel.ControlListener() {
            @Override public void onRun(String array, int target) { run(array, target); }
            @Override public void onStep() {
                if (steps.isEmpty()) {
                    run(controls.getArrayText(), controls.getTarget());
                } else {
                    stepForward();
                }
            }
            @Override public void onRandom() { generateRandom(); }
            @Override public void onReset() { reset(); }
        });

        JScrollPane sidebarScroll = new JScrollPane(sidebar,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setBorder(null);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebarScroll.setBackground(Theme.BG2);
        sidebarScroll.getViewport().setBackground(Theme.BG2);

        JPanel mainArea = buildMainArea();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarScroll, mainArea);
        splitPane.setDividerSize(1);
        splitPane.setDividerLocation(Theme.SIDEBAR_W);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);
        splitPane.setBackground(Theme.BG2);

        setContentPane(splitPane);
        setupKeyBindings();

        updateTopbar("bubble");
        info.update("bubble", "sort", META.get("bubble").desc());
        practice.setAlgorithm("bubble");
        switchMode(WorkspaceMode.VISUALIZER);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);

        JPanel headerStack = new JPanel();
        headerStack.setBackground(Theme.BG);
        headerStack.setLayout(new BoxLayout(headerStack, BoxLayout.Y_AXIS));

        topbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        topbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.TOPBAR_H));
        headerStack.add(topbar);
        headerStack.add(buildModeBar());

        main.add(headerStack, BorderLayout.NORTH);
        main.add(workspaceCards, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildModeBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG3);
        bar.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel label = new JLabel("VIEW");
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT2);
        left.add(label);
        left.add(visualizerModeButton);
        left.add(practiceModeButton);
        bar.add(left, BorderLayout.WEST);

        JLabel hint = new JLabel("Practice Lab opens a full compiler workspace");
        hint.setFont(Theme.FONT_LABEL);
        hint.setForeground(Theme.TEXT3);
        bar.add(hint, BorderLayout.EAST);
        return bar;
    }

    private ControlsPanel.StyledButton createModeButton(String text, Color accent, WorkspaceMode mode) {
        ControlsPanel.StyledButton button = new ControlsPanel.StyledButton(text, accent, Theme.BG3);
        button.setPreferredSize(new Dimension(text.equals("PRACTICE LAB") ? 150 : 124, 32));
        button.addActionListener(e -> switchMode(mode));
        return button;
    }

    private JPanel buildWorkspaceCards() {
        JPanel cards = new JPanel(new CardLayout());
        cards.setBackground(Theme.BG);
        cards.add(buildVisualizerWorkspace(), WorkspaceMode.VISUALIZER.name());
        cards.add(buildPracticeWorkspace(), WorkspaceMode.PRACTICE.name());
        return cards;
    }

    private JPanel buildVisualizerWorkspace() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        speedControl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(speedControl);

        controls.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(controls);

        barChart.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setBackground(Theme.BG);
        chartWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        chartWrapper.add(barChart, BorderLayout.CENTER);
        chartWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(chartWrapper);

        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setBackground(Theme.BG);
        statsWrapper.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        statsWrapper.add(stats, BorderLayout.CENTER);
        statsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statsWrapper);

        playback.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(playback);

        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(info);
        return panel;
    }

    private JPanel buildPracticeWorkspace() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(practice, BorderLayout.CENTER);
        return panel;
    }

    private void switchMode(WorkspaceMode mode) {
        activeMode = mode;
        if (mode == WorkspaceMode.PRACTICE) {
            stopPlay();
        }

        CardLayout layout = (CardLayout) workspaceCards.getLayout();
        layout.show(workspaceCards, mode.name());
        setModeButtonState(visualizerModeButton, Theme.ACCENT, mode == WorkspaceMode.VISUALIZER);
        setModeButtonState(practiceModeButton, Theme.ACCENT5, mode == WorkspaceMode.PRACTICE);

        if (mode == WorkspaceMode.PRACTICE) {
            practice.focusEditor();
        }

        workspaceCards.revalidate();
        workspaceCards.repaint();
    }

    private void setModeButtonState(ControlsPanel.StyledButton button, Color accent, boolean selected) {
        button.setForeground(selected ? Theme.BG : accent);
    }

    private void onAlgoSelected(String id, String cat) {
        reset();
        updateTopbar(id);
        controls.setSearchMode(cat.equals("search"));
        AlgoMeta meta = META.get(id);
        if (meta != null) {
            info.update(id, cat, meta.desc());
        }
        practice.setAlgorithm(id);
        if (activeMode == WorkspaceMode.VISUALIZER) {
            run(controls.getArrayText(), controls.getTarget());
        }
    }

    private void run(String arrayStr, int target) {
        stopPlay();
        int[] arr = parseArray(arrayStr);
        if (arr == null || arr.length == 0) {
            showError("Invalid array. Use comma-separated integers, for example 64,34,25,12");
            return;
        }

        String id = sidebar.getSelectedId();
        AlgoResult result = dispatch(id, arr, target);
        if (result == null) {
            return;
        }

        steps = result.getSteps();
        currentStep = 0;
        renderStep(0);
        startPlay();
    }

    private AlgoResult dispatch(String id, int[] arr, int target) {
        return switch (id) {
            case "bubble" -> BubbleSort.sort(arr);
            case "insertion" -> InsertionSort.sort(arr);
            case "selection" -> SelectionSort.sort(arr);
            case "gnome" -> GnomeSort.sort(arr);
            case "bogo" -> BogoSort.sort(arr);
            case "merge" -> MergeSort.sort(arr);
            case "quick" -> QuickSort.sort(arr);
            case "heap" -> HeapSort.sort(arr);
            case "tim" -> TimSort.sort(arr);
            case "radix" -> RadixSort.sort(arr);
            case "linear" -> LinearSearch.search(arr, target);
            case "binary" -> BinarySearch.search(arr, target);
            case "jump" -> JumpSearch.search(arr, target);
            case "interpolation" -> InterpolationSearch.search(arr, target);
            case "exponential" -> ExponentialSearch.search(arr, target);
            case "fibonacci" -> FibonacciSearch.search(arr, target);
            case "ternary" -> TernarySearch.search(arr, target);
            default -> null;
        };
    }

    private void startPlay() {
        if (steps.isEmpty()) {
            return;
        }
        if (currentStep >= steps.size() - 1) {
            currentStep = 0;
        }

        playing = true;
        playback.setPlaying(true);

        int baseDelayMs = 700;
        int delay = (int) Math.max(25, Math.round(baseDelayMs / speedControl.getSpeedMultiplier()));

        playTimer = new Timer(delay, e -> {
            if (!playing || steps.isEmpty()) {
                stopPlay();
                return;
            }
            if (currentStep >= steps.size() - 1) {
                stopPlay();
                return;
            }
            currentStep++;
            renderStep(currentStep);
        });
        playTimer.start();
    }

    private void stopPlay() {
        playing = false;
        if (playTimer != null) {
            playTimer.stop();
        }
        playback.setPlaying(false);
    }

    private void togglePlay() {
        if (playing) {
            stopPlay();
        } else if (steps.isEmpty()) {
            run(controls.getArrayText(), controls.getTarget());
        } else {
            startPlay();
        }
    }

    private void stepForward() {
        stopPlay();
        if (steps.isEmpty()) {
            run(controls.getArrayText(), controls.getTarget());
            return;
        }
        if (currentStep < steps.size() - 1) {
            currentStep++;
            renderStep(currentStep);
        }
    }

    private void stepBack() {
        stopPlay();
        if (currentStep > 0) {
            currentStep--;
            renderStep(currentStep);
        }
    }

    private void goFirst() {
        stopPlay();
        if (!steps.isEmpty()) {
            currentStep = 0;
            renderStep(0);
        }
    }

    private void goLast() {
        stopPlay();
        if (!steps.isEmpty()) {
            currentStep = steps.size() - 1;
            renderStep(currentStep);
        }
    }

    private void renderStep(int idx) {
        if (steps.isEmpty() || idx < 0 || idx >= steps.size()) {
            return;
        }
        AlgoStep step = steps.get(idx);

        SwingUtilities.invokeLater(() -> {
            barChart.setStep(step);
            stats.update(step.getComparisons(), step.getSwaps(), idx, steps.size() - 1, step.getDescription());
        });
    }

    private void reset() {
        stopPlay();
        steps.clear();
        currentStep = 0;
        barChart.reset();
        stats.reset();
    }

    private void generateRandom() {
        String id = sidebar.getSelectedId();
        int size = id.equals("bogo") ? 6 : 14;
        Random rng = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(rng.nextInt(95) + 5);
        }
        controls.setArrayText(builder.toString());
        reset();
    }

    private void updateTopbar(String id) {
        AlgoMeta meta = META.get(id);
        if (meta == null) {
            return;
        }
        topbar.update(meta.name(), meta.cat(), meta.stable(), meta.inPlace(),
                meta.best(), meta.avg(), meta.worst(), meta.space());
    }

    private void setupKeyBindings() {
        JComponent root = (JComponent) getContentPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "playPause");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "stepFwd");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "stepBack");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "first");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "last");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "random");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "run");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), "showPractice");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "showVisualizer");

        actionMap.put("playPause", action(e -> { if (allowVisualizerHotkeys()) togglePlay(); }));
        actionMap.put("stepFwd", action(e -> { if (allowVisualizerHotkeys()) stepForward(); }));
        actionMap.put("stepBack", action(e -> { if (allowVisualizerHotkeys()) stepBack(); }));
        actionMap.put("first", action(e -> { if (allowVisualizerHotkeys()) goFirst(); }));
        actionMap.put("last", action(e -> { if (allowVisualizerHotkeys()) goLast(); }));
        actionMap.put("random", action(e -> {
            if (allowVisualizerHotkeys()) {
                generateRandom();
                run(controls.getArrayText(), controls.getTarget());
            }
        }));
        actionMap.put("run", action(e -> { if (allowVisualizerHotkeys()) run(controls.getArrayText(), controls.getTarget()); }));
        actionMap.put("showPractice", action(e -> switchMode(WorkspaceMode.PRACTICE)));
        actionMap.put("showVisualizer", action(e -> switchMode(WorkspaceMode.VISUALIZER)));
    }

    private boolean allowVisualizerHotkeys() {
        if (activeMode != WorkspaceMode.VISUALIZER) {
            return false;
        }
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return !(focusOwner instanceof JTextComponent);
    }

    private AbstractAction action(java.util.function.Consumer<ActionEvent> handler) {
        return new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { handler.accept(e); }
        };
    }

    private int[] parseArray(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            String[] parts = input.split(",");
            int[] arr = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                arr[i] = Integer.parseInt(parts[i].trim());
            }
            return arr;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.put("Panel.background", Theme.BG);
            UIManager.put("OptionPane.background", Theme.BG2);
            UIManager.put("OptionPane.messageForeground", Theme.TEXT);
            UIManager.put("Button.background", Theme.SURFACE);
            UIManager.put("Button.foreground", Theme.TEXT);
            UIManager.put("TextField.background", Theme.SURFACE);
            UIManager.put("TextField.foreground", Theme.TEXT);
            UIManager.put("TextField.caretForeground", Theme.ACCENT);
            UIManager.put("SplitPane.background", Theme.BG2);
            UIManager.put("SplitPane.dividerSize", 1);
            UIManager.put("TabbedPane.background", Theme.BG3);
            UIManager.put("TabbedPane.foreground", Theme.TEXT);
            UIManager.put("TabbedPane.selectedBackground", Theme.BG2);
            UIManager.put("TabbedPane.underlineColor", Theme.ACCENT);
            UIManager.put("TabbedPane.focusColor", Theme.ACCENT);
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            AlgoVizApp app = new AlgoVizApp();
            app.setVisible(true);
        });
    }
}