package com.elastic.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elastic.model.RapidModel;
import com.elastic.model.RosetteRequestData;
import com.elastic.response.ResponseData;
import com.elastic.service.RosetteAPIService;
import com.elastic.utility.ResponseElastic;

@RestController
@RequestMapping("api/v2")
public class RosetteAPIController {
	
	@Autowired
	private RosetteAPIService service;
	
	@PostMapping("/callAPI")
	public ResponseEntity<ResponseData> callService(@RequestBody RosetteRequestData data){
		//AccessToken accToken = KeycloakAuthorization.authorizeToken(refreshToken);
		List<ResponseData> response = new ArrayList<>();
		 response = service.searchByNameElastic(data);
		
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
	@PostMapping("/callService")
	public ResponseEntity<ResponseData> callElasticService(@RequestBody RosetteRequestData data){
		//AccessToken accToken = KeycloakAuthorization.authorizeToken(refreshToken);
		List<ResponseData> response = new ArrayList<>();
		 response = service.searchByNameElastic(data);
		
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
	@PostMapping("/callAPIAR")
	public ResponseEntity<ResponseData> callServiceAR(@RequestBody RosetteRequestData data){
		
		List<ResponseData> response = service.searchByArabicNameElastic(data);
		
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
	@PostMapping("/callWithMulti")
	public ResponseEntity<ResponseData> callServiceWithMultiMatch(@RequestBody RosetteRequestData data){
		
		List<ResponseElastic> response = service.searchByENMultiMatch(data);
		
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
	@GetMapping("/callRapid")
	public ResponseEntity<ResponseData> callRapidService(@RequestBody RapidModel data){
		
		 service.callService(data);
		
		return new ResponseEntity("Rapid Service", HttpStatus.OK);
	}

}
