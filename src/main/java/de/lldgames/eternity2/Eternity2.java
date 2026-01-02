package de.lldgames.eternity2;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Eternity2 {
    public static AuthManager authManager;

    public static void main(String[] args) {
        if(args.length == 0){
            printHelp();
            return;
        }
        AppManager.load();
        loadAuthManager();
        //AppManager.instance.apps.get(0).init();
        //initAddCommand();
        String cmd = args[0];
        switch (cmd.toLowerCase()){
            case "start"->start();
            case "add"->initAddCommand();
            case "remove"->initRemove();
            case "adduser"->initAddToken();
        }


    }

    private static void loadAuthManager(){
        try {
            authManager = new ObjectMapper().readValue(new File("./authentication.json"), AuthManager.class);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("auth manager not set up yet. Creating new one...");
            authManager = new AuthManager();
            authManager.save();
        }
    }

    private static void start(){
        for(App app: AppManager.instance.apps){
            app.init();
        }
        System.out.println("tried to start " +AppManager.instance.apps.size() + " apps.");
    }

    private static void initAddToken(){
        Scanner sc = new Scanner(System.in);
        System.out.println("please enter the GitHub username");
        String user = sc.nextLine();
        System.out.println("please enter the token");
        String token = sc.nextLine();
        System.out.println("Thank you. Please verify this information:");
        System.out.println("username: " + user);
        System.out.println("token: " + token);
        System.out.println("y/n");
        String input = sc.nextLine();
        if(input.toLowerCase().trim().equals("y")){
            authManager.addUser(user, token);
            System.out.println("User added! Thank you for your time and patience!");
            System.exit(0);
            return;
        }
        System.out.println("user add cancelled!");
        System.exit(0);
    }

    private static void initRemove(){
        Scanner sc = new Scanner(System.in);
        System.out.println("select an app to remove:");
        for(int i = 0; i<AppManager.instance.apps.size(); ++i){
            App app = AppManager.instance.apps.get(i);
            String line = i+" - "+ app.getRemoteURL() + " (@ " + app.getLocalLocation()+ ")";
            System.out.println(line);
        }
        int idx = sc.nextInt();
        if(idx<0) {
            System.out.println("cancelled. Nothing was removed.");
            return;
        }
        AppManager.instance.apps.remove(idx);
        System.out.println("app removed. Saving...");
        AppManager.save();
        sc.close();
    }

    private static void initAddCommand(){
        Scanner sc = new Scanner(System.in);
        System.out.println("please enter the repository remote (e.g. https://github.com/LordLifeDream/eternity2.git");
        String remote = sc.nextLine();
        System.out.println("please enter a local location.");
        String localLoc = sc.nextLine();
        System.out.println("please enter your GH username (should be mapped to an token in ./authentication.json), or leave blank for none.");
        String token = sc.nextLine();
        if(token.isEmpty()) token = null;
        System.out.println("please enter a run command (e.g. java -jar eternity2.jar)");
        String runCmd = sc.nextLine();
        System.out.println("please enter your restart time in minutes (negative = no restart).");
        long restartTime = sc.nextLong()*60*1000; //*60-> seconds *1000-> milliseconds
        System.out.println("please confirm these are correct:");
        System.out.println("remote: " +remote);
        System.out.println("local: " + localLoc);
        System.out.println("token: " + token);
        System.out.println("runCmd: " + runCmd);
        System.out.println("restartTime: " + restartTime+"ms");
        System.out.println("y/n");
        String input = sc.next();
        if(input.toLowerCase().trim().equals("y")){
            App app = new App(remote, localLoc, token, runCmd, restartTime);
            AppManager.instance.addApp(app);
            app.init();
            sc.close();
            return;
        }
        System.out.println("addition cancelled.");
        sc.close();
    }

    private static void printHelp(){
        System.out.println("welcome to Eternity2.");
        System.out.println("to start eternity, use the start argument.");
        System.out.println("to add an app, use add argument.");
        System.out.println("to remove an app, use remove argument.");
        System.out.println("to add an account, use adduser");
    }
}
