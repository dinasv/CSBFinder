package MVC.View.Components.Shapes;

import java.awt.*;
import java.util.List;

/**
 */
public class ShapesInstance implements Shape{

    private List<GeneShape> geneShapesList;
    private int x;
    private int y;
    //the distance between shapes
    private int DIST_SHAPES = 10;
    private int NAME_LABEL_HEIGHT = 22;
    private int LOCATION_LABELS_HEIGHT = 15;
    //private Dimension containerDimensions;

    private LabelShape nameLabel;
    private LabelShape startIndexLabel;
    private LabelShape endIndexLabel;

    int width;
    int height;

    public ShapesInstance(List<GeneShape> geneShapesList, int x, int y){

        this.geneShapesList = geneShapesList;
        this.x = x;
        this.y = y;
        height = 0;

        calcWidth();

        if (geneShapesList.size()>0){
            height = geneShapesList.get(0).getHeight();
        }

        nameLabel = null;
        startIndexLabel = null;
        endIndexLabel = null;

    }

    public ShapesInstance(List<GeneShape> geneShapesList, int x, int y, Label nameLabel, Label startIndexLabel,
                          Label endIndexLabel){

        this(geneShapesList, x, y);

        int labelStartX = x + 2;

        this.nameLabel = new LabelShape(labelStartX, y + (int)(NAME_LABEL_HEIGHT*0.75), nameLabel);

        if (geneShapesList.size()>0){
            height = geneShapesList.get(0).getHeight() + NAME_LABEL_HEIGHT + LOCATION_LABELS_HEIGHT;
        }

        this.startIndexLabel = new LabelShape(labelStartX, y + height, startIndexLabel);
        this.endIndexLabel = new LabelShape(x, y + height, endIndexLabel);

    }

    private void calcWidth(){
        width = 0;
        for (GeneShape geneShape: geneShapesList){
            width += geneShape.getWidth();
        }
        width += DIST_SHAPES * (geneShapesList.size()-1);
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getGeneY(){
        return y + NAME_LABEL_HEIGHT;
    }

    public int getGeneEndY(){
        return y + height - LOCATION_LABELS_HEIGHT;
    }

    public List<GeneShape> getGeneShapesList(){
        return geneShapesList;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }


    public void draw(Graphics g) {

        int curr_x = x;
        int curr_y = y;

        if (nameLabel != null) {
            curr_y += NAME_LABEL_HEIGHT;
        }

        ShapeDimensions sd = null;
        for (GeneShape geneShape : geneShapesList) {
            geneShape.setX(curr_x);
            geneShape.setY(curr_y);

            geneShape.draw(g);

            sd = geneShape.getShapeDimensions();
            curr_x += sd.getRectWidth() + sd.getArrowWidth() + DIST_SHAPES;
        }


        if (endIndexLabel != null) {
            int endLabelWidth = endIndexLabel.getLabelWidth(g);

            endIndexLabel.setX(x + width - endLabelWidth);
            endIndexLabel.draw(g);
        }
        if (nameLabel != null) {
            nameLabel.draw(g);
        }
        if (startIndexLabel != null) {
            startIndexLabel.draw(g);
        }

    }
}
