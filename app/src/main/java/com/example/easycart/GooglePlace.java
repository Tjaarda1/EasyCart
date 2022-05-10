package com.example.easycart;

// Clase donde guardamos datos de los supermercados que buscamos
public class GooglePlace {
    
    private String name;
    private String latitude;
    private String longitude;
    private String city;
    private String street;
    private String postalCode;

    public GooglePlace() {
        this.name = "";
        this.latitude = "";
        this.longitude = "";
        this.city = "";
        this.street = "";
        this.postalCode = "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getName() {
        return name;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

}
