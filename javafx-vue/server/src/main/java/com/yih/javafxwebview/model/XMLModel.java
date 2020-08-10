package com.yih.javafxwebview.model;

public class XMLModel {
    private String name;
    private int age;

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void selfIntroduce() {
        System.out.println(">>>>> My name is " + name + ", and " + age + " years old.");
    }
}
