package com.example.BaniSWVL;


import java.util.ArrayList;
import java.util.List;


import com.example.Log.Log;
import com.example.Users.DriverInfo;
import com.example.Users.Info;
import com.example.Users.User;
import com.example.System.MainSystem;
import com.example.System.MemorySystem;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.Users.Driver;
import com.example.Users.Admin;


@RestController
public class BaniSwvlController {
    private MainSystem system;
    private User currentUser;
    public BaniSwvlController() {system = new MemorySystem(); currentUser = null;}

    @PostMapping("/signup/user/client")
    public String singupClient(@RequestBody Info info) {
        if(currentUser != null)
            return "you are already logged in please logout to make a new account";
        currentUser = system.getUser(info.getUserName());
        if(currentUser != null) {
            currentUser = null;
            return "username already exist";
        }
        currentUser =  system.register(info);
//        system.listAllUsers();
        currentUser = system.getUser(info.getUserName());
        return "Success! your account is created and logged in";
    }

    @PostMapping("/signup/user/driver")
    public String signupDriver(@RequestBody DriverInfo info) {
        if(currentUser != null)
            return "you are already logged in please logout to make a new account";
        currentUser = system.getUser(info.getUserName());
        if(currentUser != null) {
            currentUser = null;
            return "username already exist";
        }
        currentUser =  system.register(info);
        system.listAllUsers();
        currentUser = system.getUser(info.getUserName());
        return "Success! your account is created and logged in";
    }
    @GetMapping("/logout")
    public String logout() {
        if(currentUser == null)
            return "You are already logged out";
        currentUser = null;
        return "Success! you are logged out";

    }
    @GetMapping("/baniswvl/PendingDrivers")
    public ArrayList<Driver> getPendingDrivers(){
        if(currentUser instanceof Admin){
            return system.ListPendingDrivers();
        }
        else
            return null;
    }

    @PostMapping("/baniswvl/verifyDriver")
    public String verifyDriver(@RequestBody String userName){
        if(currentUser instanceof Admin){
            ((Admin) currentUser).verifyDriverRegistration(userName,getPendingDrivers());
            return userName +" is accepted successfully!";
        }
        else{return "Error: you must be an admin";}
    }
}