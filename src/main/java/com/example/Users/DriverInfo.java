package com.example.Users;

import java.util.Date;

public class DriverInfo extends Info {
    private final String driverLicense;
    private final String nationalId;
    public DriverInfo()
    {
        super();
        this.driverLicense = null;
        this.nationalId = null;
    }
    public DriverInfo(String userName,String mobileNumber,String email,String password,Date dob,String driverLicense,String nationalId)
    {
        super(userName,mobileNumber,email,password,dob);
        this.driverLicense = driverLicense;
        this.nationalId = nationalId;;
    }
    public DriverInfo(Info data, String driverLicense, String nationalId) {
        super(data);
        this.driverLicense = driverLicense;
        this.nationalId = nationalId;
    }

    public String toString()
    {
        return String.format("user name: " + getUserName() + "\nmobile number: " + getMobileNumber() + "\nDate of Birth: " + getDob() + "\nEmail: "
                + getEmail() + "\nnational Id: " + nationalId + "\ndriver license: " + driverLicense);
    }
}
