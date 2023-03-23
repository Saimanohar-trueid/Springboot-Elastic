package com.elastic.wrapper.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PosidexResponseDetails {

	 Long uid;
	 String matchedName;
	 Float score;
	//private String strengths;
	
}
