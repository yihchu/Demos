package com.joker.dto;

import com.joker.annotation.FromEntity;

@FromEntity(clazz = "com.joker.entity.PersonEntity")
public class PersonDto {
    private int age;
}
