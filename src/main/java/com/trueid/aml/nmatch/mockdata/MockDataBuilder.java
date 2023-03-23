package com.trueid.aml.nmatch.mockdata;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.trueid.aml.nmatch.data.PosidexData;
import com.trueid.aml.nmatch.data.PythonJaroData;
import com.trueid.aml.nmatch.data.PythonLevenData;
import com.trueid.aml.nmatch.data.PythonQratioData;
import com.trueid.aml.nmatch.data.PythonSetRatioData;
import com.trueid.aml.nmatch.data.PythonSortRationData;
import com.trueid.aml.nmatch.data.RosetteData;

public class MockDataBuilder {
    public List buildData(String fileName, Class<?> cls) throws FileNotFoundException {
        System.err.println("Rading File:"+fileName);
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        // JsonReader reader = new JsonReader(new FileReader(fileName));
        JsonReader reader = new JsonReader(streamReader);

        List<Object> list = null;
        if (cls.equals(PosidexData.class)) {
            list = Arrays.asList(new Gson().fromJson(reader, PosidexData[].class));
        } else if (cls.equals(RosetteData.class)) {
            list = Arrays.asList(new Gson().fromJson(reader, RosetteData[].class));
        } else if (cls.equals(PythonJaroData.class)) {
            list = Arrays.asList(new Gson().fromJson(reader, PythonJaroData[].class));
        } else if (cls.equals(PythonLevenData.class)) {
            list = Arrays.asList(new Gson().fromJson(reader, PythonLevenData[].class));
        } else if (cls.equals(PythonQratioData.class)) {
            list = Arrays.asList(new Gson().fromJson(reader, PythonQratioData[].class));
        } else if (cls.equals(PythonSetRatioData.class)) {
            list = Arrays.asList(new Gson().fromJson(reader, PythonSetRatioData[].class));
        } else if (cls.equals(PythonSortRationData.class)) {
            list = Arrays.asList(new Gson().fromJson(reader, PythonSortRationData[].class));
        }

        return list;
    }

    public HashMap prepareMockData() {
        HashMap dataMap = new HashMap();

        try {

            try {
                List<PosidexData> pData = (List<PosidexData>) buildData(
                        "json/posidex.json",
                        PosidexData.class);

                dataMap.put("POSIDEX", pData);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                List<RosetteData> rData = (List<RosetteData>) buildData(
                        "json/rosette.json",
                        RosetteData.class);

                dataMap.put("ROSETTE", rData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                List<PythonJaroData> rData = (List<PythonJaroData>) buildData(
                        "json/pythonJaro.json",
                        PythonJaroData.class);

                dataMap.put("JARO", rData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                List<PythonLevenData> rData = (List<PythonLevenData>) buildData(
                        "json/pythonLeven.json",
                        PythonLevenData.class);

                dataMap.put("LEVENSHTEIN", rData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                List<PythonQratioData> rData = (List<PythonQratioData>) buildData(
                        "json/pythonQratio.json",
                        PythonQratioData.class);

                dataMap.put("QRATIO", rData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                List<PythonSetRatioData> rData = (List<PythonSetRatioData>) buildData(
                        "json/pythonSetRatio.json",
                        PythonSetRatioData.class);

                dataMap.put("SETRATIO", rData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                List<PythonSortRationData> rData = (List<PythonSortRationData>) buildData(
                        "json/pythonSortRatio.json",
                        PythonSortRationData.class);

                dataMap.put("SORTRATIO", rData);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataMap;
    }
}
