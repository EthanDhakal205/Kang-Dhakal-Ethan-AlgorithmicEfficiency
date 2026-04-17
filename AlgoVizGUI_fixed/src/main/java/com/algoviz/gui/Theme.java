package com.algoviz.gui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Theme — centralized design system for AlgoViz GUI.
 * All colors, fonts, sizes, and paint utilities live here.
 * Dark terminal-meets-neon aesthetic.
 */
public final class Theme {

    private Theme() {}

    //  PALETTE 
    public static final Color BG = new Color(0x0a0a0f);
    public static final Color BG2= new Color(0x0f0f17);
    public static final Color BG3 = new Color(0x141420);
    public static final Color SURFACE = new Color(0x1a1a2e);
    public static final Color SURFACE2 = new Color(0x222238);
    public static final Color BORDER = new Color(0x2a2a45);
    public static final Color BORDER2 = new Color(0x3a3a60);

    public static final Color ACCENT = new Color(0x00f5a0);   // electric green
    public static final Color ACCENT2  = new Color(0x7b2ff7);   // violet
    public static final Color ACCENT3= new Color(0xff3c6e);   // hot pink
    public static final Color ACCENT4 = new Color(0xffcc00);   // amber
    public static final Color ACCENT5 = new Color(0x00cfff);   // cyan

    public static final Color TEXT = new Color(0xe8e8f0);
    public static final Color TEXT2 = new Color(0x9090b0);
    public static final Color TEXT3 = new Color(0x555577);

    //  BAR COLORS by step type 
    public static final Color BAR_DEFAULT = new Color(0x3d3d6b);
    public static final Color BAR_COMPARE = new Color(0xffcc00);
    public static final Color BAR_SWAP  = new Color(0xff3c6e);
    public static final Color BAR_PIVOT = new Color(0x9b4fff);
    public static final Color BAR_MERGE = new Color(0x00cfff);
    public static final Color BAR_SORTED = new Color(0x00f5a0);
    public static final Color BAR_FOUND  = new Color(0x00f5a0);
    public static final Color BAR_NOTFOUND  = new Color(0xff3c6e);
    public static final Color BAR_HIGHLIGHT = new Color(0xff8800);
    public static final Color BAR_FINAL = new Color(0x00f5a0);

    //  FONTS 
    public static final Font FONT_TITLE = new Font("Dialog", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE  = new Font("Dialog", Font.BOLD, 12);
    public static final Font FONT_MONO  = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    public static final Font FONT_MONO_SM  = new Font(Font.MONOSPACED, Font.PLAIN, 10);
    public static final Font FONT_MONO_BOLD = new Font(Font.MONOSPACED, Font.BOLD, 12);
    public static final Font FONT_STAT = new Font("Dialog", Font.BOLD, 28);
    public static final Font FONT_LABEL = new Font(Font.MONOSPACED, Font.BOLD, 9);
    public static final Font FONT_NAV = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    public static final Font FONT_BTN = new Font(Font.MONOSPACED, Font.BOLD, 10);
    public static final Font FONT_BADGE= new Font(Font.MONOSPACED, Font.BOLD, 9);

    //  SIZES 
    public static final int SIDEBAR_W = 272;
    public static final int TOPBAR_H = 60;
    public static final int RADIUS= 6;
    public static final int RADIUS_LG= 10;

    //  PAINT UTILITIES 

    /** Fill a rounded rectangle with a solid color */
    public static void fillRounded(Graphics2D g, int x, int y, int w, int h, int r, Color c) {
        g.setColor(c);
        g.fillRoundRect(x, y, w, h, r, r);
    }

    /** Draw a rounded rectangle border */
    public static void drawRounded(Graphics2D g, int x, int y, int w, int h, int r, Color c) {
        g.setColor(c);
        g.drawRoundRect(x, y, w - 1, h - 1, r, r);
    }

    /** Fill a rounded rect with a vertical gradient */
    public static void fillGradientV(Graphics2D g, int x, int y, int w, int h, int r,
                                      Color top, Color bottom) {
        GradientPaint gp = new GradientPaint(x, y, top, x, y + h, bottom);
        g.setPaint(gp);
        g.fillRoundRect(x, y, w, h, r, r);
        g.setPaint(null);
    }

    /** Fill a rounded rect with a horizontal gradient */
    public static void fillGradientH(Graphics2D g, int x, int y, int w, int h, int r,
                                      Color left, Color right) {
        GradientPaint gp = new GradientPaint(x, y, left, x + w, y, right);
        g.setPaint(gp);
        g.fillRoundRect(x, y, w, h, r, r);
        g.setPaint(null);
    }

    /** Enable antialiasing and rendering hints */
    public static void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,      RenderingHints.VALUE_STROKE_PURE);
    }

    /** Centered string draw */
    public static void drawCentered(Graphics2D g, String text, int x, int y, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
    }

    /** Draw a glowing bar — the highlight effect for active bars */
    public static void paintGlow(Graphics2D g, int x, int y, int w, int h, Color glowColor) {
        int layers = 3;
        for (int i = layers; i >= 1; i--) {
            int alpha = 30 + (layers - i) * 15;
            int pad = i * 2;
            g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), alpha));
            g.fillRoundRect(x - pad, y - pad, w + pad * 2, h + pad * 2, RADIUS, RADIUS);
        }
    }

    /** Resolve the bar color for a given AlgoStep type */
    public static Color barColor(com.algoviz.models.AlgoStep.StepType type, boolean highlighted) {
        if (!highlighted) {
            return switch (type) {
                case SORTED -> new Color(0x1a5c38);
                case FINAL -> new Color(0x1a5c38);
                default -> BAR_DEFAULT;
            };
        }
        return switch (type) {
            case COMPARE-> BAR_COMPARE;
            case SWAP -> BAR_SWAP;
            case PIVOT -> BAR_PIVOT;
            case MERGE -> BAR_MERGE;
            case SORTED-> BAR_SORTED;
            case FOUND -> BAR_FOUND;
            case NOT_FOUND -> BAR_NOTFOUND;
            case HIGHLIGHT -> BAR_HIGHLIGHT;
            case FINAL -> BAR_FINAL;
            default -> new Color(0x6b6baa);
        };
    }

    /** Translucent version of a color */
    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}

