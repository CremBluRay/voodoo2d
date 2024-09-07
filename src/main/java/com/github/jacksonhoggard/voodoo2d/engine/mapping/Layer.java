package com.github.jacksonhoggard.voodoo2d.engine.mapping;

import com.github.jacksonhoggard.voodoo2d.engine.gameObject.GameObject;
import com.github.jacksonhoggard.voodoo2d.engine.graphic.Mesh;
import com.github.jacksonhoggard.voodoo2d.engine.log.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Layer {

    private GameObject layer;
    private final int[][] layerCoords;
    private final int tileWidth;
    private final int tileHeight;

    public Layer(int[][] layerCoords, int x, int y, int tileWidth, int tileHeight, ArrayList<TileSet> tileSets) {
        this.layerCoords = layerCoords;
        // Build layer texture
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        int width = tileWidth * x;
        int height = tileHeight * y;
        BufferedImage layerTex = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Make the layer
        try {
            HashMap<String, BufferedImage> imageCache = new HashMap<>();
            String basePath = String.format("src%smain%sresources%smaps%s", File.separator, File.separator, File.separator, File.separator);
            for (int b = 0; b < y; b++) {
                for (int a = 0; a < x; a++) {
                    if (layerCoords[a][b] > 0) {
                        // Get the correct tileSet
                        TileSet currentTileSet = null;
                        for (TileSet tileSet : tileSets) {
                            if (layerCoords[a][b] >= tileSet.getFirstGID())
                                currentTileSet = tileSet;
                        }
                        String path = basePath + currentTileSet.getSource();

                        BufferedImage tileSet;
                        if (imageCache.containsKey(path)) {
                            tileSet = imageCache.get(path);
                        } else {
                            tileSet = ImageIO.read(new File(path));
                            imageCache.put(path, tileSet);
                        }

                        // Get subImage from tileset
                        BufferedImage subImage;
                        for (int d = 0; d < currentTileSet.getGIDs()[0].length; d++) {
                            for (int c = 0; c < currentTileSet.getGIDs().length; c++) {
                                if (currentTileSet.getGIDs()[c][d] == layerCoords[a][b]) {
                                    subImage = tileSet.getSubimage(c * currentTileSet.getTileWidth(), d * currentTileSet.getTileHeight(), currentTileSet.getTileWidth(), currentTileSet.getTileHeight());
                                    layerTex = joinBufferedImage(layerTex, subImage, b, a);
                                }
                            }
                        }
                    }
                }
            }
            File layerFile = new File("layer.png");
            ImageIO.write(layerTex, "png", layerFile);
            layer = new GameObject(Mesh.loadMesh("layer.png"));
            layerFile.delete();
        } catch (final Exception e) {
            Log.engine().error(e.getMessage());
        }
    }

    private BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2, int tileX, int tileY) {
        int offsetX = tileWidth * tileX;
        int offsetY = tileHeight * tileY;

        BufferedImage newImg = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImg.createGraphics();
        // Draw image
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, offsetX, offsetY);
        g2.dispose();

        return newImg;
    }

    public int[][] getLayerCoords() {
        return layerCoords;
    }

    public GameObject asGameObject() {
        return layer;
    }
}
