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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeHelper;
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

	public static final int LONG_TITLE_MAX_LENGTH = 75;  // recommendation
	public static final int SHORT_TITLE_MAX_LENGTH = 25; // must

	private TextElement shortTitleEl;
	private TextElement titleEl;
	private RichTextElement descriptionEl;
	private FormLink showAdditionalLink;
	private RichTextElement objectivesEl;
	private RichTextElement instructionEl;
	private RichTextElement instructionalDesignEl;

	private final CourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	private boolean showAdditional;
	

	public NodeConfigController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		this.showAdditional = StringHelper.containsNonWhitespace(courseNode.getObjectives())
				|| StringHelper.containsNonWhitespace(courseNode.getInstruction())
				|| StringHelper.containsNonWhitespace(courseNode.getInstructionalDesign());

		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// add the title input text element
		String longTitle = courseNode.getLongTitle();
		titleEl = uifactory.addTextElement("nodeConfigForm.displaytitle", "nodeConfigForm.displaytitle", 255, longTitle, formLayout);
		titleEl.setMandatory(true);
		titleEl.setCheckVisibleLength(true);
		titleEl.setExampleKey("nodeConfigForm.max.length.recommended", new String[] {String.valueOf(LONG_TITLE_MAX_LENGTH)});
		titleEl.setElementCssClass("o_sel_node_editor_title");
		
		String shortTitle = Formatter.truncateOnly(courseNode.getShortTitle(), SHORT_TITLE_MAX_LENGTH);
		if (longTitle.equals(shortTitle) || longTitle.startsWith(shortTitle)) {
			shortTitle = null;
		}
		shortTitleEl = uifactory.addTextElement("nodeConfigForm.shorttitle", "nodeConfigForm.shorttitle",
				SHORT_TITLE_MAX_LENGTH, shortTitle, formLayout);
		shortTitleEl.setCheckVisibleLength(true);
		shortTitleEl.setExampleKey("nodeConfigForm.max.length", new String[] {String.valueOf(SHORT_TITLE_MAX_LENGTH)});
		shortTitleEl.enablePlaceholderUpdate(titleEl.getFormDispatchId(), SHORT_TITLE_MAX_LENGTH);
		shortTitleEl.setElementCssClass("o_sel_node_editor_shorttitle");
		
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
		
		showAdditionalLink = uifactory.addFormLink("show.additional", "nodeConfigForm.show.additional", null, formLayout, Link.LINK);
		showAdditionalLink.setIconLeftCSS("o_icon o_icon-lg o_icon_open_togglebox");

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

	private void updateUI() {
		showAdditionalLink.setVisible(!showAdditional);
		objectivesEl.setVisible(showAdditional);
		instructionEl.setVisible(showAdditional);
		instructionalDesignEl.setVisible(showAdditional);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == showAdditionalLink) {
			showAdditional = true;
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		titleEl.clearError();
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (titleEl.getValue().length() > LONG_TITLE_MAX_LENGTH) {
			titleEl.setErrorKey("error.title.too.long", true, new String[] {String.valueOf(LONG_TITLE_MAX_LENGTH)});
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String longTitle = titleEl.getValue();
		courseNode.setLongTitle(longTitle);
		
		String shortTitle = shortTitleEl.getValue();
		if (!CourseNodeHelper.isCustomShortTitle(longTitle, shortTitle)) {
			shortTitle = null;
			shortTitleEl.setValue(null);
		}
		courseNode.setShortTitle(shortTitle);
		
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
