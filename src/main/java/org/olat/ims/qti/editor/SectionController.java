/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.ims.qti.editor.beecom.objects.Duration;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.editor.beecom.objects.SelectionOrdering;

/**
 * 
 * Initial date: 05.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SectionController extends FormBasicController implements TabbableController, ControllerEventListener {
	
	private static final String[] yesnoKeys = new String[] { "y", "n" };
	
	private TextElement titleEl;
	private IntegerElement timeMinEl, timeSecEl;
	private RichTextElement objectivesEl;
	private SingleSelection limitTimeEl, shuffleEl, selectionNumEl;
	
	private Section section;
	private final QTIEditorPackage qtiPackage;
	private final boolean restrictedEdit;
	private final boolean blockedEdit;

	/**
	 * @param section
	 * @param qtiPackage
	 * @param locale
	 * @param wControl
	 */
	public SectionController(Section section, QTIEditorPackage qtiPackage, UserRequest ureq, WindowControl wControl,
			boolean restrictedEdit, boolean blockedEdit) {
		super(ureq, wControl);
		this.blockedEdit = blockedEdit;
		this.restrictedEdit = restrictedEdit;
		this.section = section;
		this.qtiPackage = qtiPackage;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("fieldset.legend.sectionsettings");
		setFormContextHelp("Test and Questionnaire Editor in Detail#details_testeditor_test_konf");

		String title = section.getTitle();
		titleEl = uifactory.addTextElement("title", "form.metadata.title", 255, title, formLayout);
		titleEl.setEnabled(!blockedEdit);
		
		String objectives = section.getObjectives();
		objectivesEl = uifactory.addRichTextElementForStringData("objectives", "form.metadata.objectives", objectives, 6, 12, false,
				qtiPackage.getBaseDir(), null, formLayout, ureq.getUserSession(), getWindowControl());
		objectivesEl.getEditorConfiguration().setFigCaption(false);
		objectivesEl.setEnabled(!blockedEdit);
		
		RichTextConfiguration richTextConfig = objectivesEl.getEditorConfiguration();
		// disable <p> element for enabling vertical layouts
		richTextConfig.disableRootParagraphElement();
		// set upload dir to the media dir
		richTextConfig.setFileBrowserUploadRelPath("media");
		// manually enable the source edit button
		richTextConfig.enableCode();
		//allow script tags...
		richTextConfig.setInvalidElements(RichTextConfiguration.INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE_WITH_SCRIPT);
		richTextConfig.setExtendedValidElements("script[src,type,defer]");
		
		//form.section.durationswitch
		String[] yesnoValues = new String[] { translate("yes"), translate("no") };
		limitTimeEl = uifactory.addRadiosHorizontal("form.section.durationswitch", formLayout, yesnoKeys, yesnoValues);
		limitTimeEl.addActionListener(FormEvent.ONCHANGE);
		limitTimeEl.setEnabled(!restrictedEdit && !blockedEdit);
		
		timeMinEl = uifactory.addIntegerElement("form.imd.time.min", 0, formLayout);
		timeMinEl.setDisplaySize(3);
		timeMinEl.setEnabled(!restrictedEdit && !blockedEdit);
		timeSecEl = uifactory.addIntegerElement("form.imd.time.sek", 0, formLayout);
		timeSecEl.setDisplaySize(3);
		timeSecEl.setEnabled(!restrictedEdit && !blockedEdit);
		if (section.getDuration() != null && section.getDuration().isSet()) {
			limitTimeEl.select(yesnoKeys[0], true);
			timeMinEl.setIntValue(section.getDuration().getMin());
			timeSecEl.setIntValue(section.getDuration().getSec());
		} else {
			limitTimeEl.select(yesnoKeys[1], true);
			timeMinEl.setVisible(false);
			timeSecEl.setVisible(false);
		}

		//ordering
		boolean random = SelectionOrdering.RANDOM.equals(section.getSelection_ordering().getOrderType());
		shuffleEl = uifactory.addRadiosHorizontal("shuffle", "form.section.shuffle", formLayout, yesnoKeys, yesnoValues);
		shuffleEl.addActionListener(FormEvent.ONCHANGE);
		shuffleEl.setEnabled(!restrictedEdit && !blockedEdit);
		if (random) {
			shuffleEl.select(yesnoKeys[0], true);
		} else {
			shuffleEl.select(yesnoKeys[1], true);
		}
		
		int numOfItems = section.getItems().size();
		String[] theKeys = new String[numOfItems + 1];
		String[] theValues = new String[numOfItems + 1];
		theKeys[0] = "0";
		theValues[0] = translate("form.section.selection_all");
		for(int i=0; i<numOfItems; i++) {
			theKeys[i+1] = Integer.toString(i+1);
			theValues[i+1] = Integer.toString(i+1);
		}
		selectionNumEl = uifactory.addDropdownSingleselect("selection.num", "form.section.selection_pre", formLayout, theKeys, theValues, null);
		selectionNumEl.setHelpText(translate("form.section.selection_pre.hover"));
		selectionNumEl.setEnabled(!restrictedEdit && !blockedEdit);
		int selectionNum = section.getSelection_ordering().getSelectionNumber();
		if(selectionNum <= 0) {
			selectionNumEl.select(theKeys[0], true);
		} else if(selectionNum > 0 && selectionNum < theKeys.length) {
			selectionNumEl.select(theKeys[selectionNum], true);
		} else {
			selectionNumEl.select(theKeys[theKeys.length - 1], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		if(!blockedEdit) {
			uifactory.addFormSubmitButton("submit", buttonsCont);
		}
	}
	
	public void childNodeChanges() {
		int numOfItems = section.getItems().size();
		String[] theKeys = new String[numOfItems + 1];
		String[] theValues = new String[numOfItems + 1];
		theKeys[0] = "0";
		theValues[0] = translate("form.section.selection_all");
		for(int i=0; i<numOfItems; i++) {
			theKeys[i+1] = Integer.toString(i+1);
			theValues[i+1] = Integer.toString(i+1);
		}
		
		String selectedKey = selectionNumEl.isOneSelected() ? selectionNumEl.getSelectedKey() : null;
		selectionNumEl.setKeysAndValues(theKeys, theValues, null);
		//reselect the key
		if(selectedKey != null && shuffleEl.isOneSelected() && shuffleEl.isSelected(0)) {
			boolean found = false;
			for(String theKey:theKeys) {
				if(selectedKey.equals(theKey)) {
					found = true;
				}
			}
			
			if(found) {
				selectionNumEl.select(selectedKey, true);
			} else {
				selectionNumEl.select(theKeys[theKeys.length - 1], true);
			}
		}
	}

	protected void doDispose() {
		//		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
		String newTitle = titleEl.getValue();
		if (newTitle.trim().isEmpty()) { // Remove empty title to fix OLAT-2296
			newTitle = "";
		}
		
		String oldTitle = section.getTitle();
		boolean hasTitleChange = newTitle != null && !newTitle.equals(oldTitle);
		String newObjectives = objectivesEl.getValue();
		String oldObjectives = section.getObjectives();
		boolean hasObjectivesChange = newObjectives != null && !newObjectives.equals(oldObjectives);
		NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
		if (hasTitleChange) {
			nce.setNewTitle(newTitle);
		}
		if (hasObjectivesChange) {
			nce.setNewObjectives(newObjectives);
		}
		if (hasTitleChange || hasObjectivesChange) {
			// create a memento first
			nce.setSectionIdent(section.getIdent());
			fireEvent(ureq, nce);
			// then apply changes
			section.setTitle(newTitle);
			section.setObjectives(newObjectives);
		}

		if (!restrictedEdit && !blockedEdit) {
			String selectionNumStr = selectionNumEl.getSelectedKey();
			int selectionNum = 1;
			try {
				selectionNum = Integer.parseInt(selectionNumStr);
			} catch(NumberFormatException e) {
				logWarn("", e);
			}
			section.getSelection_ordering().setSelectionNumber(selectionNum);
			
			boolean randomType = shuffleEl.isOneSelected() && shuffleEl.isSelected(0);
			if(randomType) {
				section.getSelection_ordering().setOrderType(SelectionOrdering.RANDOM);
			} else {
				section.getSelection_ordering().setOrderType(SelectionOrdering.SEQUENTIAL);
			}
			
			boolean duration = limitTimeEl.isOneSelected() && limitTimeEl.isSelected(0);
			if (duration) {
				String durationMin = timeMinEl.getValue();
				String durationSec = timeSecEl.getValue();
				try {
					Integer.parseInt(durationMin);
					int sec = Integer.parseInt(durationSec);
					if (sec > 60) throw new NumberFormatException();
				} catch (NumberFormatException nfe) {
					durationMin = "0";
					durationSec = "0";							
					showWarning("error.duration");
				}
				Duration d = new Duration(durationMin, durationSec);
				section.setDuration(d);
				timeMinEl.setIntValue(d.getMin());
				timeSecEl.setIntValue(d.getSec());
			} else {
				section.setDuration(null);
			}
		}
		qtiPackage.serializeQTIDocument();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (limitTimeEl == source) {
			boolean enabled = limitTimeEl.isOneSelected() && limitTimeEl.isSelected(0);
			timeMinEl.setVisible(enabled);
			timeSecEl.setVisible(enabled);
		}
		super.formInnerEvent(ureq, source, event);
	}

	public void addTabs(TabbedPane tabbedPane) {
		tabbedPane.addTab(translate("tab.section"), getInitialComponent());
	}
}