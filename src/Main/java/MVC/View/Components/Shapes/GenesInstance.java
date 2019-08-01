package MVC.View.Components.Shapes;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class GenesInstance implements Shape{

    private List<GeneShape> instanceShapesList;
    private List<GeneShape> leftNeighborsShapeList;
    private List<GeneShape> rightNeighborsShapeList;

    private int x;
    private int y;

    //the distance between shapes
    private int DIST_SHAPES = 10;
    private int NAME_LABEL_PADDING = 2;

    private LabelShape repliconNameLabel;
    private LabelShape startLocationLabel;
    private LabelShape endLocationLabel;

    private int width;
    private int leftNeighborsWidth;
    private int rightNeighborsWidth;
    private int instanceShapesWidth;
    private int height;

    public GenesInstance(List<GeneShape> instanceShapesList, int x, int y){
        this(instanceShapesList, new ArrayList<>(), new ArrayList<>(), x, y,
                null, null, null);

    }

    public GenesInstance(List<GeneShape> instanceShapesList, List<GeneShape> leftNeighborsShapeList,
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
            height += repliconNameLabel == null ? 0 : repliconNameLabel.getHeight();
            height += startLocationLabel == null ? 0 : startLocationLabel.getHeight();
        }

        int labelStartX = x + NAME_LABEL_PADDING;
        this.repliconNameLabel = repliconNameLabel == null ? null :
                new LabelShape(labelStartX, y + (int) (repliconNameLabel.getHeight() * 0.75), repliconNameLabel);

        this.startLocationLabel = startLocationLabel == null ? null :
                new LabelShape(labelStartX, y + height, startLocationLabel);

        this.endLocationLabel = endLocationLabel == null ? null : new LabelShape(x, y + height, endLocationLabel);


        width = calcWidth();

        setElementsCoordinates();
    }

    private void setElementsCoordinates(){

        int currX = x;
        int currY = y;

        if (repliconNameLabel != null) {
            currY += repliconNameLabel.getLabelHeight();
        }

        currX = setGenesCoordinates(currX, currY, leftNeighborsShapeList);

        currX = setGenesCoordinates(currX, currY, instanceShapesList);

        setGenesCoordinates(currX, currY, rightNeighborsShapeList);

    }

    private int setGenesCoordinates(int currX, int currY, List<GeneShape> genes){
        for (GeneShape geneShape : genes) {
            geneShape.setX(currX);
            geneShape.setY(currY);

            currX += geneShape.getWidth() + DIST_SHAPES;
        }
        return currX;
    }

    private int calcWidth(){
        instanceShapesWidth = calcGeneShapeListWidth(instanceShapesList);
        leftNeighborsWidth = calcGeneShapeListWidth(leftNeighborsShapeList);
        rightNeighborsWidth = calcGeneShapeListWidth(rightNeighborsShapeList);

        return instanceShapesWidth + leftNeighborsWidth + rightNeighborsWidth - DIST_SHAPES;
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

        //return y + NAME_LABEL_HEIGHT;
        int nameLabelHeight = repliconNameLabel == null ? 0 : repliconNameLabel.getLabelHeight();
        return y + nameLabelHeight;
    }

    public int getGeneEndY(){
        //return y + height - LOCATION_LABELS_HEIGHT;
        int locationLabelsHeight = startLocationLabel == null ? 0 : startLocationLabel.getLabelHeight();
        return y + height - locationLabelsHeight;
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

        int width = drawGenes(g, leftNeighborsShapeList);
        int currX = x + width;

        //Draw white rectangle around the instance
        g.setColor(Color.WHITE);
        g.fillRect(currX-DIST_SHAPES/2, y, instanceShapesWidth, height);
        g.setColor(Color.black);
        g.drawRect(currX-DIST_SHAPES/2, y, instanceShapesWidth, height);

        drawGenes(g, instanceShapesList);

        drawGenes(g, rightNeighborsShapeList);

        drawLabels(g);

    }

    @Override
    public boolean containsPoint(Point point) {
        return point.x >= x && point.x <= x+width && point.y >= y && point.y <= y + height;
    }

    @Override
    public Shape getShapeWithPoint(Point point) {
        Shape geneWithPoint = null;
        if (point.x <= x + leftNeighborsWidth){
            geneWithPoint = getGeneWithPoint(point, leftNeighborsShapeList);
        }else if (point.x <= x + leftNeighborsWidth + instanceShapesWidth){
            geneWithPoint = getGeneWithPoint(point, instanceShapesList);
        }else if (point.x <= x + leftNeighborsWidth + instanceShapesWidth + rightNeighborsWidth){
            geneWithPoint = getGeneWithPoint(point, rightNeighborsShapeList);
        }

        return geneWithPoint;
    }

    private Shape getGeneWithPoint(Point point, List<GeneShape> geneShapes){
        for (GeneShape geneShape: geneShapes) {
            if (geneShape.containsPoint(point)){
                return geneShape;
            }
        }
        return null;
    }

    private int drawGenes(Graphics g, List<GeneShape> genes){
        int width = 0;
        for (GeneShape geneShape : genes) {

            geneShape.draw(g);

            width += geneShape.getWidth() + DIST_SHAPES;
        }
        return width;
    }

    private void drawLabels(Graphics g){
        if (endLocationLabel != null) {
            int endLabelWidth = endLocationLabel.getLabelWidth();

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

    public GeneShape getGeneShapeWithLabel(String label){
        for (GeneShape geneShape: instanceShapesList) {
            if (geneShape.getLabel().getText().equals(label)){
                return geneShape;
            }
        }
        return null;
    }

    public String toString(){
        return leftNeighborsShapeList.toString() + instanceShapesList.toString() + rightNeighborsShapeList.toString();
    }

    private void reverseGeneList(List<GeneShape> geneList){
        Collections.reverse(geneList);
        for (GeneShape gene : geneList){
            gene.setStrand(gene.getStrand().reverseStrand());
        }
    }

    public void reverse(){
        reverseGeneList(instanceShapesList);
        reverseGeneList(leftNeighborsShapeList);
        reverseGeneList(rightNeighborsShapeList);

        List<GeneShape> temp = rightNeighborsShapeList;
        rightNeighborsShapeList = leftNeighborsShapeList;
        leftNeighborsShapeList = temp;

        setElementsCoordinates();
    }
}
