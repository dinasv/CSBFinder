package MVC.View.Components.Panels;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import MVC.View.Graphics.GeneColors;
import Model.OrthologyGroups.COG;


public class CSBSummaryPanel extends JPanel {

    private JTextPane summary;
    private JScrollPane scroll;

    private static final String NEWLINE = "\n";
    private static final String NO_COG_INFO_WARNING = "Oops, No gene information was found. " +
            "A gene orthology information file was not specified, or it did not load properly";

    private Style titleStyle;
    private Style textStyle;
    private Highlighter highlighter;

    private String missingInfoText;

    public CSBSummaryPanel() {
        missingInfoText = NO_COG_INFO_WARNING;

        setLayout(new BorderLayout());
        summary = new JTextPane();
        summary.setEditable(false);

        titleStyle = summary.addStyle("Title", null);
        StyleConstants.setFontSize(titleStyle, 12);
        StyleConstants.setBold(titleStyle, true);

        textStyle = summary.addStyle("Text", null);

        scroll = new JScrollPane(summary);
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


    public void displaySummary(List<COG> patternGenes, Collection<COG> InsertedGenes, GeneColors colorsUsed) {

        clearPanel();

        StyledDocument doc = summary.getStyledDocument();

        if (patternGenes.size() == 0) {
            try {
                doc.insertString(doc.getLength(), missingInfoText, textStyle);
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
                if (highlightInfo == null){
                    continue;
                }
                painter = new DefaultHighlighter.DefaultHighlightPainter(highlightInfo.getColor());
                try {

                    highlighter.addHighlight(highlightInfo.getStart(), highlightInfo.getEnd(), painter);
                } catch (BadLocationException e) {
                    //skip
                }
            }
            summary.setCaretPosition(0);
        }
    }

    private void appendGenes(Collection<COG> genes, String title, List<HighlightInfo> highlightInfos,
                             GeneColors colorsUsed, Set<COG> genesUsed, StyledDocument doc){

        if (genes.size() > 0) {
            try {

                doc.insertString(doc.getLength(), title +"\n", titleStyle);

                for (COG cog : genes) {
                    if (!genesUsed.contains(cog)) {
                        genesUsed.add(cog);

                        int startIndex = doc.getLength();

                        doc.insertString(doc.getLength(), cog.getCogId(), textStyle);
                        //sb.append(cog.getCogID());
                        highlightInfos.add(new HighlightInfo(startIndex, doc.getLength(), colorsUsed.getColor(cog.getCogId())));

                        StringBuilder sb = new StringBuilder();
                        sb.append(" ");
                        sb.append(String.join("/", cog.getFunctionalCategories()));
                        sb.append(" | ");
                        sb.append(cog.getCogDesc());
                        sb.append(" | ");
                        sb.append(cog.getGeneName());

                        sb.append(NEWLINE);

                        doc.insertString(doc.getLength(), sb.toString(), textStyle);

                    }
                }

            } catch (BadLocationException e) {
            }

        }
    }


    public void setMissingInfoText(String text){
        missingInfoText = text;
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
