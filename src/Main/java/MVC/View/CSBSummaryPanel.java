package MVC.View;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import MVC.View.Shapes.Shape;
import Utils.COG;


public class CSBSummaryPanel extends JPanel {

    JTextPane summary;
    private static final String NEWLINE = "\n";
    private static final String NO_COG_INFO_WARNNING = "Oops, No gene information was found. Did you load the gene orthology info file?";

    private Style titleStyle;
    private Style textStyle;
    Highlighter highlighter;

    CSBSummaryPanel() {
        setLayout(new BorderLayout());
        summary = new JTextPane();
        summary.setEditable(false);
        summary.setFont(new Font("Serif", Font.PLAIN, 16));


        titleStyle = summary.addStyle("Title", null);
        StyleConstants.setFontFamily(titleStyle, "Serif");
        StyleConstants.setFontSize(titleStyle, 20);
        StyleConstants.setBold(titleStyle, true);

        textStyle = summary.addStyle("Title", null);
        StyleConstants.setFontFamily(textStyle, "Serif");
        StyleConstants.setFontSize(textStyle, 16);


        JScrollPane scroll = new JScrollPane(summary);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);

        highlighter = summary.getHighlighter();

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Legend"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

    }

    public void clearPanel(){
        summary.setText("");
    }


    public void displaySummary(List<COG> patternGenes, Collection<COG> InsertedGenes, Map<String, Color> colorsUsed) {

        clearPanel();

        StyledDocument doc = summary.getStyledDocument();

        if (patternGenes.size() == 0) {
            try {
                doc.insertString(doc.getLength(), NO_COG_INFO_WARNNING, textStyle);
            } catch (BadLocationException e) {
            }
        }else {
            List<HighlightInfo> highlightInfos = new ArrayList<>(patternGenes.size() + InsertedGenes.size());
            Set<COG> genesUsed = new HashSet<>();

            appendGenes(patternGenes, "CSB Genes:", highlightInfos, colorsUsed, genesUsed, doc);
            appendGenes(InsertedGenes, "Inserted Genes:", highlightInfos, colorsUsed, genesUsed, doc);


            //highlight text
            Highlighter.HighlightPainter painter;
            for (HighlightInfo highlightInfo : highlightInfos) {
                painter = new DefaultHighlighter.DefaultHighlightPainter(highlightInfo.getColor());
                try {
                    highlighter.addHighlight(highlightInfo.getStart(), highlightInfo.getEnd(), painter);
                } catch (BadLocationException e) {

                }
            }
        }
    }

    private void appendGenes(Collection<COG> genes, String title, List<HighlightInfo> highlightInfos,
                             Map<String, Color> colorsUsed, Set<COG> genesUsed, StyledDocument doc){

        if (genes.size() > 0) {

            try {

                doc.insertString(doc.getLength(), title +"\n", titleStyle);

                for (COG cog : genes) {
                    if (!genesUsed.contains(cog)) {
                        genesUsed.add(cog);

                        int startIndex = doc.getLength();

                        doc.insertString(doc.getLength(), cog.getCogID(), textStyle);
                        //sb.append(cog.getCogID());
                        highlightInfos.add(new HighlightInfo(startIndex, doc.getLength(), colorsUsed.get(cog.getCogID())));

                        StringBuilder sb = new StringBuilder();
                        sb.append(" ");
                        sb.append(String.join("/", cog.getFunctional_categories()));
                        sb.append(" | ");
                        sb.append(cog.getCog_desc());
                        sb.append(" | ");
                        sb.append(cog.getGeneName());

                        sb.append(NEWLINE);
                        sb.append(NEWLINE);

                        doc.insertString(doc.getLength(), sb.toString(), textStyle);

                    }
                }

            } catch (BadLocationException e) {
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
