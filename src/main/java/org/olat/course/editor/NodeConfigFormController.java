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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.edusharing.EdusharingProvider;
import org.olat.repository.ui.settings.LazyRepositoryEdusharingProvider;

/**
 * Provides a FlexiForm that lets the user configure details for a course node.
 * 
 * @author twuersch
 *
 */
public class NodeConfigFormController extends FormBasicController {
	
	/**
	 * Maximum length of a course's short title.
	 */
	public static final int SHORT_TITLE_MAX_LENGTH =25;
	
	private static final String[] displayOptionsKeys = new String[]{
		CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT,
		CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT,
		CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT,
		CourseNode.DISPLAY_OPTS_TITLE_CONTENT,
		CourseNode.DISPLAY_OPTS_CONTENT};
	
	private String menuTitle;
	
	private String displayTitle;
	
	private String learningObjectives;
	
	private String displayOption;
	
	/**
	 * Input element for this course's short title.
	 */
	private TextElement shortTitle;
	
	/**
	 * Input element for this course's title.
	 */
	private TextElement title;
	
	/**
	 * Input element for the description of this course's objectives.
	 */
	private RichTextElement objectives;
	
	/**
	 * Selection fot the options title
	 */
	private SingleSelection displayOptions;

	private Long repositoryEntryKey;
	private String nodeIdent;
	
	/**
	 * Initializes this controller.
	 * 
	 * @param ureq The user request.
	 * @param wControl The window control.
	 * @param courseNode The course node this controller will access.
	 * @param repoKey 
	 * @param withCancel Decides whether to show a <i>cancel</i> button.
	 */
	public NodeConfigFormController(UserRequest ureq, WindowControl wControl, CourseNode courseNode, Long repoKey) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
		menuTitle = Formatter.truncate(courseNode.getShortTitle(), SHORT_TITLE_MAX_LENGTH);
		displayTitle = courseNode.getLongTitle();
		learningObjectives = courseNode.getLearningObjectives();
		displayOption = courseNode.getDisplayOption();
		nodeIdent = courseNode.getIdent();
		repositoryEntryKey = repoKey;
		initForm(ureq);
	}
	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// Don't dispose anything.
		
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formNOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formCancelled(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// add the short title text input element
		shortTitle = uifactory.addTextElement("nodeConfigForm.menutitle", "nodeConfigForm.menutitle", SHORT_TITLE_MAX_LENGTH, (menuTitle == null ? "": menuTitle), formLayout);
		shortTitle.setElementCssClass("o_sel_node_editor_shorttitle");
		shortTitle.setMandatory(true);
		shortTitle.setCheckVisibleLength(true);
		
		// add the title input text element
		title = uifactory.addTextElement("nodeConfigForm.displaytitle", "nodeConfigForm.displaytitle", 255, (displayTitle==null? "": displayTitle), formLayout);
		String longTitle = new String(translate("longtitle.placeholder", new String[]{menuTitle}));
		title.setPlaceholderText(longTitle);
		title.setElementCssClass("o_sel_node_editor_title");
		
		// add the learning objectives rich text input element
		objectives = uifactory.addRichTextElementForStringData("nodeConfigForm.learningobjectives", "nodeConfigForm.learningobjectives", (learningObjectives==null?"":learningObjectives), 10, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		objectives.setMaxLength(4000);
		EdusharingProvider provider = new LazyRepositoryEdusharingProvider(repositoryEntryKey, "course-learning-objectives-" + nodeIdent);
		objectives.getEditorConfiguration().enableEdusharing(getIdentity(), provider);
		
		String[] values = new String[]{
				translate("nodeConfigForm.short_title_desc_content"),
				translate("nodeConfigForm.title_desc_content"),
				translate("nodeConfigForm.short_title_content"),
				translate("nodeConfigForm.title_content"),
				translate("nodeConfigForm.content_only")};
		displayOptions = uifactory.addDropdownSingleselect("displayOptions", "nodeConfigForm.display_options", formLayout, displayOptionsKeys, values, null);
		for(String displayOptionsKey:displayOptionsKeys) {
			if(displayOptionsKey.equals(displayOption)) {
				displayOptions.select(displayOption, true);
			}
		}
		
		// Create submit and cancel buttons
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("nodeConfigForm.save", buttonLayout)
			.setElementCssClass("o_sel_node_editor_submit");
	}

	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		shortTitle.clearError();
		if (!StringHelper.containsNonWhitespace(shortTitle.getValue())) {
			// the short title is mandatory
			shortTitle.setErrorKey("nodeConfigForm.menumust", null);
			allOk &= false;
		} else if (shortTitle.hasError()) {
			allOk &= false;
		}
		
		if(!displayOptions.isOneSelected()) {
			displayOptions.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	/**
	 * Get the short title.
	 * @return The short title.
	 */
	public String getMenuTitle() {
		return shortTitle.getValue();
	}
	
	/**
	 * Gets the title.
	 * @return The title.
	 */
	public String getDisplayTitle() {
		return title.getValue();
	}
	
	/** 
	 * Gets the description of this course's objectives.
	 * @return The description of this course's objectives.
	 */
	public String getLearningObjectives() {
		return objectives.getValue();
	}
	
	/**
	 * Return the selected option
	 * @return
	 */
	public String getDisplayOption() {
		return displayOptions.getSelectedKey();
	}
}
