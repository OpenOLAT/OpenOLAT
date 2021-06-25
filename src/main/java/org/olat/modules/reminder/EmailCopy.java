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
package org.olat.modules.reminder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 18 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum EmailCopy {
	
	owner,
	assignedCoach,
	custom;
	
	private static final EmailCopy[] VALUES = values();
	
	public static String join(Collection<EmailCopy> emailCopies) {
		if (emailCopies == null || emailCopies.isEmpty()) {
			return  null;
		}
		return emailCopies.stream().map(EmailCopy::name).collect(Collectors.joining(","));
	}
	
	public static Set<EmailCopy> split(String emailCopies) {
		if (StringHelper.containsNonWhitespace(emailCopies)) {
			Set<EmailCopy> values = new HashSet<>();
			for (String value : emailCopies.split(",")) {
				if (isValid(value)) {
					values.add(EmailCopy.valueOf(value));
				}
			}
			return values;
		}
		return null;
	}
	
	public static boolean isValid(String string) {
		if (StringHelper.containsNonWhitespace(string)) {
			for(EmailCopy emailCopy : VALUES) {
				if(emailCopy.name().equals(string)) {
					return true;
				}
			}
		}
		return false;
	}	

}
