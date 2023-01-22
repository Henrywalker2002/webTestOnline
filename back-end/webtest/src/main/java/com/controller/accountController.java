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
public class accountController {

    connectDb conn = new connectDb();
    MongoDatabase db = conn.gDatabase();
    MongoCollection <Account> accCollection = db.getCollection("account", Account.class);
    MongoCollection <Document> accDoc = db.getCollection("account");

    @RequestMapping(value = "/account", method = RequestMethod.POST)
    public ResponseEntity<Document> getStudent(@RequestBody JsonNode json) {
        String username;
        Document res = new Document();
        try {
            username = json.get("username").asText();
        }
        catch (Exception e) {
            res.append("message", "wrong json")
                .append("result", "fail");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        var temp = this.accCollection.find(Filters.eq("username", username));
        if (temp.first() == null) {
            res.append("message", "username is not exist")
                .append("result", "fail");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        res.append("result", "ok")
            .append("message", temp.first());
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }


}
