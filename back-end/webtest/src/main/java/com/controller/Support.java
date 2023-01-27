package com.controller;

import java.util.List;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class Support {

    public Support() {}
    public Document getTest(Document test, MongoCollection <Document> quesDoc) {
        List <ObjectId> lstQuesId = test.getList("quesId", ObjectId.class);

        List<Document> lstQues = new ArrayList<>();
        lstQuesId.forEach((quesId) -> {
            Document temp = quesDoc.find(Filters.eq("_id", quesId)).first();
            temp.remove("_id");
            temp.append("id", quesId.toString());
            lstQues.add(temp);
        });
        test.remove("quesId");
        test.remove("_id");
        test.append("question", lstQues);
        return test;
    }
}
