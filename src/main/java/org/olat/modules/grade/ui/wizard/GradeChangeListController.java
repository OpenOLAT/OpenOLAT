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
package org.olat.modules.grade.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.modules.assessment.AssessmentEntry;

/**
 * 
 * Initial date: 10 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeChangeListController extends AbstractGradeListController {

	private final boolean autoGrade;

	public GradeChangeListController(UserRequest ureq, WindowControl wControl, Form form, StepsRunContext runContext, boolean autoGrade) {
		super(ureq, wControl, form, runContext);
		this.autoGrade = autoGrade;
	}

	@Override
	protected boolean isShowCurrentGrade() {
		return true;
	}

	@Override
	protected boolean isMultiSelect() {
		return false;
	}

	@Override
	protected boolean filter(AssessmentEntry assessmentEntry) {
		return autoGrade || StringHelper.containsNonWhitespace(assessmentEntry.getGrade());
	}
	
}
