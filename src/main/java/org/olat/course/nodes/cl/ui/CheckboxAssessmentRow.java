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
package org.olat.course.nodes.cl.ui;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;


/**
 * 
 * This is a compact view of the assessment data with indexed arrays. It prevent
 * to have 1000x identities in memory which is a memory issue.
 * 
 * Initial date: 17.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxAssessmentRow {
	
	private final Long identityKey;
	private final String identityName;
	private final String[] identityProps;
	private final Float[] scores;
	private final Boolean[] checked;
	private TextElement pointEl;
	private MultipleSelectionElement checkedEl;
	
	public CheckboxAssessmentRow(CheckListAssessmentRow initialRow, Boolean[] checked, Float[] scores) {
		identityKey = initialRow.getIdentityKey();
		identityName = initialRow.getIdentityName();
		this.checked = checked;
		this.scores = scores;
		identityProps = initialRow.getIdentityProps();
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}

	public String getIdentityName() {
		return identityName;
	}

	public String getIdentityProp(int index) {
		return identityProps[index];
	}

	public Boolean[] getChecked() {
		return checked;
	}

	public Float[] getScores() {
		return scores;
	}

	public TextElement getPointEl() {
		return pointEl;
	}

	public void setPointEl(TextElement pointEl) {
		this.pointEl = pointEl;
	}

	public MultipleSelectionElement getCheckedEl() {
		return checkedEl;
	}

	public void setCheckedEl(MultipleSelectionElement checkedEl) {
		this.checkedEl = checkedEl;
	}
}
