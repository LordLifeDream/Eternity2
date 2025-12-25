package de.lldgames.eternity2.io;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class ProcessOutputViewer extends JFrame {
    private JPanel contentPane;
    private JScrollPane logScroll;
    private JPanel logPanel;
    private JTextField inputField;
    public ProcessOutputViewer(){
        this.contentPane = new JPanel(new BorderLayout());
        this.logPanel = new JPanel();
        this.logPanel.setLayout(new BoxLayout(this.logPanel, BoxLayout.Y_AXIS));

        this.logScroll = new JScrollPane(logPanel);
        this.contentPane.add(this.logScroll, BorderLayout.CENTER);

        this.inputField = new JTextField("I don't work yet!");
        this.contentPane.add(this.inputField, BorderLayout.SOUTH);

        this.setContentPane(this.contentPane);
        this.setTitle("Process Output");
        setSize(600, 400);
    }

    private void addLine(String ln, Color c){
        Date now = new Date(System.currentTimeMillis());
        String dateStr = now.toString();
        JLabel finalLine = new JLabel(dateStr+" | "+ln);
        finalLine.setForeground(c);
        if(logPanel.getComponents().length>100){
            logPanel.remove(0);
        }
        logPanel.add(finalLine);
        logPanel.revalidate();
    }

    public void addErr(String line){
        addLine(line, Color.RED);
    }

    public void addLog(String line){
        addLine(line, Color.BLUE);
    }
}
