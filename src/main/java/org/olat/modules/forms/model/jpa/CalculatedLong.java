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
package org.olat.modules.forms.model.jpa;

import java.math.BigDecimal;

import org.olat.modules.forms.EvaluationFormSessionStatus;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CalculatedLong {
	
	private final String identifier;
	private final String subIdentifier;
	private final long value;
	
	public CalculatedLong(String identifier, long value) {
		this(identifier, "", value);
	}
	
	public CalculatedLong(EvaluationFormSessionStatus status, long value) {
		this(status.name(), "", value);
	}
	
	public CalculatedLong(String identifier, BigDecimal subIdentifier, long value) {
		this(identifier, trimZerosFromEnd(subIdentifier.toPlainString()), value);
	}
	
	public CalculatedLong(String identifier, String subIdentifier, long value) {
		super();
		this.identifier = identifier;
		this.subIdentifier = subIdentifier;
		this.value = value;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getSubIdentifier() {
		return subIdentifier;
	}

	public long getValue() {
		return value;
	}
	
	private static String trimZerosFromEnd(String value) {
		if (value.contains(".")) {
			int len = value.length();
			int st = 0;
			while (st < len) {
				if (value.charAt(len - 1) == '0') {
					len--;
				} else if (value.charAt(len - 1) == '.') {
					len--;
					break; // stop at decimal point
				}
			}
			System.out.println("trimmed: "+value.substring(0, len));
			return value.substring(0, len);
		}
		System.out.println("untrimmed: " + value);
		return value;
	}

}
