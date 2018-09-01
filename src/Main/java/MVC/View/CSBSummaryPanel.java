package MVC.View;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import MVC.View.Shapes.Shape;
import Utils.COG;


public class CSBSummaryPanel extends JPanel {

    JTextPane summary;
    //private static final String HEADER = "CSB Information";
    private static final String NEWLINE = "\n";
    private static final String NO_COG_INFO_WARNNING = "Oops, No gene information was found. Did you load the gene orthology info file?";

    Highlighter highlighter;

    CSBSummaryPanel() {
        setLayout(new BorderLayout());
        summary = new JTextPane();
        summary.setEditable(false);
        summary.setFont(new Font("Serif", Font.PLAIN, 16));

        JScrollPane scroll = new JScrollPane(summary);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);

        highlighter = summary.getHighlighter();

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Legend"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

    }


    public void displaySummary(List<COG> info, Map<String, Color> colorsUsed) {
        StringBuilder sb = new StringBuilder();
        //sb.append(HEADER + NEWLINE);

        if (info.size() == 0) {
            sb.append(NO_COG_INFO_WARNNING);
        }

        List<HighlightInfo> highlightInfos = new ArrayList<>(info.size());

        for (COG cog: info){
            sb.append(NEWLINE);
            int startIndex = sb.length();
            sb.append(cog.getCogID());
            highlightInfos.add(new HighlightInfo(startIndex, sb.length(), colorsUsed.get(cog.getCogID())));

            sb.append(" ");
            sb.append(String.join("/",cog.getFunctional_categories()));
            sb.append(" | ");
            sb.append(cog.getCog_desc());
            sb.append(NEWLINE);
        }

        summary.setText(sb.toString());

        //highlight text
        Highlighter.HighlightPainter painter;
        for (HighlightInfo highlightInfo: highlightInfos) {
            painter = new DefaultHighlighter.DefaultHighlightPainter(highlightInfo.getColor());
            try {
                System.out.println(highlightInfo.getStart() + " " + highlightInfo.getEnd() + sb.length());
                highlighter.addHighlight(highlightInfo.getStart(), highlightInfo.getEnd(), painter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

    }

    private class HighlightInfo{

        private int start;
        private int end;
        private Color color;


        private HighlightInfo(int start, int end, Color color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public Color getColor() {
            return color;
        }
    }
}
