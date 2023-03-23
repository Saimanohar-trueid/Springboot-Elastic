package com.elastic.wrapper.model;

import java.util.LinkedList;
import java.util.List;

import com.elastic.utility.ResponseElastic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoadListDataIntoCSV {

	private List<PosidexResponseDetails> posidexDetails;
	private List<ResponseElastic> rosetteDetails;
	private List<ResponseElastic> rosetteDetailsAlias;
	private List<ResponseTargetNames> pythonDetails;
//	private PythonResponseModel model;
}
