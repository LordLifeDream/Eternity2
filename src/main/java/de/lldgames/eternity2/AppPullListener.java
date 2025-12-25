package de.lldgames.eternity2;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppPullListener {
    private String repoName;
    private App app;

    public AppPullListener(App app){
        this.app = app;
        String remoteUrl = app.getRemoteURL();
        Pattern p = Pattern.compile(".*//github\\.com/(.+)\\.git");
        Matcher m = p.matcher(remoteUrl);
        m.matches();
        this.repoName = m.group(1);
        createClient();
    }

    private void createClient(){
        WebSocketClient cl = new WebSocketClient(URI.create(AppManager.instance.GHWWUrl)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                this.send("{\"t\":\"GREETINGS\",\"token\":\""+AppManager.instance.GHWWToken+"\"}");
            }

            @Override
            public void onMessage(String message) {
                System.out.println(message);
                JsonNode root =AppManager.mapper.readTree(message);
                String type = root.get("t").asString();
                switch(type){
                    case "GREETINGS_ACK"-> this.send("{\"t\":\"SUBSCRIBE\",\"repo\":\""+repoName+"\"}");
                    case "EVENT"->{
                        String eventType = root.get("type").asString();
                        if(eventType.toLowerCase().equals("push")){
                            app.pullAndRestart();
                        }
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("closed");
                try {
                    TimeUnit.MILLISECONDS.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //try again after 5 seconds
                createClient();
            }

            @Override
            public void onError(Exception ex) {

            }
        };
        cl.connect();
    }
}
