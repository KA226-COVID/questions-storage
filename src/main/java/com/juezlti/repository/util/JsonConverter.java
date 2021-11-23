package com.juezlti.repository.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {
	
	public static JSONObject failConverter(HttpStatus code, String fail, Object objeto) {

		ObjectMapper objectMapper = new ObjectMapper();
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("code", code.toString());
			jsonObject.put("fail", fail);
			jsonObject.put("question", objectMapper.writeValueAsString(objeto));
			System.out.println(jsonObject);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject;
	}

	public static JSONObject okConverter(HttpStatus code, Object objeto) {

		ObjectMapper objectMapper = new ObjectMapper();
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("code", code.toString());
			jsonObject.put("question", objectMapper.writeValueAsString(objeto));

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject;
	}

}
