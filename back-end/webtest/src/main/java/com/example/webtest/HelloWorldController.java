package com.example.webtest;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloWorldController {
    
    @RequestMapping(value = "/")
    public ResponseEntity hello() {
        return new ResponseEntity("Hello World!", HttpStatus.OK);
    }
}   

