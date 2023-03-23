package com.elastic.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.rescore.RescorerBuilder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.basistech.rni.es.RNIFunctionScoreQuery;
import com.basistech.rni.es.rescorer.RNIRescorerBuilder;
import com.elastic.model.Aliases;
import com.elastic.model.ElasticMain;
import com.elastic.response.ResponseEN;
import com.elastic.response.ResponseENAR;
import com.elastic.response.ResponseENMainData;
import com.elastic.utility.ResponseElastic;
import com.elastic.wrapper.model.PosidexResponseDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.trueid.aml.nmatch.data.RosetteData;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;

@Repository
public class RosetteAPIRepository {

	@Value("${elasticsearch.indexname-English}")
	private static String INDEX_NAME_EN;

	@Value("${elasticsearch.indexname-Arabic}")
	private static String INDEX_NAME_AR;

	@Value("${elasticsearch.type.fullname}")
	private static String TYPE_FULLNAME;

	@Value("${elasticsearch.type.aliases}")
	private static String TYPE_ALIASES;

	public List<ResponseElastic> searchByMultiMatch(String name) throws IOException {

		List<String> list = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		List<ResponseENMainData> jsonList = new ArrayList<>();
		List<String> l_fullname = new ArrayList<>();
		List<ResponseElastic> elasticList = new ArrayList<>();

		@SuppressWarnings("deprecation")
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")));

		// Multi-match query in elasticsearch
		SearchRequest searchRequest = new SearchRequest("rni-worldcheckdata-en");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(name, "data.fullname", "aliases.alias")
				.operator(Operator.OR);
		// RNIRescorerBuilder rniBuilder = new RNIRescorerBuilder(multiMatchQuery);

		searchSourceBuilder.query(multiMatchQuery);
		// searchSourceBuilder.addRescorer(rniBuilder);
		searchRequest.source(searchSourceBuilder);
		System.out.println(searchSourceBuilder);

		try {
			@SuppressWarnings("deprecation")
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] hits = searchResponse.getHits().getHits();

			for (SearchHit searchHit : hits) {
				String sourceCode = searchHit.getSourceAsString();
				list.add(sourceCode);
			}

			/*
			 * for (String jsonString : list) { ResponseEN object =
			 * objectMapper.readValue(jsonString, ResponseEN.class);
			 * 
			 * jsonList.add(object); }
			 */

			for (String jsonString : list) {
				// ResponseEN object = objectMapper.readValue(jsonString, ResponseEN.class);
				ResponseENMainData object = objectMapper.readValue(jsonString, ResponseENMainData.class);

				jsonList.add(object);
			}

			System.out.println(jsonList);
			ResponseEN data = new ResponseEN();

			/*
			 * for (int i = 0; i < jsonList.size(); i++) { data = jsonList.get(i);
			 * l_fullname.add(data.getPrimary_name().getData().getFullname()); //
			 * l_fullname.addAll(data.getPrimary_name().getData().getAliases()); }
			 */

			for (int i = 0; i < jsonList.size(); i++) {
				ResponseElastic search = new ResponseElastic();
				// data = jsonList.get(i);
				// uid.add(jsonList.get(i).getData().getUid());
				l_fullname.add(jsonList.get(i).getData().getFullname());
				search.setUid(jsonList.get(i).getData().getUid());
				search.setFullname(jsonList.get(i).getData().getFullname());
				elasticList.add(search);
			}

			System.out.println(elasticList);

			// System.out.println(l_fullname);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.close();
		}

		return elasticList;
	}

	public List<RosetteData> searchByQueryString(String name) throws IOException {

		List<String> list = new ArrayList<>();
		List<String> scoreList = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		List<ElasticMain> jsonList = new ArrayList<>();
		List<RosetteData> elasticList = new ArrayList<>();
		LinkedHashMap<String, PosidexResponseDetails> map = new LinkedHashMap<>();
		Gson gson = new Gson();
		@SuppressWarnings("deprecation")
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")));

		// Query String in elasticsearch

		SearchRequest searchRequest = new SearchRequest("rni-amlworldcheckdata-en");

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// MultiMatchQueryBuilder multiMatchQueryBuilder1 = new
		// MultiMatchQueryBuilder(name.trim(),"data.fullname","data.aliases");
		// QueryStringQueryBuilder queryBuilder =
		// QueryBuilders.queryStringQuery(name.trim());
		// queryBuilder.field("data.fullname");
	
		QueryBuilder builder = QueryBuilders.matchQuery("data.fullname", name);
		
		/*
		 * BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
		 * .must(QueryBuilders.matchQuery("data.fullname", name));
		 * boolQueryBuilder.boost((float) 0.85);
		 */
		
		RNIRescorerBuilder rniBuilder = new RNIRescorerBuilder(builder);
		
		RNIFunctionScoreQuery scoreQuery = new RNIFunctionScoreQuery(null, null, 0);

		searchSourceBuilder.query(builder);
		searchSourceBuilder.addRescorer(rniBuilder);
		rniBuilder.windowSize(200);
		searchRequest.source(searchSourceBuilder);
		System.out.println(searchSourceBuilder);

		try {
			@SuppressWarnings("deprecation")
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] hits = searchResponse.getHits().getHits();
			System.out.println("Rosette object --> " + searchResponse);
			float score = 0f;
			for (SearchHit searchHit : hits) {
				score = searchHit.getScore();
				String responseString = searchHit.getSourceAsString();
				list.add(responseString);
				scoreList.add(String.valueOf(score));

			}

			if (list.size() == scoreList.size()) {
				for (int i = 0; i < list.size(); i++) {
					String jsonString = list.get(i);
					ElasticMain object = objectMapper.readValue(jsonString, ElasticMain.class);
					String scoreVal = scoreList.get(i);
					object.setScore(scoreVal);
					jsonList.add(object);
				}
			}
			for (int i = 0; i < jsonList.size(); i++) {
				RosetteData response = new RosetteData();
				response.setUid(Long.valueOf(jsonList.get(i).getData().getUid()));
				response.setMatchedName(jsonList.get(i).getData().getFullname());
				response.setScore(Float.valueOf(jsonList.get(i).getScore()));
				elasticList.add(response);
			}

			System.out.println("elasticList Fullname " + elasticList);

			
			  List<RosetteData> listData = searchByQueryAliases(name);
			  
			  elasticList.addAll(listData);
			  
			  System.out.println("elasticList Aliases " + listData);
			 

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.close();
		}

		return elasticList;
	}

	public List<RosetteData> searchByQueryAliases(String name) throws IOException {

		List<String> list = new ArrayList<>();
		List<String> scoreList = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		List<ElasticMain> jsonList = new ArrayList<>();
		List<String> l_fullname = new ArrayList<>();
		List<String> uid = new ArrayList<>();
		List<RosetteData> elasticList = new ArrayList<>();
		LinkedHashMap<String, PosidexResponseDetails> map = new LinkedHashMap<>();

		@SuppressWarnings("deprecation")
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")));

		// Query String in elasticsearch

		SearchRequest searchRequest = new SearchRequest("rni-amlworldcheckdata-en");

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// QueryStringQueryBuilder queryBuilder =
		// QueryBuilders.queryStringQuery("data.aliases:" + name.trim());
		//BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("aliases.alias", name);
		NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("aliases", matchQuery,ScoreMode.Max);
		matchQuery.boost(1);
		nestedQuery.boost(1);
		RNIRescorerBuilder rniBuilder = new RNIRescorerBuilder(nestedQuery);

		searchSourceBuilder.query(nestedQuery);
		searchRequest.source(searchSourceBuilder.addRescorer(rniBuilder));

		System.out.println("aliases " + searchSourceBuilder);

		try {
			@SuppressWarnings("deprecation")
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] hits = searchResponse.getHits().getHits();
			float score = 0f;
			for (SearchHit searchHit : hits) {
				score = searchHit.getScore();
				String responseString = searchHit.getSourceAsString();
				list.add(responseString);
				scoreList.add(String.valueOf(score));

			}

			if (list.size() == scoreList.size()) {
				for (int i = 0; i < list.size(); i++) {
					String jsonString = list.get(i);
					ElasticMain object = objectMapper.readValue(jsonString, ElasticMain.class);
					String scoreVal = scoreList.get(i);
					object.setScore(scoreVal);
					jsonList.add(object);
				}
			}
			System.out.println(jsonList);
			String uuid = "";

			for (int i = 0; i < jsonList.size(); i++) {

				for (Aliases alias : jsonList.get(i).getAliases()) {
					//if (alias.getAlias().trim().contains(name.toUpperCase().trim())) {
						RosetteData response = new RosetteData();
						response.setUid(Long.valueOf(jsonList.get(i).getData().getUid()));
						response.setMatchedName(alias.getAlias());
						response.setScore(Float.valueOf(jsonList.get(i).getScore()));
						elasticList.add(response); // uuid = jsonList.get(i).getData().getUid();

					//}
				}

			}

			// System.out.println(elasticList);

		} catch (

		IOException e) {
			e.printStackTrace();
		} finally {
			client.close();
		}

		return elasticList;
	}

	public List<String> searchByQuery(String name) throws IOException {

		List<String> list = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		List<ResponseENAR> jsonList = new ArrayList<>();
		List<String> l_fullname = new ArrayList<>();

		@SuppressWarnings("deprecation")
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")));

		// Query String in elasticsearch

		SearchRequest searchRequest = new SearchRequest("rni-worldcheckdataar");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery("primary_name.aliasnames:" + name);
		searchSourceBuilder.query(queryBuilder);
		searchRequest.source(searchSourceBuilder);

		try {
			@SuppressWarnings("deprecation")
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] hits = searchResponse.getHits().getHits();
			for (SearchHit searchHit : hits) {
				String responseString = searchHit.getSourceAsString();
				list.add(responseString);

			}

			for (String jsonString : list) {
				ResponseENAR object = objectMapper.readValue(jsonString, ResponseENAR.class);

				jsonList.add(object);
			}
			System.out.println(jsonList);
			ResponseENAR data = new ResponseENAR();

			for (int i = 0; i < jsonList.size(); i++) {
				data = jsonList.get(i);
				l_fullname.add(data.getPrimary_name().getAliasnames());
			}

			System.out.println(l_fullname);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.close();
		}

		return l_fullname;
	}

	public List<String> searchByFuzzyQuery(String name) throws IOException {

		List<String> list = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		List<ResponseEN> jsonList = new ArrayList<>();
		List<String> l_fullname = new ArrayList<>();

		@SuppressWarnings("deprecation")
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost("localhost", 9200, "http")));

		// Query String in elasticsearch

		SearchRequest searchRequest = new SearchRequest("rni-worldcheckdataen");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		FuzzyQueryBuilder qbuilder = QueryBuilders.fuzzyQuery("primary_name.data.fullname:", name)
				.fuzziness(Fuzziness.AUTO).prefixLength(3).maxExpansions(10);
		searchSourceBuilder.query(qbuilder);
		searchRequest.source(searchSourceBuilder);

		try {
			@SuppressWarnings("deprecation")
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] hits = searchResponse.getHits().getHits();
			for (SearchHit searchHit : hits) {
				String responseString = searchHit.getSourceAsString();
				list.add(responseString);

			}

			for (String jsonString : list) {
				ResponseEN object = objectMapper.readValue(jsonString, ResponseEN.class);

				jsonList.add(object);
			}
			System.out.println(jsonList);
			ResponseEN data = new ResponseEN();

			for (int i = 0; i < jsonList.size(); i++) {
				data = jsonList.get(i);
				// l_fullname.add(data.getPrimary_name().getData().getFullname());
			}

			System.out.println(l_fullname);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.close();
		}

		return l_fullname;
	}

}
