package com.example.elasticsearch.constant;

import java.text.SimpleDateFormat;

public interface ElasticIngestionConstant {
	
	/** The Constant _id. */
	public static final String ID = "_id";
	/** The Constant HITS. */
	public static final String HITS = "hits";
	/** The Constant SOURCE. */
	public static final String SOURCE = "_source";
	/** The Date Format */
	public static final SimpleDateFormat DATE_FORMATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public static final String CREATION_DATE = "creationDate";
	
	public static final String USER_ID = "userId";
	
	public static final String AGE = "age";
	
	public static final String NAME = "name";
	
	public static final String ADDRESS = "address";
	
	public static final String USER_SETTING = "userSettings";
}
