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
package org.olat.course.nodes.pf.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.pf.PFModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Nov 01, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PFDefaultsEditController extends FormBasicController {

	private SelectionElement participantDropBox;
	private SelectionElement coachDropBox;
	private SelectionElement alterFiles;
	private SelectionElement limitFileCount;
	private TextElement fileCount;
	private FormLink resetDefaultsButton;
	private FormLink backLink;

	private DialogBoxController confirmReset;

	@Autowired
	private PFModule pfModule;

	public PFDefaultsEditController(UserRequest ureq, WindowControl wControl, String title) {
		super(ureq, wControl, "pf_def_conf");
		flc.contextPut("title", title);
		initForm(ureq);
		loadDefaultConfigValues();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		resetDefaultsButton = uifactory.addFormLink("reset", "course.node.reset.defaults", null, formLayout, Link.BUTTON);
		resetDefaultsButton.setElementCssClass("o_sel_cal_delete pull-right");
		backLink = uifactory.addFormLink("back", flc);
		backLink.setIconLeftCSS("o_icon o_icon_back");

		FormLayoutContainer pfCont = FormLayoutContainer.createDefaultFormLayout("pfCont", getTranslator());
		pfCont.setRootForm(mainForm);
		formLayout.add(pfCont);

		coachDropBox = uifactory.addCheckboxesHorizontal("coach.drop", pfCont, new String[]{"xx"}, new String[]{null});
		coachDropBox.addActionListener(FormEvent.ONCLICK);

		uifactory.addSpacerElement("spacer1", pfCont, false);

		participantDropBox = uifactory.addCheckboxesHorizontal("participant.drop", pfCont, new String[]{"xx"}, new String[]{null});
		participantDropBox.addActionListener(FormEvent.ONCLICK);

		alterFiles = uifactory.addCheckboxesHorizontal("alter.file", "blank.label", pfCont, new String[]{"xx"}, new String[]{translate("alter.file")});
		alterFiles.addActionListener(FormEvent.ONCLICK);

		limitFileCount = uifactory.addCheckboxesHorizontal("limit.count", "blank.label", pfCont, new String[]{"xx"}, new String[]{translate("limit.count")});
		limitFileCount.addActionListener(FormEvent.ONCLICK);
		fileCount = uifactory.addTextElement("file.count", 4, "3", pfCont);
		fileCount.setHelpTextKey("limit.count.coach.info", null);

		// Create submit button
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		pfCont.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout)
				.setElementCssClass("o_sel_node_editor_submit");
	}

	private void loadDefaultConfigValues() {
		coachDropBox.select("xx", pfModule.hasCoachBox());
		participantDropBox.select("xx", pfModule.hasParticipantBox());

		alterFiles.select("xx", pfModule.canAlterFile());

		limitFileCount.select("xx", pfModule.canLimitCount());

		fileCount.setValue(String.valueOf(pfModule.getFileCount()));
	}

	private void updateDefaultConfigValues() {
		int numOfFiles = 0;
		if (fileCount.isVisible() && StringHelper.isLong(fileCount.getValue())) {
			numOfFiles = Integer.parseInt(fileCount.getValue());
		}

		pfModule.setHasParticipantBox(participantDropBox.isSelected(0));
		pfModule.setHasCoachBox(coachDropBox.isSelected(0));
		pfModule.setCanAlterFile(alterFiles.isSelected(0));
		pfModule.setCanLimitCount(limitFileCount.isSelected(0));
		pfModule.setFileCount(numOfFiles);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (source == limitFileCount) {
			if (limitFileCount.isSelected(0)) {
				showInfo("limit.count.coach.info");
			}
		} else if (source == resetDefaultsButton) {
			confirmReset = activateYesNoDialog(ureq, null, translate("course.node.confirm.reset"), confirmReset);
		}

		// at least one box must be enabled
		if (!(participantDropBox.isSelected(0) || coachDropBox.isSelected(0))) {
			participantDropBox.setErrorKey("folderselection.error");
		} else {
			participantDropBox.clearError();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateDefaultConfigValues();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmReset) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				pfModule.resetProperties();
				loadDefaultConfigValues();
			}
			// Fire this event regardless of yes, no or close
			// Little hack to prevent a dirty form after pressing reset button
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		fileCount.clearError();
		participantDropBox.clearError();

		// at least one box must be enabled
		if (!(participantDropBox.isSelected(0) || coachDropBox.isSelected(0))) {
			participantDropBox.setErrorKey("folderselection.error");
			allOk &= false;
		}
		if (participantDropBox.isSelected(0) && (limitFileCount.isSelected(0))) {
			// if file limit is enabled, ensure limit is greater than 0
			if (StringHelper.containsNonWhitespace(fileCount.getValue())) {
				try {
					int numOfFiles = Integer.parseInt(fileCount.getValue());
					if (1 > numOfFiles) {
						fileCount.setErrorKey("filecount.error");
						allOk &= false;
					}
				} catch (NumberFormatException e) {
					fileCount.setErrorKey("form.error.nointeger");
					allOk &= false;
				}
			} else {
				fileCount.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		return allOk;
	}
}
