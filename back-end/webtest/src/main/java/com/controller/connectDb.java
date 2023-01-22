package com.controller;

import com.mongodb.client.MongoClients;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class connectDb {
    private MongoClient client;
    private  String uri = "mongodb+srv://henry:hungnguyen0304@cluster0.zz9j3qe.mongodb.net/?retryWrites=true&w=majority";
    private CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    public connectDb() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
        
         // Register the POJO codec provider
        client = MongoClients.create(uri);
    }

    public MongoClient gClient() {
        return this.client;
    }

    public MongoDatabase gDatabase() {
        return this.client.getDatabase("webtest").withCodecRegistry(this.pojoCodecRegistry);
    }
    public MongoDatabase gDatabase(String database) {
        return this.client.getDatabase(database).withCodecRegistry(this.pojoCodecRegistry);
    }

    public void closeConnection() {
        this.client.close();
    }
}
