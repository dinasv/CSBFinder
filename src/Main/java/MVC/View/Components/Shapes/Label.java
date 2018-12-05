package MVC.View.Components.Shapes;


import java.awt.*;

public class Label {

    public final static Font DEFAULT_FONT = new Font("Monospaced", Font.BOLD, 24);

    private String text;
    private Font font;
    private Color color;

    public Label(String text, Font font){
        this.text = text;
        this.font = font;
        color = Color.black;
    }

    public Label(String text){
        this(text, DEFAULT_FONT);
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
}
