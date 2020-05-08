package com.example.chatfunction;


//getter and setter method for getting user from database and storing it into variables

public class Contacts
{
    public String name, status, image;

    public Contacts()
    {
        //empty constructor
    }

    public Contacts(String name, String status, String image)
    {
        this.name = name;
        this.status = status;
        this.image = image;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }
}
