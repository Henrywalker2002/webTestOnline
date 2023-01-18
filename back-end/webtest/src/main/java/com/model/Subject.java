package com.model;

public class Subject {
    public int id;
    public String name;
    public String teacherId;
    public String topic;

    public Subject(int id, String name, String teacherId, String topic) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.topic = topic;
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
}
