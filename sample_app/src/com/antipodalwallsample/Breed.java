package com.antipodalwallsample;

public class Breed {
	private String name;
	private int imgId;
	
	public Breed(String name, int imgId) {
		super();
		this.name = name;
		this.imgId = imgId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getImgId() {
		return imgId;
	}
	public void setImgId(int imgId) {
		this.imgId = imgId;
	}
}
