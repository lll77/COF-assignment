/**
 * 
 */
package com.capitalone.codingtask;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.HttpClientErrorException;

import com.capitalone.codingtask.beans.Transaction;
import com.capitalone.codingtask.beans.TransactionSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import static com.capitalone.codingtask.util.TransactionBotConstants.REQUEST_PARAM_EXCLUDE_DONUTS;
import static com.capitalone.codingtask.util.TransactionBotConstants.DUNKIN_STORE;
import static com.capitalone.codingtask.util.TransactionBotConstants.KRISPY_STORE;

/**
 * @author MalgariV
 *
 */
public class TransactionService implements UserTransaction{

	static Log log = LogFactory.getLog(TransactionService.class.getName());
	public static final String DEBIT = "debit";
	public static final String CREDIT = "credit";

	public void monthlyTransactionAnalysis(Map<String,Object> requestParameters){

		//TODO set approriate error codes and add try/catch here
		TransactionSearchResponse allTransactionsSearchResponse = new TransactionSearchResponse();
		//List<Transaction> list = new ArrayList<Transaction>();
		try {
			//get all transactions from the endpoint
			allTransactionsSearchResponse =  getAllUserTransactions(requestParameters);
			if (	allTransactionsSearchResponse !=null && 
					allTransactionsSearchResponse.getError()!=null && 
					allTransactionsSearchResponse.getError().equals(SUCCESS)
					){
				//to track if a YYYY-MM has transactions already
				Set<String> distinctYearMonths =  new HashSet<String>();//size can be used later for avereage

				//track monthly debit, credit amounts
				Map<String,Map> monthlyBudgetAnalysis = new TreeMap<String,Map>();

				log.info("Total number of transactions is:"+allTransactionsSearchResponse.getTransactions().size());

				//int ignoringDebitXtnCount = 0;
				//process fetched transactions for monthly aggregates 
				for(Transaction transaction:allTransactionsSearchResponse.getTransactions()){
					Boolean excludeDonutTransactions = (Boolean)requestParameters.get(REQUEST_PARAM_EXCLUDE_DONUTS);
					boolean ignoreDebitTransaction = false;
					if (excludeDonutTransactions && 
							(transaction.getMerchant().equalsIgnoreCase((DUNKIN_STORE))||
									transaction.getMerchant().equalsIgnoreCase(KRISPY_STORE))){
						ignoreDebitTransaction = true;
						//ignoringDebitXtnCount++;
					}

					/*
					 * 	under the assumption that transaction time is consistent, pattern-wise, and not null; 
					 *  if not revisit this-  Fetch first 7 chars i.e, YYYY-MM from YYYY-MM-DDThh:MM:SS.000Z
					 */
					String transactiondateYYYMM = transaction.getTransactionTime().substring(0, Math.min(transaction.getTransactionTime().length(), 7));

					Boolean isDebitAmnt = null;
					if (Long.signum(transaction.getAmount())<0){//checking debit case
						isDebitAmnt = true;
					}

					if (distinctYearMonths.contains(transactiondateYYYMM)){
						if (isDebitAmnt!=null && isDebitAmnt){
							//TODO ignore donut transactions for debit
							if (!ignoreDebitTransaction){
								Map<String,Long> monthlyAmountAggregate = monthlyBudgetAnalysis.get(transactiondateYYYMM);
								long newTotalAmnt;
								if (monthlyAmountAggregate.get(DEBIT)!=null){
									long previousTotalDebitAmnt = monthlyAmountAggregate.get(DEBIT);
									newTotalAmnt = addTwoLongs(previousTotalDebitAmnt,transaction.getAmount());
								}else{
									newTotalAmnt = transaction.getAmount();
								}
								monthlyAmountAggregate.put(DEBIT, newTotalAmnt);
								monthlyBudgetAnalysis.put(transactiondateYYYMM, monthlyAmountAggregate);
							}
						}else{
							Map<String,Long> monthlyAmountAggregate = monthlyBudgetAnalysis.get(transactiondateYYYMM);
							long newTotalAmnt;
							if (monthlyAmountAggregate.get(CREDIT)!=null){
								long previousTotalCreditAmnt = monthlyAmountAggregate.get(CREDIT);
								newTotalAmnt = addTwoLongs(previousTotalCreditAmnt,transaction.getAmount());
							}else{
								newTotalAmnt = transaction.getAmount();
							}
							monthlyAmountAggregate.put(CREDIT, newTotalAmnt);
							monthlyBudgetAnalysis.put(transactiondateYYYMM, monthlyAmountAggregate);
						}
					}else{
						distinctYearMonths.add(transactiondateYYYMM);
						Map<String,Long> monthTransactions =  new HashMap<String,Long>();
						if (isDebitAmnt!=null && isDebitAmnt){
							//TODO ignore donut transactions for debit
							if (!ignoreDebitTransaction){
								monthTransactions.put(DEBIT, transaction.getAmount());
							}
						}else{
							monthTransactions.put(CREDIT, transaction.getAmount());
						}
						monthlyBudgetAnalysis.put(transactiondateYYYMM, monthTransactions);
					}
				}

				BigInteger sumDebit =  BigInteger.ZERO;
				BigInteger sumCredit = BigInteger.ZERO;
				DecimalFormat f = new DecimalFormat("##.00");

				System.out.println("-------------------------");
				System.out.println("YYYY-MM|  Spent |Income");
				System.out.println("-------------------------");
				for (Map.Entry<String,Map> entry : monthlyBudgetAnalysis.entrySet()) {
					entry.getKey();//YYYY-MM
					entry.getValue();//DEBIT;CREDIT aggregates
					Map<String,Long> monthlyDebitCreditTotals = entry.getValue();
					Long monthlyDebitTotal = monthlyDebitCreditTotals.get(DEBIT);
					sumDebit = sumDebit.add(BigInteger.valueOf(Math.abs(monthlyDebitTotal)));//summing up debt. for calc. avg.
					Long monthlyCreditTotal = monthlyDebitCreditTotals.get(CREDIT);
					sumCredit = sumCredit.add(BigInteger.valueOf(monthlyCreditTotal));//summing up credit. for calc. avg.
					double monthlyDebitTotalDecimalValue = ((double)Math.abs(monthlyDebitTotal)/10000L);
					double monthlyCreditTotalDecimalValue = ((double)monthlyCreditTotal/10000L);

					System.out.println(entry.getKey()+"|$"+f.format(monthlyDebitTotalDecimalValue)+"|$"+f.format(monthlyCreditTotalDecimalValue));
				}
				double averageMonthlyDebt = ((double)(sumDebit.longValue())/(new Long(monthlyBudgetAnalysis.size())*10000L));
				double averageMonthlyIncome = ((double)(sumCredit.longValue())/(new Long(monthlyBudgetAnalysis.size())*10000L));
				System.out.println("-------------------------");
				System.out.println("Average:$"+f.format(averageMonthlyDebt)+"|$"+f.format(averageMonthlyIncome));
				if ((boolean) requestParameters.get(REQUEST_PARAM_EXCLUDE_DONUTS)){
					System.out.println("Note: Donuts not included in spent amounts.");
				}
				System.out.println("-------------------------");
				//System.out.println("Total Number of debit transactions ignored:"+ignoringDebitXtnCount);
			}else{
				System.out.println("There was an error getting all transactions.");
			}
		} catch (HttpClientErrorException e) {
			log.error("HttpClientErrorException while calling getAllTransactions endpoint.", e);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in getAllTransactions.", e);
			e.printStackTrace();
		}
	}

	private static long addTwoLongs(long value1, long value2){
		BigInteger result = BigInteger.valueOf(value1).add(BigInteger.valueOf(value2));
		return result.longValue();
	}
}