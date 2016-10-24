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
 * Interface for endpoint operations. Using default method implementations for this specific coding-task scenario.
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

	/**
	 *   Loads a user's transactions from the GetAllTransactions endpoint.
	 *   @param  requestParameters
	 */

	default TransactionSearchResponse getAllUserTransactions(Map<String,Object> requestParameters)
			throws JsonProcessingException, HttpClientErrorException {

		HttpEntity<String> entity = getEntity(requestParameters);
		TransactionSearchResponse transactionSearchResponse = new TransactionSearchResponse();
		String endPoint = (String) requestParameters.get(REQUEST_PARAM_ENDPOINT);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

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

	default TransactionSearchResponse getProjectedTransactionsForMonth(Map<String,Object> requestParameters)
			throws JsonProcessingException, HttpClientErrorException {

		TransactionSearchResponse transactionSearchResponse = new TransactionSearchResponse();

		String endPoint = (String) requestParameters.get(REQUEST_PARAM_MON_PROJ_ENDPOINT);

		HttpEntity<String> entity = getEntity(requestParameters);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		//log.info("making monthly projection call for crystal report...");
		ResponseEntity<TransactionSearchResponse> response = restTemplate.postForEntity(endPoint, entity, TransactionSearchResponse.class);

		if (response!=null && response.getBody()!=null){
			//log.info("Response received from GetProjectedTransactionsForMonth endpoint.");
			
			if (!response.getBody().getError().equals(SUCCESS)){
				log.error("Error response received from GetProjectedTransactionsForMonth endpoint."+response.getBody().getError());;
			}
			transactionSearchResponse.setError(response.getBody().getError());
			transactionSearchResponse.setTransactions(response.getBody().getTransactions());
		}else{
			transactionSearchResponse.setError(NULL_RESPONSE);
			log.error("Null response received from GetProjectedTransactionsForMonth endpoint.");
		}
		return transactionSearchResponse;
	}

	default HttpEntity<String> getEntity(Map<String,Object> requestParameters) throws JsonProcessingException{

		Map<String, Object> argsMap = new HashMap<String, Object>();
		String authToken = (String) requestParameters.get(REQUEST_PARAM_AUTHTOKEN);
		Integer userId = (Integer) requestParameters.get(REQUEST_PARAM_USERID);
		String apiToken = (String) requestParameters.get(REQUEST_PARAM_APITOKEN) ;

		HttpHeaders headers = new HttpHeaders();
		headers.add(ACCEPT,APPLICATION_JSON_HEADER);
		headers.add(CONTENT_TYPE,APPLICATION_JSON_HEADER);

		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put(UID, userId);
		dataMap.put(TOKEN, authToken);
		dataMap.put(API_TOKEN, apiToken);
		dataMap.put(JSON_STRICT_MODE, false);
		dataMap.put(JSON_VERBOSE_RESPONSE, false);

		if ((requestParameters.get(YEAR)!=null) && (requestParameters.get(MONTH)!=null)){
			//crystalball option
			Long year = (Long) requestParameters.get(YEAR);
			Long month = (Long) requestParameters.get(MONTH);
			argsMap.put(YEAR,year);
			argsMap.put(MONTH, month);
		}

		argsMap.put(ARGS, dataMap);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(argsMap);
		//System.out.println("request is:"+json);
		HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		return entity;
	}
}