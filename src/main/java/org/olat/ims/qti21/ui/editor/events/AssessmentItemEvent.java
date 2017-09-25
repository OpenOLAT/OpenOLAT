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
package org.olat.ims.qti21.ui.editor.events;

import org.olat.core.gui.control.Event;
import org.olat.ims.qti21.model.QTI21QuestionType;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 03.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemEvent extends Event {
	
	private static final long serialVersionUID = -1768118856227595311L;
	
	public static final String ASSESSMENT_ITEM_CHANGED = "assessment-item-changed";
	public static final String ASSESSMENT_ITEM_NEED_RELOAD = "assessment-item-need-reload";
	public static final String ASSESSMENT_ITEM_METADATA_CHANGED = "assessment-item-metadata-changed";
	
	private AssessmentItem item;
	private AssessmentItemRef itemRef;
	
	private QTI21QuestionType questionType;
	
	public AssessmentItemEvent(String cmd, AssessmentItem item) {
		super(cmd);
		this.item = item;
	}

	public AssessmentItemEvent(String cmd, AssessmentItem item, QTI21QuestionType questionType) {
		super(cmd);
		this.item = item;
		this.questionType = questionType;
	}
	
	public AssessmentItemEvent(String cmd, AssessmentItem item, AssessmentItemRef itemRef, QTI21QuestionType questionType) {
		super(cmd);
		this.item = item;
		this.itemRef = itemRef;
		this.questionType = questionType;
	}

	public AssessmentItem getAssessmentItem() {
		return item;
	}
	
	public AssessmentItemRef getAssessmentItemRef() {
		return itemRef;
	}
	
	public QTI21QuestionType getQuestionType() {
		return questionType;
	}
}
