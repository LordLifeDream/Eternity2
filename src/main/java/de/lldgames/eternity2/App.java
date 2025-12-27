package de.lldgames.eternity2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.lldgames.eternity2.io.AppIO;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class App {
    private String remoteURL;
    private String localLocation;
    private String authentication;
    private String runCmd;
    private long restartTime = -1; //in milliseconds
    @JsonIgnore
    private Process process;
    @JsonIgnore
    private Git repo;
    @JsonIgnore
    private AppPullListener pl;
    @JsonIgnore
    private AppIO io;
    @JsonIgnore
    private Timer timer;
    @JsonIgnore
    private boolean restartLoopRunning = false;

    public App(){}

    @JsonCreator
    public App(
            @JsonProperty("remoteURL") String remoteURL,
            @JsonProperty("localLocation") String localLocation,
            @JsonProperty("authentication") String authentication,
            @JsonProperty("runCmd") String runCmd,
            @JsonProperty("restartTime") long restartTime
    ) {
        this.remoteURL = remoteURL;
        this.localLocation = localLocation;
        this.authentication = authentication;
        this.runCmd = runCmd;
        this.restartTime = restartTime;
        this.timer = new Timer();
    }

    public String getRemoteURL(){
        return this.remoteURL;
    }

/*
    public App(String remoteURL, String localLocation, String authentication, String runCmd){
        this.remoteURL = remoteURL;
        this.localLocation = localLocation;
        this.authentication = authentication;
        this.runCmd = runCmd;
    }*/

    public void createPullListener(){
        this.pl = new AppPullListener(this);
    }

    public void pullAndRestart(){
        System.out.println("pullAndRestart()");
        this.stop();
        this.pull();
        this.start();
    }

    public void restart(){
        this.stop();
        this.start();
    }

    private void startRestartTimer(){
        if(this.restartTime <0) return;
        if(this.restartLoopRunning) return;
        //this.timer.cancel();
        //this.timer = new Timer();
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                App.this.restart();
            }
        };
        this.timer.scheduleAtFixedRate(t, restartTime, restartTime);
        this.restartLoopRunning = true;
    }

    public void pull(){
        try {
            PullCommand cmd= this.repo.pull();

            if(this.authentication!=null) cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", this.authentication));

            cmd.call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLocalLocation() {
        return localLocation;
    }

    public String getAuthentication() {
        return authentication;
    }

    public String getRunCmd() {
        return runCmd;
    }

    /**
     * creates the repo object and pulls.
     */
    public void init(){
        try {
            this.repo = Git.open(new File(localLocation));
            //this.pull();
        }catch (Exception e){
            System.err.println("failed to init App "+ remoteURL+", trying clone...");
            this.cloneRepo();
            e.printStackTrace();
        }
        this.io = new AppIO();
        createPullListener();
        start();
    }

    private void cloneRepo(){
        try {
            CloneCommand c = Git.cloneRepository()
                    .setURI(this.remoteURL)
                    .setDirectory(new File(this.localLocation));
            if(this.authentication!=null) c.setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", this.authentication));

            try(Git git = c.call()){
                this.repo = git;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public boolean isRunning(){
        return this.process!=null && this.process.isAlive();
    }

    public void start(){
        if(isRunning()) {
            System.out.println("shit, tried to start but already running!");
            return;
        }
        try{
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
            //fix for npm on windows
            if(isWindows&& runCmd.contains("npm ")) runCmd= runCmd.replaceAll("npm ", "npm.cmd ");
            ProcessBuilder pb = new ProcessBuilder(this.runCmd.split(" "))
                    .directory(new File(this.localLocation));
            //pb.inheritIO();
            this.process = pb.start();
            io.addProcess(this.process);
        }catch (Exception e){
            e.printStackTrace();
        }
        this.startRestartTimer();
    }

    public void stop(){
        if(!isRunning())return;
        this.killChildren(this.process);
        this.process.destroy();
        System.out.println("stop() finished");
    }


    //taken from eternity v1
    private void killChildren(Process p){
        long pid = p.pid();
        //System.out.println("killing processes for "+pid);
        ProcessBuilder builder;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            builder = new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(pid));
        } else {
            builder = new ProcessBuilder("sh", "-c", "pkill -P " + pid);
        }

        try {
            //builder.inheritIO();
            builder.start().waitFor();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
