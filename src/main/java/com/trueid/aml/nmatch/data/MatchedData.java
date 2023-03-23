package com.trueid.aml.nmatch.data;

import lombok.Data;

@Data
public class MatchedData {
    public String ALOG_NAME;
    public MatchedData(String algoName){
        this.ALOG_NAME = algoName;
    }
}
