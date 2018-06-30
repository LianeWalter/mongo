/**
 * 
 */
package com.model;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Property;


/**
 * @author Piksenia
 *
 */
//Verhindert den Eintrag classname: com.model.Item
@Entity(noClassnameStored = true)
@JsonDeserialize(as = Item.class)
public class Item implements Serializable {
	@Id
	@Property("id")
	private String id;
	@Property("name")
	private String name;
	@Property("total")
	private int total;
	public Item item;

	
	
	public Item() {}
	
	public Item(String name, int t) {
		this.name = name;
		this.total = t;

	}

	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	
	
	
	

}

