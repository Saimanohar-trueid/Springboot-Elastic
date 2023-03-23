package com.elastic.utility;

import java.util.ArrayList;
import java.util.List;


import com.basistech.rosette.api.HttpRosetteAPI;
import com.basistech.rosette.apimodel.Name;
import com.basistech.rosette.apimodel.NameSimilarityRequest;
import com.basistech.rosette.apimodel.NameSimilarityResponse;
import com.basistech.rosette.apimodel.NameTranslationRequest;
import com.basistech.rosette.apimodel.NameTranslationResponse;
import com.basistech.util.ISO15924;
import com.basistech.util.LanguageCode;
import com.elastic.response.ResponseData;
import com.basistech.rosette.apimodel.Response;
import com.basistech.rosette.apimodel.jackson.ApiModelMixinModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class RosetteAPIUtility {
	
    private static final String KEY_PROP_NAME = "rosette.api.key";
    private static final String URL_PROP_NAME = "rosette.api.altUrl";
    public static final String NAME_SIMILARITY_SERVICE_PATH = "/name-similarity";
    public static final String NAME_TRANSLATION_SERVICE_PATH = "/name-translation";

	public List<ResponseData> callRosetteAPI(List<ResponseElastic> list, String fullname) throws JsonProcessingException {

		int size = list.size();
		List<String> listVal = new ArrayList<>();
		List<ResponseData> listData = new ArrayList<>();
		List<ResponseData> listDataAll = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			ResponseData data = new ResponseData();
			ResponseElastic dataOne = list.get(i);
			//ResponseElastic search = new ResponseElastic();
			Name name1 = Name.builder().text(fullname)
	                .entityType("PERSON")
	                .script(ISO15924.Zyyy)
	                .language(LanguageCode.ENGLISH)
	                .build();
			Name name2 = Name.builder().text(dataOne.getFullname())
	                .entityType("PERSON")
	                .script(ISO15924.Zyyy)
	                .language(LanguageCode.ENGLISH)
	                .build();
	       // Name name2 = Name.builder().text(list.get(i)).build();
	        HttpRosetteAPI rosetteApi = new HttpRosetteAPI.Builder()
	                                    .key(getApiKeyFromSystemProperty())
	                                    .url(getAltUrlFromSystemProperty())
	                                    .build();
	        //The api object creates an http client, but to provide your own:
	        NameSimilarityRequest request = NameSimilarityRequest.builder().name1(name1).name2(name2).build();
	        NameSimilarityResponse response = rosetteApi.perform(NAME_SIMILARITY_SERVICE_PATH, request, NameSimilarityResponse.class);
	        data.setUid(dataOne.getUid());
	        data.setRequestedName(fullname);
	        data.setElasticSearchName(dataOne.getFullname());
	        data.setMatchScore(response.getScore());
	        
	        listData.add(data);
	        
		}
		listDataAll.addAll(listData);

		return listDataAll;
	}
	
	public List<ResponseData> callRosetteAPIAR(List<String> list, String fullname) throws JsonProcessingException {

		int size = list.size();
		List<String> listVal = new ArrayList<>();
		List<ResponseData> listData = new ArrayList<>();
		List<ResponseData> listDataAll = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			ResponseData data = new ResponseData();
			Name name1 = Name.builder().text(fullname)
	                .entityType("PERSON")
	                .script(ISO15924.Zyyy)
	                .language(LanguageCode.ARABIC)
	                .build();
			Name name2 = Name.builder().text(list.get(i))
	                .entityType("PERSON")
	                .script(ISO15924.Zyyy)
	                .language(LanguageCode.ARABIC)
	                .build();
	     //   Name name2 = Name.builder().text(list.get(i)).build();
	        HttpRosetteAPI rosetteApi = new HttpRosetteAPI.Builder()
	                                    .key(getApiKeyFromSystemProperty())
	                                    .url(getAltUrlFromSystemProperty())
	                                    .build();
	        //The api object creates an http client, but to provide your own:
	        NameSimilarityRequest request = NameSimilarityRequest.builder().name1(name1).name2(name2).build();
	        NameSimilarityResponse response = rosetteApi.perform(NAME_SIMILARITY_SERVICE_PATH, request, NameSimilarityResponse.class);
	       
	        data.setRequestedName(fullname+" translate Name  "+nameTransalate(fullname));
	        data.setElasticSearchName(list.get(i)+" translate Name  "+nameTransalate(list.get(i)));
	        data.setMatchScore(response.getScore());
	        
	        listData.add(data);
	        
		}
		listDataAll.addAll(listData);

		return listDataAll;
	}
	
	
	// For Name-Transalate Purpose
	public String nameTransalate(String name) {
		 NameTranslationRequest reqData = NameTranslationRequest.builder()
	                .name(name)
	                .targetLanguage(LanguageCode.ENGLISH)
	                .build();

	        HttpRosetteAPI rosApi = new HttpRosetteAPI.Builder()
	                                    .key(getApiKeyFromSystemProperty())
	                                    .url(getAltUrlFromSystemProperty())
	                                    .build();
	        //The api object creates an http client, but to provide your own:
	        NameTranslationResponse  resp = rosApi.perform(NAME_TRANSLATION_SERVICE_PATH, reqData, NameTranslationResponse.class);
	        
	        return resp.getTranslation();
	}
	
	 protected String getApiKeyFromSystemProperty() {
	        String apiKeyStr = System.getProperty(KEY_PROP_NAME);
	        if (apiKeyStr == null || apiKeyStr.trim().length() < 1) {
	            //showUsage(getClass());
	            //System.exit(1);
	        	apiKeyStr = "dacf2e175b1acc8b61f73af9375e36c7";
	        }
	        return apiKeyStr.trim();
	    }

	    /**
	     * @return alternate url using system property {@value com.basistech.rosette.examples.ExampleBase#URL_PROP_NAME}
	     */
	    protected String getAltUrlFromSystemProperty() {
	        String altUrlStr = System.getProperty(URL_PROP_NAME);
	        if (altUrlStr == null || altUrlStr.trim().length() < 1) {
	            altUrlStr = "https://api.rosette.com/rest/v1";
	        }
	        return altUrlStr.trim();
	    }
	    
	    protected static String responseToJson(Response response) throws JsonProcessingException {
	        ObjectMapper mapper = ApiModelMixinModule.setupObjectMapper(new ObjectMapper());
	        mapper.enable(SerializationFeature.INDENT_OUTPUT);
	        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        return mapper.writeValueAsString(response);
	    }

}
