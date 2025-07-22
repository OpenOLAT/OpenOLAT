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
package org.olat.modules.creditpoint;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.PriceAmountFormat;
import org.olat.core.util.i18n.I18nModule;

/**
 * Based on PriceAmountFormat and its preferences.
 * 
 * 
 * Initial date: 21 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointFormat {
	
	private static I18nModule i18nModule;
	
	private static I18nModule getI18nModule() {
		if(i18nModule == null) {
			i18nModule = CoreSpringFactory.getImpl(I18nModule.class);
		}
		return i18nModule;
	}
	
	public static final String format(BigDecimal points) {
		PriceAmountFormat priceAmountFormat = getI18nModule().getPriceAmountFormat();
		DecimalFormat decimalFormat = new DecimalFormat("#,##0", new DecimalFormatSymbols(Locale.ENGLISH));
		String formatted = decimalFormat.format(points);
		return formatted
				.replace(',', 'G').replace('.', 'D')
				.replace('G', priceAmountFormat.groupingSeparator()).replace('D', priceAmountFormat.decimalSeparator());
	}
	
	public static final String format(BigDecimal points, CreditPointSystem system) {
		String formattedPoints = format(points);
		return formattedPoints + " " + system.getLabel();
	}
}
