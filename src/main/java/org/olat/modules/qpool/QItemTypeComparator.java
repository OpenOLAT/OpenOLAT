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
package org.olat.modules.qpool;

import java.util.Comparator;

import org.olat.modules.qpool.model.QItemType;

/**
 * 
 * Initial date: 18 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class QItemTypeComparator implements Comparator<QItemType> {

	@Override
	public int compare(QItemType q1, QItemType q2) {
		QuestionType t1 = QuestionType.typeOf(q1.getType());
		QuestionType t2 = QuestionType.typeOf(q2.getType());
		int c = Integer.compare(t1.ordinal(), t2.ordinal());
		if(c == 0) {
			String rt1 = toType(q1);
			String rt2 = toType(q2);
			c = rt1.compareTo(rt2);
		}
		
		if(c == 0) {
			c = q1.getKey().compareTo(q2.getKey());
		}
		return c;
	}
	
	private String toType(QItemType type) {
		return type.getType() == null ? "" : type.getType();
	}
}
