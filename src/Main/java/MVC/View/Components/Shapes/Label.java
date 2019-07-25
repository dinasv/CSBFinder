package MVC.View.Components.Shapes;


import java.awt.*;

public class Label {

    public final static Font DEFAULT_FONT = new Font("Monospaced", Font.BOLD, 24);
    private final static Color DEFAULT_COLOR = Color.black;

    private String text;
    private Font font;
    private Color color;

    private Graphics graphics;

    public Label(String text, Font font, Graphics graphics){
        this.text = text;
        this.font = font;
        color = DEFAULT_COLOR;
        this.graphics = graphics;
    }

    public Label(String text, Graphics graphics){
        this(text, DEFAULT_FONT, graphics);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getWidth(){
        graphics.setFont(font);
        return graphics.getFontMetrics().stringWidth(text);
    }

    public int getHeight(){
        graphics.setFont(font);
        return graphics.getFontMetrics().getAscent();
    }

}
