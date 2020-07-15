package com.example.elasticsearch.controller;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.elasticsearch.constant.ElasticIngestionConstant;
import com.example.elasticsearch.exception.MessageProcessingException;
import com.example.elasticsearch.model.User;
import com.example.elasticsearch.util.UserIngestionQueryUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.searchbox.client.JestResult;

/**
 * This class is to demo how ElasticsearchTemplate can be used to Save/Retrieve
 */

@RestController
@RequestMapping("/rest/users")
public class UserController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
	@Value("${elasticsearch.index}")
	public String index;
	@Value("${elasticsearch.type}")
	public String type;

	@Autowired
	Client client;
	
	@Autowired
	private UserIngestionQueryUtil userIngestionQueryUtil;
	
	@PostMapping(value = "/create", consumes = "application/json; charset=utf-8")
    public String create(@RequestBody User user) throws IOException, MessageProcessingException {
    	 
		/* 1st method by use of this we can also insert
		 * 
		 * IndexResponse response = client.prepareIndex(index, type, user.getUserId())
		 * .setSource(jsonBuilder() .startObject()
		 * .field(ElasticIngestionConstant.USER_ID, user.getUserId())
		 * .field(ElasticIngestionConstant.CREATION_DATE,
		 * ElasticIngestionConstant.DATE_FORMATE.format(user.getCreationDate()))
		 * .field(ElasticIngestionConstant.NAME, user.getName())
		 * .field(ElasticIngestionConstant.AGE, user.getAge())
		 * .field(ElasticIngestionConstant.ADDRESS, user.getAddress())
		 * .field(ElasticIngestionConstant.USER_SETTING, user.getUserSettings())
		 * .endObject() ) .get(); System.out.println("response id:"+response.getId());
		 * return response.getResult().toString();
		 */

		// 2nd method by which we can use to insert data to ES
        JestResult jestResult = userIngestionQueryUtil.insertUserDataIntoElasticSearch(user, user.getUserId(), index, type);
        return jestResult.getValue("result").toString().toUpperCase();
        
    }

	@GetMapping(value = "/view/{id}", produces = "application/json; charset=utf-8")
	public Map<String, Object> view(@PathVariable final String id) {
		/*
		 * // 1st method useing client 
		 * GetResponse getResponse =client.prepareGet(index, type, id).get(); 
		 * System.out.println(getResponse.getSource()); 
		 * return getResponse.getSource();
		 */
		
		//2nd method by use of query 
		final QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(ElasticIngestionConstant.ID, id));
		// Gets total hits.
		JsonNode hitsNode = userIngestionQueryUtil.commonElasticSearhBuilder(query, index,type);
		JsonNode sourceNode = Optional.ofNullable(hitsNode).filter(hits -> hits.size() > 0).map(t -> t.get(0)).map(t -> t.get(ElasticIngestionConstant.SOURCE)).orElse(null);
		LOGGER.info("sourceNode ::::   {}",sourceNode);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> result = mapper.convertValue(sourceNode, new TypeReference<Map<String, Object>>(){});
		return result;
	}

	@GetMapping(value = "/view/name/{field}", produces = "application/json; charset=utf-8")
	public Map<String, Object> searchByName(@PathVariable final String field) {
	
		/*
		 * 1st method to fetch data
		 * 
		 * Map<String, Object> map = null; 
		 * SearchResponse response = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.QUERY_AND_FETCH).setQuery(QueryBuilders.matchQuery(ElasticIngestionConstant.NAME,field)).get();
		 * List<SearchHit> searchHits =
		 * Arrays.asList(response.getHits().getHits()); 
		 * map = searchHits.get(0).getSource(); 
		 * return map;
		 */
		
		//2nd method to fetch data
		final QueryBuilder query = QueryBuilders.matchQuery(ElasticIngestionConstant.NAME, field);
		JsonNode hitsNode = userIngestionQueryUtil.commonElasticSearhBuilder(query, index,type);
		JsonNode sourceNode = Optional.ofNullable(hitsNode).filter(hits -> hits.size() > 0).map(t -> t.get(0)).map(t -> t.get(ElasticIngestionConstant.SOURCE)).orElse(null);
		LOGGER.info("sourceNode ::::   {}",sourceNode);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> result = mapper.convertValue(sourceNode, new TypeReference<Map<String, Object>>(){});
		return result;

	}

	@PutMapping(value = "/update/{id}", consumes = "application/json; charset=utf-8")
	public String update(@PathVariable final String id, @RequestBody User user) throws IOException {

		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.index(index).type(type).id(id).doc(jsonBuilder().startObject().field(ElasticIngestionConstant.NAME, user.getName()).endObject());
		try {
			UpdateResponse updateResponse = client.update(updateRequest).get();
			LOGGER.info("updateResponse status ::::   {}",updateResponse.status());
			return updateResponse.status().toString();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(""+e.getMessage());
		}
		return "Exception";
	}

	@DeleteMapping(value = "/delete/{id}")
	public String delete(@PathVariable final String id) {

		DeleteResponse deleteResponse = client.prepareDelete(index, type, id).get();
		LOGGER.info("Delete Response====> {}", deleteResponse.getResult().toString());
		return deleteResponse.getResult().toString();
	}
	
		
}