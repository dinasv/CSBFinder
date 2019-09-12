package MVC.View.Graphics;

import Model.Genomes.Gene;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class GeneColors {

    private static final Color DEFAULT_COLOR = Color.lightGray;
    private Map<String, Color> colorsUsed;

    private Random rnd;

    public GeneColors(){
        rnd = new Random();
        colorsUsed = new HashMap<>();

    }


    private Color getRandomColor(){
        float hue = rnd.nextFloat();
        // Saturation between 0.1 and 0.3
        float saturation = (rnd.nextInt(2000) + 1000) / 10000f;
        float luminance = 0.9f;
        Color color = Color.getHSBColor(hue, saturation, luminance);

        return color;
    }


    public Color getColor(Gene gene){

        return setColor(gene);

    }

    public Color getColor(String gene){

        return setColor(gene);

    }

    private Color setColor(String gene){
        Color color = colorsUsed.getOrDefault(gene, getRandomColor());

        colorsUsed.put(gene, color);

        return color;
    }

    public Color setColor(Gene gene){
        return setColor(gene.getCogId());
    }

    public void setColor(String gene, Color color){

        colorsUsed.put(gene, color);
    }

    public Iterator<Map.Entry<String, Color>> getGeneToColor(){
        return colorsUsed.entrySet().iterator();
    }
}
