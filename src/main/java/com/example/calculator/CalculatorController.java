package com.example.calculator;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalculatorController {

    private final Calculator calculator;

    public CalculatorController(Calculator calculator) {
        this.calculator = calculator;
    }

    @RequestMapping("/")
    public String index() {
        return "health check - Poll SCM test";
    }

    @RequestMapping("/add")
    public String add(@RequestParam("num1") int num1,
                      @RequestParam("num2") int num2) {
        return String.valueOf(calculator.addition(num1, num2));
    }

    @RequestMapping("/sub")
    public String sub(@RequestParam("num1") int num1,
                      @RequestParam("num2") int num2) {
        return String.valueOf(calculator.subtraction(num1, num2));
    }

    @RequestMapping("/mul")
    public String mul(@RequestParam("num1") int num1,
                      @RequestParam("num2") int num2) {
        return String.valueOf(calculator.multiplication(num1, num2));
    }
}