package com.example.BaniSWVL;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.example.Log.Log;
import com.example.Rides.Offer;
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
        system.listAllUsers();
        currentUser = system.getUser(info.getUserName());
        return "Success! your account is created and logged in";
    }

    @PostMapping("/signup/user/driver")
    public String signupDriver(@RequestBody Map<String, String> json) throws ParseException {
        System.out.println(json);
        if(currentUser != null)
            return "you are already logged in please logout to make a new account";
        String userName = json.get("userName");
        String mobileNumber = json.get("mobileNumber");
        String email = json.get("email");
        String password = json.get("password");

        Date dob=new SimpleDateFormat("yyyy-MM-dd").parse(json.get("dob"));

        String driverLicense = json.get("driverLicense");
        String nationalId = json.get("nationalId");
        DriverInfo info = new DriverInfo((new Info(userName,mobileNumber,email,password,dob)),driverLicense,nationalId);
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
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> json)
    {
        System.out.println(json);
        if(currentUser != null)
        {
            return "You are already logged in";
        }
        currentUser = system.login(json.get("username"),json.get("password"));
        if(currentUser == null)
        {
            return "username or password is wrong";
        }

        if(currentUser instanceof Driver) {
            if (((Driver) currentUser).getState() == State.Suspended) {
                currentUser = null;
                return "This account is suspended";
            } else if (((Driver) currentUser).getState() == State.Pending) {
                currentUser = null;
                return "This account is pending";
            }
        }
        if (currentUser instanceof Client) {
            if (((Client) currentUser).getState() == State.Suspended) {
                currentUser = null;
                System.out.println("This account is suspended");
            }
        }
        return "Welcome back " + currentUser.getUserData().getUserName() + " !";
    }

    @GetMapping("/logout")
    public String logout() {
        if(currentUser == null)
            return "You are already logged out";
        currentUser = null;
        return "Success! you are logged out";

    }
    @GetMapping("/driver/getrides")
    public List<RideRequest> getRideRequests(){
        if (currentUser == null)
        {
            return null;
        }
        if(!(currentUser instanceof Driver))
            return null;
        List<RideRequest> rideRequests = new ArrayList<RideRequest>();
        for (RideRequest rideRequest : system.getRideRequests()) {
            if (system.checkdriver((Driver) currentUser, rideRequest.getSource()))
                rideRequests.add(rideRequest);
        }
        return rideRequests;
    }
    @PostMapping("/driver/makeoffer")
    public String makeOffer(@RequestBody Map<String,String> json) {
        if (currentUser == null)
        {
            return "you are not logged in";
        }
        if(!(currentUser instanceof Driver))
            return "you are not a driver";
        if(((Driver) currentUser).getState() == State.Busy)
        {
            return "you are busy now!";
        }
        int index = Integer.parseInt(json.get("index"));
        double price = Double.parseDouble(json.get("price"));
        List<RideRequest> rideRequests = new ArrayList<RideRequest>();
        for (RideRequest rideRequest : system.getRideRequests()) {
            if (system.checkdriver((Driver) currentUser, rideRequest.getSource()))
                rideRequests.add(rideRequest);
        }
        if(index >= rideRequests.size())
            return "please enter a valid index";
        boolean success = system.driverMakingOffer((Driver) currentUser,rideRequests.get(index),price);
        if(success)
            return "Success! Offer is added and client is notified";
        else
            return "Failure";
    }

    @GetMapping("/driver/getoffers")
    public List<Offer> getOffersDriver()
    {
        if (currentUser == null)
        {
            return null;
        }
        if(!(currentUser instanceof Driver))
            return null;
        return ((Driver) currentUser).getOffers();
    }
    @GetMapping("/driver/notifyarrival")
    public String notifyArrival()
    {
        if (currentUser == null)
        {
            return "your are not logged in";
        }
        if(!(currentUser instanceof Driver))
            return "you are not a driver";
        if(((Driver)currentUser).getState() == State.Available)
            return "you are not busy with a ride";
        ((Driver)currentUser).arrivalAtLocation(system);
        return "client has been notified by your arrival";
    }
    @GetMapping("/driver/endride")
    public String endRide()
    {
        if (currentUser == null)
        {
            return "your are not logged in";
        }
        if(!(currentUser instanceof Driver))
            return "you are not a driver";
        if(((Driver)currentUser).getState() == State.Available)
            return "you are not busy with a ride";
        ((Driver)currentUser).endRide(system);
        return "Success! ride has been finished";
    }


// client
   @PostMapping("/client/ratedriver")
   public String rateDriver(@RequestBody Map<String,String> json){
     if(currentUser == null)
         return "you are not logged in";
     if(!(currentUser instanceof Client))
     {
         return "you are not a client";
     }
     int rating = Integer.parseInt(json.get("rating"));
     String driverUserName = json.get("driverUserName");
     String comment = "";
     if(rating < 1 || rating > 5)
           return "Rating should be from 1 to 5";
     if(json.containsKey("comment")) {
         comment = json.get("comment");
     }
     UserRating userRating = ((Client)currentUser).rateDriver(rating, comment);
     boolean success = system.rateDriver(userRating, driverUserName);
     if(success)
         return "Rating submitted for" + driverUserName;
     else
         return "no such driver exists";
    }

    @GetMapping("/client/getoffers")
    public List<Offer> getOffersClient()
    {
        if(currentUser == null)
            return  null;
        if(!(currentUser instanceof Client))
        {
            return null;
        }
        return ((Client)currentUser).getOffers();
    }

    @PostMapping("/client/acceptOffer")
    public String acceptOffer(@RequestBody Map<String,String> json) {
        if(currentUser == null)
            return  null;
        if(!(currentUser instanceof Client))
        {
            return null;
        }
        int offerIndex = Integer.parseInt(json.get("index"));

        ArrayList<Offer> offers = ((Client)currentUser).getOffers();
        if(offerIndex > offers.size() || offerIndex < 1)
            return "please enter a valid index";
        system.clientAcceptOffer(offers.get(offerIndex - 1), (Client) currentUser);
        ((Client) currentUser).removeOffer(offerIndex - 1);
        ((Client)currentUser).incrementRide();
        return "Success! Driver is on his way";
    }





//    some admin
    @GetMapping("/admin/PendingDrivers")
    public ArrayList<Driver> getPendingDrivers(){
        if(currentUser instanceof Admin){
            return system.ListPendingDrivers();
        }
        else
            return null;
    }

    @PostMapping("/admin/verifyDriver")
    public String verifyDriver(@RequestBody String userName){
        if(currentUser == null)
            return "you are not logged in";
        if(currentUser instanceof Admin){
            ((Admin) currentUser).verifyDriverRegistration(userName,getPendingDrivers());
            return userName +" is accepted successfully!";
        }
        else{return "Error: you must be an admin";}
    }
}