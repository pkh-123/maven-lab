package com.example.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAdditionFunctionality {

    @Test
    public void testAdditionWithPositiveNumbers() {
        Calculator calculator = new Calculator();

        int result = calculator.addition(10, 20);

        assertEquals(31, result);
    }
}