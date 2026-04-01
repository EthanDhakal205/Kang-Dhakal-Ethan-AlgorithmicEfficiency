package com.algoviz.gui;

import javax.swing.*;
import java.awt.*;

/**
 * TopbarPanel — custom-painted top header bar.
 * Shows algorithm name, stable/category tags, and all 4 complexity chips.
 */
public class TopbarPanel extends JPanel {

    private String algoName     = "Bubble Sort";
    private String category     = "SORT";
    private boolean stable      = true;
    private boolean inPlace     = true;
    private String complexBest  = "O(n)";
    private String complexAvg   = "O(n²)";
    private String complexWorst = "O(n²)";
    private String complexSpace = "O(1)";

    public TopbarPanel() {
        setPreferredSize(new Dimension(0, Theme.TOPBAR_H));
        setMinimumSize(new Dimension(0, Theme.TOPBAR_H));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.TOPBAR_H));
        setBackground(Theme.BG2);
    }

    public void update(String name, String cat, boolean stable, boolean inPlace,
                       String best, String avg, String worst, String space) {
        this.algoName     = name;
        this.category     = cat.toUpperCase();
        this.stable       = stable;
        this.inPlace      = inPlace;
        this.complexBest  = best;
        this.complexAvg   = avg;
        this.complexWorst = worst;
        this.complexSpace = space;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAA(g2);

        int W = getWidth(), H = getHeight();

        // Background
        g2.setColor(Theme.BG2);
        g2.fillRect(0, 0, W, H);

        // Bottom border line
        g2.setColor(Theme.BORDER);
        g2.drawLine(0, H - 1, W, H - 1);

        int x = 20;

        // Algorithm name
        g2.setFont(Theme.FONT_TITLE);
        g2.setColor(Theme.TEXT);
        g2.drawString(algoName, x, 38);
        x += g2.getFontMetrics().stringWidth(algoName) + 14;

        // Category tag
        x = paintTag(g2, x, 22, category,
                category.equals("SORT") ? Theme.ACCENT2 : Theme.ACCENT5);
        x += 6;

        // Stable tag
        x = paintTag(g2, x, 22, stable ? "STABLE" : "UNSTABLE",
                stable ? Theme.ACCENT : Theme.ACCENT3);
        x += 6;

        // In-place tag
        paintTag(g2, x, 22, inPlace ? "IN-PLACE" : "EXTRA SPACE", Theme.ACCENT4);

        // Complexity chips — right-aligned
        int chipW = 72, chipH = 40, chipGap = 8;
        int totalChipsW = 4 * chipW + 3 * chipGap;
        int cx = W - totalChipsW - 16;

        cx = paintChip(g2, cx, 10, chipW, chipH, "BEST",  complexBest);  cx += chipGap;
        cx = paintChip(g2, cx, 10, chipW, chipH, "AVG",   complexAvg);   cx += chipGap;
        cx = paintChip(g2, cx, 10, chipW, chipH, "WORST", complexWorst); cx += chipGap;
        paintChip(g2, cx, 10, chipW, chipH, "SPACE", complexSpace);

        g2.dispose();
    }

    private int paintTag(Graphics2D g2, int x, int y, String text, Color color) {
        g2.setFont(Theme.FONT_BADGE);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text) + 12;
        int th = 18;

        g2.setColor(Theme.withAlpha(color, 35));
        g2.fillRoundRect(x, y, tw, th, 4, 4);
        g2.setColor(Theme.withAlpha(color, 100));
        g2.drawRoundRect(x, y, tw - 1, th - 1, 4, 4);
        g2.setColor(color);
        g2.drawString(text, x + 6, y + 13);

        return x + tw;
    }

    private int paintChip(Graphics2D g2, int x, int y, int w, int h, String label, String value) {
        // Background
        g2.setColor(Theme.BG3);
        g2.fillRoundRect(x, y, w, h, Theme.RADIUS, Theme.RADIUS);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(x, y, w - 1, h - 1, Theme.RADIUS, Theme.RADIUS);

        // Label
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + 14);

        // Value
        g2.setFont(Theme.FONT_MONO_BOLD);
        g2.setColor(Theme.ACCENT4);
        fm = g2.getFontMetrics();
        int vw = fm.stringWidth(value);
        // Scale font if too wide
        if (vw > w - 8) {
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
            fm = g2.getFontMetrics();
            vw = fm.stringWidth(value);
        }
        g2.drawString(value, x + (w - vw) / 2, y + 32);

        return x + w;
    }
}
