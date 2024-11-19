/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.grade.ui.wizard;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.DefaultStepsRunContext;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: Nov 18, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GradeApplyConfirmationController extends FormBasicController {

	private GradeApplayBulkListController gradeApplayListCtrl;
	
	private final StepsRunContext runContext;
	private final List<Identity> identities;

	public GradeApplyConfirmationController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, GradeScale gradeScale, List<Breakpoint> breakpoints, List<Identity> identities) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.identities = identities;
		
		runContext = new DefaultStepsRunContext();
		runContext.put(GradeScaleAdjustCallback.KEY_COURSE_ENTRY, courseEntry);
		runContext.put(GradeScaleAdjustCallback.KEY_COURSE_NODE, courseNode);
		runContext.put(GradeScaleAdjustCallback.KEY_GRADE_SCALE, gradeScale);
		runContext.put(GradeScaleAdjustCallback.KEY_BREAKPOINTS, breakpoints);

		initForm(ureq);
	}

	public List<Identity> getIdentities() {
		return identities;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Set<Long> identityKeys = identities.stream().map(Identity::getKey).collect(Collectors.toSet());
		gradeApplayListCtrl = new GradeApplayBulkListController(ureq, getWindowControl(), mainForm, runContext, identityKeys);
		listenTo(gradeApplayListCtrl);
		formLayout.add(gradeApplayListCtrl.getInitialFormItem());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_block_large_top o_button_group_right");
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("apply", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public static final class GradeApplayBulkListController extends AbstractGradeListController {
		
		private final Set<Long> identityKeys;

		public GradeApplayBulkListController(UserRequest ureq, WindowControl wControl, Form form,
				StepsRunContext runContext, Set<Long> identityKeys) {
			super(ureq, wControl, form, runContext);
			this.identityKeys = identityKeys;
			loadModel();
		}

		@Override
		protected boolean isShowCurrentValues() {
			return false;
		}

		@Override
		protected boolean isMultiSelect() {
			return false;
		}

		@Override
		protected boolean filter(AssessmentEntry assessmentEntry) {
			return identityKeys.contains(assessmentEntry.getIdentity().getKey());
		}
		
	}

}
