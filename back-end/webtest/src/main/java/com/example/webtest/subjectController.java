package com.example.webtest;

import com.controller.connectDb;
import com.model.Subject;
import com.fasterxml.jackson.databind.JsonNode;

import com.mongodb.client.MongoDatabase;
import java.util.List;


import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.BasicDBObject;

@Controller
public class subjectController {
    @RequestMapping(value= "/getAllSubject", method = RequestMethod.GET)
    public ResponseEntity<List<Document>> getAll() {
        connectDb conn = new connectDb();
        MongoDatabase db = conn.gDatabase();

        MongoCollection <Document> account = db.getCollection("subject");
        MongoCursor <Document> cursor = account.find().iterator();
        List<Document> res = new ArrayList<Document>();

        while(cursor.hasNext()) {
            var temp = cursor.next();
            temp.remove("_id");
            res.add(temp);
        }
        return new ResponseEntity<List<Document>>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/addSubject", method = RequestMethod.POST)
    public ResponseEntity<Document> addSubject(@RequestBody JsonNode json) {
        // get data from json
        String name, teacherId, topic;
        try {
            name = json.get("name").textValue();
            teacherId = json.get("teacherId").textValue();
            topic = json.get("topic").textValue();
        }
        catch (Exception e) {
            Document res = new Document("result", "fail");
            res.append("message", "wrong json");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }

        int id = ThreadLocalRandom.current().nextInt(10000, 99999);

        connectDb conn = new connectDb();
        MongoDatabase db = conn.gDatabase();
        MongoCollection <Subject> subjCollection = db.getCollection("subject", Subject.class);

        // if id is exist, random again
        var temp = subjCollection.find(Filters.eq("id", id));
        while (temp.first() != null) {
            id = ThreadLocalRandom.current().nextInt(10000, 99999);
            temp = subjCollection.find(Filters.eq("id", id));
        }

        // check if subject and teacher is exist
        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
        obj.add(new BasicDBObject("teacherId", teacherId));
        obj.add(new BasicDBObject("name", name));
        andQuery.put("$and", obj);

        temp = subjCollection.find(andQuery);
        if (temp.first() != null) {
            Document res = new Document("result", "fail");
            res.append("message", "have already exist");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        Subject newSub = new Subject(id, name, teacherId, topic);

        subjCollection.insertOne(newSub);
        Document res = new Document("result","ok");
        res.append("id", id);
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }
}
