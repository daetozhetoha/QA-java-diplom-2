package edu.praktikum.diploma.generators;

import edu.praktikum.diploma.models.Order;

public class OrderGenerator {

    public static Order order(String ingredient) {
        return new Order().setIngredients(new String[]{ingredient});
    }
}
