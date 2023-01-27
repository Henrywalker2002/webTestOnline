package com.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;

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
public class doTest {
    connectDb conn = new connectDb();
    MongoDatabase db = conn.gDatabase();
    MongoCollection <Document> quesDoc = db.getCollection("question");
    MongoCollection <Document> doTestDoc = db.getCollection("doTest");
    MongoCollection <Document> testDoc = db.getCollection("test");

    float calcGrade(List<Document> lstAns) {
        int count = 0;
        var it = lstAns.iterator();
        while (it.hasNext()) {
            var ans = it.next();
            ObjectId quesId = new ObjectId(ans.get("quesId").toString());
            // select answer
            // ArrayList <String> arrayNode = (ArrayList<Document>) ans.get("ansContent");
            List <String> arrayNode = ans.getList("ansContent", String.class);
            var it2 = arrayNode.iterator();
            List<String> selAns = new ArrayList<>();
            while(it2.hasNext()) {
                String temp = it2.next();
                selAns.add(temp);
            }

            var queryRes = this.quesDoc.find(Filters.eq("_id", quesId));
            if (queryRes.first() == null) {
                return -1;
            }
            var ques = queryRes.first();
            // get list of content
            List<Document> arrAns = ques.getList("ans", Document.class);
            // count total correct choice
            int countFlag = 0;
            // count number of selected choice correct
            int tempCount = 0;
            for (int i = 0; i < selAns.size(); i++) {
                Document doc = (Document) arrAns.get(i);
                String content = doc.getString("content");
                Boolean flag = doc.getBoolean("flag");
                if (flag) {
                    countFlag++;
                }
                if (selAns.contains(content) && flag) {
                    tempCount++;
                }
            }
            if (countFlag == 0) {
                return -1;
            }
            else {
                count += tempCount/countFlag;
            }

        }
        
        return (float)((float) count/lstAns.size()) * 10;
    }

    // set processState = true if in process and false otherwise
    @RequestMapping(value = "/doTest", method = RequestMethod.POST)
    public ResponseEntity<Document> updateState(@RequestBody JsonNode json) {
        String studentId, timeBeginStr;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        int testId;
        Date timeBegin;
        Boolean isStart = true;
        Document res = new Document();
        try {
            timeBeginStr = json.get("timeBegin").asText();
            timeBegin = sdf.parse(timeBeginStr);
            testId = json.get("testId").asInt();
            studentId = json.get("studentId").asText();
        }
        catch (Exception e) {
            res.append("result" , "fail")
                .append("message" , "wrong json");
            return new ResponseEntity <>(res, HttpStatus.OK);
        }
        Document query = new Document("testId", testId)
                        .append("studentId", studentId);
        var doTest = this.doTestDoc.aggregate(Arrays.asList(
            new Document("$match", query),
            new Document("$sort", new Document("no", -1))
        )).first();
        if (isStart) {
            int no = 1;
            if (doTest != null) {
                if (doTest.getBoolean("isStart", false)) {
                    res.append("result", "fail")
                        .append("message", "please complete your previous test");
                    return new ResponseEntity<Document>(res, HttpStatus.OK);
                }
                no = doTest.getInteger("no") + 1;
            }
            query.append("no", no)
                .append("timeBegin", timeBegin);
            query.append("isStart", true);
            this.doTestDoc.insertOne(query);
            query.remove("timeBegin");
            query.put("testId", query.get("testId").toString());
            var idStr = query.get("_id").toString();
            query.append("id", idStr);
            query.remove("_id");
            query.remove("isStart");
            res.append("result", "ok")
                .append("message", query);
        }       
        else {
            res.append("result", "fail")
                .append("message", "send by method PUT if test is done");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/doTest", method = RequestMethod.PUT)
    public ResponseEntity<Document> addDoTest(@RequestBody JsonNode json) {
        String  timeEndStr;
        String id;
        Date  timeEnd;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        List<Document> selAns = new ArrayList<>();
        Document res = new Document();
        try {
            timeEndStr = json.get("timeEnd").asText();
            timeEnd = sdf.parse(timeEndStr);
            id = json.get("id").asText();
            ArrayNode arrayNode = (ArrayNode) json.get("selectAns");
            Iterator <JsonNode> it = arrayNode.iterator();
            while(it.hasNext()) {
                var temp = it.next();
                ObjectId quesId = new ObjectId(temp.get("quesId").asText());
                // get ans content
                ArrayNode arrayNode2 = (ArrayNode) temp.get("ansContent");
                Iterator <JsonNode> it2 = arrayNode2.iterator();
                List<String> lstAns = new ArrayList<>();
                while(it2.hasNext()) {
                    lstAns.add(it2.next().asText());
                }

                Document node = new Document("quesId", quesId)
                                            .append("ansContent", lstAns);
                selAns.add(node);
            }
        }
        catch (Exception e) {
            res.append("result" , "fail")
                .append("message" , e.toString());
            return new ResponseEntity <>(res, HttpStatus.OK);
        }
        // var time = timeEnd.compareTo();
        float grade = calcGrade(selAns);
        if (grade == -1) {
            res.append("result", "fail")
                .append("message", "something went wrong, retry later");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        Document matchQuery = new Document("_id", new ObjectId(id));
        Document updateQuery = new Document("timeEnd", timeEnd)
                                .append("grade", grade)
                                .append("selectAns", selAns)
                                .append("isStart", false);
        this.doTestDoc.findOneAndUpdate(matchQuery, new Document("$set", updateQuery));
        res.append("result", "ok")
            .append("message", "success");
        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/doTest", method = RequestMethod.GET)
    public ResponseEntity<Document> getDoTest(@RequestParam String id) {
        var doTest = this.doTestDoc.find(Filters.eq("_id", new ObjectId(id))).first();
        Document res = new Document();
        List <Document> lstSelAns = new ArrayList<>();
        int testId;
        if (doTest == null) {
            res.append("result", "fail")
                .append("message", "no review found");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        try {
            testId = doTest.getInteger("testId");
            lstSelAns = doTest.getList("selectAns", Document.class);
        }
        catch (Exception e) {
            res.append("result", "fail")
                .append("message", e.toString());
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }

        Document test = this.testDoc.find(Filters.eq("id", testId)).first();
        if (test == null) {
            res.append("result", "fail")
                .append("message", "something went wrong");
            return new ResponseEntity<Document>(res, HttpStatus.OK);
        }
        Support sp = new Support();
        test = sp.getTest(test, quesDoc);
        List<Document> lstQues = test.getList("question", Document.class);
        for (int i = 0; i < lstQues.size();i++) {
            var ques = lstQues.get(i);
            List<Document> lstAns = ques.getList("ans", Document.class);
            List<String> selAns = new ArrayList<>();
            for (int j = 0; j < lstSelAns.size();j++) {
                Document tempAns = lstSelAns.get(j);
                if (tempAns.getObjectId("quesId").toString().equals(ques.getString("id"))) {
                    selAns = tempAns.getList("ansContent", String.class);
                    break;
                }
            }
            for (int k= 0; k <lstAns.size();k++) {
                Document ans = lstAns.get(k);
                if (selAns.contains(ans.getString("content"))) {
                    ans.append("isSelected", true);
                }
                else {
                    ans.append("isSelected", false);
                }
                lstAns.set(k, ans);
            }
            ques.remove("ans");
            ques.append("ans", lstAns);
            lstQues.set(i, ques);
        }
        test.remove("question");
        test.append("question", lstQues);
        res.append("result", "ok")
            .append("message", test);

        return new ResponseEntity<Document>(res, HttpStatus.OK);
    }
}
