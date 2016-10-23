/**
 * 
 */
package com.capitalone.codingtask.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author MalgariV
 *
 */
public class Transaction {

	//aggregation-time is listed on the eval func; however, the response documentation is,perhaps, missing.
	
	private String transactionId;
	private String accountId;
	private String rawmerchant;
	private String merchant;
	private boolean isPending; 
	private String transactionTime;
	//Negative amount = debit, positive amount = credit. Centocents. 20000 centocents = $2.
	private long amount;
	private String previousTransactionId;	
	private String categorization;
	private long clearDate;
	
	@JsonProperty("transaction-id")
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	@JsonProperty("account-id")
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	@JsonProperty("raw-merchant")
	public String getRawmerchant() {
		return rawmerchant;
	}
	public void setRawmerchant(String rawmerchant) {
		this.rawmerchant = rawmerchant;
	}
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	
	@JsonProperty("is-pending")
	public boolean isPending() {
		return isPending;
	}
	public void setPending(boolean isPending) {
		this.isPending = isPending;
	}

	@JsonProperty("transaction-time")
	public String getTransactionTime() {
		return transactionTime;
	}
	public void setTransactionTime(String transactionTime) {
		this.transactionTime = transactionTime;
	}
	
	public String getPreviousTransactionId() {
		return previousTransactionId;
	}
	public void setPreviousTransactionId(String previousTransactionId) {
		this.previousTransactionId = previousTransactionId;
	}
	public String getCategorization() {
		return categorization;
	}
	public void setCategorization(String categorization) {
		this.categorization = categorization;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	@JsonProperty("clear-date")
	public long getClearDate() {
		return clearDate;
	}
	public void setClearDate(long clearDate) {
		this.clearDate = clearDate;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
		result = prime * result + (int) (amount ^ (amount >>> 32));
		result = prime * result + ((categorization == null) ? 0 : categorization.hashCode());
		result = prime * result + (int) (clearDate ^ (clearDate >>> 32));
		result = prime * result + (isPending ? 1231 : 1237);
		result = prime * result + ((merchant == null) ? 0 : merchant.hashCode());
		result = prime * result + ((previousTransactionId == null) ? 0 : previousTransactionId.hashCode());
		result = prime * result + ((rawmerchant == null) ? 0 : rawmerchant.hashCode());
		result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
		result = prime * result + ((transactionTime == null) ? 0 : transactionTime.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		if (amount != other.amount)
			return false;
		if (categorization == null) {
			if (other.categorization != null)
				return false;
		} else if (!categorization.equals(other.categorization))
			return false;
		if (clearDate != other.clearDate)
			return false;
		if (isPending != other.isPending)
			return false;
		if (merchant == null) {
			if (other.merchant != null)
				return false;
		} else if (!merchant.equals(other.merchant))
			return false;
		if (previousTransactionId == null) {
			if (other.previousTransactionId != null)
				return false;
		} else if (!previousTransactionId.equals(other.previousTransactionId))
			return false;
		if (rawmerchant == null) {
			if (other.rawmerchant != null)
				return false;
		} else if (!rawmerchant.equals(other.rawmerchant))
			return false;
		if (transactionId == null) {
			if (other.transactionId != null)
				return false;
		} else if (!transactionId.equals(other.transactionId))
			return false;
		if (transactionTime == null) {
			if (other.transactionTime != null)
				return false;
		} else if (!transactionTime.equals(other.transactionTime))
			return false;
		return true;
	}
	
	
}