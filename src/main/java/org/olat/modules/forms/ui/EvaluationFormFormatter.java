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
package org.olat.modules.forms.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormFormatter {
	
	private static final NumberFormat ZERO_OR_ONE_DECIMAL = new DecimalFormat("#0.#");
	private static final long hour = 60*60*1000;
	private static final long day = 24*60*60*1000;

	public static String formatDouble(Double value) {
		if (value == null || Double.isNaN(value)) {
			return "";
		}
		return String.format("%.2f", value);
	}
	
	public static String oneDecimal(Double value) {
		if (value == null || Double.isNaN(value)) {
			return "";
		}
		return String.format("%.1f", value);
	}
	
	public static String formatZeroOrOneDecimals(double value) {
		return ZERO_OR_ONE_DECIMAL.format(value);
	}
	
	public static String duration(long duration) {
		if (duration >= day) {
			return String.format("%d d %d h %d m %d s", 
					TimeUnit.MILLISECONDS.toDays(duration),
					TimeUnit.MILLISECONDS.toHours(duration)
						- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration)),
					TimeUnit.MILLISECONDS.toMinutes(duration)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
					TimeUnit.MILLISECONDS.toSeconds(duration)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
				);
		}
		if (duration >= hour) {
			return String.format("%d h %d m %d s", 
					TimeUnit.MILLISECONDS.toHours(duration),
					TimeUnit.MILLISECONDS.toMinutes(duration)
					- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
					TimeUnit.MILLISECONDS.toSeconds(duration)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
				);
		}
		return String.format("%d m %d s", 
				TimeUnit.MILLISECONDS.toMinutes(duration),
				TimeUnit.MILLISECONDS.toSeconds(duration)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
			);
	}

	public static String period(Date firstSubmission, Date lastSubmission, Locale locale) {
		if (firstSubmission == null || lastSubmission == null) return "";
		
		Formatter formatter = Formatter.getInstance(locale);
		return new StringBuilder()
				.append(formatter.formatDate(firstSubmission))
				.append(" - ")
				.append(formatter.formatDate(lastSubmission))
				.toString();
	}
	
	public static String formatSliderLabel(Slider slider) {
		boolean hasStartLabel = StringHelper.containsNonWhitespace(slider.getStartLabel());
		boolean hasEndLabel = StringHelper.containsNonWhitespace(slider.getEndLabel());
		if (hasStartLabel && hasEndLabel) {
			return slider.getStartLabel() + " ... " + slider.getEndLabel();
		} else if (hasStartLabel) {
			return slider.getStartLabel();
		} else if (hasEndLabel) {
			return slider.getEndLabel();
		}
		return null;
	}
}
