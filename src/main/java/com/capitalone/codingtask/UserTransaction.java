/**
 * 
 */
package com.capitalone.codingtask;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import com.capitalone.codingtask.beans.TransactionSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static com.capitalone.codingtask.util.TransactionBotConstants.*;


/**
 * Interface for all (2 i.e.,) user transaction get operations. Using default
 * implementations for this specific coding-task scenario.
 * 
 * @author MalgariV
 *
 */
public interface UserTransaction {
	
	static Log log = LogFactory.getLog(UserTransaction.class.getName());
	String SUCCESS = "no-error";
	String NULL_RESPONSE = "Null Response";
	String APPLICATION_JSON_HEADER = "application/json";
	String ACCEPT = "Accept";
	String CONTENT_TYPE = "Content-Type";
	String UID = "uid";
	String TOKEN = "token";
	String API_TOKEN = "api-token";
	String JSON_STRICT_MODE = "json-strict-mode";
	String JSON_VERBOSE_RESPONSE = "json-verbose-response";
	String ARGS = "args";
	
	/*
	 * 
	 */

	default TransactionSearchResponse getAllUserTransactions(Map<String,Object> requestParameters)
			throws JsonProcessingException, HttpClientErrorException {

		String endPoint = (String) requestParameters.get(REQUEST_PARAM_ENDPOINT);
		String authToken = (String) requestParameters.get(REQUEST_PARAM_AUTHTOKEN);
		Integer userId = (Integer) requestParameters.get(REQUEST_PARAM_USERID);
		String apiToken = (String) requestParameters.get(REQUEST_PARAM_APITOKEN) ;
		Boolean ignoreDonuts = (Boolean)requestParameters.get(REQUEST_PARAM_EXCLUDE_DONUTS);

		TransactionSearchResponse transactionSearchResponse = new TransactionSearchResponse();

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		HttpHeaders headers = new HttpHeaders();
		headers.add(ACCEPT,APPLICATION_JSON_HEADER);
		headers.add(CONTENT_TYPE,APPLICATION_JSON_HEADER);

		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put(UID, userId);
		dataMap.put(TOKEN, authToken);
		dataMap.put(API_TOKEN, apiToken);
		dataMap.put(JSON_STRICT_MODE, false);
		dataMap.put(JSON_VERBOSE_RESPONSE, false);

		Map<String, Object> argsMap = new HashMap<String, Object>();
		argsMap.put(ARGS, dataMap);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(argsMap);

		HttpEntity<String> entity = new HttpEntity<String>(json, headers);
		ResponseEntity<TransactionSearchResponse> response = restTemplate.postForEntity(endPoint, entity, TransactionSearchResponse.class);
	
		if (response!=null && response.getBody()!=null){
			log.info("Response received from getAllTransactions endpoint.");
			if (!response.getBody().getError().equals(SUCCESS)){
				//TODO verify if this captures right error message
				log.info("Error response received from getAllTransactions endpoint."+response.getBody().getError());;
			}
			transactionSearchResponse.setError(response.getBody().getError());
			transactionSearchResponse.setTransactions(response.getBody().getTransactions());
		}else{
			transactionSearchResponse.setError(NULL_RESPONSE);
			log.error("Null response received from getAllTransactions endpoint.");
		}
		return transactionSearchResponse;
	}
}