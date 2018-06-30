/**
 * 
 */
package com.vural.helga.xenia;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import static org.springframework.data.mongodb.core.aggregation.Aggregation;

import com.google.code.morphia.Morphia;
import com.google.code.morphia.mapping.Mapper;
import com.google.gson.Gson;

import com.model.Item;
import com.model.Security;

import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
//import com.mongodb.async.client;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.util.JSON;



/**
 * @author Piksenia
 *
 */

@RestController
@RequestMapping(value="/restService")
public class MainController {
	final Logger logger = Logger.getLogger(this.getClass());
	private static MongoClient mongoClient;
	private static DB database;
	private static DBCollection collection;
	private JSONObject nestedJSON = new JSONObject();
	private JSONObject oberJSON = new JSONObject();
	private DBCursor cursor1;
	private DBObject query,neuItem;
	private Morphia morphia = new Morphia();
	private Mapper mapper = new Mapper();
	private Item item = new Item();
	private Gson gson = new Gson();
	private String json = null;
	private String ergebnis = null;
	private ArrayList<JSONObject> array = new ArrayList<JSONObject>();
	private int counter = 0;
	private int ergebniszahl = 0;
	
	private final static String collectionName = "animalCollection";
	private final static String databaseName = "test";
	
	
	public static void startDBConnection() throws UnknownHostException {
		mongoClient = new MongoClient();
        database = mongoClient.getDB(databaseName);
        collection = database.getCollection(collectionName);
	}
	
	public static void endDBConnection() {
		mongoClient.close();
	}
	
	//START- DELETE METHODS
	//Delete all Items from Database
    @DeleteMapping(value="/delete")
    public String deleteAllItem() throws NoSuchAlgorithmException, IOException {
    	logger.info("INFO: Start process to delete all Items in the database");
		startDBConnection();
		
        //Delete Item with ID
        cursor1 = collection.find();
        while (cursor1.hasNext()) {
            collection.remove(cursor1.next());
        }
		
		endDBConnection();
		logger.info("INFO: End process to delete all Items in the database");
		return "Alle Items wurden gelöscht";
    }
	
	//Delete a Item from Database, which has the given ID
	@DeleteMapping(value="/delete/{id}")
	@ResponseBody
	public String deleteItem(@PathVariable String id) throws UnknownHostException {
		logger.info("INFO: Start process to delete the Item with the ID " + id);

		startDBConnection();
        //Delete Item with ID
        query = new BasicDBObject("_id", id);
		collection.findAndRemove(query);
		
		endDBConnection();
		logger.info("INFO: End process to delete the Item with the ID " + id);
		return "Das Item "+id+" wurde entfernt";
	}
	
	//END- DELETE METHOD
	
	//START- PUT METHOD
	@PutMapping(value="/updateItem/{id}", consumes = {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@ResponseBody
	public String updateItem(@PathVariable String id, @RequestBody Item updateItem ) throws UnknownHostException {
		logger.info("INFO: Start process to update (put) the Item with the ID " + id);

		startDBConnection();

        //Update updateItem with ID
		morphia.mapPackage("com.model");
		query = new BasicDBObject("_id", id.toString());
		cursor1 = collection.find(query);
		neuItem = mapper.toDBObject(updateItem);
		collection.update(cursor1.one(), neuItem);
     
		endDBConnection();
		logger.info("INFO: End process to update (put) the Item with the ID " + id);
		return "Das Item wurde aktualisiert";
	}
	//END- PUT METHOD
	
	//START- POST METHOD
	@PostMapping(value="/createItem", consumes = {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@ResponseBody
	public String createItem(@RequestBody Item item) throws UnknownHostException {
		logger.info("INFO: Start process to post the Item with the name " + item.getName());
		
		item.setId(new ObjectId().toString());
		
		startDBConnection();
		
        //Control, if document already exists; Name and ID in these combination must exist in the database
        DBObject id = new BasicDBObject( "_id", item.getId() );
        DBObject name = new BasicDBObject( "name", item.getName() );
        BasicDBList secondAndValues = new BasicDBList();
        secondAndValues.add( id );
        secondAndValues.add( name );
        DBObject query = new BasicDBObject( "$and", secondAndValues );
        cursor1 = collection.find(query);

        if(cursor1.hasNext()) {
        	endDBConnection();
        	logger.error("FAILED: Item with the combination of ID and Name already exists");
        	return "Das Item " +item.getId()+ " existiert bereits schon";
        }
        else {
        	
            //Create Document
            morphia.mapPackage("com.model");
            neuItem = mapper.toDBObject(item);
            
            //Database Method
            collection.insert(neuItem);
            
            //Control, if document was created
            query = new BasicDBObject("_id", item.getId());
            cursor1 = collection.find(query);
            String ergebnis1 = (String)cursor1.one().get("_id");
            String ergebnis2 = item.getId();
            if(ergebnis1.equals(ergebnis2)) {
            	endDBConnection();
            	logger.info("INFO: End process to post the Item with the name " + item.getName());
            	return "Das Item " +item.getId()+ " wurde angelegt";
            }
            else {        
            	endDBConnection();
            	logger.warn("WARN: post process interrupted unexpected. Item wasn't save in the database");
        		return "Das Item " +item.getId()+ " wurde nicht angelegt";
        	}
        }       

	}
	
	//END- POST METHOD


	//START- GET METHODS
	//Get a Item from Database, which has the given ID
    @GetMapping(value="{id}", produces = {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String readItem(@PathVariable("id") String id) throws NoSuchAlgorithmException, UnknownHostException {
    	logger.info("INFO: Start process to get the Item with the id " + id);
		
		startDBConnection();

        //Search Item with given ID
		query = new BasicDBObject("_id", id);
        cursor1 = collection.find(query);
        ergebnis = (String)cursor1.one().get("name");
        item.setId(id);
        item.setName(ergebnis);
        ergebniszahl = (int)cursor1.one().get("total");
        item.setTotal(ergebniszahl);
        
        //Transform -> Item in JSON format into Base64 format 
        json = gson.toJson(item);

        
		endDBConnection();
		logger.info("INFO: END process to get the Item with the id " + id);
    	return json;
    }
    
    //Get all Items from Database
    @GetMapping(produces = {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String readAllItem() throws NoSuchAlgorithmException, IOException, JSONException {
    	//have to clear, because the next call causes to enlarge the JSON with the same JSONObjects
    	array.clear();
    	nestedJSON = new JSONObject();
    	oberJSON = new JSONObject();
    	logger.info("INFO: Start process to get the all Items, which are in the database");

		startDBConnection();
        
      //Search Item without given ID (Search for all Items, which are in the database)
        cursor1 = collection.find();
        while(cursor1.hasNext()) {
        	neuItem = cursor1.next();
        	item.setId((String)neuItem.get("_id"));
        	item.setName((String)neuItem.get("name"));
        	item.setTotal((int)neuItem.get("total"));

        	//Transform -> Item in JSON String 
            json = gson.toJson(item);
            //array.add(transformB64(json));
            //json = removeEscapeChars(json);
            //Transform -> JSON String into JSONObject
            array.add(new JSONObject(json));
            counter++;
            }

        
        endDBConnection();
        json = array.toString();
        nestedJSON.put("Itemlist",array );
        oberJSON.put("List", nestedJSON);
		logger.info("INFO: End process to get the all Items, which are in the database");

    	return oberJSON.toString();
    }
	//END- GET METHODS

    
    //cryptography - Encode
    public static String transformB64(String jsonArray) {
		return Base64.getEncoder().encodeToString(jsonArray.getBytes());
    	
    }

    
    //Remove the escape char \ from the Array
    private static String removeEscapeChars(String remainingValue) {
    	String splitter = File.separator.replace("\\","\\\\");
    	String[] split = remainingValue.split(splitter);
        return remainingValue;
    }
    

}
