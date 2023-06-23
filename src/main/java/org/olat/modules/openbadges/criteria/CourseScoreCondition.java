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
package org.olat.modules.openbadges.criteria;

import org.olat.core.gui.translator.Translator;

/**
 * Initial date: 2023-06-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseScoreCondition implements BadgeCondition {
	public static final String KEY = "courseScore";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String toString(Translator translator) {
		return translator.translate("badgeCondition." + KEY, getSymbol().getSymbolString(), Double.toString(getValue()));
	}

	public enum Symbol {
		greaterThan(">"),
		greaterThanOrEqual("≥"),
		equals("="),
		lessThanOrEqual("≤"),
		lessThan("<"),
		notEqual("≠");

		private final String symbolString;

		Symbol(String symbolString) {
			this.symbolString = symbolString;
		}

		public String getSymbolString() {
			return symbolString;
		}
	}

	private Symbol symbol;

	private double value;

	public CourseScoreCondition(Symbol symbol, double value) {
		this.symbol = symbol;
		this.value = value;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
