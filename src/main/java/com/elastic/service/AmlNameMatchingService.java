package com.elastic.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.elastic.repository.RosetteAPIRepository;
import com.elastic.wrapper.model.MatchCriteria;
import com.elastic.wrapper.model.PythonResponseModel;
import com.elastic.wrapper.model.RequestModel;
import com.elastic.wrapper.model.ResponseJaroWinkler;
import com.elastic.wrapper.model.ResponseLevenshtein;
import com.elastic.wrapper.model.ResponseModel;
import com.elastic.wrapper.model.ResponseQRatio;
import com.elastic.wrapper.model.ResponseSetRatio;
import com.elastic.wrapper.model.ResponseSortRatio;
import com.elastic.wrapper.model.ResponseTargetNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trueid.aml.nmatch.data.PosidexData;
import com.trueid.aml.nmatch.data.PythonJaroData;
import com.trueid.aml.nmatch.data.PythonLevenData;
import com.trueid.aml.nmatch.data.PythonQratioData;
import com.trueid.aml.nmatch.data.PythonSetRatioData;
import com.trueid.aml.nmatch.data.PythonSortRationData;
import com.trueid.aml.nmatch.data.RosetteData;
import com.trueid.aml.nmatch.export.MatchingResultsExporter;

@Service
public class AmlNameMatchingService {

	@Autowired
	private RestTemplate template;

	@Autowired
	private RosetteAPIRepository repository;

	@Value("${posidex.api.url}")
	private String posidexUrl;

	@Value("${python.api.url}")
	private String pythonUrl;

	public String consumerService(RequestModel model) {

		HashMap dataMap = new HashMap();

		List<PosidexData> posidexList = new ArrayList<>();
		try {
			posidexList = callPosidexService(model);
			dataMap.put("POSIDEX", posidexList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String name = "";

		if (!"".equals(model.getFirstNameEnglish()) && (!"".equals(model.getSecondNameEnglish()))) {
			name = model.getFirstNameEnglish() + " " + model.getSecondNameEnglish();
		} else if (!"".equals(model.getFirstNameEnglish())) {
			name = model.getFirstNameEnglish();
		} else {
			name = model.getSecondNameEnglish();
		}
		List<RosetteData> rosetteList = new ArrayList<>();

		try {
			rosetteList = repository.searchByQueryString(name);
			dataMap.put("ROSETTE", rosetteList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		HashMap pythonMap = new HashMap();
		List<ResponseTargetNames> targetList = new ArrayList<>();
		try {
			pythonMap = this.callPythonServiceReq(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		dataMap.putAll(pythonMap);

		System.out.println("Entire Data -->  " + dataMap);

		MatchingResultsExporter exporter = new MatchingResultsExporter();
		exporter.export(dataMap, name);

		return "Data Export Successfully";
	}

	public List<PosidexData> callPosidexService(RequestModel model) {
		HttpEntity<RequestModel> requestEntity = new HttpEntity<RequestModel>(model);

		ResponseEntity<String> response = null;
		ObjectMapper objectMapper = new ObjectMapper();
		List<PosidexData> listResponse = new ArrayList<>();
		List<MatchCriteria> match = new ArrayList<>();
		String matchNames = "";
		String strengths = "";

		try {

			response = template.exchange(posidexUrl, HttpMethod.POST, requestEntity, String.class);
			ResponseModel object = objectMapper.readValue(response.getBody().toString(), ResponseModel.class);
			match = object.getResponse().getWorldcheckmatchresponse().getMatchCriteriaList();

			for (MatchCriteria posidex : match) {

				matchNames = posidex.getMatchedName();
				strengths = posidex.getStrengths();
				String[] splitMatchNames = matchNames.split(";");
				String[] splitStrengths = strengths.split(";");

				int x = splitMatchNames.length;
				int y = splitStrengths.length;

				if (x == y) {
					for (int i = 0; (i < x && i < y); i++) {
						PosidexData details = new PosidexData();
						details.setUid(Long.valueOf(posidex.getPrimarypersonality()));
						details.setMatchedName(splitMatchNames[i]);
						details.setScore(Float.valueOf(splitStrengths[i]));
						listResponse.add(details);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// String jsonCartList = gson.toJson(listResponse);
		System.out.println("Posidex List  Before --" + listResponse);

		/*
		 * Collections.sort(listResponse, new Comparator<PosidexData>() {
		 * 
		 * @Override public int compare(PosidexData e1, PosidexData e2) { return
		 * e1.getUid().compareTo(e2.getUid()); } });
		 * 
		 * System.out.println("Posidex List  After --" + listResponse);
		 */

		return listResponse;
	}

	public HashMap callPythonServiceReq(RequestModel model) {
		String name = model.getFirstNameEnglish() + " " + model.getSecondNameEnglish() + " "
				+ model.getThirdNameEnglish() + " " + model.getTribeNameEnglish();

		// adding the formdata into headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("lang_val", "1");
		multipartBodyBuilder.part("uinput_val", name);

		// multipart/form-data request body
		MultiValueMap<String, HttpEntity<?>> multipartBody = multipartBodyBuilder.build();

		// The complete http request body.
		HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity = new HttpEntity<>(multipartBody, headers);

		ResponseEntity<String> response = null;
		ObjectMapper objectMapper = new ObjectMapper();
		PythonResponseModel object = new PythonResponseModel();
		List<ResponseTargetNames> targetNamesList = new ArrayList<>();
		List<PythonJaroData> pData = new ArrayList<>();
		List<PythonLevenData> rData = new ArrayList<>();
		List<PythonQratioData> qData = new ArrayList<>();
		List<PythonSetRatioData> setData = new ArrayList<>();
		List<PythonSortRationData> sortData = new ArrayList<>();
		HashMap dataMap = new HashMap();
		try {

			response = template.postForEntity(pythonUrl, httpEntity, String.class);
			object = objectMapper.readValue(response.getBody().toString(), PythonResponseModel.class);
			// System.out.println("Python object --> " + response);
			for (ResponseTargetNames targetNames : object.getTargetNames()) {

				for (ResponseJaroWinkler winkler : targetNames.getJaroWinkler()) {
					PythonJaroData jaroData = new PythonJaroData();
					jaroData.setUid(winkler.getUid());
					jaroData.setMatchedName(winkler.getPersonName());
					jaroData.setScore(winkler.getScore());

					pData.add(jaroData);
				}

				for (ResponseLevenshtein winkler : targetNames.getLevenshTein()) {
					PythonLevenData pyData = new PythonLevenData();
					pyData.setUid(winkler.getUid());
					pyData.setMatchedName(winkler.getPersonName());
					pyData.setScore(winkler.getScore());

					rData.add(pyData);
				}

				for (ResponseQRatio winkler : targetNames.getQratio()) {
					PythonQratioData qrData = new PythonQratioData();
					qrData.setUid(winkler.getUid());
					qrData.setMatchedName(winkler.getPersonName());
					qrData.setScore(winkler.getScore());

					qData.add(qrData);
				}

				for (ResponseSetRatio winkler : targetNames.getSetRatio()) {
					PythonSetRatioData setRData = new PythonSetRatioData();
					setRData.setUid(winkler.getUid());
					setRData.setMatchedName(winkler.getPersonName());
					setRData.setScore(winkler.getScore());

					setData.add(setRData);
				}

				for (ResponseSortRatio winkler : targetNames.getSortRatio()) {
					PythonSortRationData sortRData = new PythonSortRationData();
					sortRData.setUid(winkler.getUid());
					sortRData.setMatchedName(winkler.getPersonName());
					sortRData.setScore(winkler.getScore());

					sortData.add(sortRData);
				}

			}

			dataMap.put("JARO", pData);
			dataMap.put("LEVENSHTEIN", rData);
			dataMap.put("QRATIO", qData);
			dataMap.put("SETRATIO", setData);
			dataMap.put("SORTRATIO", sortData);

			System.out.println(dataMap);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataMap;
	}

}
