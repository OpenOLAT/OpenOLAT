/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.assessment;

import java.util.Random;

import org.olat.core.util.crypto.RandomUtils;

/**
 * 
 * Initial date: Nov 7, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserDisplayIdentifierGenerator {
	
	private static final String CHARSET = "ABCDEFGHJKMNPQRSTUVWXYZ";
	
	public static final String generate() {
		return generate(CHARSET);
	}
	
	private static final String generate(String set) {
		Random secRnd = RandomUtils.secureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			double dPos = secRnd.nextDouble() * set.length();
			long pos = Math.round(dPos);
			if(pos >= set.length()) {
				pos = set.length() - 1l;
			}
			char ch = set.charAt((int)pos);
			sb.append(ch);
			if (i == 2) {
				sb.append("-");
			}
		}
		return sb.toString();
	}

}
