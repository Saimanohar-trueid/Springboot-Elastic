package com.trueid.aml.nmatch.data;

import org.springframework.beans.factory.config.YamlProcessor.MatchCallback;
import lombok.Data;

@Data
public class PythonJaroData extends MatchedData{
    String matchedName;
    Float score;
    Long uid;
    
    public PythonJaroData(){
        super("JARO");
    }
    @Override
    public String toString() {
        return  uid+","+matchedName.replace(",", ";")+","+score;
    }

}
