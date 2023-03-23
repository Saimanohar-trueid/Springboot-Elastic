package com.elastic.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aspose.cells.JsonLayoutOptions;
import com.aspose.cells.JsonUtility;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.elastic.model.RapidModel;
import com.elastic.model.RosetteRequestData;
import com.elastic.repository.RosetteAPIRepository;
import com.elastic.response.ResponseData;
import com.elastic.utility.ResponseElastic;
import com.elastic.utility.RosetteAPIUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trueid.aml.nmatch.data.RosetteData;

@Service
public class RosetteAPIService {

	@Autowired
	private RosetteAPIRepository repository;

	public List<ResponseData> searchByNameElastic(RosetteRequestData data) {

		String key = data.getDataType();
		String value = data.getName();

		RosetteAPIUtility utility = new RosetteAPIUtility();
		List<ResponseData> response = new ArrayList<>();

		
		  try { List<RosetteData> list = repository.searchByQueryString(value); //
		 // List<ResponseElastic> listval = repository.searchByQueryString(value);
		  
		   //response = utility.callRosetteAPI(list, value);
		   
		   System.out.println(response);
		  
		//  loadElasticDataintoCSV(list);
		  
		  } catch (IOException e) { // TODO Auto-generated catch block
		  e.printStackTrace(); }
		 

		return response;
	}

	public void loadDataintoCSV(List<ResponseData> data) {
		// load JSON data
		String jsonInput = "";
		String fileName = "D:\\Files\\RosetteResponse" + LocalDate.now() + ".csv";
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonInput = mapper.writeValueAsString(data);
			System.out.println("ResultingJSONstring = " + jsonInput);
			// System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		// String jsonInput =
		// "[{'nodeId':1,'reputation':1134},{'nodeId':2,'reputation':547},{'nodeId':3,'reputation':1703},{'nodeId':4,'reputation':-199},{'nodeId':5,'reputation':-306},{'nodeId':6,'reputation':-49},{'nodeId':7,'reputation':1527},{'nodeId':8,'reputation':1223}]";

		// create a blank Workbook object
		Workbook workbook = new Workbook();

		// access default empty worksheet
		Worksheet worksheet = workbook.getWorksheets().get(0);

		// set JsonLayoutOptions for formatting
		JsonLayoutOptions layoutOptions = new JsonLayoutOptions();
		layoutOptions.setArrayAsTable(true);

		// export JSON data to CSV
		JsonUtility.importData(jsonInput, worksheet.getCells(), 0, 0, layoutOptions);

		// save CSV file
		try {
			workbook.save(fileName, SaveFormat.CSV);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadElasticDataintoCSV(List<ResponseElastic> data) {
		// load JSON data
		String jsonInput = "";
		String fileName = "D:\\Files\\RosetteResponse" + LocalDate.now() + ".csv";
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonInput = mapper.writeValueAsString(data);
			System.out.println("ResultingJSONstring = " + jsonInput);
			// System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		// String jsonInput =
		// "[{'nodeId':1,'reputation':1134},{'nodeId':2,'reputation':547},{'nodeId':3,'reputation':1703},{'nodeId':4,'reputation':-199},{'nodeId':5,'reputation':-306},{'nodeId':6,'reputation':-49},{'nodeId':7,'reputation':1527},{'nodeId':8,'reputation':1223}]";

		// create a blank Workbook object
		Workbook workbook = new Workbook();

		// access default empty worksheet
		Worksheet worksheet = workbook.getWorksheets().get(0);

		// set JsonLayoutOptions for formatting
		JsonLayoutOptions layoutOptions = new JsonLayoutOptions();
		layoutOptions.setArrayAsTable(true);

		// export JSON data to CSV
		JsonUtility.importData(jsonInput, worksheet.getCells(), 0, 0, layoutOptions);

		// save CSV file
		try {
			workbook.save(fileName, SaveFormat.CSV);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<ResponseData> searchByArabicNameElastic(RosetteRequestData data) {

		String key = data.getDataType();
		String value = data.getName();

		RosetteAPIUtility utility = new RosetteAPIUtility();
		List<ResponseData> response = new ArrayList<>();

		try {
			List<String> list = repository.searchByQuery(value);

			response = utility.callRosetteAPIAR(list, value);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	public List<ResponseElastic> searchByENMultiMatch(RosetteRequestData data) {

		String key = data.getDataType();
		String value = data.getName();

		RosetteAPIUtility utility = new RosetteAPIUtility();
		List<ResponseData> response = new ArrayList<>();
		List<ResponseElastic> list = new ArrayList<>();

		try {
			list = repository.searchByMultiMatch(value);

			// response = utility.callRosetteAPI(list, value);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}

	public void callService(RapidModel data) {
		
		String value = "https://full-name-match.p.rapidapi.com/getfullnamematch?fullname="+data.getFullname();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(value))
				.header("X-RapidAPI-Key", "a61e1310c6mshb1cb7ccdc0dbc5dp1e9059jsnf681510baf93")
				.header("X-RapidAPI-Host", "full-name-match.p.rapidapi.com")
				.method("GET", HttpRequest.BodyPublishers.noBody()).build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(response.body());

	}

}
