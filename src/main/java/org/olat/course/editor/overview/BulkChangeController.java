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
package org.olat.course.editor.overview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.editor.EditorMainController;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 21 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BulkChangeController extends FormBasicController {
	
	private static final String[] EMPTY_VALUES = new String[]{ "" };
	
	private static final String[] displayOptionsKeys = new String[]{
			CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT,
			CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT,
			CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT,
			CourseNode.DISPLAY_OPTS_TITLE_CONTENT,
			CourseNode.DISPLAY_OPTS_CONTENT};
	
	private SingleSelection displayEl;
	
	private Map<MultipleSelectionElement, FormLayoutContainer> checkboxContainer = new HashMap<>();
	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>();
	
	private final List<CourseNode> courseNodes;

	public BulkChangeController(UserRequest ureq, WindowControl wControl, List<CourseNode> courseNodes) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		this.courseNodes = courseNodes;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initGeneralForm(formLayout);

		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("global", getTranslator());
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add("buttonsWrapper", buttonsWrapperCont);
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		buttonsWrapperCont.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void initGeneralForm(FormItemContainer formLayout) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		String[] values = new String[]{
				translate("nodeConfigForm.short_title_desc_content"),
				translate("nodeConfigForm.title_desc_content"),
				translate("nodeConfigForm.short_title_content"),
				translate("nodeConfigForm.title_content"),
				translate("nodeConfigForm.content_only")};
		displayEl = uifactory.addDropdownSingleselect("nodeConfigForm.display_options", generalCont, displayOptionsKeys,
				values, null);
		decorate(displayEl, generalCont);
		
	}
	
	private FormItem decorate(FormItem item, FormLayoutContainer formLayout) {
		String itemName = item.getName();
		MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("cbx_" + itemName, itemName, formLayout,
				new String[] { itemName }, EMPTY_VALUES);
		checkbox.select(itemName, false);
		checkbox.addActionListener(FormEvent.ONCLICK);
		checkbox.setUserObject(item);
		checkboxSwitch.add(checkbox);

		item.setLabel(null, null);
		item.setVisible(false);
		item.setUserObject(checkbox);
		
		checkboxContainer.put(checkbox, formLayout);
		formLayout.moveBefore(checkbox, item);
		return checkbox;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			checkboxContainer.get(checkbox).setDirty(true);
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (CourseNode courseNode : courseNodes) {
			formOKGeneral(courseNode);
		}
//		for(QuestionItemShort item : items) {
//			QuestionItem fullItem = qpoolService.loadItemById(item.getKey());
//			if(fullItem instanceof QuestionItemImpl) {
//				QuestionItemImpl itemImpl = (QuestionItemImpl)fullItem;
//				QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
//						Action.UPDATE_QUESTION_ITEM_METADATA);
//				builder.withBefore(itemImpl);
//				
//				formOKGeneral(itemImpl);
//				formOKQuestion(itemImpl);
//				formOKTechnical(itemImpl);
//				formOKRights(itemImpl);
//				QuestionItem merged = qpoolService.updateItem(itemImpl);
//				builder.withAfter(itemImpl);
//				qpoolService.persist(builder.create());
//				updatedItems.add(merged);
//			}
//		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void formOKGeneral(CourseNode courseNode) {
		if(isEnabled(displayEl)) {
			String displayOption = displayEl.getSelectedKey();
			courseNode.setDisplayOption(displayOption);
		}
	}
	
	private boolean isEnabled(FormItem item) {
		if (item == null) return false;
		
		return ((MultipleSelectionElement)item.getUserObject()).isAtLeastSelected(1);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
