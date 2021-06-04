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
package org.olat.ims.qti21.ui.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticFormatter {
	
	private static final DecimalFormat numberFormat = new DecimalFormat("#0.#", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final DecimalFormat scoreFormat = new DecimalFormat("#0.##", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public static String getLabel(int i) {
		return Character.toString((char)(65 + i));
	}
	
	public static String format(Float score) {
		if(score == null) return "";
		synchronized(numberFormat) {
			return numberFormat.format(score);
		}
	}
	
	public static String format(double score) {
		if(Double.isNaN(score)) {
			return "";
		}
		
		synchronized(numberFormat) {
			return numberFormat.format(score);
		}
	}
	
	public static String formatTwo(double score) {
		if(Double.isNaN(score)) {
			return "";
		}
		
		synchronized(scoreFormat) {
			return scoreFormat.format(score);
		}
	}
	
	public static String duration(long duration) {
		return String.format("%d min %d sec", 
			    TimeUnit.MILLISECONDS.toMinutes(duration),
			    TimeUnit.MILLISECONDS.toSeconds(duration) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
			);
	}
	
	public static String getModeString(List<Double> modes) {
		StringBuilder sb = new StringBuilder();
		for(Double mode:modes) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(format(mode));
		}
		return sb.toString();
	}
}
