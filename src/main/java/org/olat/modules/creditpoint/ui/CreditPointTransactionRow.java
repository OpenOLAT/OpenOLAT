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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionType;

/**
 * 
 * Initial date: 7 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreditPointTransactionRow {
	
	private final CreditPointSource source;
	private final CreditPointTransaction transaction;
	
	private FormLink noteLink;
	
	public CreditPointTransactionRow(CreditPointTransaction transaction, CreditPointSource source) {
		this.transaction = transaction;
		this.source = source;
	}

	public Long getKey() {
		return transaction.getKey();
	}
	
	public CreditPointTransaction getTransaction() {
		return transaction;
	}
	
	public Date getCreationDate() {
		return transaction.getCreationDate();
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
	
	public Date getExpirationDate() {
		return transaction.getExpirationDate();
	}
	
	public String getNote() {
		return transaction.getNote();
	}
	
	public Long getOrderNumber() {
		return transaction.getOrderNumber();
	}
	
	public CreditPointSource getSource() {
		return source;
	}
	
	public FormLink getNoteLink() {
		return noteLink;
	}

	public void setNoteLink(FormLink noteLink) {
		this.noteLink = noteLink;
	}



	public record CreditPointSource(String source, String sourceIconCssClass) {
		//
	}

}
