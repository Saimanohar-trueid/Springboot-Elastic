package com.elastic.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elastic.service.AmlNameMatchingService;
import com.elastic.wrapper.model.RequestModel;

@RestController
@RequestMapping("/consumer")
public class ServiceConsumerController {

	@Autowired
	private AmlNameMatchingService service;

	@PostMapping("/service")
	private ResponseEntity<?> callService(@RequestBody RequestModel model) {
		String response = "";

		try {
			response = service.consumerService(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * MockDataBuilder mdb = new MockDataBuilder(); HashMap dmap =
		 * mdb.prepareMockData(); MatchingResultsExporter mre = new
		 * MatchingResultsExporter(); mre.export(dmap);
		 */

		return new ResponseEntity(response, HttpStatus.OK);
	}

}
