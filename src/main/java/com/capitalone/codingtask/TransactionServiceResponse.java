/**
 * 
 */
package com.capitalone.codingtask;

import java.util.List;
import com.capitalone.codingtask.beans.Transaction;

/**
 * @author MalgariV
 * @param <Transaction>
 *
 */
public class TransactionServiceResponse {

	private int responseCode;
	
	private List<Transaction> transactionList;

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public List<Transaction> getTransactionList() {
		return transactionList;
	}

	public void setTransactionList(List<Transaction> transactionList) {
		this.transactionList = transactionList;
	}
}
