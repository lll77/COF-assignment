package com.capitalone.codingtask;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

//static import of constants class
import static com.capitalone.codingtask.util.TransactionBotConstants.*;

import java.util.HashMap;
import java.util.Map;

/**
 * An spring boot application that is developed as part of CapitalOne coding task.
 *
 * @author MalgariV
 * 
 */
@SpringBootApplication
public class TransactionBot implements UserTransaction
{
	//for logging purpose
	static Log log = LogFactory.getLog(TransactionBot.class.getName());
	
	//Spring boot application requires a main starting point.
    public static void main( String[] args )
    {
        log.info("TransactionBot-main method entered");
        ApplicationContext ctx = SpringApplication.run(TransactionBot.class, args);
        
        //loading properties & validating them
        String allTransactionsEndpoint = ctx.getEnvironment().getProperty(ALL_TRANSACTIONS_ENDPOINT);
        Validate.notNull(allTransactionsEndpoint, ALL_TRANSACTIONS_ENDPOINT_VALIDATION_MSG);
        
        String authToken = ctx.getEnvironment().getProperty(AUTH_TOKEN);
        Validate.notNull(authToken, AUTH_TOKEN_VALIDATION_MSG);
        
        String userId = ctx.getEnvironment().getProperty(USER_ID);
        Validate.notNull(userId, USER_ID_VALIDATION_MSG);
        
        String apiToken = ctx.getEnvironment().getProperty(API_TOKEN_VALUE);
        Validate.notNull(apiToken, API_TOKEN_VALIDATION_MSG);
        
    	Map<String,Object> requestParameters = new HashMap<String,Object>();
    	requestParameters.put(REQUEST_PARAM_ENDPOINT, allTransactionsEndpoint);
    	requestParameters.put(REQUEST_PARAM_AUTHTOKEN, authToken);
    	requestParameters.put(REQUEST_PARAM_USERID, Integer.parseInt(userId));
    	requestParameters.put(REQUEST_PARAM_APITOKEN, apiToken);
        
    	TransactionService transactionService = new TransactionService();
        //TODO default case with no args and handle args for other to-have features, implement atleast one
        if (args.length == 0){
        	//default case get all transactions
        	log.info("no runtime arguments passed, fetching alltransactions");
        	
        	requestParameters.put(REQUEST_PARAM_EXCLUDE_DONUTS,false);//default case include donuts in debit
        	transactionService.monthlyTransactionAnalysis(requestParameters);
        }else if (args.length > 1){
        	//based on the requirements there would be a (one) flag or no flag.
        	log.error("Invalid number of arguments.  Expecting zero or one argument.");
        	System.out.println("Invalid number of arguments.  Expecting zero or one argument.");
        }else if (args.length == 1){
        	//implement features, atleast one here as per requirement.
        	String inputArgument = args[0];
        	if (inputArgument !=null && inputArgument.trim().length()>0 && inputArgument.equals("--ignore-donuts")){
        		log.info("ignore donuts...");
        		requestParameters.put(REQUEST_PARAM_EXCLUDE_DONUTS,true);//default case include donuts in debit
        		transactionService.monthlyTransactionAnalysis(requestParameters);
        	}
        	
        }
        log.info("<end-of-program>");
    }
}