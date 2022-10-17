/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Embeddable;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  23 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Embeddable
public class PriceImpl implements Price, Serializable {
	
	private static final long serialVersionUID = -757711036712675302L;
	private static final Logger log = Tracing.createLoggerFor(PriceImpl.class);
	
	private BigDecimal amount;
	private String currencyCode;
	
	public PriceImpl() {
		//
	}
	
	public PriceImpl(BigDecimal amount, String currencyCode) {
		this.amount = amount;
		this.currencyCode = currencyCode;
	}
	
	@Override
	public BigDecimal getAmount() {
		return amount;
	}

	@Override
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	@Override
	public String getCurrencyCode() {
		return currencyCode;
	}

	@Override
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	@Override
	public boolean isEmpty() {
		if(amount == null) {
			return true;
		} else if(BigDecimal.ZERO.compareTo(amount) >= 0) {
			return true;
		}
		return false;
	}

	@Override
	public PriceImpl add(Price price) {
		//replace with the multi currency manager
		if(currencyCode != null && price.getCurrencyCode() != null && !currencyCode.equals(price.getCurrencyCode())) {
			log.warn("Problem: two differents iso currency codes");
		}
		return new PriceImpl(amount.add(price.getAmount()), currencyCode);
	}

	@Override
	public Price substract(Price price) {
		//replace with the multi currency manager
		if(currencyCode != null && price.getCurrencyCode() != null && !currencyCode.equals(price.getCurrencyCode())) {
			log.warn("Problem: two differents iso currency codes");
		}
		return new PriceImpl(amount.subtract(price.getAmount()), currencyCode);
	}

	@Override
	public Price multiply(int multiplicand) {
		if(amount == null) {
			return new PriceImpl(BigDecimal.ZERO, currencyCode);
		}
		
		BigDecimal bigMultiplicand = new BigDecimal(multiplicand);
		return new PriceImpl(amount.multiply(bigMultiplicand), currencyCode);
	}

	@Override
	public Price multiply(BigDecimal multiplicand) {
		BigDecimal v = amount.multiply(multiplicand);
		v = v.setScale(2, RoundingMode.HALF_UP);
		return new PriceImpl(v, currencyCode);
	}

	@Override
	public Price divide(BigDecimal divisor) {
		BigDecimal v = amount.divide(divisor, 2, RoundingMode.HALF_UP);
		return new PriceImpl(v, currencyCode);
	}
	
	@Override
	public PriceImpl clone() {
		PriceImpl clone = new PriceImpl();
		clone.setAmount(amount);
		clone.setCurrencyCode(currencyCode);
		return clone;
	}

	@Override
	public String toString() {
		return currencyCode + '\u00A0' + (amount == null ? "" : amount.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
	}
}
