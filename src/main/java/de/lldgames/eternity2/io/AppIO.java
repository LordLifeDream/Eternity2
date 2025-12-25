package de.lldgames.eternity2.io;

import java.io.BufferedReader;
import java.util.function.Consumer;

public class AppIO {
    private ProcessOutputViewer view;

    public AppIO(){
        try{
            this.view = new ProcessOutputViewer();
            view.setVisible(true);
        }catch (Exception e){
            System.out.println("failed to create view. are we headless?");
        }
    }

    private void onErr(String ln){
        if(view!=null) this.view.addErr(ln);
    }
    private void onOutput(String ln){
        if(view!=null) this.view.addLog(ln);
    }

    public void addProcess(Process p){
        new Thread(()-> read(p.errorReader(), this::onErr)).start();
        new Thread(()-> read(p.inputReader(), this::onOutput)).start();
    }

    private void read(BufferedReader r, Consumer<String> onLine) {
        String line;
        try {
            while ((line = r.readLine()) != null) {
                onLine.accept(line);
            }
        }catch (Exception e){
            onLine.accept("line reader errored. :(");
        }
    }
}
