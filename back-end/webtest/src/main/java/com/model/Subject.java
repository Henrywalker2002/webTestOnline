package com.model;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class Subject {
    @BsonProperty(value = "id")
    public int id;
    public String name;
    public String teacherId;
    public String topic;

    public Subject() {
        this.id = 0;
        this.name = "";
        this.teacherId = "";
        this.topic = "";
    }

    public Subject(int id, String name, String teacherId, String topic) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.topic = topic;
    }

    public Subject(Subject otherSub) {
        this.id = otherSub.id;
        this.name = otherSub.name;
        this.teacherId = otherSub.teacherId;
        this.topic = otherSub.topic;
    }

    public String toString() {
        return Integer.toString(this.id) + name + teacherId + topic;
    }

    public Boolean equals(Subject otherSub) {
        return this.id == otherSub.id;
    }
    
    public int hashCode() {
        return id + name.hashCode() + teacherId.hashCode() + topic.hashCode();
    }


    public void assign(Subject otherSub) {
        this.id = otherSub.id;
        this.name = otherSub.name;
        this.teacherId = otherSub.teacherId;
        this.topic = otherSub.topic;
    }
}
