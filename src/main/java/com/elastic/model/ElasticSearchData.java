package com.elastic.model;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "rni-wc-data-en")
public class ElasticSearchData {
	
	@Field(type = FieldType.Integer, name = "uid")
	private String uid;

	@Field(type = FieldType.Text, name = "FIRST_NAME")
	private FirstNameData FIRST_NAME;

	@Field(type = FieldType.Text, name = "LAST_NAME")
	private LastNameData LAST_NAME;

	@Field(type = FieldType.Text, name = "FULL_NAME")
	private FullNameData FULL_NAME;
	
	@Field(type = FieldType.Flattened, name = "ALIASES")
	private AliasesData ALIASES;		


}
