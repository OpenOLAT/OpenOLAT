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

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormFormatter {
	
	private static final long hour = 60*60*1000;
	private static final long day = 24*60*60*1000;

	public static String formatDouble(Double value) {
		if (value == null || Double.isNaN(value)) {
			return "";
		}
		return String.format("%.2f", value);
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
}
