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
package org.olat.core.util.crypto;

import java.util.Random;

/**
 * 
 * Initial date: 14 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PasswordGenerator {

	private static final String CHARSET = "23456789abcdefghjkmnpqrstuvwxyz23456789ABCDEFGHJKMNPQRSTUVWXYZ";
	private static final String NUMERICAL = "123456789";
	
	private PasswordGenerator() {
		//
	}
	
	public static final String generateNumericalCode(int length) {
		return generate(NUMERICAL, length);
	}
	
	public static final String generateAlphaNumericToken(int length) {
		return generate(CHARSET, length);
	}
	
	public static final String generatePassword(int length) {
		return generate(CHARSET, length);
	}
	
	private static final String generate(String set, int length) {
		Random secRnd = RandomUtils.secureRandom();
		StringBuilder sb = new StringBuilder();
		int countNum = 0;
		for (int i = 0; i < length; i++) {
			double dPos = secRnd.nextDouble() * set.length();
			long pos = Math.round(dPos);
			if(pos >= set.length()) {
				pos = set.length() - 1l;
			}
			char ch = set.charAt((int)pos);
			if(Character.isDigit(ch)) {
				countNum++;
			}
			sb.append(ch);
		}
		
		//make a OLAT-compatible password
		for (int i = countNum; i < 2; i++) {
			double dPos = secRnd.nextDouble() * 10;
			long pos = Math.round(dPos);
			if(pos >= set.length()) {
				pos = set.length() - 1l;
			}
			char ch = set.charAt((int)pos);
			sb.append(ch);
		}
		return sb.toString();
	}
}
