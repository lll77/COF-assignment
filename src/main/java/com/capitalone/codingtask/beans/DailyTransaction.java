/**
 * 
 */
package com.capitalone.codingtask.beans;

import java.util.List;

/**
 * @author MalgariV
 *
 */
public class DailyTransaction {

	private String date;
	private List<Long> debitAmounts;
	private List<Long> creditAmounts;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public List<Long> getDebitAmounts() {
		return debitAmounts;
	}
	public void setDebitAmounts(List<Long> debitAmounts) {
		this.debitAmounts = debitAmounts;
	}
	public List<Long> getCreditAmounts() {
		return creditAmounts;
	}
	public void setCreditAmounts(List<Long> creditAmounts) {
		this.creditAmounts = creditAmounts;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		DailyTransaction other = (DailyTransaction) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}
	
	}
