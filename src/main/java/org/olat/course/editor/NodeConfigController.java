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

package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GenericCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.ui.settings.LazyRepositoryEdusharingProvider;

/**
 * Initial date: 19 July 2021<br>
 * >
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class NodeConfigController extends FormBasicController {

	public static final int SHORT_TITLE_MAX_LENGTH = 25;

	private TextElement shortTitleEl;
	private TextElement titleEl;
	private RichTextElement descriptionEl;
	private RichTextElement objectivesEl;
	private RichTextElement instructionEl;
	private RichTextElement instructionalDesignEl;

	private final CourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;

	public NodeConfigController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String shortTitle = Formatter.truncate(courseNode.getShortTitle(), SHORT_TITLE_MAX_LENGTH);
		shortTitleEl = uifactory.addTextElement("nodeConfigForm.menutitle", "nodeConfigForm.menutitle",
				SHORT_TITLE_MAX_LENGTH, shortTitle, formLayout);
		shortTitleEl.setElementCssClass("o_sel_node_editor_shorttitle");
		shortTitleEl.setMandatory(true);
		shortTitleEl.setCheckVisibleLength(true);

		// add the title input text element
		titleEl = uifactory.addTextElement("nodeConfigForm.displaytitle", "nodeConfigForm.displaytitle", 255,
				courseNode.getLongTitle(), formLayout);
		String longTitle = new String(translate("longtitle.placeholder", new String[] { shortTitle }));
		titleEl.setPlaceholderText(longTitle);
		titleEl.setElementCssClass("o_sel_node_editor_title");

		descriptionEl = uifactory.addRichTextElementForStringData("nodeConfigForm.description",
				"nodeConfigForm.description", courseNode.getDescription(), 10, -1, false, null, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		descriptionEl.setMaxLength(4000);
		descriptionEl.setHelpTextKey("nodeConfigForm.description.help", null);
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		descriptionEl.getEditorConfiguration().enableEdusharing(getIdentity(),
				new LazyRepositoryEdusharingProvider(
						userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry().getKey(),
						"course-learning-objectives-" + courseNode.getIdent()));

		objectivesEl = uifactory.addRichTextElementForStringData("nodeConfigForm.objectives",
				"nodeConfigForm.objectives", courseNode.getObjectives(), 10, -1, false, null, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		objectivesEl.setHelpTextKey("nodeConfigForm.objectives.help", null);
		objectivesEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		objectivesEl.setMaxLength(4000);
		objectivesEl.getEditorConfiguration().enableEdusharing(getIdentity(),
				new LazyRepositoryEdusharingProvider(
						userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry().getKey(),
						"course-objectives-" + courseNode.getIdent()));

		instructionEl = uifactory.addRichTextElementForStringData("nodeConfigForm.instruction",
				"nodeConfigForm.instruction", courseNode.getInstruction(), 10, -1, false, null, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		instructionEl.setMaxLength(4000);
		instructionEl.setHelpTextKey("nodeConfigForm.instruction.help", null);
		instructionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		instructionEl.getEditorConfiguration().enableEdusharing(getIdentity(),
				new LazyRepositoryEdusharingProvider(
						userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry().getKey(),
						"course-instruction-" + courseNode.getIdent()));

		instructionalDesignEl = uifactory.addRichTextElementForStringData("nodeConfigForm.instructional.design",
				"nodeConfigForm.instructional.design", courseNode.getInstructionalDesign(), 10, -1, false, null, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		instructionalDesignEl.setMaxLength(4000);
		instructionalDesignEl.setHelpTextKey("nodeConfigForm.instructional.design.help", null);
		instructionalDesignEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		instructionalDesignEl.getEditorConfiguration().enableEdusharing(getIdentity(),
				new LazyRepositoryEdusharingProvider(
						userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry().getKey(),
						"course-instructional-design-" + courseNode.getIdent()));

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("nodeConfigForm.save", buttonLayout)
				.setElementCssClass("o_sel_node_editor_submit");
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		shortTitleEl.clearError();
		if (!StringHelper.containsNonWhitespace(shortTitleEl.getValue())) {
			shortTitleEl.setErrorKey("nodeConfigForm.menumust", null);
			allOk &= false;
		} else if (shortTitleEl.hasError()) {
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		courseNode.setShortTitle(shortTitleEl.getValue());
		courseNode.setLongTitle(titleEl.getValue());
		if (courseNode instanceof GenericCourseNode) {
			// Indicator that migration is done
			((GenericCourseNode) courseNode).setLearningObjectives(null);
		}
		courseNode.setDescription(descriptionEl.getValue());
		courseNode.setObjectives(objectivesEl.getValue());
		courseNode.setInstruction(instructionEl.getValue());
		courseNode.setInstructionalDesign(instructionalDesignEl.getValue());

		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
