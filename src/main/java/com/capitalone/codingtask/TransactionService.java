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

import com.capitalone.codingtask.beans.DailyTransaction;
import com.capitalone.codingtask.beans.Transaction;
import com.capitalone.codingtask.beans.TransactionSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import static com.capitalone.codingtask.util.TransactionBotConstants.REQUEST_PARAM_EXCLUDE_DONUTS;
import static com.capitalone.codingtask.util.TransactionBotConstants.DUNKIN_STORE;
import static com.capitalone.codingtask.util.TransactionBotConstants.KRISPY_STORE;
import static com.capitalone.codingtask.util.TransactionBotConstants.YEAR;
import static com.capitalone.codingtask.util.TransactionBotConstants.MONTH;


/**
 * @author MalgariV
 *
 */
public class TransactionService implements UserTransaction{

	static Log log = LogFactory.getLog(TransactionService.class.getName());
	public static final String DEBIT = "debit";
	public static final String CREDIT = "credit";
	public static final String IGNORE_PAYOFF = "IGNORE_PAYOFF";
	public static final String IGNORE_DONUTS = "IGNORE_DONUTS";
	public static final String DEFAULT = "DEFAULT";
	public static final String CRYSTAL_BALL = "CRYSTAL_BALL";
	

	/*
	 *  This method displays how much money the user spends 
	 *  and makes in each of the months that have data, and averages for number of months.
	 *  Based on requestParameter, if ignore donuts option is set, this method also ignores
	 *  those transaction amounts from monthly debits.
	 */

	@SuppressWarnings({ "rawtypes" })
	public void monthlyTransactionAnalysis(Map<String,Object> requestParameters){

		//set approriate error codes and add try/catch here
		TransactionSearchResponse allTransactionsSearchResponse = new TransactionSearchResponse();
		Map<String,Map> monthlyBudgetAnalysis = null;
		//List<Transaction> list = new ArrayList<Transaction>();
		try {
			//get all transactions from the endpoint
			allTransactionsSearchResponse =  getAllUserTransactions(requestParameters);
			if ((allTransactionsSearchResponse ==null)||allTransactionsSearchResponse !=null && 
					allTransactionsSearchResponse.getError()!=null && 
					(!allTransactionsSearchResponse.getError().equals(SUCCESS))
					){
				System.out.println("There was an error getting all transactions.");
			}else{
				monthlyBudgetAnalysis = monthlyAggregates(allTransactionsSearchResponse,requestParameters);
				Boolean ignoreDonuts = (Boolean) requestParameters.get(REQUEST_PARAM_EXCLUDE_DONUTS);
				if (ignoreDonuts !=null && ignoreDonuts){
					displayReport(monthlyBudgetAnalysis,IGNORE_DONUTS);
				}else{
					displayReport(monthlyBudgetAnalysis, DEFAULT);
				}
			}
		} catch (HttpClientErrorException e) {
			log.error("HttpClientErrorException while calling getAllTransactions endpoint.", e);
			//e.printStackTrace();
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in getAllTransactions.", e);
			//e.printStackTrace();
		}catch (Exception e) {
			log.error("Exception in getAllTransactions.", e);
		}
	}

	/**
	 * This method disregards credit card, debit card payments in monthly aggregates for spent and income.
	 * 
	 * @param requestParameters
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void ignoreCreditCardPayments(Map<String,Object> requestParameters){

		TransactionSearchResponse allTransactionsSearchResponse = new TransactionSearchResponse();
		Map<String,DailyTransaction> datedTransactions =  new HashMap<String,DailyTransaction>();

		int numberOfCreditCardPaymentsIgnored = 0;
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

				//process all transactions and collect daily debt, credit for available data.
				for(Transaction transaction:allTransactionsSearchResponse.getTransactions()){

					String transactiondateYYYYMMDD = transaction.getTransactionTime().substring(0, Math.min(transaction.getTransactionTime().length(), 10));
					DailyTransaction dailyTransaction = datedTransactions.get(transactiondateYYYYMMDD);
					List<Long> dailyCreditAmounts = null;
					List<Long> dailyDebitAmounts = null;

					if (dailyTransaction ==  null){
						dailyTransaction =  new DailyTransaction();
						dailyCreditAmounts = new ArrayList<Long>();
						dailyDebitAmounts = new ArrayList<Long>();
					}else{
						dailyCreditAmounts= dailyTransaction.getCreditAmounts();
						dailyDebitAmounts = dailyTransaction.getDebitAmounts();
					}

					if (Long.signum(transaction.getAmount())<0){
						//debit amount
						dailyDebitAmounts.add(Math.abs(transaction.getAmount()));
					}else{
						//credit amount
						dailyCreditAmounts.add(Math.abs(transaction.getAmount()));
					}
					dailyTransaction.setCreditAmounts(dailyCreditAmounts);
					dailyTransaction.setDebitAmounts(dailyDebitAmounts);
					dailyTransaction.setDate(transactiondateYYYYMMDD);
					datedTransactions.put(transactiondateYYYYMMDD, dailyTransaction);
				}//end of for loop that collects daily transaction.

				//process fetched transactions for monthly aggregates 
				for(Transaction transaction:allTransactionsSearchResponse.getTransactions()){
					String transactiondateYYYYMMDD = transaction.getTransactionTime().substring(0, Math.min(transaction.getTransactionTime().length(), 10));
					DailyTransaction dailyTransaction = datedTransactions.get(transactiondateYYYYMMDD);

					//ignores transactions that show up as credit and debit with same amounts (-ve for debit) in a day
					if (dailyTransaction!=null){
						if (dailyTransaction.getDebitAmounts()!=null && dailyTransaction.getDebitAmounts()!=null){
							Boolean inDebitList = dailyTransaction.getDebitAmounts().contains(Math.abs(transaction.getAmount()));
							Boolean inCreditList = dailyTransaction.getCreditAmounts().contains(Math.abs(transaction.getAmount()));
							if (inDebitList && inCreditList){
								numberOfCreditCardPaymentsIgnored++;
								continue;
							}
						}
					}
					String transactiondateYYYMM = transaction.getTransactionTime().substring(0, Math.min(transaction.getTransactionTime().length(), 7));

					Boolean isDebitAmnt = null;
					if (Long.signum(transaction.getAmount())<0){//checking debit case
						isDebitAmnt = true;
					}

					if (distinctYearMonths.contains(transactiondateYYYMM)){
						if (isDebitAmnt!=null && isDebitAmnt){
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
							monthTransactions.put(DEBIT, transaction.getAmount());
						}else{
							monthTransactions.put(CREDIT, transaction.getAmount());
						}
						monthlyBudgetAnalysis.put(transactiondateYYYMM, monthTransactions);
					}
				}//end of endpoint response processing

				log.info("Number of credit, credit transactions ignored:"+numberOfCreditCardPaymentsIgnored);
				displayReport(monthlyBudgetAnalysis,IGNORE_PAYOFF);
			}else{
				System.out.println("There was an error getting all transactions.");
			}
		} catch (HttpClientErrorException e) {
			log.error("HttpClientErrorException while calling ignoreCreditCardPayments endpoint.", e);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in ignoreCreditCardPayments.", e);
		}catch (Exception e) {
			log.error("Exception in ignoreCreditCardPayments.", e);
		}
	}

	@SuppressWarnings("rawtypes")
	public void crystalBallReport(Map<String,Object> requestParameters){

		//TODO get all transactions first
		TransactionSearchResponse allTransactionsSearchResponse = new TransactionSearchResponse();
		Map<String,Map> monthlyBudgetAnalysis = null;
		Map<String,Map> projectedMonthlyBudgetAnalysis = null;

		//collect months that have projections, for summing up with all transactions data.
		List<Map<String,Map>> monthsWithProjectionData = new ArrayList<Map<String,Map>>();

		//List<Transaction> list = new ArrayList<Transaction>();
		try {
			//get all transactions from the endpoint
			allTransactionsSearchResponse =  getAllUserTransactions(requestParameters);
			if ((allTransactionsSearchResponse ==null)||allTransactionsSearchResponse !=null && 
					allTransactionsSearchResponse.getError()!=null && 
					(!allTransactionsSearchResponse.getError().equals(SUCCESS))
					){
				//for logging purpose
				log.error("There was an error getting crystalBallReport-all transactions.");
				//for user
				System.out.println("There was an error getting crystalBallReport-all transactions.");
			}else{
				monthlyBudgetAnalysis = monthlyAggregates(allTransactionsSearchResponse,requestParameters);

				for (Map.Entry<String,Map> entry : monthlyBudgetAnalysis.entrySet()) {
					Map<String,Long> monthlyDebitCreditTotals = entry.getValue();//TODO use this for summation
					String YYYY_MM = entry.getKey();
					if (YYYY_MM.contains("-")){
						String[] tokens = YYYY_MM.split("-");
						if (tokens[0] !=null){
							requestParameters.put(YEAR,Long.valueOf(tokens[0]));
							//log.info("year:"+tokens[0]);
						}
						if (tokens[1] !=null){
							requestParameters.put(MONTH,Long.valueOf(tokens[1]));
							//log.info("month:"+tokens[1]);
						}
						//projectedmonth response
						TransactionSearchResponse projectedMonthResponse =getProjectedTransactionsForMonth(requestParameters);
						if ((projectedMonthResponse ==null)||projectedMonthResponse !=null && 
								projectedMonthResponse.getError()!=null && 
								(!projectedMonthResponse.getError().equals(SUCCESS))
								){
							//for logging purpose
							log.error("There was an error getting crystalBallReport-projectedMonthlyTransactions.");
							//for user
							System.out.println("There was an error getting crystalBallReport-projectedMonthlyTransactions.");
						}else{
							if (projectedMonthResponse.getTransactions()!=null && projectedMonthResponse.getTransactions().size()>0){
								//where there is data for projectedMonthlyEstimates
								projectedMonthlyBudgetAnalysis = monthlyAggregates(projectedMonthResponse,requestParameters);
								monthsWithProjectionData.add(projectedMonthlyBudgetAnalysis);
							}
						}
					}
				}//end of for loop

				//merge projected data with all transaction data
				log.info("Number of months that have monthlyProjection data:"+monthsWithProjectionData.size());
				for (Map<String,Map> projMonthData: monthsWithProjectionData){
					for (Map.Entry<String,Map> entry : projMonthData.entrySet()) {
						String YYYY_MM = entry.getKey();//YYYY-MM
						
						Map<String,Long> monthlyDebitCreditTotals = entry.getValue();
						if (monthlyDebitCreditTotals.get(DEBIT)!=null){
							Long monthlyDebitTotal = monthlyDebitCreditTotals.get(DEBIT);
							monthlyBudgetAnalysis=addAmounts(monthlyBudgetAnalysis,DEBIT,YYYY_MM,monthlyDebitTotal);
						}
						if (monthlyDebitCreditTotals.get(DEBIT)!=null){
							Long monthlyCreditTotal = monthlyDebitCreditTotals.get(CREDIT);
							monthlyBudgetAnalysis=addAmounts(monthlyBudgetAnalysis,CREDIT,YYYY_MM,monthlyCreditTotal);
						}
					}
				}
				//display crystall-ball report
				displayReport(monthlyBudgetAnalysis,CRYSTAL_BALL);

			}
		} catch (HttpClientErrorException e) {
			log.error("HttpClientErrorException while calling crystalBallReport.", e);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in crystalBallReport.", e);
		}catch (Exception e) {
			log.error("Exception in crystalBallReport-getAllTransactions.", e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String,Map> monthlyAggregates(TransactionSearchResponse allTransactionsSearchResponse,Map<String,Object> requestParameters){
		//track monthly debit, credit amounts
		Map<String,Map> monthlyBudgetAnalysis = new TreeMap<String,Map>();

		if (	allTransactionsSearchResponse !=null && 
				allTransactionsSearchResponse.getError()!=null && 
				allTransactionsSearchResponse.getError().equals(SUCCESS)
				){
			//to track if a YYYY-MM has transactions already
			Set<String> distinctYearMonths =  new HashSet<String>();//size can be used later for avereage

			//process fetched transactions for monthly aggregates 
			for(Transaction transaction:allTransactionsSearchResponse.getTransactions()){
				Boolean excludeDonutTransactions = (Boolean)requestParameters.get(REQUEST_PARAM_EXCLUDE_DONUTS);
				boolean ignoreDebitTransaction = false;
				if (excludeDonutTransactions!=null && excludeDonutTransactions && 
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
						//ignore donut transactions for debit
						if (!ignoreDebitTransaction){
							monthlyBudgetAnalysis=addAmounts(monthlyBudgetAnalysis,DEBIT,transactiondateYYYMM,transaction.getAmount());
						}
					}else{
						monthlyBudgetAnalysis=addAmounts(monthlyBudgetAnalysis,CREDIT,transactiondateYYYMM,transaction.getAmount());
					}
				}else{
					distinctYearMonths.add(transactiondateYYYMM);
					Map<String,Long> monthTransactions =  new HashMap<String,Long>();
					if (isDebitAmnt!=null && isDebitAmnt){
						//ignore donut transactions for debit
						if (!ignoreDebitTransaction){
							monthTransactions.put(DEBIT, transaction.getAmount());
						}
					}else{
						monthTransactions.put(CREDIT, transaction.getAmount());
					}
					monthlyBudgetAnalysis.put(transactiondateYYYMM, monthTransactions);
				}
			}
		}
		return monthlyBudgetAnalysis;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void displayReport(Map<String,Map> monthlyBudgetAnalysis, String reportType){
		BigInteger sumDebit =  BigInteger.ZERO;
		BigInteger sumCredit = BigInteger.ZERO;
		DecimalFormat f = new DecimalFormat("##.00");

		System.out.println("-------------------------");
		System.out.println("YYYY-MM|  Spent |Income");
		System.out.println("-------------------------");
		for (Map.Entry<String,Map> entry : monthlyBudgetAnalysis.entrySet()) {
			Map<String,Long> monthlyDebitCreditTotals = entry.getValue();
			Long monthlyDebitTotal = monthlyDebitCreditTotals.get(DEBIT);

			//summing up debit for calc. avg.
			sumDebit = sumDebit.add(BigInteger.valueOf(Math.abs(monthlyDebitTotal)));
			Long monthlyCreditTotal = monthlyDebitCreditTotals.get(CREDIT);

			//summing up credit for calc. avg.
			sumCredit = sumCredit.add(BigInteger.valueOf(monthlyCreditTotal));//summing up credit. for calc. avg.
			double monthlyDebitTotalDecimalValue = ((double)Math.abs(monthlyDebitTotal)/10000L);
			double monthlyCreditTotalDecimalValue = ((double)monthlyCreditTotal/10000L);

			System.out.println(entry.getKey()+"|$"+f.format(monthlyDebitTotalDecimalValue)+"|$"+f.format(monthlyCreditTotalDecimalValue));
		}
		//preserving digits for averages...
		double averageMonthlyDebt = ((double)(sumDebit.longValue())/(new Long(monthlyBudgetAnalysis.size())*10000L));
		double averageMonthlyIncome = ((double)(sumCredit.longValue())/(new Long(monthlyBudgetAnalysis.size())*10000L));
		System.out.println("-------------------------");
		System.out.println("Average:$"+f.format(averageMonthlyDebt)+"|$"+f.format(averageMonthlyIncome));

		if (reportType.equals(IGNORE_PAYOFF)){
			System.out.println("Note: Payoff transactions are not included.");
		}else if (reportType.equals(IGNORE_DONUTS)){
			System.out.println("Note: Donuts not included in spent amounts.");
		}else if (reportType.equals(CRYSTAL_BALL)){
			System.out.println("Note: These are predicted spending and income amounts.");
		}

		System.out.println("-------------------------");

	}

	private static Map<String,Map> addAmounts(Map<String,Map>monthlyBudgetAnalysis, String type, 
			String transactiondateYYYYMM, Long amount){
		Map<String,Long> monthlyAmountAggregate = monthlyBudgetAnalysis.get(transactiondateYYYYMM);
		long newTotalAmnt;
		if (monthlyAmountAggregate.get(type)!=null){
			long previousTotalCreditAmnt = monthlyAmountAggregate.get(type);
			newTotalAmnt = addTwoLongs(previousTotalCreditAmnt,amount);
		}else{
			newTotalAmnt = amount;
		}
		monthlyAmountAggregate.put(type, newTotalAmnt);
		monthlyBudgetAnalysis.put(transactiondateYYYYMM, monthlyAmountAggregate);
		return monthlyBudgetAnalysis;
	}

	private static long addTwoLongs(long value1, long value2){
		BigInteger result = BigInteger.valueOf(value1).add(BigInteger.valueOf(value2));
		return result.longValue();
	}
}