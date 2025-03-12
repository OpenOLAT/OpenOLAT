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
package org.olat.core.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Initial date: 2025-03-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum PriceAmountFormat {
	apostrophePoint("1'234'567.89", '\'', '.'),
	pointComma("1.234.567,89", '.', ','),
	commaPoint("1,234,567.89", ',', '.'),
	spacePoint("1 234 567.89", ' ', '.'),
	spaceComma("1 234 567,89", ' ', ',');

	private final String displayString;
	private final char groupingSeparator;
	private final char decimalSeparator;

	PriceAmountFormat(String displayString, char groupingSeparator, char decimalSeparator) {
		this.displayString = displayString;
		this.groupingSeparator = groupingSeparator;
		this.decimalSeparator = decimalSeparator;
	}

	public String getDisplayString() {
		return displayString;
	}

	public String format(BigDecimal amount) {
		DecimalFormat decimalFormat = new DecimalFormat("#,###.00", new DecimalFormatSymbols(Locale.ENGLISH));
		String formatted = decimalFormat.format(amount);
		return formatted
				.replace(',', 'G').replace('.', 'D')
				.replace('G', groupingSeparator).replace('D', decimalSeparator);
	}
}
