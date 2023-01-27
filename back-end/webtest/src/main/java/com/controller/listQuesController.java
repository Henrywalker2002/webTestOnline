package com.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import com.mongodb.BasicDBObject;

import java.util.List;
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
public class listQuesController {
    connectDb conn = new connectDb();
    MongoDatabase db = conn.gDatabase();
    MongoCollection <Document> listQuesDoc = db.getCollection("listQues");

    @RequestMapping(value = "/listQues", method = RequestMethod.POST)
    public ResponseEntity<Document> addToList(@RequestBody JsonNode json) {
        String teacherId, name;
        List <ObjectId> lstQues = new ArrayList<>();
        Document res =  new Document();
        try {
            teacherId = json.get("teacherId").asText();
            name = json.get("name").asText();
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
        // if list already existed then just insert 
        BasicDBObject andQuery = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
        obj.add(new BasicDBObject("teacherId", teacherId));
        obj.add(new BasicDBObject("name", name));
        andQuery.put("$and", obj);

        var temp = this.listQuesDoc.find(andQuery);
        if (temp.first() != null) {
            // var rows = temp.first().get("quesId");
            // List <ObjectId> array = (ArrayList<ObjectId>) rows;
            List<ObjectId> array = temp.first().getList("quesId", ObjectId.class);
            if (array != null) {
                for (int i = 0; i < lstQues.size(); i ++) {
                    if (array.contains(lstQues.get(i))) {
                        lstQues.remove(i);
                    }
                }
            }
            this.listQuesDoc.updateOne(andQuery, Updates.addEachToSet("quesId", lstQues));
        }
        else {
            // else create one 
            Document doc = new Document("name", name)
                            .append("teacherId", teacherId)
                            .append("quesId", lstQues);
                                                    
            this.listQuesDoc.insertOne(doc);
        }
        res.append("resukt", "ok")
            .append("message", "success");
        return new ResponseEntity<Document>(res, HttpStatus.CREATED);
    }

    @RequestMapping(value="/listQues", method=RequestMethod.GET)
    public ResponseEntity<Document> getList(@RequestParam String teacherId,
                                            @RequestParam String name) {
        Document res = new Document();
        Document doc = new Document("name", name).append("teacherId", teacherId);
        var temp = this.listQuesDoc.find(doc);
        if (temp.first() == null) {
            this.listQuesDoc.insertOne(doc);
            temp = this.listQuesDoc.find(doc);
        }
        // var lstQues = temp.first().get("quesId");
        List <ObjectId> array = temp.first().getList("quesId", ObjectId.class);
        // List <ObjectId> array = (ArrayList<ObjectId>) lstQues;
        List<String> templst = new ArrayList<>();
        if (array != null) {
            array.forEach((n) -> templst.add(n.toString()));
        }
        var docRes = temp.first();
        docRes.remove("quesId");
        docRes.remove("_id");
        docRes.append("quesId", templst);
        res.append("result", "ok")
            .append("message", docRes);
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/delFromList", method = RequestMethod.DELETE)
    public ResponseEntity<Document> delFromList(@RequestBody JsonNode json) {
        String quesId, teacherId, name;
        Document res = new Document();
        try {
            quesId = json.get("quesId").asText();
            teacherId = json.get("teacherId").asText();
            name = json.get("name").asText();
        }
        catch (Exception e) {
            res.append("result" , "fail")
                .append("message" , "wrong json");
            return new ResponseEntity <>(res, HttpStatus.OK);
        }
        Document doc = new Document()
                            .append("name",  name)
                            .append("teacherId", teacherId);
        var tempRes = this.listQuesDoc.updateOne(doc, Updates.pull("quesId", new ObjectId(quesId)));
        if (tempRes.getModifiedCount() == 0) {
            res.append("result", "fail")
                .append("message", "no question in this list found");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        res.append("result", "ok")
            .append("message", "success");
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/listQues", method = RequestMethod.DELETE)
    public ResponseEntity<Document> delList(@RequestBody JsonNode json) {
        String name, teacherId;
        Document res = new Document();
        try {
            name = json.get("name").asText();
            teacherId = json.get("teacherId").asText();
        }
        catch (Exception e) {
            res.append("result" , "fail")
                .append("message" , "wrong json");
            return new ResponseEntity <>(res, HttpStatus.OK);
        }
        Document doc = new Document("name", name)
                        .append("teacherId", teacherId);
        var temp = this.listQuesDoc.findOneAndDelete(doc);
        if (temp == null) {
            res.append("result", "fail")
                .append("message", "no list found");
        }
        else {
            res.append("result", "ok")
                .append("message", "success");
        }
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }

    
}
