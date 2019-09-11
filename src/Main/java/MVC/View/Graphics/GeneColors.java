package MVC.View.Graphics;

import Model.Genomes.Gene;

import java.awt.*;
import java.util.HashMap;
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

        return colorsUsed.getOrDefault(gene.getCogId(), DEFAULT_COLOR);

    }

    public Color getColor(String gene){

        return colorsUsed.getOrDefault(gene, DEFAULT_COLOR);

    }

    public void setColor(Gene gene){

        Color color = colorsUsed.getOrDefault(gene.getCogId(), getRandomColor());

        colorsUsed.put(gene.getCogId(), color);
    }

    public void setColor(String gene, Color color){

        colorsUsed.put(gene, color);
    }
}
