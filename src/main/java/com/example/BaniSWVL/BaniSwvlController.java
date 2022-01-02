package com.example.BaniSWVL;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;


import com.example.Log.Log;
import com.example.Rides.RideRequest;
import com.example.Users.*;
import com.example.System.MainSystem;
import com.example.System.MemorySystem;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BaniSwvlController {
    private MainSystem system;
    private User currentUser;
    private User driver;
    public BaniSwvlController() {system = new MemorySystem(); currentUser = new Admin(new Info("peter", "010000000", "0000000000", "0", null));
        driver = new Driver(new Info("d", "010000000", "0000000000", "0", null));
    system.addUser(driver);
    }

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
        if(currentUser instanceof Admin){return system.getPendingDrivers();}
        else {return null;}
    }

    @PostMapping("/baniswvl/verifyDriver")
    public String verifyDriver(@RequestBody String userName){
        if(currentUser instanceof Admin){
            if(userName!=null && system.getPendingDrivers().contains(userName)) {
                ((Admin) currentUser).verifyDriverRegistration(userName, getPendingDrivers());
                return "Driver is accepted successfully!";
            }
            else{return "Driver doesn't exist!";}
        }
        else{return "Error";}
    }

    @PostMapping("/baniswvl/requestride")
    public String requestRide(@RequestBody RideRequest rideRequest) {
        if (currentUser instanceof Client) {
            new RideRequest(rideRequest.getSource(), rideRequest.getDestination(),
                    rideRequest.getClientUserName(), rideRequest.getNumberOfPassengers());
            boolean success = system.updateSystemRideRequests(rideRequest);
            if (success) {return "Relevant drivers have been notified...";}
            else {return ("Area doesn't exist in our database!");}
        }
        else{return"Error";}
    }

    @PostMapping("/baniswvl/addArea")
    public String addArea(@RequestBody Map<String, String> json){
        if(currentUser instanceof Driver){
            String area = json.get("area");
            boolean success = system.addAreaToDriver(area, (Driver) currentUser);
            if (success) {return "area is added successfully";}
            else{return "Area already exists!";}
        }
       else {return "Error";}
    }

    @PostMapping("/baniswvl/suspend")
    public String suspendDriver(@RequestBody Map<String, String> json){
        if(currentUser instanceof Admin){
            String userName= json.get("userName");
            if(userName!=null && system.getUser(userName)!=null ) {
                User user = system.getUser(userName);
                ((Admin) currentUser).suspendUser(user);
                return "Driver is suspended successfully!";
            }
            else{return "Driver doesn't exist!";}
        }
        else{return "Error";}
    }
    @GetMapping("/baniswvl/listratings")
    public ArrayList<UserRating> getRatings(){
        if(currentUser instanceof Driver){return ((Driver) currentUser).ListUserRatings();}
        else{return null;}
    }
}