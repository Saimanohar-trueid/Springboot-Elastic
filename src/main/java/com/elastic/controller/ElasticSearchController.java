package com.elastic.controller;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.elastic.model.Aliases;
import com.elastic.model.ElasticARData;
import com.elastic.model.ElasticData;
import com.elastic.model.ElasticMain;
import com.elastic.model.Product;
import com.elastic.model.UserData;
import com.elastic.repository.ElasticSearchQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import jakarta.json.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ElasticSearchController {

	@Autowired
	private ElasticSearchQuery elasticSearchQuery;

	@PostMapping("/createOrUpdateDocument")
	public ResponseEntity<Object> createOrUpdateDocument(@RequestBody Product product) throws IOException {
		String response = elasticSearchQuery.createOrUpdateDocument(product);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/getDocument/{productId}")
	public ResponseEntity<Object> getDocumentById(@PathVariable String productId) throws IOException {
		Product product = elasticSearchQuery.getDocumentById(productId);
		return new ResponseEntity<>(product, HttpStatus.OK);
	}

	@DeleteMapping("/deleteDocument/{productId}")
	public ResponseEntity<Object> deleteDocumentById(@PathVariable String productId) throws IOException {
		String response = elasticSearchQuery.deleteDocumentById(productId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/searchDocument")
	public ResponseEntity<Object> searchAllDocument() throws IOException {
		List<Product> products = elasticSearchQuery.searchAllDocuments();
		return new ResponseEntity<>(products, HttpStatus.OK);
	}

	@PostMapping("/upload")
	public ResponseEntity<Object> upload() throws IOException, ParseException {

		String data = readJsonFromFile("C:\\Users\\TrueID - Sai\\Desktop\\Data\\wc-names-data1.json");

		System.out.println(data);

		List<UserData> userDatas = convert(data);
		String response = null;
		ElasticData elasticData = new ElasticData();
		int count = 0;

		for (int i = 0; i < userDatas.size(); i++) {

			List<Aliases> listAliases = new ArrayList<>();
			List<String> aliaStr = new ArrayList<>();
			ElasticMain main = new ElasticMain();

			UserData userData = userDatas.get(i);
			elasticData.setUid(userData.getUid());
			aliaStr.addAll(userData.getAliases());

			for (String fname : userData.getpData().getFn()) {
				elasticData.setFirstname(fname);
			}
			for (String lname : userData.getpData().getLf()) {
				elasticData.setLastname(lname);
			}

			if (!"".equals(elasticData.getFirstname()) && "".equals(elasticData.getLastname()))
				elasticData.setFullname(elasticData.getFirstname());
			else if ("".equals(elasticData.getFirstname()) && !"".equals(elasticData.getLastname()))
				elasticData.setFullname(elasticData.getLastname());
			else
				elasticData.setFullname(elasticData.getFirstname() + " " + elasticData.getLastname());

			aliaStr = userData.getAliases();
			String aliasesList = String.join("; ", aliaStr);

			if (!"".equals(aliaStr)) {
				for (String string : aliaStr) {
					Aliases aliases = new Aliases();
					if (!"".equals(string)) {
						aliases.setAlias(string);
						listAliases.add(aliases);
					}

				}
			} else {
				Aliases aliases = new Aliases();
				aliases.setAlias(" ");
				listAliases.add(aliases);
			}
			if(aliasesList != null && !"".equals(aliasesList))
			//elasticData.setAliases(aliasesList);
			elasticData.setEntityType("PERSON");
			main.setData(elasticData);
			/*
			 * if (listAliases.isEmpty()) main.setAliases(null); else
			 * main.setAliases(listAliases);
			 */
			System.out.println("Uid  " + elasticData.getUid());
			response = elasticSearchQuery.create(main);
			System.out.println("Count  " + count++);

		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/csvUpload")
	public ResponseEntity<Object> csvUpload() {
		String line = "";
		ElasticARData arData = new ElasticARData();
		ElasticARData arData1 = new ElasticARData();
		ElasticARData arData2 = new ElasticARData();
		int count = 0;
		String response = null;
		try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/AML-AR-NAMES-UUID.CSV"))) {
			while ((line = br.readLine()) != null) {

				String[] data = line.split(",");
				arData.setUuid(data[0]);
				arData.setAliases(data[1]);
				arData1.setPerson(arData);

				response = elasticSearchQuery.createCSV(arData1);
				System.out.println("Count " + count++);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	public String readJsonFromFile(String filePath) throws IOException, ParseException {
		String json = Files.readString(Paths.get(filePath));
		// JsonNode node = new JSONParser().parse(json);
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = objectMapper.writeValueAsString(new JSONParser().parse(json));
		return jsonString;
	}

	public List<UserData> convert(String JsonString) throws JsonMappingException, JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		CollectionType typeReference = TypeFactory.defaultInstance().constructCollectionType(List.class,
				UserData.class);
		List<UserData> data = om.readValue(JsonString, typeReference);
		// List<UserData> userDataList = om.readValue(JsonString, new
		// TypeReference<List<UserData>>() {});

		return data;
	}

}