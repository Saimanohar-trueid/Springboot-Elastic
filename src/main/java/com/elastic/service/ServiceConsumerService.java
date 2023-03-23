package com.elastic.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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

import com.aspose.cells.JsonLayoutOptions;
import com.aspose.cells.JsonUtility;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.elastic.repository.RosetteAPIRepository;
import com.elastic.utility.ResponseElastic;
import com.elastic.wrapper.model.LoadListDataIntoCSV;
import com.elastic.wrapper.model.LoadListDataIntoLocalCSV;
import com.elastic.wrapper.model.MatchCriteria;
import com.elastic.wrapper.model.PosidexResponseDetails;
import com.elastic.wrapper.model.PythonResponseDetails;
import com.elastic.wrapper.model.PythonResponseModel;
import com.elastic.wrapper.model.RequestModel;
import com.elastic.wrapper.model.ResponseJaroWinkler;
import com.elastic.wrapper.model.ResponseLevenshtein;
import com.elastic.wrapper.model.ResponseModel;
import com.elastic.wrapper.model.ResponseQRatio;
import com.elastic.wrapper.model.ResponseSetRatio;
import com.elastic.wrapper.model.ResponseSortRatio;
import com.elastic.wrapper.model.ResponseTargetNames;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.trueid.aml.nmatch.data.PosidexData;

@Service
public class ServiceConsumerService {

	@Autowired
	private RestTemplate template;

	/*
	 * @Autowired private RosetteAPIRepository repository;
	 */
//	@Value("${posidex.api.url}")
	private String posidexUrl;

	//@Value("${python.api.url}")
	private String pythonUrl;

	public String consumerService(RequestModel model) {
		LoadListDataIntoCSV dataIntoCSV = new LoadListDataIntoCSV();
		LoadListDataIntoLocalCSV localDataInfo = new LoadListDataIntoLocalCSV();
		// Calling Posidex API
		List<PosidexData> posidexList = new ArrayList<>();
		// LinkedHashMap<Integer, PosidexResponseDetails> posidexMap = new
		// LinkedHashMap<>();
		try {
			posidexList = callPosidexService(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Calling ElasticSerach API
		String name = "";
		name = model.getFirstNameEnglish() + " " + model.getSecondNameEnglish() + " " + model.getThirdNameEnglish()
				+ " " + model.getTribeNameEnglish();

		List<ResponseElastic> rosetteList = new ArrayList<>();
		List<ResponseElastic> elasticAliasList = new ArrayList<>();
		try {
			//rosetteList = repository.searchByQueryString(name);
			//elasticAliasList = repository.searchByQueryAliases(name);
			rosetteList.addAll(elasticAliasList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Call Python Service
		List<ResponseTargetNames> targetList = new ArrayList<>();
		try {
			// targetList = this.callPythonServiceReq(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

		localDataInfo.setRosetteDetails(rosetteList);

		/*
		 * for (ResponseTargetNames responseTargetNames : targetList) {
		 * localDataInfo.setJaroListDetails(responseTargetNames.getJaroWinkler());
		 * localDataInfo.setLevenshteinListDetails(responseTargetNames.getLevenshTein())
		 * ; localDataInfo.setQratioListDetails(responseTargetNames.getQratio());
		 * localDataInfo.setSetRatioList(responseTargetNames.getSetRatio());
		 * localDataInfo.setSortRatioList(responseTargetNames.getSortRatio()); }
		 */

		//dataIntoCSV.setPosidexDetails(posidexList);
		dataIntoCSV.setPythonDetails(targetList);
		dataIntoCSV.setRosetteDetails(rosetteList);

		// Grouping the Uid and stored the data into CSV
		dataInAlgorithms(dataIntoCSV, name);

		return "";

	}

	public void dataInAlgorithms(LoadListDataIntoCSV loadList, String name) {
		String uid = "";

		List<PosidexResponseDetails> scoreList = loadList.getPosidexDetails();

		List<PosidexResponseDetails> posidexList = new ArrayList<>(); //
		List<ResponseElastic> rosetteList = new ArrayList<>();
		List<PythonResponseDetails> pythonList = new ArrayList<>();
		LinkedHashMap<String, PosidexResponseDetails> map = new LinkedHashMap<>();

		/*
		 * Collections.sort(scoreList, new Comparator<PosidexResponseDetails>() {
		 * 
		 * @Override public int compare(PosidexResponseDetails e1,
		 * PosidexResponseDetails e2) { return e1.getUuid().compareTo(e2.getUuid()); }
		 * });
		 */

		/*
		 * for (int i = 0; i < scoreList.size(); i++) {
		 * 
		 * PosidexResponseDetails temp = loadList.getPosidexDetails().get(i);
		 * 
		 * if (Long.valueOf(scoreList.get(i).getScore()) >
		 * Long.valueOf(score.getScore())) { //PosidexResponseDetails details = new
		 * PosidexResponseDetails();
		 * //details.setUuid(loadList.getPosidexDetails().get(i).getUuid());
		 * //details.setName(loadList.getPosidexDetails().get(i).getName());
		 * //details.setScore(loadList.getPosidexDetails().get(i).getScore());
		 * 
		 * score = temp;
		 * 
		 * //score.setScore(loadList.getPosidexDetails().get(i).getScore()); //=
		 * Long.valueOf(loadList.getPosidexDetails().get(i).getScore()); }
		 * 
		 * }
		 */
		// posidexList.add(score);
		// loadList.setPosidexDetails(posidexList);
		/*
		 * try { //loadData(loadList, name); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

	public List<PosidexData> callPosidexService(RequestModel model) {
		HttpEntity<RequestModel> requestEntity = new HttpEntity<RequestModel>(model);

		ResponseEntity<String> response = null;
		ObjectMapper objectMapper = new ObjectMapper();
		List<PosidexData> listResponse = new ArrayList<>();
		LinkedHashMap<Integer, PosidexResponseDetails> map = new LinkedHashMap<>();
		Gson gson = new Gson();
		List<MatchCriteria> match = new ArrayList<>();
		String matchNames = "";
		String strengths = "";
		try {

			response = template.exchange(posidexUrl, HttpMethod.POST, requestEntity, String.class);
			ResponseModel object = objectMapper.readValue(response.getBody().toString(), ResponseModel.class);
			match = object.getResponse().getWorldcheckmatchresponse().getMatchCriteriaList();

			// System.out.println("Posidex object --> " + response);

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
						// map.put(i, details);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		String jsonCartList = gson.toJson(listResponse);
		System.out.println("jsonCartList   --" + jsonCartList);
		return listResponse;
	}

	public List<PythonResponseDetails> callPythonService(RequestModel model) {
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
		List<PythonResponseDetails> listResponse = new ArrayList<>();

		try {

			response = template.postForEntity(pythonUrl, httpEntity, String.class);
			PythonResponseModel object = objectMapper.readValue(response.getBody().toString(),
					PythonResponseModel.class);

			/*
			 * for (ResponseTargetNames targetNames : object.getTargetNames()) {
			 * 
			 * for (ResponseJaroWinkler jaroWinkler : targetNames.getJaroWinkler()) {
			 * PythonResponseDetails details = new PythonResponseDetails();
			 * details.setUuid(jaroWinkler.getUid().toString());
			 * details.setName_strengths(jaroWinkler.getPersonName() + ";" +
			 * jaroWinkler.getScore()); listResponse.add(details); }
			 * 
			 * for (ResponseLevenshtein levenshtein : targetNames.getLevenshTein()) {
			 * PythonResponseDetails details = new PythonResponseDetails();
			 * details.setUuid(levenshtein.getUid().toString());
			 * details.setName_strengths(levenshtein.getPersonName() + ";" +
			 * levenshtein.getScore()); listResponse.add(details); }
			 * 
			 * for (ResponseQRatio qRatio : targetNames.getQratio()) { PythonResponseDetails
			 * details = new PythonResponseDetails();
			 * details.setUuid(qRatio.getUid().toString());
			 * details.setName_strengths(qRatio.getPersonName() + ";" + qRatio.getScore());
			 * listResponse.add(details); }
			 * 
			 * for (ResponseSetRatio setRatio : targetNames.getSetRatio()) {
			 * PythonResponseDetails details = new PythonResponseDetails();
			 * details.setUuid(setRatio.getUid().toString());
			 * details.setName_strengths(setRatio.getPersonName() + ";" +
			 * setRatio.getScore()); listResponse.add(details); }
			 * 
			 * for (ResponseSortRatio sortRatio : targetNames.getSortRatio()) {
			 * PythonResponseDetails details = new PythonResponseDetails();
			 * details.setUuid(sortRatio.getUid().toString());
			 * details.setName_strengths(sortRatio.getPersonName() + ";" +
			 * sortRatio.getScore()); listResponse.add(details); }
			 * 
			 * }
			 */
			// System.out.println(listResponse);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return listResponse;
	}

	public List<ResponseTargetNames> callPythonServiceReq(RequestModel model) {
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
		try {

			response = template.postForEntity(pythonUrl, httpEntity, String.class);
			object = objectMapper.readValue(response.getBody().toString(), PythonResponseModel.class);
			// System.out.println("Python object --> " + response);
			for (ResponseTargetNames targetNames : object.getTargetNames()) {
				ResponseTargetNames names = new ResponseTargetNames();
				names.setJaroWinkler(targetNames.getJaroWinkler());
				names.setLevenshTein(targetNames.getLevenshTein());
				names.setQratio(targetNames.getQratio());
				names.setSetRatio(targetNames.getSetRatio());
				names.setSortRatio(targetNames.getSortRatio());
				targetNamesList.add(names);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return targetNamesList;
	}

	private static HttpEntity<?> getHeaders() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		return new HttpEntity<>(headers);
	}

	/*
	 * public void loadElasticDataintoCSV(LoadListDataIntoLocalCSV data) { // load
	 * JSON data String jsonInput = ""; String fileName = "D:\\Files\\Response" +
	 * LocalDate.now() + ".csv"; ObjectMapper mapper = new ObjectMapper();
	 * 
	 * data.getJaroListDetails();
	 * 
	 * try { jsonInput = mapper.writeValueAsString(data);
	 * //System.out.println("ResultingJSONstring = " + jsonInput); //
	 * System.out.println(json); } catch (JsonProcessingException e) {
	 * e.printStackTrace(); }
	 * 
	 * // String jsonInput = //
	 * "[{'nodeId':1,'reputation':1134},{'nodeId':2,'reputation':547},{'nodeId':3,'reputation':1703},{'nodeId':4,'reputation':-199},{'nodeId':5,'reputation':-306},{'nodeId':6,'reputation':-49},{'nodeId':7,'reputation':1527},{'nodeId':8,'reputation':1223}]";
	 * 
	 * // create a blank Workbook object Workbook workbook = new Workbook();
	 * 
	 * // access default empty worksheet Worksheet worksheet =
	 * workbook.getWorksheets().get(0);
	 * 
	 * // set JsonLayoutOptions for formatting JsonLayoutOptions layoutOptions = new
	 * JsonLayoutOptions(); layoutOptions.setArrayAsTable(true);
	 * 
	 * // export JSON data to CSV JsonUtility.importData(jsonInput,
	 * worksheet.getCells(), 0, 0, layoutOptions);
	 * 
	 * // save CSV file try { workbook.save(fileName, SaveFormat.CSV); } catch
	 * (Exception e) { e.printStackTrace(); } }
	 * 
	 * public void loadData(LoadListDataIntoCSV listData, String requName) throws
	 * IOException {
	 * 
	 * String[] columns = { "Algorithm", " Screened Name", " matched data" };
	 * 
	 * org.apache.poi.ss.usermodel.Workbook workbook = new HSSFWorkbook(); // create
	 * a new workbook CreationHelper createHelper = workbook.getCreationHelper();
	 * Sheet sheet = workbook.createSheet("WorldCheck Data");
	 * 
	 * // Create a Font for styling header cells Font headerFont =
	 * workbook.createFont(); headerFont.setBold(true);
	 * headerFont.setFontHeightInPoints((short) 10);
	 * headerFont.setColor(IndexedColors.BLACK.getIndex());
	 * 
	 * // Create a CellStyle with the font CellStyle headerCellStyle =
	 * workbook.createCellStyle(); headerCellStyle.setFont(headerFont);
	 * 
	 * // create a row for the header Row headerRow = sheet.createRow(0);
	 * 
	 * // Creating cells for (int i = 0; i < columns.length; i++) { Cell cell =
	 * headerRow.createCell(i); cell.setCellValue(columns[i]);
	 * cell.setCellStyle(headerCellStyle); }
	 * 
	 * // Cell Style for formatting Date CellStyle dateCellStyle =
	 * workbook.createCellStyle();
	 * dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(
	 * "dd-MM-yyyy")); //System.out.println(listData); // Create Other rows and
	 * cells with employees data int rowNum = 1; int rowNum1 = 2, rowNum2 = 3,
	 * rowNum3 = 4, rowNum4 = 5, rowNum5 = 6, rowNum6 = 7, rowNum7 = 8; Row r =
	 * sheet.createRow(rowNum);
	 * 
	 * List<PosidexResponseDetails> psdList = listData.getPosidexDetails();
	 * List<ResponseElastic> rspElastic = listData.getRosetteDetails();
	 * List<ResponseTargetNames> namesList = listData.getPythonDetails();
	 * List<ResponseJaroWinkler> jaroList = new ArrayList<>();
	 * List<ResponseLevenshtein> levenList = new ArrayList<>(); List<ResponseQRatio>
	 * qratioList = new ArrayList<>(); List<ResponseSetRatio> setList = new
	 * ArrayList<>(); List<ResponseSortRatio> sortList = new ArrayList<>(); for
	 * (ResponseTargetNames responseTargetNames : namesList) { jaroList =
	 * responseTargetNames.getJaroWinkler(); levenList =
	 * responseTargetNames.getLevenshTein(); qratioList =
	 * responseTargetNames.getQratio(); setList = responseTargetNames.getSetRatio();
	 * sortList = responseTargetNames.getSortRatio(); }
	 * 
	 * Collections.sort(psdList, new Comparator<PosidexResponseDetails>() {
	 * 
	 * @Override public int compare(PosidexResponseDetails e1,
	 * PosidexResponseDetails e2) { return
	 * Long.valueOf(e1.getUuid()).compareTo(Long.valueOf(e2.getUuid())); } });
	 * 
	 * List<PosidexResponseDetails> psdListMain = new ArrayList<>();
	 * 
	 * if (psdList.size() == 1) { psdListMain.add(psdList.get(0)); } else { if
	 * (psdList.get(0).getUuid() != psdList.get(1).getUuid()) {
	 * psdListMain.add(psdList.get(0));
	 * 
	 * } // Long scoreVal = PosidexResponseDetails tempVal = psdList.get(1); for
	 * (int i = 1; i < psdList.size() - 1; i++) { // PosidexResponseDetails temp1=
	 * psdList.get(i); // PosidexResponseDetails temp2=psdList.get(i-1); while
	 * (psdList.get(i).getUuid().equals(psdList.get(i - 1).getUuid())) { if
	 * (tempVal.getScore() < psdList.get(i).getScore()) { tempVal = psdList.get(i);
	 * } i++; } psdListMain.add(tempVal);
	 * 
	 * if (i < psdList.size()) tempVal = psdList.get(i); } }
	 * 
	 * for (PosidexResponseDetails posidexResponseDetails : psdList) {
	 * //System.out.print("Score Posidex List &&&&&   " +
	 * posidexResponseDetails.getUuid() + "   ---  " // +
	 * posidexResponseDetails.getScore()); } // System.out.println(); for
	 * (PosidexResponseDetails posidexResponseDetails : psdListMain) {
	 * //System.out.print("Score Posidex List   " + posidexResponseDetails.getUuid()
	 * + "   ---  " // + posidexResponseDetails.getScore()); }
	 * 
	 * //System.out.println();
	 * 
	 * Collections.sort(rspElastic, new Comparator<ResponseElastic>() {
	 * 
	 * @Override public int compare(ResponseElastic e1, ResponseElastic e2) { return
	 * Long.valueOf(e1.getUid()).compareTo(Long.valueOf(e2.getUid())); } });
	 * 
	 * List<ResponseElastic> rosListMain = new ArrayList<>();
	 * 
	 * if (rspElastic.size() == 1) { rosListMain.add(rspElastic.get(0)); } else { if
	 * (!rspElastic.get(0).getUid().equals(rspElastic.get(1).getUid())) {
	 * rosListMain.add(rspElastic.get(0));
	 * 
	 * } // Long scoreVal = ResponseElastic tempVal = rspElastic.get(1); for (int i
	 * = 1; i < rspElastic.size() - 1; i++) { boolean flag = false; // //
	 * PosidexResponseDetails temp1 = psdList.get(i); // // PosidexResponseDetails
	 * temp2 = psdList.get(i - 1); while
	 * (rspElastic.get(i).getUid().equals(rspElastic.get(i - 1).getUid())) { if
	 * (tempVal.getScore() < rspElastic.get(i).getScore()) { tempVal =
	 * rspElastic.get(i); } i++; flag = true; } if (i < rspElastic.size() && flag ==
	 * false) tempVal = rspElastic.get(i);
	 * 
	 * rosListMain.add(tempVal);
	 * 
	 * } }
	 * 
	 * for (ResponseElastic posidexResponseDetails : rspElastic) {
	 * //System.out.print("Score Rosette List &&&&&   " +
	 * posidexResponseDetails.getUid() + "   ---  " // +
	 * posidexResponseDetails.getScore()); } //System.out.println(); for
	 * (ResponseElastic posidexResponseDetails : rosListMain) {
	 * //System.out.print("Score Rosette List   " + posidexResponseDetails.getUid()
	 * + "   ---  " // + posidexResponseDetails.getScore()); }
	 * 
	 * //System.out.println(); TreeSet<Double> scoreStr = new TreeSet<>(); for
	 * (PosidexResponseDetails string : psdList) {
	 * scoreStr.add(Double.valueOf(string.getScore()));
	 * //System.out.print(string.getScore() + " "); } //System.out.println(); for
	 * (ResponseElastic string : rspElastic) {
	 * scoreStr.add(Double.valueOf(string.getScore()));
	 * //System.out.print(string.getScore() + " "); } //System.out.println();
	 * 
	 * for (ResponseJaroWinkler string : jaroList) {
	 * scoreStr.add(string.getScore()); // System.out.print(string.getScore() +
	 * " "); } //System.out.println(); for (ResponseLevenshtein string : levenList)
	 * { scoreStr.add(string.getScore()); // System.out.print(string.getScore() +
	 * " "); } // System.out.println(); for (ResponseQRatio string : qratioList) {
	 * scoreStr.add(string.getScore()); //System.out.print(string.getScore() + " ");
	 * } // System.out.println(); for (ResponseSetRatio string : setList) {
	 * scoreStr.add(string.getScore()); // System.out.print(string.getScore() +
	 * " "); } // System.out.println(); for (ResponseSortRatio string : sortList) {
	 * scoreStr.add(string.getScore()); // System.out.print(string.getScore() +
	 * " "); }
	 * 
	 * // System.out.println();
	 * 
	 * TreeSet<Long> uidStr = new TreeSet<>(); // uidStr.add(requName); //
	 * System.out.println("PosidexResponseDetails ::"); for (PosidexResponseDetails
	 * string : psdList) { uidStr.add(Long.valueOf(string.getUuid())); //
	 * System.out.print(string.getUuid() + " "); }
	 * 
	 * // System.out.println(); // System.out.println("ResponseElastic ::"); for
	 * (ResponseElastic string : rspElastic) {
	 * uidStr.add(Long.valueOf(string.getUid())); //
	 * System.out.print(string.getUid() + " "); }
	 * 
	 * // System.out.println(); // System.out.println("ResponseJaroWinkler ::"); for
	 * (ResponseJaroWinkler string : jaroList) { uidStr.add(string.getUid()); //
	 * System.out.print(string.getUid() + " "); }
	 * 
	 * // System.out.println(); // System.out.println("ResponseLevenshtein ::"); for
	 * (ResponseLevenshtein string : levenList) { uidStr.add(string.getUid()); //
	 * System.out.print(string.getUid() + " "); }
	 * 
	 * // System.out.println(); // System.out.println("ResponseQRatio ::"); for
	 * (ResponseQRatio string : qratioList) { uidStr.add(string.getUid()); //
	 * System.out.print(string.getUid() + " "); }
	 * 
	 * // System.out.println(); // System.out.println("ResponseSetRatio ::"); for
	 * (ResponseSetRatio string : setList) { uidStr.add(string.getUid()); //
	 * System.out.print(string.getUid() + " "); }
	 * 
	 * // System.out.println(); // System.out.println("ResponseSortRatio ::"); for
	 * (ResponseSortRatio string : sortList) { uidStr.add(string.getUid()); //
	 * System.out.print(string.getUid() + " "); }
	 * 
	 * // System.out.println(uidStr);
	 * 
	 * List<Long> str = new ArrayList<>(); str.addAll(uidStr);
	 * 
	 * // System.out.println("System.out.println(uidStr);" + str);
	 * 
	 * //System.out.println("List 1");
	 * 
	 * int k = 0;
	 * 
	 * List<Double> strScore = new ArrayList<>(); strScore.addAll(scoreStr);
	 * 
	 * Collections.sort(strScore, Collections.reverseOrder()); //
	 * Collections.sort(strScore.)
	 * 
	 * //System.out.println("scoreStr  -->  " + strScore);
	 * 
	 * Collections.sort(psdListMain, new Comparator<PosidexResponseDetails>() {
	 * 
	 * @Override public int compare(PosidexResponseDetails e1,
	 * PosidexResponseDetails e2) { return e2.getScore().compareTo(e1.getScore()); }
	 * });
	 * 
	 * // System.out.println("Posidex  " + psdListMain);
	 * 
	 * Row row = sheet.createRow(rowNum++); for (int i = 0, j = 0; j <
	 * psdListMain.size() && i < strScore.size(); i++) { // PosidexResponseDetails
	 * details = csvData.get(i);
	 * 
	 * if (rowNum == 2) { row.createCell(i).setCellValue("Posidex");
	 * row.createCell(i + 1).setCellValue(requName);
	 * 
	 * if (j < psdListMain.size() && (strScore.get(i) +
	 * "").equals(psdListMain.get(j).getScore().toString())) {
	 * 
	 * row.createCell(k + 2).setCellValue(psdListMain.get(j).getUuid());
	 * row.createCell(k + 3) .setCellValue(psdListMain.get(j).getName() + " ;" +
	 * psdListMain.get(j).getScore()); // row.createCell(i +
	 * 4).setCellValue(psdList.get(i).getScore());
	 * 
	 * //System.out.print(psdListMain.get(j).getScore() + " "); j++; // k = k + 2; }
	 * else if (j < psdListMain.size() && (!(strScore.get(i) +
	 * "").equals(psdListMain.get(j).getScore().toString()))) {
	 * 
	 * row.createCell(k + 2).setCellValue(""); row.createCell(k +
	 * 3).setCellValue(""); // row.createCell(i + 4).setCellValue(""); // k = k + 2;
	 * // System.out.print("Else condition "); }
	 * 
	 * } else { if (j < psdListMain.size() && (strScore.get(i) +
	 * "").equals(psdListMain.get(j).getScore().toString())) { row.createCell(k +
	 * 2).setCellValue(psdListMain.get(j).getUuid()); row.createCell(k + 3)
	 * .setCellValue(psdListMain.get(j).getName() + " ;" +
	 * psdListMain.get(j).getScore()); // row.createCell(i + i +
	 * 5).setCellValue(psdList.get(i).getScore()); //
	 * System.out.println(psdListMain.get(j).getUuid() + " "); // k = k + 2; j++; }
	 * else if (j < psdListMain.size() && (!(strScore.get(i) +
	 * "").equals(psdListMain.get(j).getScore().toString()))) { row.createCell(k +
	 * 2).setCellValue(""); row.createCell(k + 3).setCellValue(""); //
	 * System.out.println("Else "); // k = k + 2; // row.createCell(i + i +
	 * 5).setCellValue(""); } // // row.createCell(i + 5).setCellValue(); } k = k +
	 * 2; rowNum++; }
	 * 
	 * // rowNum1 = rowNum++;
	 * 
	 * Collections.sort(rosListMain, new Comparator<ResponseElastic>() {
	 * 
	 * @Override public int compare(ResponseElastic e1, ResponseElastic e2) { return
	 * Double.valueOf(e2.getScore()).compareTo(Double.valueOf(e1.getScore())); } });
	 * 
	 * // System.out.println("");
	 * 
	 * // System.out.println("Rosette  " + rosListMain);
	 * 
	 * Set<Double> nameSet = new HashSet<>(); List<ResponseElastic> distinctByScore
	 * = rosListMain.stream().filter(e -> nameSet.add(e.getScore()))
	 * .collect(Collectors.toList());
	 * 
	 * // List<ResponseElastic> rosetMainSet = new ArrayList<>();
	 * 
	 * 
	 * Double scoreVal = 0.0;
	 * 
	 * for(int s=0;s<rosListMain.size();s++) {
	 * 
	 * if(rosListMain.get(s).getScore() != scoreVal) { ResponseElastic elastic = new
	 * ResponseElastic(); elastic.setUid(rosListMain.get(s).getUid());
	 * elastic.setName_strength(rosListMain.get(s).getName_strength());
	 * 
	 * scoreVal = rosListMain.get(s).getScore(); rosetMainSet.add(elastic); }
	 * 
	 * 
	 * }
	 * 
	 * 
	 * // System.out.println("Rosette  123" + distinctByScore);
	 * 
	 * // System.out.println("List 2"); k = 0; Row row1 =
	 * sheet.createRow(rowNum1++); for (int i = 0, j = 0; j < distinctByScore.size()
	 * && i < strScore.size(); i++) { if (rowNum1 == 3) {
	 * row1.createCell(i).setCellValue("Rosette"); row1.createCell(i +
	 * 1).setCellValue(requName); if (j < distinctByScore.size() && (strScore.get(i)
	 * + "").equals(distinctByScore.get(j).getScore().toString())) {
	 * row1.createCell(k + 2).setCellValue(distinctByScore.get(j).getUid());
	 * row1.createCell(k +
	 * 3).setCellValue(distinctByScore.get(j).getName_strength()); //
	 * System.out.print(distinctByScore.get(j).getUid() + " "); j++; // k = k + 2;
	 * 
	 * } else if (j < distinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(distinctByScore.get(j).getScore().toString()))) {
	 * row1.createCell(k + 2).setCellValue(""); row1.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else "); // k = k + 2; } } else {
	 * if (j < distinctByScore.size() && (strScore.get(i) +
	 * "").equals(distinctByScore.get(j).getScore().toString())) { row1.createCell(k
	 * + 2).setCellValue(distinctByScore.get(j).getUid()); row1.createCell(k +
	 * 3).setCellValue(distinctByScore.get(j).getName_strength()); //
	 * System.out.print(distinctByScore.get(j).getUid() + " "); j++; // k = k + 2; }
	 * else if (j < distinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(distinctByScore.get(j).getScore().toString()))) {
	 * row1.createCell(k + 2).setCellValue(""); row1.createCell(k +
	 * 3).setCellValue(""); // System.out.println("Else "); // k = k + 2; } } k = k
	 * + 2; rowNum1++; }
	 * 
	 * // System.out.println("");
	 * 
	 * Collections.sort(jaroList, new Comparator<ResponseJaroWinkler>() {
	 * 
	 * @Override public int compare(ResponseJaroWinkler e1, ResponseJaroWinkler e2)
	 * { return e1.getUid().compareTo(e2.getUid()); } });
	 * 
	 * 
	 * Collections.sort(jaroList, new Comparator<ResponseJaroWinkler>() {
	 * 
	 * @Override public int compare(ResponseJaroWinkler e1, ResponseJaroWinkler e2)
	 * { return e2.getScore().compareTo(e1.getScore()); } });
	 * 
	 * Set<Double> nameSet1 = new HashSet<>(); List<ResponseJaroWinkler>
	 * jarodistinctByScore = jaroList.stream().filter(e ->
	 * nameSet1.add(e.getScore())) .collect(Collectors.toList());
	 * 
	 * // System.out.println("Rosette jaroList " + jarodistinctByScore);
	 * 
	 * // rowNum2 = rowNum1++;
	 * 
	 * 
	 * Row row2 = sheet.createRow(rowNum2++);
	 * row2.createCell(0).setCellValue("Python");
	 * 
	 * k = 0; Row row2 = sheet.createRow(rowNum2++); for (int i = 0, j = 0; j <
	 * jarodistinctByScore.size() && i < strScore.size(); i++) {
	 * 
	 * if (rowNum2 == 4) { row2.createCell(i).setCellValue("jaro_winkler");
	 * row2.createCell(i + 1).setCellValue(requName); if (j <
	 * jarodistinctByScore.size() && (strScore.get(i) +
	 * "").equals(jarodistinctByScore.get(j).getScore().toString())) {
	 * row2.createCell(k + 2).setCellValue(jarodistinctByScore.get(j).getUid());
	 * row2.createCell(k +
	 * 3).setCellValue(jarodistinctByScore.get(j).getPersonName().replace(",", "") +
	 * " ;" + jarodistinctByScore.get(j).getScore()); //
	 * System.out.print(jarodistinctByScore.get(j).getUid() + " "); j++; // k = k +
	 * 2; } else if (j < jarodistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(jarodistinctByScore.get(j).getScore().toString()))) {
	 * row2.createCell(k + 2).setCellValue(""); row2.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } else {
	 * if (j < jarodistinctByScore.size() && (strScore.get(i) +
	 * "").equals(jarodistinctByScore.get(j).getScore().toString())) {
	 * row2.createCell(k + 2).setCellValue(jarodistinctByScore.get(j).getUid());
	 * row2.createCell(k +
	 * 3).setCellValue(jarodistinctByScore.get(j).getPersonName().replace(",", "") +
	 * " ;" + jarodistinctByScore.get(j).getScore()); //
	 * System.out.print(jarodistinctByScore.get(j).getUid() + " "); j++; // k = k +
	 * 2; } else if (j < jarodistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(jarodistinctByScore.get(j).getScore().toString()))) {
	 * row2.createCell(k + 2).setCellValue(""); row2.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } k = k +
	 * 2; rowNum2++; }
	 * 
	 * k = 0;
	 * 
	 * 
	 * Collections.sort(levenList, new Comparator<ResponseLevenshtein>() {
	 * 
	 * @Override public int compare(ResponseLevenshtein e1, ResponseLevenshtein e2)
	 * { return e1.getUid().compareTo(e2.getUid()); } });
	 * 
	 * 
	 * Collections.sort(levenList, new Comparator<ResponseLevenshtein>() {
	 * 
	 * @Override public int compare(ResponseLevenshtein e1, ResponseLevenshtein e2)
	 * { return e2.getScore().compareTo(e1.getScore()); } });
	 * 
	 * Set<Double> nameSet2 = new HashSet<>(); List<ResponseLevenshtein>
	 * levendistinctByScore = levenList.stream().filter(e ->
	 * nameSet2.add(e.getScore())) .collect(Collectors.toList());
	 * 
	 * // System.out.println("Rosette  levenList " + levendistinctByScore);
	 * 
	 * Row row3 = sheet.createRow(rowNum3++); for (int i = 0, j = 0; j <
	 * levendistinctByScore.size() && i < strScore.size(); i++) {
	 * 
	 * if (rowNum3 == 5) { row3.createCell(k).setCellValue("levenshtein");
	 * row3.createCell(k + 1).setCellValue(requName); if (j <
	 * levendistinctByScore.size() && (strScore.get(i) +
	 * "").equals(levendistinctByScore.get(j).getScore().toString())) {
	 * row3.createCell(k + 2).setCellValue(levendistinctByScore.get(j).getUid());
	 * row3.createCell(k +
	 * 3).setCellValue(levendistinctByScore.get(j).getPersonName().replace(",", "")
	 * + " ;" + levendistinctByScore.get(j).getScore()); //
	 * System.out.print(levendistinctByScore.get(j).getUid() + " "); j++; // k = k +
	 * 2; } else if (j < levendistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(levendistinctByScore.get(j).getScore().toString()))) {
	 * row3.createCell(k + 2).setCellValue(""); row3.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } else {
	 * if (j < levendistinctByScore.size() && (strScore.get(i) +
	 * "").equals(levendistinctByScore.get(j).getScore().toString())) {
	 * row3.createCell(k + 2).setCellValue(levendistinctByScore.get(j).getUid());
	 * row3.createCell(k +
	 * 3).setCellValue(levendistinctByScore.get(j).getPersonName().replace(",", "")
	 * + " ;" + levendistinctByScore.get(j).getScore()); //
	 * System.out.print(levendistinctByScore.get(j).getUid() + " "); j++; // k = k +
	 * 2; } else if (j < levendistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(levendistinctByScore.get(j).getScore().toString()))) {
	 * row3.createCell(k + 2).setCellValue(""); row3.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } k = k +
	 * 2; rowNum3++; }
	 * 
	 * 
	 * Collections.sort(qratioList, new Comparator<ResponseQRatio>() {
	 * 
	 * @Override public int compare(ResponseQRatio e1, ResponseQRatio e2) { return
	 * e1.getUid().compareTo(e2.getUid()); } });
	 * 
	 * 
	 * Collections.sort(qratioList, new Comparator<ResponseQRatio>() {
	 * 
	 * @Override public int compare(ResponseQRatio e1, ResponseQRatio e2) { return
	 * e2.getScore().compareTo(e1.getScore()); } });
	 * 
	 * Set<Double> nameSet3 = new HashSet<>(); List<ResponseQRatio>
	 * qratiodistinctByScore = qratioList.stream().filter(e ->
	 * nameSet3.add(e.getScore())) .collect(Collectors.toList());
	 * 
	 * // System.out.println("Rosette  qratioList " + qratiodistinctByScore);
	 * 
	 * k = 0; Row row4 = sheet.createRow(rowNum4++); for (int i = 0, j = 0; j <
	 * qratiodistinctByScore.size() && i < strScore.size(); i++) {
	 * 
	 * if (rowNum4 == 6) { row4.createCell(i).setCellValue("qratio");
	 * row4.createCell(i + 1).setCellValue(requName); if (j <
	 * qratiodistinctByScore.size() && (strScore.get(i) +
	 * "").equals(qratiodistinctByScore.get(j).getScore().toString())) {
	 * row4.createCell(k + 2).setCellValue(qratiodistinctByScore.get(j).getUid());
	 * row4.createCell(k +
	 * 3).setCellValue(qratiodistinctByScore.get(j).getPersonName().replace(",", "")
	 * + " ;" + qratiodistinctByScore.get(j).getScore()); //
	 * System.out.print(qratiodistinctByScore.get(j).getUid() + " "); j++; // k = k
	 * + 2; } else if (j < qratiodistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(qratiodistinctByScore.get(j).getScore().toString()))) {
	 * row4.createCell(k + 2).setCellValue(""); row4.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } else {
	 * if (j < qratiodistinctByScore.size() && (strScore.get(i) +
	 * "").equals(qratiodistinctByScore.get(j).getScore().toString())) {
	 * row4.createCell(k + 2).setCellValue(qratiodistinctByScore.get(j).getUid());
	 * row4.createCell(k +
	 * 3).setCellValue(qratiodistinctByScore.get(j).getPersonName().replace(",", "")
	 * + " ;" + qratiodistinctByScore.get(j).getScore()); //
	 * System.out.print(qratiodistinctByScore.get(j).getUid() + " "); // k = k + 2;
	 * j++; } else if (j < qratiodistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(qratiodistinctByScore.get(j).getScore().toString()))) {
	 * row4.createCell(k + 2).setCellValue(""); row4.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } k = k +
	 * 2; rowNum4++; }
	 * 
	 * 
	 * Collections.sort(setList, new Comparator<ResponseSetRatio>() {
	 * 
	 * @Override public int compare(ResponseSetRatio e1, ResponseSetRatio e2) {
	 * return e1.getUid().compareTo(e2.getUid()); } });
	 * 
	 * 
	 * Collections.sort(setList, new Comparator<ResponseSetRatio>() {
	 * 
	 * @Override public int compare(ResponseSetRatio e1, ResponseSetRatio e2) {
	 * return e2.getScore().compareTo(e1.getScore()); } });
	 * 
	 * Set<Double> nameSet4 = new HashSet<>(); List<ResponseSetRatio>
	 * setdistinctByScore = setList.stream().filter(e -> nameSet4.add(e.getScore()))
	 * .collect(Collectors.toList());
	 * 
	 * //System.out.println("Rosette  setList " + setdistinctByScore);
	 * 
	 * k = 0; Row row5 = sheet.createRow(rowNum5++); for (int i = 0, j = 0; j <
	 * setdistinctByScore.size() && i < strScore.size(); i++) {
	 * 
	 * if (rowNum5 == 7) { row5.createCell(i).setCellValue("set_ratio");
	 * row5.createCell(i + 1).setCellValue(requName); if (j <
	 * setdistinctByScore.size() && (strScore.get(i) +
	 * "").equals(setdistinctByScore.get(j).getScore().toString())) {
	 * row5.createCell(k + 2).setCellValue(setdistinctByScore.get(j).getUid());
	 * row5.createCell(k +
	 * 3).setCellValue(setdistinctByScore.get(j).getPersonName().replace(",", "") +
	 * " ;" + setdistinctByScore.get(j).getScore()); //
	 * System.out.print(setdistinctByScore.get(j).getUid() + " "); // k = k + 2;
	 * j++; } else if (j < setdistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(setdistinctByScore.get(j).getScore().toString()))) {
	 * row5.createCell(k + 2).setCellValue(""); row5.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } else {
	 * if (j < setdistinctByScore.size() && (strScore.get(i) +
	 * "").equals(setdistinctByScore.get(j).getScore().toString())) {
	 * row5.createCell(k + 2).setCellValue(setdistinctByScore.get(j).getUid());
	 * row5.createCell(k +
	 * 3).setCellValue(setdistinctByScore.get(j).getPersonName().replace(",", "") +
	 * " ;" + setdistinctByScore.get(j).getScore()); //
	 * System.out.print(setdistinctByScore.get(j).getUid() + " "); j++; // k = k +
	 * 2; } else if (j < setdistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(setdistinctByScore.get(j).getScore().toString()))) {
	 * row5.createCell(k + 2).setCellValue(""); row5.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } k = k +
	 * 2; rowNum5++; }
	 * 
	 * 
	 * Collections.sort(sortList, new Comparator<ResponseSortRatio>() {
	 * 
	 * @Override public int compare(ResponseSortRatio e1, ResponseSortRatio e2) {
	 * return e1.getUid().compareTo(e2.getUid()); } });
	 * 
	 * 
	 * Collections.sort(sortList, new Comparator<ResponseSortRatio>() {
	 * 
	 * @Override public int compare(ResponseSortRatio e1, ResponseSortRatio e2) {
	 * return e2.getScore().compareTo(e1.getScore()); } });
	 * 
	 * Set<Double> nameSet5 = new HashSet<>(); List<ResponseSortRatio>
	 * sortdistinctByScore = sortList.stream().filter(e ->
	 * nameSet5.add(e.getScore())) .collect(Collectors.toList());
	 * 
	 * // System.out.println("Rosette  sortList " + sortdistinctByScore);
	 * 
	 * k = 0; Row row6 = sheet.createRow(rowNum6++); for (int i = 0, j = 0; j <
	 * sortdistinctByScore.size() && i < strScore.size(); i++) {
	 * 
	 * if (rowNum6 == 8) { row6.createCell(i).setCellValue("sort_ratio");
	 * row6.createCell(i + 1).setCellValue(requName); if (j <
	 * sortdistinctByScore.size() && (strScore.get(i) +
	 * "").equals(sortdistinctByScore.get(j).getScore().toString())) {
	 * row6.createCell(k + 2).setCellValue(sortdistinctByScore.get(j).getUid());
	 * row6.createCell(k +
	 * 3).setCellValue(sortdistinctByScore.get(j).getPersonName().replace(",", "") +
	 * " ;" + sortdistinctByScore.get(j).getScore()); //
	 * System.out.print(sortdistinctByScore.get(j).getUid() + " "); j++; // k = k +
	 * 2; } else if (j < sortdistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(sortdistinctByScore.get(j).getScore().toString()))) {
	 * row6.createCell(k + 2).setCellValue(""); row6.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } else {
	 * if (j < sortdistinctByScore.size() && (strScore.get(i) +
	 * "").equals(sortdistinctByScore.get(j).getScore().toString())) {
	 * row6.createCell(k + 2).setCellValue(sortdistinctByScore.get(j).getUid());
	 * row6.createCell(k +
	 * 3).setCellValue(sortdistinctByScore.get(j).getPersonName().replace(",", "") +
	 * " ;" + sortdistinctByScore.get(j).getScore()); //
	 * System.out.print(sortdistinctByScore.get(j).getUid() + " "); j++; // k = k +
	 * 2; } else if (j < sortdistinctByScore.size() && (!(strScore.get(i) +
	 * "").equals(sortdistinctByScore.get(j).getScore().toString()))) {
	 * row6.createCell(k + 2).setCellValue(""); row6.createCell(k +
	 * 3).setCellValue(""); // System.out.print("Else  "); // k = k + 2; } } k = k +
	 * 2; rowNum6++; }
	 * 
	 * // Delete the Blank Columns from the worksheet //
	 * sheet.getCells().deleteBlankColumns(); // Resize all columns to fit the
	 * content size for (int i = 0; i < columns.length; i++) {
	 * sheet.autoSizeColumn(i); }
	 * 
	 * 
	 * 
	 * 
	 * String filePath = "D:\\Files\\WorldCheckResponseData.csv";
	 * 
	 * 
	 * // Write the output to a file FileOutputStream fileOut = null; try { fileOut
	 * = new FileOutputStream(filePath); workbook.write(fileOut); } catch
	 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * finally { fileOut.close(); try { workbook.close(); } catch (IOException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } } }
	 */
}
