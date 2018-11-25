package com.cj.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.BSON;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.Test;

import com.cj.mongo.module.Account;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationAction;
import com.mongodb.client.model.ValidationOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

// http://www.runoob.com/mongodb/mongodb-java.html

public class MongoTest {

	@Test
	public void helloWorld(){
    	//MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        //MongoClient client = new MongoClient("localhost","27017");
		
    	MongoClient client = new MongoClient(
    			new ServerAddress("localhost", 27017),
    			MongoCredential.createCredential("cj", "admin", "123456".toCharArray()),
    			MongoClientOptions.builder().build());
       
        MongoDatabase db=client.getDatabase("demo");
        
        MongoCollection<Document> articleCollection=db.getCollection("articles");
        System.out.println(articleCollection.find().first());
        
        // CRUD
        db.getCollection("test").drop();
        db.createCollection("test");
        MongoCollection<Document> testCollection=db.getCollection("test");
        
        // 1. Insert
        List<Document> docs=new ArrayList<Document>();
        docs.add(new Document("title","MongoDB")
				.append("author", "Tom")
				.append("postDate", new Date())
				.append("clicks",20)
				);
        docs.add(new Document("title","Redux")
				.append("author", "Lucy")
				.append("postDate", new Date())
				.append("clicks",50)
				);
        docs.add(new Document("title","NodeJS")
				.append("author", "Lucy")
				.append("postDate", new Date())
				.append("clicks",70)
				);
        docs.add(new Document("title","Java")
				.append("author", "Tom")
				.append("postDate", new Date())
				.append("clicks",120)
				);
        testCollection.insertMany(docs);
        
        // 2. Read
       System.out.println("Read test collection:");
       FindIterable<Document> result = testCollection.find(
    		   Filters.and(Filters.gt("clicks", 30),Filters.lt("clicks",100))
    		   );
       MongoCursor<Document> it = result.iterator();  
       while(it.hasNext()){  
          System.out.println(it.next());  
       }  
       
       System.out.println("Read articles collection:");
       result=articleCollection.find()
    		   .projection(new Document("title",1).append("tags", 1).append("_id", 0))
    		   .sort(new Document("title",1));
       it = result.iterator();  
       while(it.hasNext()){  
          System.out.println(it.next());  
       } 
       
       System.out.println("Aggregate:");
       Document match = new Document("clicks",new Document("$gt",30));
       Document group = new Document("_id","$author")
		   		.append("total_click",new Document("$sum","$clicks"));
       AggregateIterable<Document> aggResult=testCollection.aggregate(Arrays.asList(
    		   new Document("$match",match),
    		   new Document("$group",group)
    		   ));
       it = aggResult.iterator();  
       while(it.hasNext()){  
          System.out.println(it.next());  
       }  
       
        // 3. Update
       UpdateResult updates=testCollection.updateMany(Filters.lt("clicks", 100), new Document("$set",new Document("clicks",100))); 
       System.out.println(updates);
       
       // 4. Delete
       DeleteResult deletes = testCollection.deleteMany(new Document("title","MongoDB"));
       System.out.println(deletes);
	}
	
	@Test
	public void pojoTest(){
		CodecRegistry codecRegistry=CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(
						PojoCodecProvider.builder()
						.register("com.cj.mongo.module")
					.build()
					)
			);
		MongoClientOptions options=MongoClientOptions.builder()
				.codecRegistry(codecRegistry)
				.build();
		
		MongoClient client = new MongoClient(
				new ServerAddress("localhost", 27017),
				MongoCredential.createCredential("cj", "admin", "123456".toCharArray()),
					options);
		   
		MongoDatabase db=client.getDatabase("demo");
		db.getCollection("accounts").drop();
		//db.createCollection("accounts",productJsonSchemaValidtor());
		db.createCollection("accounts");
		
		MongoCollection<Account> accounts=db.getCollection("accounts",Account.class);
		accounts.insertOne(new Account("Tom",10));
		System.out.println(accounts.find().first());
	}
	
	/*
	 {$jsonSchema:{bsonType:"object",required:["_id","balance"],properties:{_id:{bsonType:"string",description:"must be a string and is required"},balance:{bsonType:"int",minimum:0,description:"must be a positive integer and is required"}}}}
	 */
	private final static String jsonSchema="{$jsonSchema:{"
			+ "bsonType:\"object\","
			+ "required:[\"_id\",\"balance\"],"
			+ "properties:{"
			+ "_id:{bsonType:\"string\",description:\"must be a string and is required\"},"
			+ "balance:{bsonType:\"int\",minimum:0,description:\"must be a positive integer and is required\"}"
			+ "}"
			+ "}}";
	
	public CreateCollectionOptions productJsonSchemaValidtor(){
		ValidationOptions validationOptions = new ValidationOptions()
					.validationAction(ValidationAction.ERROR)
					.validator(BsonDocument.parse(jsonSchema));
		return new CreateCollectionOptions().validationOptions(validationOptions);
	}
	
	
}
