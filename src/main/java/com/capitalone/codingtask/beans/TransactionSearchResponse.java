/**
 * 
 */
package com.capitalone.codingtask.beans;

import java.util.List;

/**
 * @author MalgariV
 *
 */
public class TransactionSearchResponse {
	
	private List<Transaction> transactions;
	
	private String error;

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
