package com.elastic.response;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.elastic.model.AliasesData;
import com.elastic.model.FirstNameData;
import com.elastic.model.FullNameData;
import com.elastic.model.LastNameData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEN {
	
	//private ResponseENMainData primary_name;
	@Field(type = FieldType.Integer, name = "uid")
	private String uid;

	@Field(type = FieldType.Text, name = "FIRST_NAME")
	private FirstNameData first_NAME;

	@Field(type = FieldType.Text, name = "LAST_NAME")
	private LastNameData last_NAME;

	@Field(type = FieldType.Text, name = "full_NAME")
	private FullNameData full_NAME;
	
	@Field(type = FieldType.Flattened, name = "aliases")
	private AliasesData aliases;
	

}
