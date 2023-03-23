package com.trueid.aml.nmatch.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.opencsv.CSVWriter;
import com.trueid.aml.nmatch.data.MatchedData;
import com.trueid.aml.nmatch.data.PosidexData;
import com.trueid.aml.nmatch.data.PythonJaroData;
import com.trueid.aml.nmatch.data.PythonLevenData;
import com.trueid.aml.nmatch.data.PythonQratioData;
import com.trueid.aml.nmatch.data.PythonSetRatioData;
import com.trueid.aml.nmatch.data.PythonSortRationData;
import com.trueid.aml.nmatch.data.RosetteData;

import lombok.Data;

public class MatchingResultsExporter {

	public void export(HashMap dataMap,String requestedName) {

		ArrayList<DataRank> dataOrder = new ArrayList();
		HashMap<Long, ColumnData> colDataMap = new HashMap<>();

		List<RosetteData> rData = (List<RosetteData>) dataMap.get("ROSETTE");
		List<PosidexData> pData = (List<PosidexData>) dataMap.get("POSIDEX");

		if (rData != null) {
			for (RosetteData r : rData) {
				DataRank dRank = new DataRank(r.getUid(), r.getScore());
				dataOrder.add(dRank);
				ColumnData cData = new ColumnData();
				cData.rData = r;
				colDataMap.put(r.getUid(), cData);
			}
		}

		// posidex Data
		if (pData != null) {
			for (PosidexData p : pData) {
				ColumnData cData = colDataMap.get(p.getUid());
				if (cData != null) {
					cData.pData = p;
					continue;
				}

				// if uuid is not found in the map, create new column and add find its position
				cData = new ColumnData();
				cData.pData = p;
				colDataMap.put(p.getUid(), cData);
				DataRank dRank = new DataRank(p.getUid(), p.getScore());
				dataOrder.add(dRank);
			}
		}

		List<PythonJaroData> pJaroData = (List<PythonJaroData>) dataMap.get("JARO");
		if (pJaroData != null) {
			for (PythonJaroData p : pJaroData) {
				ColumnData cData = colDataMap.get(p.getUid());
				if (cData != null) {
					cData.pJaro = p;
					continue;
				}

				// if uuid is not found in the map, create new column and add find its position
				cData = new ColumnData();
				cData.pJaro = p;
				colDataMap.put(p.getUid(), cData);
				DataRank dRank = new DataRank(p.getUid(), p.getScore());
				dataOrder.add(dRank);
			}
		}

		List<PythonLevenData> pLevenData = (List<PythonLevenData>) dataMap.get("LEVENSHTEIN");
		if (pLevenData != null) {
			for (PythonLevenData p : pLevenData) {
				ColumnData cData = colDataMap.get(p.getUid());
				if (cData != null) {
					cData.pLenen = p;
					continue;
				}

				// if uuid is not found in the map, create new column and add find its position
				cData = new ColumnData();
				cData.pLenen = p;
				colDataMap.put(p.getUid(), cData);
				DataRank dRank = new DataRank(p.getUid(), p.getScore());
				dataOrder.add(dRank);
			}
		}

		List<PythonQratioData> pQrData = (List<PythonQratioData>) dataMap.get("QRATIO");
		if (pQrData != null) {
			for (PythonQratioData p : pQrData) {
				ColumnData cData = colDataMap.get(p.getUid());
				if (cData != null) {
					cData.pQr = p;
					continue;
				}

				// if uuid is not found in the map, create new column and add find its position
				cData = new ColumnData();
				cData.pQr = p;
				colDataMap.put(p.getUid(), cData);
				DataRank dRank = new DataRank(p.getUid(), p.getScore());
				dataOrder.add(dRank);
			}
		}

		List<PythonSetRatioData> pSrData = (List<PythonSetRatioData>) dataMap.get("SETRATIO");
		if (pSrData != null) {
			for (PythonSetRatioData p : pSrData) {
				ColumnData cData = colDataMap.get(p.getUid());
				if (cData != null) {
					cData.pSr = p;
					continue;
				}

				// if uuid is not found in the map, create new column and add find its position
				cData = new ColumnData();
				cData.pSr = p;
				colDataMap.put(p.getUid(), cData);
				DataRank dRank = new DataRank(p.getUid(), p.getScore());
				dataOrder.add(dRank);
			}
		}

		List<PythonSortRationData> pSorData = (List<PythonSortRationData>) dataMap.get("SORTRATIO");
		if (pSorData != null) {
			for (PythonSortRationData p : pSorData) {
				ColumnData cData = colDataMap.get(p.getUid());
				if (cData != null) {
					cData.pSor = p;
					continue;
				}

				// if uuid is not found in the map, create new column and add find its position
				cData = new ColumnData();
				cData.pSor = p;
				colDataMap.put(p.getUid(), cData);
				DataRank dRank = new DataRank(p.getUid(), p.getScore());
				dataOrder.add(dRank);
			}
		}

		Collections.sort(dataOrder, new Comparator<DataRank>() {

			@Override
			public int compare(DataRank o1, DataRank o2) {

				if (o1.getScore() - o2.getScore() > 0) {
					return -1;
				} else
					return 1;

			}

		});

		// System.err.println("colDataMap:"+colDataMap);
		System.err.println("Sort Order:" + dataOrder);

		// print the values

		String posidexRow = printRow(dataOrder, colDataMap, "POSIDEX");
		String rosetteRow = printRow(dataOrder, colDataMap, "ROSETTE");
		String jaroRow = printRow(dataOrder, colDataMap, "JARO");
		String levenRow = printRow(dataOrder, colDataMap, "LEVENSHTEIN");
		String qRationRow = printRow(dataOrder, colDataMap, "QRATIO");
		String setRatioRow = printRow(dataOrder, colDataMap, "SETRATIO");
		String sortRationRow = printRow(dataOrder, colDataMap, "SORTRATIO");
		
		requestedName = requestedName+",";
		
		posidexRow = "POSIDEX," +requestedName+ posidexRow;
		rosetteRow = "ROSETTE," +requestedName+ rosetteRow;
		jaroRow = "JARO," +requestedName+ jaroRow;
		levenRow = "LEVENSHTEIN," +requestedName+ levenRow;
		qRationRow = "QRATIO," +requestedName+ qRationRow;
		setRatioRow = "SETRATIO," +requestedName+ setRatioRow;
		sortRationRow = "SORTRATIO," +requestedName+ sortRationRow;
		
		String[] columns = { "Algorithm", " Screened Name", " matched data" };

		String[] arrayPosidex = posidexRow.split(",");
		String[] arrayRosette = rosetteRow.split(",");
		String[] arrayJaro = jaroRow.split(",");
		String[] arrayLeven = levenRow.split(",");
		String[] arrayQratio = qRationRow.split(",");
		String[] arraySet = setRatioRow.split(",");
		String[] arraySort = sortRationRow.split(",");
		
		List<String[]> listData = new ArrayList<>();
		listData.add(columns);
		listData.add(arrayPosidex);
		listData.add(arrayRosette);
		listData.add(arrayJaro);
		listData.add(arrayLeven);
		listData.add(arrayQratio);
		listData.add(arraySet);
		listData.add(arraySort);
		
		
		System.out.println("\nFinal CSV Data  ");

		try {
		storeDataIntoCSV(listData);
		}catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * System.out.println("ROSETTE," + rosetteRow); System.out.println("POSIDEX," +
		 * posidexRow); System.out.println("JARO," + jaroRow);
		 * System.out.println("LEVENSHTEIN," + levenRow); System.out.println("QRATIO," +
		 * qRationRow); System.out.println("SETRATIO," + setRatioRow);
		 * System.out.println("SORTRATIO," + sortRationRow);
		 */

	}

	private void storeDataIntoCSV(List<String[]>  listData) throws IOException {

		
		/*
		 * Calendar calendar = Calendar.getInstance(); Date today = calendar.getTime();
		 * SimpleDateFormat sdf=new SimpleDateFormat("yyy-MM-dd hh:mm");
		 * 
		 * String date = sdf.format(today);
		 */
	  //  System.out.println("Current Time: " + "D:\\AML\\WorldCheckResponse "+date+" .csv");
	    Date date = new Date();
		date.getMinutes();
		
		String filePath = "D:\\AML-WC\\WorldCheckResponse"+date.getHours()+"-"+date.getMinutes()+".csv";
		
		File file = new File(filePath);
   	 try {
   	        // create FileWriter object with file as parameter
   	        FileWriter outputfile = new FileWriter(file);
   	        
   	        try (CSVWriter writer = new CSVWriter(outputfile)) {
				writer.writeAll(listData);
			}
   	        
   	        
   	        
   	 }catch (Exception e) {
			// TODO: handle exception
		} 
		

	}

	private String printRow(List<DataRank> dataOrderList, HashMap<Long, ColumnData> dataMap, String algoName) {
		Boolean isFirst = true;
		String dataStr = "";
		for (DataRank dr : dataOrderList) {
			ColumnData cData = dataMap.get(dr.getUuid());
			Object o = cData.getData(algoName);
			if (isFirst) {
				isFirst = false;
			} else {
				dataStr += ",";
			}
			if (o != null) {
				dataStr += o;
			} else {
				dataStr += getBlankData();
			}

		}

		return dataStr;

	}

	String getBlankData() {
		return ",,";
	}

	// private List insertDataRank(ArrayList<DataRank> dataOrderList, DataRank
	// dataRank){

	// for()

	// return dataOrderList;
	// }

	@Data
	class DataRank {
		Long uuid;
		Float score;

		DataRank(Long uuid, Float score) {
			this.uuid = uuid;
			this.score = score;
		}
	}

	@Data
	class ColumnData {
		RosetteData rData;
		PosidexData pData;
		PythonJaroData pJaro;
		PythonLevenData pLenen;
		PythonQratioData pQr;
		PythonSetRatioData pSr;
		PythonSortRationData pSor;

		MatchedData getData(String type) {
			if (rData != null && type.equals(rData.getALOG_NAME()))
				return rData;
			if (pData != null && type.equals(pData.getALOG_NAME()))
				return pData;
			if (pJaro != null && type.equals(pJaro.getALOG_NAME()))
				return pJaro;
			if (pLenen != null && type.equals(pLenen.getALOG_NAME()))
				return pLenen;
			if (pQr != null && type.equals(pQr.getALOG_NAME()))
				return pQr;
			if (pSr != null && type.equals(pSr.getALOG_NAME()))
				return pSr;
			if (pSor != null && type.equals(pSor.getALOG_NAME()))
				return pSor;

			return null;
		}
	}

}
