package edu.praktikum.diploma.generators;

import com.github.javafaker.Faker;
import edu.praktikum.diploma.models.User;

public class UserGenerator {
    public static Faker faker = new Faker();

    public static User randomUser() {
        return new User()
                .setEmail(faker.internet().safeEmailAddress())
                .setName(faker.name().username())
                .setPassword(faker.internet().password(12, 20));
    }
}
