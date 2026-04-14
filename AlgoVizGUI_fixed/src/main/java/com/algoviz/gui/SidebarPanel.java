package com.algoviz.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SidebarPanel extends JPanel {

    public record AlgoEntry(String id, String name, String category, String badge, Color badgeColor) {}

    private static final List<AlgoEntry> SORT_ALGOS = List.of(
        new AlgoEntry("bubble", "Bubble Sort", "sort", "O(n^2)", Theme.ACCENT3),
        new AlgoEntry("insertion", "Insertion Sort", "sort", "O(n^2)", Theme.ACCENT3),
        new AlgoEntry("selection", "Selection Sort", "sort", "O(n^2)", Theme.ACCENT3),
        new AlgoEntry("gnome", "Gnome Sort", "sort", "O(n^2)", Theme.ACCENT3),
        new AlgoEntry("bogo", "Bogo Sort", "sort", "O(infinity)", Theme.ACCENT2),
        new AlgoEntry("merge", "Merge Sort", "sort", "O(n log n)", Theme.ACCENT5),
        new AlgoEntry("quick", "Quick Sort", "sort", "O(n log n)", Theme.ACCENT5),
        new AlgoEntry("heap", "Heap Sort", "sort", "O(n log n)", Theme.ACCENT5),
        new AlgoEntry("tim", "Tim Sort", "sort", "O(n log n)", Theme.ACCENT),
        new AlgoEntry("radix", "Radix Sort", "sort", "O(nk)", Theme.ACCENT)
    );

    private static final List<AlgoEntry> SEARCH_ALGOS = List.of(
        new AlgoEntry("linear", "Linear Search", "search", "O(n)", Theme.ACCENT3),
        new AlgoEntry("binary", "Binary Search", "search", "O(log n)", Theme.ACCENT5),
        new AlgoEntry("jump", "Jump Search", "search", "O(sqrt n)", Theme.ACCENT5),
        new AlgoEntry("interpolation", "Interpolation", "search", "O(log log n)", Theme.ACCENT),
        new AlgoEntry("exponential", "Exponential", "search", "O(log n)", Theme.ACCENT5),
        new AlgoEntry("fibonacci", "Fibonacci", "search", "O(log n)", Theme.ACCENT5),
        new AlgoEntry("ternary", "Ternary Search", "search", "O(log_3 n)", Theme.ACCENT5)
    );

    private String selectedId = "bubble";
    private final BiConsumer<String, String> onSelect;
    private String hoveredId = null;
    private final List<AlgoEntry> allAlgos = new ArrayList<>();

    public SidebarPanel(BiConsumer<String, String> onSelect) {
        this.onSelect = onSelect;
        allAlgos.addAll(SORT_ALGOS);
        allAlgos.addAll(SEARCH_ALGOS);

        int contentH = 72 + 28 + (SORT_ALGOS.size() * 34) + 1 + 28 + (SEARCH_ALGOS.size() * 34) + 1 + 46;
        setPreferredSize(new Dimension(Theme.SIDEBAR_W, contentH));
        setMinimumSize(new Dimension(Theme.SIDEBAR_W, 300));
        setBackground(Theme.BG2);

        setupMouseListeners();
    }

    public String getSelectedId() {
        return selectedId;
    }

    public AlgoEntry getSelected() {
        return allAlgos.stream().filter(a -> a.id().equals(selectedId)).findFirst().orElse(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAA(g2);

        int width = getWidth();
        g2.setColor(Theme.BG2);
        g2.fillRect(0, 0, width, getHeight());

        g2.setColor(Theme.BORDER);
        g2.drawLine(width - 1, 0, width - 1, getHeight());

        int y = 0;
        y = paintLogo(g2, width, y);
        y = paintSection(g2, width, y, "SORTING", SORT_ALGOS);
        y = paintSection(g2, width, y, "SEARCHING", SEARCH_ALGOS);
        paintFooter(g2, width, y);

        g2.dispose();
    }

    private int paintLogo(Graphics2D g2, int width, int y) {
        int h = 72;
        g2.setColor(Theme.BG2);
        g2.fillRect(0, y, width, h);

        g2.setColor(Theme.BORDER);
        g2.drawLine(0, y + h - 1, width, y + h - 1);

        g2.setFont(new Font("Dialog", Font.BOLD, 28));
        g2.setColor(Theme.ACCENT);
        g2.drawString("{", 16, y + 44);

        g2.setFont(new Font("Dialog", Font.BOLD, 18));
        g2.setColor(Theme.TEXT);
        g2.drawString("ALGO", 36, y + 32);
        g2.drawString("VIZ", 36, y + 52);

        g2.setFont(new Font("Dialog", Font.BOLD, 28));
        g2.setColor(Theme.ACCENT);
        g2.drawString("}", 90, y + 44);

        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        g2.drawString("ALGORITHM VISUALIZER", 16, y + 66);

        return y + h;
    }

    private int paintSection(Graphics2D g2, int width, int y, String label, List<AlgoEntry> algos) {
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        g2.drawString(label, 16, y + 18);
        y += 28;

        for (AlgoEntry algo : algos) {
            y = paintNavItem(g2, width, y, algo);
        }

        g2.setColor(Theme.BORDER);
        g2.drawLine(0, y, width, y);
        return y + 1;
    }

    private int paintNavItem(Graphics2D g2, int width, int y, AlgoEntry algo) {
        int itemH = 34;
        boolean active = algo.id().equals(selectedId);
        boolean hovered = algo.id().equals(hoveredId);

        if (active) {
            GradientPaint gp = new GradientPaint(0, y, Theme.withAlpha(Theme.ACCENT, 30),
                    width, y, Theme.withAlpha(Theme.ACCENT, 0));
            g2.setPaint(gp);
            g2.fillRect(0, y, width, itemH);
            g2.setPaint(null);
            g2.setColor(Theme.ACCENT);
            g2.fillRect(0, y, 3, itemH);
        } else if (hovered) {
            g2.setColor(Theme.withAlpha(Color.WHITE, 12));
            g2.fillRect(0, y, width, itemH);
            g2.setColor(Theme.BORDER2);
            g2.fillRect(0, y, 3, itemH);
        }

        g2.setFont(Theme.FONT_NAV);
        if (active) {
            g2.setColor(Theme.ACCENT);
            g2.drawString("▸ " + algo.name(), 14, y + 22);
        } else {
            g2.setColor(hovered ? Theme.TEXT : Theme.TEXT2);
            g2.drawString(algo.name(), 18, y + 22);
        }

        g2.setFont(Theme.FONT_BADGE);
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(algo.badge()) + 10;
        int bx = width - bw - 10;
        int by = y + (itemH - 16) / 2;

        g2.setColor(Theme.withAlpha(algo.badgeColor(), 25));
        g2.fillRoundRect(bx, by, bw, 16, 4, 4);
        g2.setColor(Theme.withAlpha(algo.badgeColor(), 120));
        g2.drawRoundRect(bx, by, bw - 1, 15, 4, 4);
        g2.setColor(algo.badgeColor());
        g2.drawString(algo.badge(), bx + 5, by + 12);

        return y + itemH;
    }

    private void paintFooter(Graphics2D g2, int width, int startY) {
        int footerH = 46;
        int y = startY;

        g2.setColor(Theme.BORDER);
        g2.drawLine(0, y, width, y);
        g2.setColor(Theme.BG2);
        g2.fillRect(0, y, width, footerH);

        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT3);
        g2.drawString("SPACE play   LEFT/RIGHT step", 16, y + 20);
        g2.drawString("R random   CTRL+P practice", 16, y + 34);
    }

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String prev = hoveredId;
                hoveredId = hitTest(e.getX(), e.getY());
                if (!java.util.Objects.equals(prev, hoveredId)) {
                    repaint();
                }
                setCursor(Cursor.getPredefinedCursor(hoveredId != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String hit = hitTest(e.getX(), e.getY());
                if (hit != null && !hit.equals(selectedId)) {
                    selectedId = hit;
                    repaint();
                    AlgoEntry entry = allAlgos.stream().filter(a -> a.id().equals(hit)).findFirst().orElse(null);
                    if (entry != null) {
                        onSelect.accept(hit, entry.category());
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredId = null;
                repaint();
            }
        });
    }

    private String hitTest(int mx, int my) {
        int y = 72 + 28;
        for (AlgoEntry algo : SORT_ALGOS) {
            if (mx >= 0 && mx < getWidth() && my >= y && my < y + 34) {
                return algo.id();
            }
            y += 34;
        }
        y += 1 + 28;
        for (AlgoEntry algo : SEARCH_ALGOS) {
            if (mx >= 0 && mx < getWidth() && my >= y && my < y + 34) {
                return algo.id();
            }
            y += 34;
        }
        return null;
    }
}