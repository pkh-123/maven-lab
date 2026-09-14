package com.example.calculator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAdditionFunctionality {

    @Test
    public void testAdditionWithPositiveNumbers() {
        Calculator calculator = new Calculator();

        int result = calculator.addition(10, 20);

        Assert.assertEquals(result, 30);
    }
}