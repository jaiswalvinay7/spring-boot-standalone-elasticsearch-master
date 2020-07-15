package com.example.elasticsearch.util;

import java.io.IOException;
import java.util.Optional;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.elasticsearch.constant.ElasticIngestionConstant;
import com.example.elasticsearch.exception.MessageProcessingException;
import com.example.elasticsearch.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

@Component
public class UserIngestionQueryUtil {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(UserIngestionQueryUtil.class);

	/** The jest client. */
	@Autowired
	private JestClient jestClient;

	/** The object mapper. */
	@Autowired
	private ObjectMapper objectMapper;
	
	

	/**
	 * Common method using jest Elastic search builder to get total hits using builder
	 * queries.
	 *
	 * @param searchQuery
	 *            the search query
	 * @param indexName
	 *            the index name
	 * @param typeName
	 *            the type name
	 * @return the json node
	 */
	public JsonNode commonElasticSearhBuilder(final QueryBuilder searchQuery, final String indexName, final String typeName) {
		final SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
		searchBuilder.query(searchQuery);
		LOGGER.info("Elastic query ====> {}", searchBuilder);
		final Search stateSearch = new Search.Builder(searchBuilder.toString()).addIndex(indexName).addType(typeName).build();
		JsonNode rootNode = null;
		try {
			final io.searchbox.core.SearchResult searchResult = jestClient.execute(stateSearch);
			final String jsonResult = Optional.ofNullable(searchResult).map(result -> result.getJsonString()).orElse(null);
			LOGGER.info("jsonResult ====> {}", jsonResult);
			if (jsonResult != null)
				rootNode = objectMapper.readTree(jsonResult);
		} catch (IOException e) {
			LOGGER.warn(">>>>>>>>>> Exception occured in ElasticUtil::SearchBuilder due to reason : [{}], cause : [{}]",e.getMessage(), e.getCause());
		}
		final JsonNode hitsNode = Optional.ofNullable(rootNode).map(t -> t.get(ElasticIngestionConstant.HITS)).map(t -> t.get(ElasticIngestionConstant.HITS)).orElse(null);
		return hitsNode;
	}

	/**
	 * Insert user data.
	 *
	 * @param user
	 *            
	 * @param id
	 *            the id
	 * @param esIndex
	 *            the es index
	 * @param esType
	 *            the es type
	 * @return the jest result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws MessageProcessingException
	 *             the message processing exception
	 */
	public JestResult insertUserDataIntoElasticSearch(final User user, final String id,final String esIndex, final String esType) throws IOException, MessageProcessingException {
		
		final ObjectNode userESNode = objectMapper.convertValue(user, ObjectNode.class);
		final Index index = new Index.Builder(userESNode.toString()).index(esIndex).id(id).type(esType).refresh(true).build();
		final JestResult jestResult = jestClient.execute(index);
		if (jestResult.getErrorMessage() == null) {
			final String docId = jestResult.getValue(ElasticIngestionConstant.ID).toString();
			LOGGER.info("Inserted the message into ES with _id {} on this ES-Index ->{} and on this ES-Type ->{}",docId, esIndex, esType);
			return jestResult;
		} else {
			LOGGER.error("Exception thrown while inserting the {} message into Elasticsearch and the exception message is {}",esType, jestResult.getErrorMessage());
			throw new MessageProcessingException(jestResult.getErrorMessage());
		}
	}



}
