package com.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.Iterator;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class testController {
    connectDb conn = new connectDb();
    MongoDatabase db = conn.gDatabase();
    MongoCollection <Document> testDoc = db.getCollection("test");
    MongoCollection <Document> quesDoc = db.getCollection("question");

    @RequestMapping(value = "/test",method = RequestMethod.POST)
    public ResponseEntity<Document> addTest(@RequestBody JsonNode json) {
        String name, desc;
        int subjectId, maxTime;
        Document res = new Document();
        List<ObjectId> lstQues = new ArrayList<>();
        try { 
            name = json.get("name").asText();
            desc = json.get("description").asText();
            subjectId = json.get("subjectId").asInt();
            maxTime = json.get("maxTime").asInt();
            ArrayNode arrayNode = (ArrayNode) json.get("quesId");
            Iterator <JsonNode> it = arrayNode.iterator();
            while(it.hasNext()) {
                var temp = it.next().asText();
                lstQues.add(new ObjectId(temp));
            }
        }
        catch (Exception e) {
            res.append("result" , "fail")
                .append("message" , "wrong json");
            return new ResponseEntity <>(res, HttpStatus.OK);
        }

        Document findQuery = new Document("name", name).append("subjectId", subjectId);
        var temp = this.testDoc.find(findQuery);
        if (temp.first() != null) {
            res.append("result", "fail")
                .append("message", "this name have already used");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }

        int id = ThreadLocalRandom.current().nextInt(10000, 99999);
        // if id is exist, random again
        temp = this.testDoc.find(Filters.eq("id", id));
        while (temp.first() != null) {
            id = ThreadLocalRandom.current().nextInt(10000, 99999);
            temp = this.testDoc.find(Filters.eq("id", id));
        }

        Document insertQuery = new Document("id", id)
                        .append("description", desc)
                        .append("name", name)
                        .append("numQues", lstQues.size())
                        .append("maxTime", maxTime)
                        .append("subjectId", subjectId)
                        .append("quesId", lstQues);
        
        this.testDoc.insertOne(insertQuery);
        res.append("result", "ok")
            .append("message", new Document("id", id));
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity<Document> getTest(@RequestParam int id) {
        var queryRes = this.testDoc.find(Filters.eq("id", id));
        Document res = new Document();
        if (queryRes.first() == null) {
            res.append("result", "fail")
                .append("message", "no test found");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        Document test = queryRes.first();
        Support sp = new Support();
        test = sp.getTest(test, quesDoc);
        res.append("result", "ok")
            .append("message", test);
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/test", method = RequestMethod.DELETE)
    public ResponseEntity<Document> delTest(@RequestParam int id) {
        var delRes = this.testDoc.findOneAndDelete(Filters.eq("id", id));
        Document res = new Document();
        if (delRes == null) {
            res.append("result", "fail")
                .append("message", "no test found");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        res.append("result", "ok")
            .append("message", "success");
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/test", method = RequestMethod.PUT)
    public ResponseEntity<Document> updateTest(@RequestBody JsonNode json) {
        String name, desc;
        int subjectId, maxTime,id;
        Document res = new Document();
        List<ObjectId> lstQues = new ArrayList<>();
        try { 
            name = json.get("name").asText();
            desc = json.get("description").asText();
            subjectId = json.get("subjectId").asInt();
            maxTime = json.get("maxTime").asInt();
            id = json.get("id").asInt();
            ArrayNode arrayNode = (ArrayNode) json.get("quesId");
            Iterator <JsonNode> it = arrayNode.iterator();
            while(it.hasNext()) {
                var temp = it.next().asText();
                lstQues.add(new ObjectId(temp));
            }
        }
        catch (Exception e) {
            res.append("result" , "fail")
                .append("message" , "wrong json");
            return new ResponseEntity <>(res, HttpStatus.OK);
        }
        var temp = this.testDoc.find(Filters.eq("id",id));
        if (temp.first() == null) {
            res.append("result", "fail")
                .append("message", "no test found");
        }
        Document oldTest = temp.first();
        // if change name, check name
        if (!oldTest.getString("name").equals(name) || oldTest.getInteger("subjectId") != subjectId) {
            Document checkDoc = new Document("name", name)
                                    .append("subjectId", subjectId);
            var temp2 = this.testDoc.find(checkDoc);
            if (temp2 != null) {
                res.append("result", "fail")
                    .append("message", "name is duplicate, please change");
                return new ResponseEntity<>(res, HttpStatus.OK);
            }
        }

        Document updateQuery = new Document()
            .append("description", desc)
            .append("name", name)
            .append("numQues", lstQues.size())
            .append("maxTime", maxTime)
            .append("subjectId", subjectId)
            .append("quesId", lstQues);
        this.testDoc.findOneAndUpdate(Filters.eq("id", id), new Document("$set", updateQuery));
        res.append("result", "ok")
            .append("message", "success");
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }
}
