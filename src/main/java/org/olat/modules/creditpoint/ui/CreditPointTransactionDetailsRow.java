/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.ui;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionDetails;
import org.olat.modules.creditpoint.CreditPointTransactionType;

/**
 * 
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointTransactionDetailsRow {

	private final CreditPointTransaction transaction;
	private final CreditPointTransactionDetails transactionDetails;
	
	public CreditPointTransactionDetailsRow(CreditPointTransactionDetails transactionDetails,
			CreditPointTransaction transaction) {
		this.transactionDetails = transactionDetails;
		this.transaction = transaction;
	}
	
	public Long getKey() {
		return transaction.getKey();
	}
	
	public Date getCreationDate() {
		return transaction.getCreationDate();
	}
	
	public Date getExpirationDate() {
		return transaction.getExpirationDate();
	}
	
	public BigDecimal getCredit() {
		if(transaction.getTransactionType() == CreditPointTransactionType.deposit) {
			return transaction.getAmount();
		}
		return null;
	}
	
	public BigDecimal getDebit() {
		if(transaction.getTransactionType() == CreditPointTransactionType.withdrawal
				|| transaction.getTransactionType() == CreditPointTransactionType.removal
				|| transaction.getTransactionType() == CreditPointTransactionType.expiration
				|| transaction.getTransactionType() == CreditPointTransactionType.reversal) {
			return transaction.getAmount();
		}
		return null;
	}
	
	public Long getSource() {
		return transaction.getOrderNumber();
	}
	
	public CreditPointTransaction getTransaction() {
		return transaction;
	}

	public CreditPointTransactionDetails getTransactionDetails() {
		return transactionDetails;
	}
}
