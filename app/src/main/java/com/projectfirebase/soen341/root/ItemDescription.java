package com.projectfirebase.soen341.root;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class ItemDescription extends Listing {
    private String Description;
    public ItemDescription(){
        super();
        this.Description = "";
    }

    public ItemDescription(Listing listing, String d){
        super(listing.getID(), listing.getName(), listing.getPrice(), listing.getImageURL());
        this.Description = d;
    }

    public ItemDescription(String id, String UserId, String name, double price, String url, String desc){
        super(id, UserId);
        this.setName(name);
        this.setPrice(price);
        this.setImageURL(url);
        this.setDescription(desc);
    }

    public ItemDescription(String id, String UserId, String name, double price, String url, String desc, int category, int subCategory){
        super(id, UserId);
        this.setName(name);
        this.setPrice(price);
        this.setImageURL(url);
        this.setDescription(desc);
        this.setCategory(category);
        this.setSubCategory(subCategory);
    }

    @PropertyName("Description")
    public String getDescription(){
        return this.Description;
    }

    @Exclude
    public void setDescription(String description){
        this.Description = description;
    }
}
