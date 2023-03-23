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
public class ElasticData {
	
	/*
	 * @Field(type = FieldType.Text, name = "type") private String type;
	 */

	@Field(type = FieldType.Integer, name = "uid")
	private String uid;

	@Field(type = FieldType.Text, name = "firstname")
	private String firstname;

	@Field(type = FieldType.Text, name = "lastname")
	private String lastname;

	@Field(type = FieldType.Text, name = "fullname")
	private String fullname;

		
	@Field(type = FieldType.Text, name = "entityType")
	private String entityType;


	
}
