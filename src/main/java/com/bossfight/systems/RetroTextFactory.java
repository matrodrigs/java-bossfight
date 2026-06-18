package com.bossfight.systems;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectSet;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Locale;

public class RetroTextFactory implements Disposable {
    private final ObjectSet<Texture> textures = new ObjectSet<>();

    public Texture createTitle(String text) {
        return createCartoonText(text, 92, 11, 0xffd64a, 0x672014, 0x050405, 16, 0.048f);
    }

    public Texture createSubtitle(String text) {
        return createText(text, 34, 7, 0xf8eed2, 0x4b2018, 0x080707, 8);
    }

    public Texture createMenuOption(String text, boolean selected) {
        return createCartoonText(text, selected ? 38 : 34, selected ? 7 : 6,
                selected ? 0xffd84a : 0xf7e5b6,
                selected ? 0x572012 : 0x3d2518,
                0x130d0a,
                9,
                selected ? 0.038f : 0.026f);
    }

    public Texture createFightCue(String text, boolean goCue) {
        int fill = goCue ? 0xffdc45 : 0xffef79;
        return createCartoonText(text, 96, 12, fill, 0x612416, 0x060506, 18, 0.08f);
    }

    public Texture createKnockout(String text) {
        return createCartoonText(text, 88, 13, 0xffdd55, 0x5a1d12, 0x050405, 18, 0.055f);
    }

    public Texture createResultTitle(String text, boolean victory) {
        int fill = victory ? 0xffdf58 : 0xf8efe1;
        int stroke = victory ? 0x5b2014 : 0x201815;
        return createCartoonText(text, 92, 12, fill, stroke, 0x050405, 18, 0.055f);
    }

    public Texture createPlayerHealthHud(int health) {
        return createPlayerHealthHudText("HP.", String.valueOf(health));
    }

    public Texture createBossIntroTitle(String text) {
        return createCartoonText(text, 58, 8, 0xffd24a, 0x5b1f13, 0x050405, 13, 0.04f);
    }

    public Texture createInstruction(String text) {
        return createText(text, 28, 5, 0xf6e5b8, 0x312017, 0x050405, 8);
    }

    public Texture createInstructionKey(String text) {
        return createText(text, 31, 6, 0xffd24a, 0x5b2014, 0x050405, 9);
    }

    private Texture createText(String text, int fontSize, int strokeWidth, int fillRgb, int strokeRgb,
                               int shadowRgb, int padding) {
        Font font = pickFont("Georgia", "Serif", fontSize);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D probeGraphics = probe.createGraphics();
        applyHints(probeGraphics);
        FontMetrics metrics = probeGraphics.getFontMetrics(font);
        int width = Math.max(32, metrics.stringWidth(text) + padding * 2 + strokeWidth * 8);
        int height = Math.max(32, metrics.getHeight() + padding * 2 + strokeWidth * 8);
        probeGraphics.dispose();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        applyHints(graphics);
        graphics.setFont(font);

        GlyphVector glyphs = font.createGlyphVector(graphics.getFontRenderContext(), text);
        Shape outline = glyphs.getOutline(
                padding + strokeWidth * 4f,
                padding + strokeWidth * 4f + metrics.getAscent()
        );

        Shape shadow = AffineTransform.getTranslateInstance(strokeWidth * 1.3, strokeWidth * 1.5)
                .createTransformedShape(outline);
        graphics.setColor(toAwtColor(shadowRgb, 180));
        graphics.fill(shadow);

        graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.setColor(toAwtColor(strokeRgb, 255));
        graphics.draw(outline);
        graphics.setColor(toAwtColor(fillRgb, 255));
        graphics.fill(outline);

        Shape shine = AffineTransform.getTranslateInstance(0, -fontSize * 0.08f).createTransformedShape(outline);
        graphics.setColor(new java.awt.Color(255, 255, 255, 42));
        graphics.draw(shine);
        graphics.dispose();

        Texture texture = toTexture(image);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        return texture;
    }

    private Texture createPlayerHealthHudText(String label, String value) {
        int strokeWidth = 5;
        int padding = 6;
        int fillRgb = 0xffdf55;
        int strokeRgb = 0x5a2118;
        int shadowRgb = 0x050405;
        int fontSize = 35;
        int valueFontSize = 38;
        int gap = 4;
        int edge = padding + strokeWidth * 2;

        Font labelFont = pickFont("Georgia", "Serif", fontSize);
        Font valueFont = pickFont("Georgia", "Serif", valueFontSize);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D probeGraphics = probe.createGraphics();
        applyHints(probeGraphics);
        FontMetrics labelMetrics = probeGraphics.getFontMetrics(labelFont);
        FontMetrics valueMetrics = probeGraphics.getFontMetrics(valueFont);
        int width = Math.max(32, edge * 2 + labelMetrics.stringWidth(label) + gap
                + valueMetrics.stringWidth(value) + strokeWidth * 2);
        int height = Math.max(32, edge * 2 + Math.max(labelMetrics.getHeight(), valueMetrics.getHeight())
                + strokeWidth);
        probeGraphics.dispose();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        applyHints(graphics);

        float baseline = edge + Math.max(labelMetrics.getAscent(), valueMetrics.getAscent());
        float labelX = edge;
        Shape labelOutline = createTextOutline(graphics, labelFont, label, labelX, baseline);
        drawOutlinedText(graphics, labelOutline, strokeWidth, fillRgb, strokeRgb, shadowRgb, fontSize);

        float valueX = labelX + labelMetrics.stringWidth(label) + gap;
        Shape valueOutline = createTextOutline(graphics, valueFont, value, valueX, baseline - 1f);
        drawOutlinedText(graphics, valueOutline, strokeWidth, fillRgb, strokeRgb, shadowRgb, valueFontSize);
        graphics.dispose();

        Texture texture = toTexture(image);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        return texture;
    }

    private Shape createTextOutline(Graphics2D graphics, Font font, String text, float x, float baseline) {
        GlyphVector glyphs = font.createGlyphVector(graphics.getFontRenderContext(), text);
        return glyphs.getOutline(x, baseline);
    }

    private void drawOutlinedText(Graphics2D graphics, Shape outline, int strokeWidth, int fillRgb, int strokeRgb,
                                  int shadowRgb, int fontSize) {
        Shape shadow = AffineTransform.getTranslateInstance(strokeWidth * 1.3, strokeWidth * 1.5)
                .createTransformedShape(outline);
        graphics.setColor(toAwtColor(shadowRgb, 180));
        graphics.fill(shadow);

        graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.setColor(toAwtColor(strokeRgb, 255));
        graphics.draw(outline);
        graphics.setColor(toAwtColor(fillRgb, 255));
        graphics.fill(outline);

        Shape shine = AffineTransform.getTranslateInstance(0, -fontSize * 0.08f).createTransformedShape(outline);
        graphics.setColor(new java.awt.Color(255, 255, 255, 42));
        graphics.draw(shine);
    }

    private Texture createCartoonText(String text, int fontSize, int strokeWidth, int fillRgb, int strokeRgb,
                                      int shadowRgb, int padding, float tiltStep) {
        Font font = pickFont("Cooper Black", "Showcard Gothic", "Georgia", fontSize);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D probeGraphics = probe.createGraphics();
        applyHints(probeGraphics);
        FontMetrics metrics = probeGraphics.getFontMetrics(font);
        int tracking = Math.max(2, fontSize / 18);
        int textWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            textWidth += metrics.charWidth(text.charAt(i)) + tracking;
        }
        int sidePadding = padding + strokeWidth * 7;
        int width = Math.max(32, textWidth + sidePadding * 2);
        int height = Math.max(32, metrics.getHeight() + padding * 2 + strokeWidth * 12);
        probeGraphics.dispose();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        applyHints(graphics);
        graphics.setFont(font);

        float x = sidePadding;
        float baseline = padding + strokeWidth * 4f + metrics.getAscent();
        for (int i = 0; i < text.length(); i++) {
            String letter = String.valueOf(text.charAt(i));
            int advance = metrics.charWidth(text.charAt(i));
            if (letter.isBlank()) {
                x += advance + tracking;
                continue;
            }

            GlyphVector glyphs = font.createGlyphVector(graphics.getFontRenderContext(), letter);
            Shape outline = glyphs.getOutline();
            float bounce = (float) Math.sin(i * 1.71f) * fontSize * 0.035f;
            float angle = ((i % 3) - 1) * tiltStep + (float) Math.sin(i * 0.9f) * tiltStep * 0.35f;
            AffineTransform transform = new AffineTransform();
            transform.translate(x, baseline + bounce);
            transform.rotate(angle, advance * 0.5, -metrics.getAscent() * 0.45);
            Shape placed = transform.createTransformedShape(outline);

            Shape shadow = AffineTransform.getTranslateInstance(strokeWidth * 1.55, strokeWidth * 1.75)
                    .createTransformedShape(placed);
            graphics.setColor(toAwtColor(shadowRgb, 218));
            graphics.fill(shadow);

            graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.setColor(toAwtColor(strokeRgb, 255));
            graphics.draw(placed);
            graphics.setColor(toAwtColor(fillRgb, 255));
            graphics.fill(placed);

            Shape shine = AffineTransform.getTranslateInstance(0, -fontSize * 0.07f).createTransformedShape(placed);
            graphics.setStroke(new BasicStroke(Math.max(2f, strokeWidth * 0.18f), BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            graphics.setColor(new java.awt.Color(255, 255, 255, 58));
            graphics.draw(shine);

            x += advance + tracking;
        }
        graphics.dispose();

        Texture texture = toTexture(image);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        return texture;
    }

    private Texture toTexture(BufferedImage image) {
        Pixmap pixmap = new Pixmap(image.getWidth(), image.getHeight(), Pixmap.Format.RGBA8888);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;
                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;
                int rgba = (red << 24) | (green << 16) | (blue << 8) | alpha;
                pixmap.drawPixel(x, y, rgba);
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void applyHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private java.awt.Color toAwtColor(int rgb, int alpha) {
        return new java.awt.Color((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, alpha);
    }

    private Font pickFont(String preferred, String fallback, int size) {
        return pickFont(preferred, fallback, "Serif", size);
    }

    private Font pickFont(String preferred, String fallback, String lastResort, int size) {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String preferredKey = preferred.toLowerCase(Locale.ROOT);
        String fallbackKey = fallback.toLowerCase(Locale.ROOT);
        for (String family : available) {
            String key = family.toLowerCase(Locale.ROOT);
            if (key.equals(preferredKey)) {
                return new Font(family, Font.BOLD, size);
            }
        }
        for (String family : available) {
            String key = family.toLowerCase(Locale.ROOT);
            if (key.equals(fallbackKey)) {
                return new Font(family, Font.BOLD, size);
            }
        }
        return new Font(lastResort, Font.BOLD, size);
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
        textures.clear();
    }
}
