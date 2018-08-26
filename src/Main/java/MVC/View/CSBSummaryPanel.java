package MVC.View;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class CSBSummaryPanel extends JPanel {

    JTextArea summary;
    private static final String HEADER = "CSB Information";
    private static final String NEWLINE = "\n";
    private static final String NO_COG_INFO_WARNNING = "Oops, No cog info found. Did you load the cog info file?";

    CSBSummaryPanel() {
        setLayout(new BorderLayout());
        summary = new JTextArea();
        summary.setEditable(false);
        summary.setFont(new Font("Serif", Font.PLAIN, 16));
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(summary);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);
    }

    public void displaySummary(Map<String, String> info) {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER + NEWLINE);

        if (info.size() == 0) {
            sb.append(NO_COG_INFO_WARNNING);
        }

        info.keySet().forEach(s -> {
            sb.append(NEWLINE);
            sb.append(s + ": ");
            sb.append(info.get(s)) ;
            sb.append(NEWLINE);
        });

        summary.setText(sb.toString());
    }
}
