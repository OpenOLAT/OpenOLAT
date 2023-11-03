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
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
	private SpacerElement spacerEl;

	@Autowired
	private PFModule pfModule;

	public PFDefaultsEditController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		coachDropBox = uifactory.addCheckboxesHorizontal("coach.drop", formLayout, new String[]{"xx"}, new String[]{null});
		coachDropBox.addActionListener(FormEvent.ONCLICK);

		spacerEl = uifactory.addSpacerElement("spacer1", formLayout, false);

		participantDropBox = uifactory.addCheckboxesHorizontal("participant.drop", formLayout, new String[]{"xx"}, new String[]{null});
		participantDropBox.addActionListener(FormEvent.ONCLICK);

		alterFiles = uifactory.addCheckboxesHorizontal("alter.file", "alter.file", formLayout, new String[]{"xx"}, new String[]{null});
		alterFiles.addActionListener(FormEvent.ONCLICK);

		limitFileCount = uifactory.addCheckboxesHorizontal("limit.count", "limit.count", formLayout, new String[]{"xx"}, new String[]{null});
		limitFileCount.addActionListener(FormEvent.ONCLICK);
		fileCount = uifactory.addTextElement("file.count", 4, "3", formLayout);
		fileCount.setHelpTextKey("limit.count.coach.info", null);

		// Create submit button
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout)
				.setElementCssClass("o_sel_node_editor_submit");

		applyDefaultConfigValues();
		updateVisibility();
	}

	private void applyDefaultConfigValues() {
		coachDropBox.select("xx", pfModule.hasCoachBox());
		participantDropBox.select("xx", pfModule.hasParticipantBox());

		spacerEl.setVisible(pfModule.hasParticipantBox());

		alterFiles.setVisible(pfModule.hasParticipantBox());
		alterFiles.select("xx", pfModule.canAlterFile());

		limitFileCount.setVisible(pfModule.hasParticipantBox());
		limitFileCount.select("xx", pfModule.canLimitCount());

		fileCount.setVisible(pfModule.canLimitCount());
		fileCount.setValue(String.valueOf(pfModule.getFileCount()));
	}

	private void updateVisibility() {
		spacerEl.setVisible(participantDropBox.isSelected(0));
		alterFiles.setVisible(participantDropBox.isSelected(0));
		limitFileCount.setVisible(participantDropBox.isSelected(0));
		fileCount.setVisible(participantDropBox.isSelected(0) && limitFileCount.isSelected(0));
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
		if (source == participantDropBox) {
			updateVisibility();
		} else if (source == limitFileCount) {
			updateVisibility();
			if (limitFileCount.isSelected(0)) {
				showInfo("limit.count.coach.info");
			}
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
		fireEvent(ureq, Event.DONE_EVENT);
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
