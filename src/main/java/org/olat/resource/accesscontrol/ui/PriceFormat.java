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
package org.olat.resource.accesscontrol.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Description:<br>
 * Format the price
 * 
 * <P>
 * Initial Date:  26 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, htttp://www.frentix.com
 */
public class PriceFormat {
	private static final Logger log = Tracing.createLoggerFor(PriceFormat.class);


	//private static final String[] tests = new String[]{"1'000.00", "1'000,00","1,000,-","1,000.-","1.000","1000","1000.00","1000,00","1000.-","1000,-"};

	public static void parse(Price price, String value) {
		try {
			if(value != null && value.length() > 0) {
				value = extractNumbers(value);
			}
				
			if(value != null && value.length() > 0) {
				double val = Double.parseDouble(value);
				BigDecimal bd = new BigDecimal(val);
				price.setAmount(bd.setScale(2, RoundingMode.HALF_UP));
			} else {
				price.setAmount(BigDecimal.ZERO);
			}
		} catch (NumberFormatException e) {
			log.error("Cannot format this value: {}", value);
		}  
	}
	
	public static BigDecimal parse(String value) {
		if(value != null && value.length() > 0) {
			value = extractNumbers(value);
			
			double val = Double.parseDouble(value);
			BigDecimal bd = new BigDecimal(val);
			return bd.setScale(2, RoundingMode.HALF_UP);
		}
		return BigDecimal.ZERO;
	}
	
	private static String extractNumbers(String value) {
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i<value.length(); i++) {
			char ch = value.charAt(i);
			if(Character.isDigit(ch)) {
				buffer.append(ch);
			} else if(ch == '.') {
				buffer.append(ch);
			} else if(ch == ',') {
				buffer.append('.');
			}
		}
		return buffer.toString();
	}
	
	public static String format(Price price) {
		if(price == null) {
			return "";
		}
		if(price.getAmount() == null) {
			return "";
		}
		return format(price.getAmount());
	}
	
	public static String format(BigDecimal value) {
		return value.setScale(2).toString();
	}
	
	public static String fullFormat(Price price) {
		if(price == null || price.getAmount() == null) {
			return "";
		}
		
		String isoCurrencyCode = price.getCurrencyCode();
		if (isoCurrencyCode == null) {
			isoCurrencyCode = "CHF";
		}

		return isoCurrencyCode + '\u00A0' + price.getAmount().setScale(2, RoundingMode.HALF_EVEN).toString();
	}
}
