package MVC.View.Components.Shapes;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ShapesInstance implements Shape{

    private List<GeneShape> instanceShapesList;
    private List<GeneShape> leftNeighborsShapeList;
    private List<GeneShape> rightNeighborsShapeList;

    private int instanceShapesWidth;

    private int x;
    private int y;

    //the distance between shapes
    private int DIST_SHAPES = 10;
    private int NAME_LABEL_HEIGHT = 22;
    private int NAME_LABEL_PADDING = 2;
    private int LOCATION_LABELS_HEIGHT = 15;

    private LabelShape repliconNameLabel;
    private LabelShape startLocationLabel;
    private LabelShape endLocationLabel;

    int width;
    int height;

    public ShapesInstance(List<GeneShape> instanceShapesList, int x, int y){
        this(instanceShapesList, new ArrayList<>(), new ArrayList<>(), x, y,
                null, null, null);

        /*
        this.instanceShapesList = instanceShapesList;
        this.x = x;
        this.y = y;
        height = 0;


        if (instanceShapesList.size()>0){
            height = instanceShapesList.get(0).getHeight();
        }

        repliconNameLabel = null;
        startLocationLabel = null;
        endLocationLabel = null;

        leftNeighborsShapeList = new ArrayList<>();
        rightNeighborsShapeList = new ArrayList<>();


        width = calcWidth();*/
    }

    public ShapesInstance(List<GeneShape> instanceShapesList, List<GeneShape> leftNeighborsShapeList,
                          List<GeneShape> rightNeighborsShapeList, int x, int y, Label repliconNameLabel,
                          Label startLocationLabel, Label endLocationLabel){

        this.instanceShapesList = instanceShapesList;
        this.leftNeighborsShapeList = leftNeighborsShapeList;
        this.rightNeighborsShapeList = rightNeighborsShapeList;
        this.x = x;
        this.y = y;

        height = 0;
        if (instanceShapesList.size()>0){
            height = instanceShapesList.get(0).getHeight();
            height += repliconNameLabel == null ? 0 : NAME_LABEL_HEIGHT;
            height += startLocationLabel == null ? 0 : LOCATION_LABELS_HEIGHT;
        }

        int labelStartX = x + NAME_LABEL_PADDING;
        this.repliconNameLabel = repliconNameLabel == null ? null :
                new LabelShape(labelStartX, y + (int) (NAME_LABEL_HEIGHT * 0.75), repliconNameLabel);

        this.startLocationLabel = startLocationLabel == null ? null :
                new LabelShape(labelStartX, y + height, startLocationLabel);

        this.endLocationLabel = endLocationLabel == null ? null : new LabelShape(x, y + height, endLocationLabel);


        width = calcWidth();
    }

    private int calcWidth(){
        int width = 0;
        instanceShapesWidth = calcGeneShapeListWidth(instanceShapesList);
        width += instanceShapesWidth;
        width += calcGeneShapeListWidth(leftNeighborsShapeList);
        width += calcGeneShapeListWidth(rightNeighborsShapeList);

        //width += DIST_SHAPES * (instanceShapesList.size()+leftNeighborsShapeList.size()+rightNeighborsShapeList.size()-1);
        return width - DIST_SHAPES;
    }

    private int calcGeneShapeListWidth(List<GeneShape> geneShapes){

        int width = geneShapes.stream().mapToInt(GeneShape::getWidth).sum();
        width += DIST_SHAPES * geneShapes.size();

        return width;
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

    public List<GeneShape> getInstanceShapesList(){
        return instanceShapesList;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public void draw(Graphics g) {

        int currX = x;
        int currY = y;

        if (repliconNameLabel != null) {
            currY += NAME_LABEL_HEIGHT;
        }

        currX = drawGenes(g, currX, currY, leftNeighborsShapeList);

        g.setColor(Color.WHITE);
        g.fillRect(currX-DIST_SHAPES/2, y, instanceShapesWidth, height);
        g.setColor(Color.black);
        g.drawRect(currX-DIST_SHAPES/2, y, instanceShapesWidth, height);

        currX = drawGenes(g, currX, currY, instanceShapesList);

        drawGenes(g, currX, currY, rightNeighborsShapeList);

        drawLabels(g);

    }

    private int drawGenes(Graphics g, int currX, int currY, List<GeneShape> genes){
        for (GeneShape geneShape : genes) {
            geneShape.setX(currX);
            geneShape.setY(currY);

            geneShape.draw(g);

            currX += geneShape.getWidth() + DIST_SHAPES;
        }
        return currX;
    }

    private void drawLabels(Graphics g){
        if (endLocationLabel != null) {
            int endLabelWidth = endLocationLabel.getLabelWidth(g);

            endLocationLabel.setX(x + width - endLabelWidth);
            endLocationLabel.draw(g);
        }
        if (repliconNameLabel != null) {
            repliconNameLabel.draw(g);
        }
        if (startLocationLabel != null) {
            startLocationLabel.draw(g);
        }
    }
}
