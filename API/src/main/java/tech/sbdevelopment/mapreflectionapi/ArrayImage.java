package tech.sbdevelopment.mapreflectionapi;

import com.bergerkiller.bukkit.common.map.MapColorPalette;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ArrayImage {
    public byte[] array;
    public int minX = 0;
    public int minY = 0;
    public int maxX = 128;
    public int maxY = 128;
    private int width;
    private int height;
    private int imageType = BufferedImage.TYPE_4BYTE_ABGR;

    public ArrayImage(byte[] data) {
        this.array = data;
    }

    /**
     * Convert a {@link BufferedImage} to an ArrayImage
     *
     * @param image image to convert
     */
    public ArrayImage(BufferedImage image) {
        this.imageType = image.getType();

        this.width = image.getWidth();
        this.height = image.getHeight();

        BufferedImage temp = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = temp.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        int[] pixels = new int[temp.getWidth() * temp.getHeight()];
        temp.getRGB(0, 0, temp.getWidth(), temp.getHeight(), pixels, 0, temp.getWidth());

        byte[] result = new byte[temp.getWidth() * temp.getHeight()];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = MapColorPalette.getColor(new Color(pixels[i], true));
        }

        this.array = result;
    }
}
