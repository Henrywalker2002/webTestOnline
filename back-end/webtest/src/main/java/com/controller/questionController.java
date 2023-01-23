package com.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.model.Quesion;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

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
public class questionController {
    connectDb conn = new connectDb();
    MongoDatabase db = conn.gDatabase();
    MongoCollection <Quesion> quesCollection = db.getCollection("question", Quesion.class);
    MongoCollection <Document> quesDoc = db.getCollection("question");
    MongoCollection <Document> listQuesDoc = db.getCollection("listQues");

    @RequestMapping(value= "/question", method = RequestMethod.POST)
    public ResponseEntity<Document> addQuestion(@RequestBody JsonNode json) {
        String content, suggestion, teacherId;
        int level, type, subjectId;
        List<Document> ans = new ArrayList<Document>();
        Document res = new Document();
        try {
            content = json.get("content").asText();
            suggestion = json.get("suggestion").asText();
            teacherId = json.get("teacherId").asText();
            subjectId = json.get("subjectId").asInt();
            level = json.get("level").asInt();
            type = json.get("type").asInt();
            ArrayNode arrayNode = (ArrayNode) json.get("ans");
            Iterator <JsonNode> iterator = arrayNode.iterator();
            while(iterator.hasNext()) {
                var temp = iterator.next();
                Document ansNode = new Document().append("content", temp.get("content").asText())
                                                .append("flag", temp.get("flag").asBoolean());
                ans.add(ansNode);
            }
        }
        catch (Exception e) {
            res.append("message", "wrong json")
                .append("result", "fail");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        var count = 0;
        var it = ans.iterator();
        while(it.hasNext()) {
            if (it.next().getBoolean("flag")) {
                count += 1;
            }
        }

        if (type == 0) {
            if (count == 0) {
                res.append("message", "no answer correct in your question")
                    .append("result", "fail");
                return new ResponseEntity<Document>(res, HttpStatus.OK);
            }
        }
        else {
            if (ans.size() != count) {
                res.append("message", "all the answer must be correct")
                    .append("result", "fail");
                return new ResponseEntity<Document>(res, HttpStatus.OK);
            }
        }

        ObjectId id = new ObjectId();
        Document ques = new Document("_id", id)
                                    .append("content" , content)
                                    .append("suggestion" , suggestion)
                                    .append("teacherId" , teacherId)
                                    .append("subjectId", subjectId)
                                    .append("level", level)
                                    .append("type", type)
                                    .append("ans", ans);
        try {
            this.quesDoc.insertOne(ques);
        }
        catch (Exception e) {
            System.out.print(e);
        }
        res.append("result", "ok")
            .append("message", new Document("id", id.toString()));
        return new ResponseEntity<Document>(res, HttpStatus.CREATED);
    }

    @RequestMapping(value="/question", method=RequestMethod.GET)
    public ResponseEntity<Document> getQuestion(@RequestParam String quesId) {
        Document res = new Document();
        ObjectId id = new ObjectId(quesId);
        var temp = this.quesDoc.find(Filters.eq("_id", id));
        if (temp.first() == null) {
            res.append("result" , "fail")
                .append("message" , "no question found");
            return new ResponseEntity <>(res, HttpStatus.OK);
        }
        var doc = temp.first();
        doc.append("id", quesId);
        doc.remove("_id");
        res.append("result", "ok")
            .append("message" , doc);
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/question" , method = RequestMethod.DELETE)
    public ResponseEntity<Document> delQuestion(@RequestParam String quesId) {
        Document res = new Document();
        ObjectId id = new ObjectId(quesId);
        var temp = this.quesDoc.findOneAndDelete(Filters.eq("_id", id));
        if (temp == null) {
            res.append("result" , "fail")
                .append("message" , "no question found");
        }
        else {
            res.append("result", "ok")
                .append("message", "success");
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    
}
