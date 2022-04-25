package com.maltaisn.calcdialoglib.demo;

public class User {
    public String fullName;

    public User(){}

    public User(String fullName) 
	{
        this.fullName = fullName;
    }
	
	public String getFullName()
	{
		return fullName;
	}
	
	public void setFullName(String newName)
	{
		this.fullName = newName;
	}
}
