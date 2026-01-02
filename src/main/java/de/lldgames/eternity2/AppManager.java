package de.lldgames.eternity2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppManager {
    @JsonIgnore
    public static final ObjectMapper mapper = new JsonMapper.Builder(new JsonFactory())
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build()
            ;
    @JsonIgnore
    public static AppManager instance;
    public List<App> apps = new ArrayList<>();
    public String GHWWUrl;
    public String GHWWToken;

    public static void load(){
        if(new File("./config.json").exists()) {
            //var conf = mapper.deserializationConfig().without(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
            AppManager mngr = mapper
                    .readValue(new File("./config.json"), AppManager.class);
            instance = mngr;
        }else {
            System.out.println("config.json not found!");
            instance = create();
            save();
        }
    }

    public AppManager(){

    }

    public AppManager(String GHWWUrl, String GHWWToken){
        this.GHWWUrl = GHWWUrl;
        this.GHWWToken = GHWWToken;
    }

    public static AppManager create(){
        Scanner sc = new Scanner(System.in);
        System.out.println("--AM Setup--\nplease enter your GHWW url.");
        String url = sc.nextLine();
        System.out.println("thank you. Please enter your GHWW token next.");
        String token = sc.nextLine();
        System.out.println("thanks. Remember that you can change these in config_.json.");
        return new AppManager(url, token);
    }

    public static void save(){
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("./config.json"), instance);
    }


    public void addApp(App app){
        this.apps.add(app);
        this.save();
    }

}
