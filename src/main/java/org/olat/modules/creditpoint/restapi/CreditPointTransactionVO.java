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
package org.olat.modules.creditpoint.restapi;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.creditpoint.CreditPointTransaction;

/**
 * 
 * Initial date: 21 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "creditPointTransactionVO")
public class CreditPointTransactionVO {
	
	private Long key;
	private String type;
	private BigDecimal amount;
	private Date expirationDate;
	
	private String note;
	
	public CreditPointTransactionVO() {
		//
	}
	
	public static final CreditPointTransactionVO valueOf(CreditPointTransaction transaction) {
		CreditPointTransactionVO vo = new CreditPointTransactionVO();
		vo.setKey(transaction.getKey());
		vo.setType(transaction.getTransactionType().name());
		vo.setAmount(transaction.getAmount());
		vo.setExpirationDate(transaction.getExpirationDate());
		vo.setNote(transaction.getNote());
		return vo;
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 26169661 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CreditPointTransactionVO transaction) {
			return getKey() != null && getKey().equals(transaction.getKey());
		}
		return false;
	}
	

}
