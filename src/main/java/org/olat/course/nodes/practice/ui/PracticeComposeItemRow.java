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
package org.olat.course.nodes.practice.ui;

import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.model.PracticeItem;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeComposeItemRow {
	
	private final PracticeItem item;
	private final PracticeAssessmentItemGlobalRef itemGlobalRef;
	
	public PracticeComposeItemRow(PracticeItem item, PracticeAssessmentItemGlobalRef itemGlobalRef) {
		this.item = item;
		this.itemGlobalRef = itemGlobalRef;
	}

	public PracticeItem getItem() {
		return item;
	}

	public PracticeAssessmentItemGlobalRef getItemGlobalRef() {
		return itemGlobalRef;
	}
	
	public boolean isAnswered() {
		return itemGlobalRef != null && itemGlobalRef.getAttempts() > 0;
	}
	
	public Long getLevel() {
		if(itemGlobalRef == null) {
			return Long.valueOf(0);
		}
		return Long.valueOf(itemGlobalRef.getLevel());
	}
	
	public double getCorrect() {
		if(itemGlobalRef == null || itemGlobalRef.getAttempts() <= 0) {
			return 0.0d;
		}
		return itemGlobalRef.getCorrectAnswers() / (double)itemGlobalRef.getAttempts();
	}
}
