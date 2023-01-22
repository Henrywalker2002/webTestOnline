package com.controller;

import com.model.Account;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class studentController {
    connectDb conn = new connectDb();
    MongoDatabase db = conn.gDatabase();
    MongoCollection <Account> stuCollection = db.getCollection("account", Account.class);
    MongoCollection <Document> stuDoc = db.getCollection("account");

    @RequestMapping(value="/student", method=RequestMethod.POST)
    public ResponseEntity<Document> addStudent(@RequestBody JsonNode json) {
        String username, name, password, email, avatar = "";
        int type = 0;
        int class_;
        Document res = new Document();
        try {
            username = json.get("username").asText();
            name = json.get("name").asText();
            password = json.get("password").asText();
            email = json.get("email").asText();
            class_ = json.get("class").asInt();
        }
        catch (Exception e) {
            res.append("message", "wrong json")
                .append("result", "fail");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        var temp = this.stuCollection.find(Filters.eq("username", username));
        if (temp.first() != null) {
            res.append("message", "username is already exist")
                .append("result", "fail");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        Account student = new Account(username, name, password, email, avatar, type, class_);
        this.stuCollection.insertOne(student);
        res.append("result", "ok")
            .append("message", "success");
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }
    
}
