package com.algoviz.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * TopbarPanel - custom-painted top header bar.
 * Shows algorithm name, stable/category tags, and all 4 complexity chips.
 */
public class TopbarPanel extends JPanel {

    private String algoName     = "Bubble Sort";
    private String category     = "SORT";
    private boolean stable      = true;
    private boolean inPlace     = true;
    private String complexBest  = "O(n)";
    private String complexAvg   = "O(n^2)";
    private String complexWorst = "O(n^2)";
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

        int width = getWidth();
        int height = getHeight();

        g2.setColor(Theme.BG2);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Theme.BORDER);
        g2.drawLine(0, height - 1, width, height - 1);

        int chipWidth = 72;
        int chipHeight = 40;
        int chipGap = 8;
        int chipCount = 4;
        int outerPad = 16;
        int complexityWidth = chipCount * chipWidth + (chipCount - 1) * chipGap;
        int complexityStartX = width - outerPad - complexityWidth;

        int tagGap = 6;
        List<TagSpec> tags = List.of(
                new TagSpec(category, category.equals("SORT") ? Theme.ACCENT2 : Theme.ACCENT5),
                new TagSpec(stable ? "STABLE" : "UNSTABLE", stable ? Theme.ACCENT : Theme.ACCENT3),
                new TagSpec(inPlace ? "IN-PLACE" : "EXTRA SPACE", Theme.ACCENT4)
        );
        int tagsWidth = computeTagsWidth(g2, tags, tagGap);
        int tagsStartX = complexityStartX - 18 - tagsWidth;

        int x = 20;
        int nameRightLimit = Math.max(x, tagsStartX - 20);
        paintAlgorithmName(g2, x, 38, nameRightLimit - x);

        int cx = complexityStartX;
        cx = paintChip(g2, cx, 10, chipWidth, chipHeight, "BEST", complexBest) + chipGap;
        cx = paintChip(g2, cx, 10, chipWidth, chipHeight, "AVG", complexAvg) + chipGap;
        cx = paintChip(g2, cx, 10, chipWidth, chipHeight, "WORST", complexWorst) + chipGap;
        paintChip(g2, cx, 10, chipWidth, chipHeight, "SPACE", complexSpace);

        int tagX = Math.max(20, tagsStartX);
        for (int i = 0; i < tags.size(); i++) {
            TagSpec tag = tags.get(i);
            tagX = paintTag(g2, tagX, 22, tag.text(), tag.color());
            if (i < tags.size() - 1) {
                tagX += tagGap;
            }
        }

        g2.dispose();
    }

    private void paintAlgorithmName(Graphics2D g2, int x, int baselineY, int maxWidth) {
        g2.setFont(Theme.FONT_TITLE);
        g2.setColor(Theme.TEXT);

        String text = fitText(g2, algoName, maxWidth);
        g2.drawString(text, x, baselineY);
    }

    private String fitText(Graphics2D g2, String text, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        if (maxWidth <= 0) {
            return "";
        }
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        if (ellipsisWidth >= maxWidth) {
            return ellipsis;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (fm.stringWidth(builder.toString() + ch) + ellipsisWidth > maxWidth) {
                break;
            }
            builder.append(ch);
        }
        return builder + ellipsis;
    }

    private int computeTagsWidth(Graphics2D g2, List<TagSpec> tags, int gap) {
        int total = 0;
        for (int i = 0; i < tags.size(); i++) {
            total += measureTagWidth(g2, tags.get(i).text());
            if (i < tags.size() - 1) {
                total += gap;
            }
        }
        return total;
    }

    private int measureTagWidth(Graphics2D g2, String text) {
        g2.setFont(Theme.FONT_BADGE);
        FontMetrics fm = g2.getFontMetrics();
        return fm.stringWidth(text) + 12;
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
        g2.setColor(Theme.BG3);
        g2.fillRoundRect(x, y, w, h, Theme.RADIUS, Theme.RADIUS);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(x, y, w - 1, h - 1, Theme.RADIUS, Theme.RADIUS);

        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + 14);

        g2.setFont(Theme.FONT_MONO_BOLD);
        g2.setColor(Theme.ACCENT4);
        fm = g2.getFontMetrics();
        int vw = fm.stringWidth(value);
        if (vw > w - 8) {
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
            fm = g2.getFontMetrics();
            vw = fm.stringWidth(value);
        }
        g2.drawString(value, x + (w - vw) / 2, y + 32);

        return x + w;
    }

    private record TagSpec(String text, Color color) {
    }
}
