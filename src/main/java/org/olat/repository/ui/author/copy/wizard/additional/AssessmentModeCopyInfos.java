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
package org.olat.repository.ui.author.copy.wizard.additional;

import org.olat.core.gui.components.form.flexible.elements.DateChooser;

/**
 * Initial date: 01.06.2021<br>
 * 
 * Wrapper object to show date chooser and text elements in assessment mode table
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class AssessmentModeCopyInfos {
	
	private DateChooser beginDateChooser;
	private DateChooser endDateChooser;
	
	public AssessmentModeCopyInfos(DateChooser beginDateChooser, DateChooser endDateChooser) {
		this.beginDateChooser = beginDateChooser;
		this.endDateChooser = endDateChooser;
	}
	
	public DateChooser getBeginDateChooser() {
		return beginDateChooser;
	}
	
	public void setBeginDateChooser(DateChooser beginDateChooser) {
		this.beginDateChooser = beginDateChooser;
	}
	
	public DateChooser getEndDateChooser() {
		return endDateChooser;
	}
	
	public void setEndDateChooser(DateChooser endDateChooser) {
		this.endDateChooser = endDateChooser;
	}
}
