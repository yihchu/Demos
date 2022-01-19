
package com.joker.param;


import com.joker.annotation.FromEntity;

import java.util.Map;

@FromEntity(clazz = "com.joker.entity.PersonEntity")
public class PersonParam {
    private int age;
    private String name;
    private Map<String, String> data;

}
