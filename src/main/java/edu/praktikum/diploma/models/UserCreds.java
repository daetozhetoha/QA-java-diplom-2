package edu.praktikum.diploma.models;

import com.github.javafaker.Faker;

public class UserCreds {
    private String email;
    private String password;
    private String name;
    static Faker faker = new Faker();


    private UserCreds(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public static UserCreds credsFromUser(User user) {
        return new UserCreds(user.getEmail(), user.getPassword(), null);
    }

    public static UserCreds credsWithWrongEmail(User user) {
        return new UserCreds(faker.internet().safeEmailAddress(), user.getPassword(), null);
    }

    public static UserCreds credsWithWrongPassword(User user) {
        return new UserCreds(user.getEmail(), faker.internet().password(12, 20), null);
    }

    public static UserCreds credsWithNullEmail(User user) {
        return new UserCreds(null, null, user.getName());
    }

    public static UserCreds credsWithNullName(User user) {
        return new UserCreds(user.getEmail(), null, null);
    }
}
