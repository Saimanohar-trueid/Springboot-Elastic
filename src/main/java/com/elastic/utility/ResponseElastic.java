package com.elastic.utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseElastic {
	
	String uid;
	String name_strength;
	String fullname;
	String requestedName;
	Double score; 
	

}
