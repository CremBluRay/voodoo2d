package com.github.jacksonhoggard.voodoo2d.engine.mapping;

import java.io.File;
import java.util.ArrayList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.github.jacksonhoggard.voodoo2d.engine.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

// The MapHost class exists as a means of loading TMX files into the working memory of the program.
public class MapHost {
    private String tmxTarget;
    private NodeList rawTileData;
    private NodeList rawImageData;
    private ArrayList<TileSet> tileSets = new ArrayList<>();
    private static Map map;

    public MapHost(String tmxTarget){
        this.tmxTarget = tmxTarget;
    }

    /**
     * This method populates the variables used to render the map.
     * @throws Exception if a bad tmx path is given, or if the tmx is malformed.
     */
    public void init(){
        try{
            // Load the xml file as a document. 
            File in = new File(tmxTarget);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            // Scrape information from the document.
            rawTileData = doc.getElementsByTagName("tileset");
            rawImageData = doc.getElementsByTagName("image");
            for(int i = 0; i < rawTileData.getLength(); i++){
                NamedNodeMap tmp = rawTileData.item(i).getAttributes();
                NamedNodeMap img = rawImageData.item(i).getAttributes();
                int firstGid = Integer.parseInt(tmp.getNamedItem("firstgid").getTextContent());
                String name = tmp.getNamedItem("name").getTextContent();
                int tileWidth = Integer.parseInt(tmp.getNamedItem("tilewidth").getTextContent());
                int tileHeight = Integer.parseInt(tmp.getNamedItem("tileheight").getTextContent());
                String source = img.getNamedItem("source").getTextContent();
                int imageWidth = Integer.parseInt(img.getNamedItem("width").getTextContent());
                int imageHeight = Integer.parseInt(img.getNamedItem("height").getTextContent());
                tileSets.add(new TileSet(firstGid, name, tileWidth, tileHeight, source, imageWidth, imageHeight));
            }
            // Create and load the map
            map = new Map(tileSets, tmxTarget);
        } catch (final Exception e) {
            Log.engine().error(e.getMessage());
        }
    }
    public ArrayList<TileSet> getTileSets(){
        return tileSets;
    }

    public void setScale(float scale) {
        for(Layer layer : map.getLayers()) {
            layer.asGameObject().setScale(scale);
        }
    }

    public Map getMap() {
        return map;
    }
}