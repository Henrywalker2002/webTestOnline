package com.model;

import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import java.util.List;
import org.bson.Document;

public class Quesion {
    @BsonProperty(value = "_id")
    public ObjectId id;
    String content, suggestion, teacherId;
    int type; // 0 is mutiple choice and 1 is eassay
    int subjectId, level; // 1-5 : easy - hard
    List <Document> ans;

    public Quesion(){}
    
    public Quesion(ObjectId id, String content, String suggestion, String teacherId, int type, int subjectId, List<Document> ans, int level) {
        this.id = id;
        this.content = content;
        this.suggestion = suggestion;
        this.teacherId = teacherId;
        this.type = type;
        this.subjectId = subjectId;
        this.ans = ans;
        this.level = level;
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
