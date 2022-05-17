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
package org.olat.course.nodes.practice.ui.events;

import org.olat.core.gui.control.Event;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;

/**
 * 
 * Initial date: 10 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResponseEvent extends Event {
	
	private static final long serialVersionUID = -5433058330267387445L;

	public static final String RESPONSE_EVENT = "response-event";
	
	private Boolean passed;
	private PracticeAssessmentItemGlobalRef golbalRef;
	
	public ResponseEvent(Boolean passed, PracticeAssessmentItemGlobalRef golbalRef) {
		super(RESPONSE_EVENT);
		this.passed = passed;
		this.golbalRef = golbalRef;
	}
	
	public Boolean getPasssed() {
		return passed;
	}
	
	public PracticeAssessmentItemGlobalRef getPracticeAssessmentItemGlobalRef() {
		return golbalRef;
	}
}
