package com.joker.entity;

import com.joker.annotation.Greet;

import java.util.HashMap;
import java.util.Map;

@Greet(data = "person")
public class PersonEntity {

    public int age;
    public String name;
    public Map<String, String> data;

    public PersonEntity(int age, String name) {
        this.age = age;
        this.name = name;
        this.data = new HashMap<>();
    }

}
