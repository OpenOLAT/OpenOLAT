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

import java.util.Comparator;

import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormSession;

/**
 * 
 * Initial date: 20.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NameShuffleAnonymousComparator implements Comparator<EvaluationFormSession> {
	
	@Override
	public int compare(EvaluationFormSession session1, EvaluationFormSession session2) {
		if(session1 == null || session2 == null) {
			return compareNullsLast(session1, session2);
		}
		
		String lastName1 = StringHelper.containsNonWhitespace(session1.getLastname())? session1.getLastname(): null;
		String lastName2 = StringHelper.containsNonWhitespace(session2.getLastname())? session2.getLastname(): null;
		String firstName1 = StringHelper.containsNonWhitespace(session1.getFirstname())? session1.getFirstname(): null;
		String firstName2 = StringHelper.containsNonWhitespace(session2.getFirstname())? session2.getFirstname(): null;
		String keyHash1 = Encoder.md5hash(String.valueOf(session1.getKey()));
		String keyHash2 = Encoder.md5hash(String.valueOf(session2.getKey()));

		int c = 0;
		if(lastName1 == null || lastName2 == null) {
			c = compareNullsLast(lastName1, lastName2);
		} else {
			c = lastName1.compareTo(lastName2);
		}
		if(c == 0) {
			if(firstName1 == null || firstName2 == null) {
				c = compareNullsLast(firstName1, firstName2);
			} else {
				c = firstName1.compareTo(firstName2);
			}
		}
		if(c == 0) {
			c = keyHash1.compareTo(keyHash2);
		}

		return c;
	}
	
	private final int compareNullsLast(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return bb? (ba? 0: -1):(ba? 1: 0);
	}
}
