package de.lldgames.eternity2;

import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;

public class AuthManager {
    //THIS IS username -> token
    private HashMap<String, String> usernameTokenMap = new HashMap<>();

    public HashMap<String, String> getUsernameTokenMap(){
        return this.usernameTokenMap;
    }

    public void save(){
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File("./authentication.json"), this);
    }

    public void addUser(String username, String token){
        this.usernameTokenMap.put(username, token);
        this.save();
    }

    public String getToken(String username){
        return usernameTokenMap.get(username);
    }
}
