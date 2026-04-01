package com.algoviz.gui;

import javax.swing.*;
import java.awt.*;

/**
 * StatsPanel — displays live statistics: comparisons, swaps, step number,
 * total steps, progress bar, and the current step description.
 */
public class StatsPanel extends JPanel {

    private int comparisons = 0;
    private int swaps       = 0;
    private int step        = 0;
    private int total       = 0;
    private String desc     = "Select an algorithm and press  ▶ RUN  to begin.";
    private double progress = 0.0;

    public StatsPanel() {
        setBackground(Theme.BG);
        setPreferredSize(new Dimension(0, 130));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
    }

    public void update(int comparisons, int swaps, int step, int total, String desc) {
        this.comparisons = comparisons;
        this.swaps       = swaps;
        this.step        = step;
        this.total       = total;
        this.desc        = desc;
        this.progress    = total > 0 ? (double) step / total : 0;
        repaint();
    }

    public void reset() {
        comparisons = 0; swaps = 0; step = 0; total = 0;
        desc = "Configure your array and press  ▶ RUN.";
        progress = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAA(g2);

        int W = getWidth(), H = getHeight();
        g2.setColor(Theme.BG);
        g2.fillRect(0, 0, W, H);

        // ── STAT BOXES ────────────────────────────────────────────────────
        int boxCount = 4;
        int boxGap   = 10;
        int boxW     = (W - boxGap * (boxCount + 1)) / boxCount;
        int boxH     = 62;
        int boxY     = 10;
        int bx       = boxGap;

        paintStatBox(g2, bx, boxY, boxW, boxH, String.valueOf(comparisons), "COMPARISONS", Theme.ACCENT);
        bx += boxW + boxGap;
        paintStatBox(g2, bx, boxY, boxW, boxH, String.valueOf(swaps),       "SWAPS / MOVES", Theme.ACCENT5);
        bx += boxW + boxGap;
        paintStatBox(g2, bx, boxY, boxW, boxH, String.valueOf(step),        "STEP",          Theme.ACCENT4);
        bx += boxW + boxGap;
        paintStatBox(g2, bx, boxY, boxW, boxH, String.valueOf(total),       "TOTAL STEPS",   Theme.ACCENT2);

        // ── PROGRESS BAR ──────────────────────────────────────────────────
        int trackY = boxY + boxH + 8;
        int trackH = 5;
        int trackX = boxGap;
        int trackW = W - boxGap * 2;

        // Track
        g2.setColor(Theme.BG3);
        g2.fillRoundRect(trackX, trackY, trackW, trackH, 3, 3);

        // Fill
        int fillW = (int)(trackW * progress);
        if (fillW > 0) {
            GradientPaint gp = new GradientPaint(trackX, trackY, Theme.ACCENT2,
                    trackX + fillW, trackY, Theme.ACCENT);
            g2.setPaint(gp);
            g2.fillRoundRect(trackX, trackY, fillW, trackH, 3, 3);
            g2.setPaint(null);

            // Glow dot at end
            if (fillW > 6) {
                g2.setColor(Theme.ACCENT);
                g2.fillOval(trackX + fillW - 5, trackY - 3, 10, 10);
                g2.setColor(Theme.withAlpha(Theme.ACCENT, 60));
                g2.fillOval(trackX + fillW - 8, trackY - 6, 16, 16);
            }
        }

        // Percent label
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        String pct = (int)(progress * 100) + "%";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pct, trackX + trackW - fm.stringWidth(pct), trackY - 4);

        // ── STEP DESCRIPTION ──────────────────────────────────────────────
        int descY = trackY + trackH + 8;
        int descH = H - descY - 4;

        // Left accent bar
        g2.setColor(Theme.ACCENT2);
        g2.fillRect(trackX, descY, 3, Math.min(descH, 36));

        // Description text
        g2.setFont(Theme.FONT_MONO);
        g2.setColor(Theme.TEXT);
        // Word-wrap if too long
        String d = desc != null ? desc : "";
        int maxW = trackW - 14;
        fm = g2.getFontMetrics();
        int lineY = descY + 14;

        if (fm.stringWidth(d) <= maxW) {
            g2.drawString(d, trackX + 10, lineY);
        } else {
            // Split at word boundary
            String[] words = d.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String test = line.isEmpty() ? word : line + " " + word;
                if (fm.stringWidth(test) > maxW) {
                    g2.drawString(line.toString(), trackX + 10, lineY);
                    lineY += fm.getHeight() + 2;
                    line = new StringBuilder(word);
                    if (lineY > H - 4) break;
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (!line.isEmpty() && lineY <= H - 4) {
                g2.drawString(line.toString(), trackX + 10, lineY);
            }
        }

        g2.dispose();
    }

    private void paintStatBox(Graphics2D g2, int x, int y, int w, int h,
                               String value, String label, Color accentColor) {
        // Box background
        g2.setColor(Theme.BG2);
        g2.fillRoundRect(x, y, w, h, Theme.RADIUS, Theme.RADIUS);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(x, y, w - 1, h - 1, Theme.RADIUS, Theme.RADIUS);

        // Top accent line
        g2.setColor(accentColor);
        g2.fillRoundRect(x + 1, y + 1, w - 2, 3, 2, 2);

        // Value
        g2.setFont(Theme.FONT_STAT);
        g2.setColor(accentColor);
        FontMetrics fm = g2.getFontMetrics();
        int vw = fm.stringWidth(value);
        // Scale down if too wide
        if (vw > w - 10) {
            g2.setFont(new Font("Dialog", Font.BOLD, 18));
            fm = g2.getFontMetrics();
            vw = fm.stringWidth(value);
        }
        g2.drawString(value, x + (w - vw) / 2, y + 38);

        // Label
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + 56);
    }
}
