package com.algoviz.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * InfoPanel — bottom panel showing algorithm description (left)
 * and complexity comparison table (right).
 */
public class InfoPanel extends JPanel {

    private String description = "Select an algorithm from the sidebar to learn about it.";
    private String currentId   = "bubble";
    private String category    = "sort";

    // Table data: {id, name, best, avg, worst, space}
    private static final String[][] SORT_TABLE = {
        {"bubble","Bubble","O(n)",  "O(n²)", "O(n²)","O(1)"},
        {"insertion", "Insert","O(n)", "O(n²)", "O(n²)", "O(1)"},
        {"selection", "Select",  "O(n²)", "O(n²)", "O(n²)", "O(1)"},
        {"merge", "Merge", "O(n log n)","O(n log n)", "O(n log n)","O(n)"},
        {"quick", "Quick",   "O(n log n)","O(n log n)", "O(n²)", "O(log n)"},
        {"heap", "Heap", "O(n log n)","O(n log n)", "O(n log n)","O(1)"},
        {"tim", "Tim", "O(n)", "O(n log n)", "O(n log n)","O(n)"},
        {"radix", "Radix","O(n)", "O(nk)","O(nk)", "O(n+k)"},
    };

    private static final String[][] SEARCH_TABLE = {
        {"linear", "Linear", "O(1)", "O(n)","O(n)", "O(1)"},
        {"binary",  "Binary", "O(1)", "O(log n)", "O(log n)","O(1)"},
        {"jump", "Jump",   "O(1)", "O(\u221An)",  "O(\u221An)", "O(1)"},
        {"interpolation", "Interp", "O(1)", "O(log log n)","O(n)", "O(1)"},
        {"exponential",  "Expo",  "O(1)", "O(log n)", "O(log n)","O(1)"},
        {"fibonacci", "Fib", "O(1)", "O(log n)", "O(log n)", "O(1)"},
        {"ternary", "Ternary","O(1)", "O(log\u2083n)","O(log\u2083n)","O(1)"},
    };

    private static final String[] HEADERS = {"ALGO", "BEST", "AVG", "WORST", "SPACE"};

    public InfoPanel() {
        setBackground(Theme.BG3);
        setPreferredSize(new Dimension(0, 184));
        setMinimumSize(new Dimension(0, 184));
    }

    public void update(String id, String cat, String desc) {
        this.currentId  = id;
        this.category = cat;
        this.description = desc;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAA(g2);

        int W = getWidth(), H = getHeight();

        // Background
        g2.setColor(Theme.BG3);
        g2.fillRect(0, 0, W, H);

        // Top border
        g2.setColor(Theme.BORDER);
        g2.drawLine(0, 0, W, 0);

        int halfW = W / 2;
        int pad = 14;

        //  LEFT: description 
        paintSectionHeader(g2, pad, 12, "ABOUT THIS ALGORITHM");

        g2.setFont(Theme.FONT_MONO);
        g2.setColor(Theme.TEXT2);
        drawWrappedText(g2, description, pad, 32, halfW - pad * 2, H - 36);

        // Center divider
        g2.setColor(Theme.BORDER);
        g2.drawLine(halfW, 10, halfW, H - 10);

        //  RIGHT: complexity table 
        int tx = halfW + pad;
        paintSectionHeader(g2, tx, 12, "COMPLEXITY COMPARISON");
        paintComplexityTable(g2, tx, 30, W - tx - pad, H - 36);

        g2.dispose();
    }

    private void paintSectionHeader(Graphics2D g2, int x, int y, String text) {
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        g2.drawString(text, x, y + 10);
        g2.setColor(Theme.BORDER);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawLine(x, y + 14, x + fm.stringWidth(text) + 40, y + 14);
    }

    private void paintComplexityTable(Graphics2D g2, int tx, int ty, int tw, int th) {
        String[][] data = category.equals("sort") ? SORT_TABLE : SEARCH_TABLE;
        int rows = data.length + 1; // +1 header
        int cols = 5;
        int rowH = Math.min(22, th / rows);
        int[] colW = computeColWidths(tw, data);

        // Header row
        g2.setColor(Theme.SURFACE2);
        g2.fillRoundRect(tx, ty, tw, rowH, 4, 4);

        g2.setFont(Theme.FONT_LABEL);
        int cx = tx;
        for (int c = 0; c < cols; c++) {
            g2.setColor(Theme.TEXT2);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(HEADERS[c], cx + (colW[c] - fm.stringWidth(HEADERS[c])) / 2, ty + rowH - 6);
            cx += colW[c];
        }

        // Data rows
        for (int r = 0; r < data.length; r++) {
            int ry = ty + (r + 1) * rowH;
            boolean current = data[r][0].equals(currentId);

            // Row background
            if (current) {
                g2.setColor(Theme.withAlpha(Theme.ACCENT, 18));
                g2.fillRoundRect(tx, ry, tw, rowH, 3, 3);
                g2.setColor(Theme.withAlpha(Theme.ACCENT, 50));
                g2.drawRoundRect(tx, ry, tw - 1, rowH - 1, 3, 3);
            } 
            else if (r % 2 == 0) {
                g2.setColor(Theme.withAlpha(Theme.SURFACE, 60));
                g2.fillRect(tx, ry, tw, rowH);
            }

            // Cells
            cx = tx;
            for (int c = 0; c < cols; c++) {
                String cell = c == 0 ? data[r][1] : data[r][c + 1];
                Color cellColor = current ? Theme.ACCENT : complexColor(c, data[r]);
                g2.setFont(c == 0 ? Theme.FONT_MONO_SM : Theme.FONT_MONO_SM);
                g2.setColor(cellColor);
                FontMetrics fm = g2.getFontMetrics();
                int cellX = cx + (colW[c] - fm.stringWidth(cell)) / 2;
                g2.drawString(cell, cellX, ry + rowH - 6);
                cx += colW[c];
            }
        }
    }

    private Color complexColor(int col, String[] row) {
        if (col == 0) return Theme.TEXT;
        String val = col < row.length ? row[col + 1] : "";
        if (val.contains("n²") || val.contains("n!") || val.equals("O(\u221E)")) return Theme.ACCENT3;
        if (val.contains("log") || val.contains("\u221A") || val.contains("k")) return Theme.ACCENT4;
        return Theme.ACCENT;
    }

    private int[] computeColWidths(int tw, String[][] data) {
        // Name col gets 20%, rest split evenly
        int nameW = tw / 5;
        int rest = (tw - nameW) / 4;
        return new int[]{nameW, rest, rest, rest, rest};
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxW, int maxH) {
        if (text == null || text.isEmpty()) return;
        FontMetrics fm = g2.getFontMetrics();
        int lineH = fm.getHeight() + 2;
        int curY = y + fm.getAscent();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW) {
                if (!line.isEmpty()) {
                    g2.drawString(line.toString(), x, curY);
                    curY += lineH;
                    if (curY > y + maxH) return;
                    line = new StringBuilder(word);
                } 
                else {
                    g2.drawString(word, x, curY);
                    curY += lineH;
                    if (curY > y + maxH) return;
                }
            } 
            else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty() && curY <= y + maxH) {
            g2.drawString(line.toString(), x, curY);
        }
    }
}

