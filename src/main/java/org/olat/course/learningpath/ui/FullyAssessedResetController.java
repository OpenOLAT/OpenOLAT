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
package org.olat.course.learningpath.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathConfigs.FullyAssessedResult;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FullyAssessedResetController extends FormBasicController implements Controller {
	
	private static final String[] configmKeys = new String[] {"confirm"};

	private MultipleSelectionElement confirmationEl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final LearningPathTreeNode lpTreeNode;

	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private LearningPathNodeAccessProvider learningPathNodeAccessProvider;

	public FullyAssessedResetController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, LearningPathTreeNode lpTreeNode) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.userCourseEnv = userCourseEnv;
		this.lpTreeNode = lpTreeNode;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		confirmationEl = uifactory.addCheckboxesHorizontal("reset.fully.assessed", formLayout, configmKeys,
				new String[] { translate("reset.fully.assessed.confirmation") });
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmationEl.clearError();
		if (!confirmationEl.isAtLeastSelected(1)) {
			confirmationEl.setErrorKey("reset.fully.assessed.confirmation.error", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		CourseNode courseNode = lpTreeNode.getCourseNode();
		CourseNode parent = lpTreeNode.getParent() != null
				? ((LearningPathTreeNode)lpTreeNode.getParent()).getCourseNode()
				: null;
		LearningPathConfigs configs = learningPathService.getConfigs(courseNode, parent);
		FullyAssessedResult result = configs.isFullyAssessedOnConfirmation(false);
		result = LearningPathConfigs.fullyAssessed(true, result.isFullyAssessed(), result.isDone());
		learningPathNodeAccessProvider.updateFullyAssessed(courseNode, userCourseEnv, result);
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
