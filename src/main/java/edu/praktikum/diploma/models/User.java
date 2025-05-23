package edu.praktikum.diploma.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class User {
    private String email;
    private String password;
    private String name;
}
