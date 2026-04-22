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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointWallet;

/**
 * 
 * Initial date: 21 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "creditPointWalletVO")
public class CreditPointWalletVO {
	
	private Long key;
	private BigDecimal balance;
	private Long identityKey;
	private Long creditPointSystemKey;
	
	public CreditPointWalletVO() {
		//
	}
	
	public static final CreditPointWalletVO valueOf(CreditPointWallet wallet) {
		CreditPointWalletVO vo = new CreditPointWalletVO();
		vo.setKey(wallet.getKey());
		vo.setBalance(wallet.getBalance());
		vo.setIdentityKey(wallet.getIdentity().getKey());
		vo.setCreditPointSystemKey(wallet.getCreditPointSystem().getKey());
		return vo;
	}
	
	public static final CreditPointWalletVO emptyVO(IdentityRef identity, CreditPointSystem system) {
		CreditPointWalletVO vo = new CreditPointWalletVO();
		vo.setKey(null);
		vo.setBalance(BigDecimal.ZERO);
		vo.setIdentityKey(identity.getKey());
		vo.setCreditPointSystemKey(system.getKey());
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getCreditPointSystemKey() {
		return creditPointSystemKey;
	}

	public void setCreditPointSystemKey(Long creditPointSystemKey) {
		this.creditPointSystemKey = creditPointSystemKey;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 234789 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CreditPointWalletVO wallet) {
			return getKey() != null && getKey().equals(wallet.getKey());
		}
		return false;
	}

}
