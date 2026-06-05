package com.eventhub.eventservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "stadiums")
public class Stadium {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private String address;

    protected Stadium() {
    }

    public static Stadium create(UUID id, String name, String city, String country, int capacity, String address) {
        var stadium = new Stadium();
        stadium.id = id;
        stadium.name = name;
        stadium.city = city;
        stadium.country = country;
        stadium.capacity = capacity;
        stadium.address = address;
        return stadium;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getAddress() {
        return address;
    }
}
