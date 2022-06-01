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
package org.olat.course.nodes.practice.manager;

import java.util.Comparator;

import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;

/**
 * 
 * Initial date: 1 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IncorrectGlobalRefComparator implements Comparator<PracticeAssessmentItemGlobalRef> {

	@Override
	public int compare(PracticeAssessmentItemGlobalRef o1, PracticeAssessmentItemGlobalRef o2) {
		double i1 = getIncorrectness(o1);
		double i2 = getIncorrectness(o2);
		return Double.compare(i1, i2);
	}
	
	private double getIncorrectness(PracticeAssessmentItemGlobalRef o) {
		if(o == null || o.getAttempts() <= 0) {
			return 0.0d;
		}
		return o.getIncorrectAnswers() / (double)o.getAttempts();
	}
}
