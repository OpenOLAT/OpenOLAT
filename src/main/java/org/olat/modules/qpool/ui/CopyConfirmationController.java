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
package org.olat.modules.qpool.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.ui.events.QItemsProcessedEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CopyConfirmationController extends FormBasicController {
	
	private static final String ADD_TO_SOURCE_KEY = "add.to.source.key";
	private static final String[] addToItemsSourceKeys = new String[] { ADD_TO_SOURCE_KEY };
	private static final String[] editableKeys = new String[] { "editable" };
	
	private MultipleSelectionElement addToItemsSourceEl;
	private MultipleSelectionElement editableEl;
	
	private final List<QuestionItemShort> itemsToCopy;
	private final QuestionItemsSource itemsSource;
	
	public CopyConfirmationController(UserRequest ureq, WindowControl wControl, List<QuestionItemShort> itemsToCopy,
			QuestionItemsSource itemsSource) {
		super(ureq, wControl);
		this.itemsToCopy = itemsToCopy;
		this.itemsSource = itemsSource;
		initForm(ureq);
	}
	
	@Autowired
	private QPoolService qpoolService;

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("confirm.copy.message");
		
		uifactory.addStaticTextElement("confirm.copy.questions", getQuestionNames(), formLayout);
		
		String[] addToItemsSourceValues = new String[] { itemsSource.getAskToSourceText(getTranslator()) };
		addToItemsSourceEl = uifactory.addCheckboxesHorizontal("confirm.copy.add.to.source", "", formLayout, addToItemsSourceKeys, addToItemsSourceValues);
		addToItemsSourceEl.addActionListener(FormEvent.ONCHANGE);
		addToItemsSourceEl.select(ADD_TO_SOURCE_KEY, itemsSource.askAddToSourceDefault());
		
		String[] editableValues = new String[] { translate("confirm.copy.editable") };
		editableEl = uifactory.addCheckboxesHorizontal("confirm.copy.editable", "", formLayout, editableKeys, editableValues);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("confirm.copy.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateUI();
	}
	
	private void updateUI() {
		boolean showAddToSource = itemsSource.askAddToSource();
		addToItemsSourceEl.setVisible(showAddToSource);
		boolean showEditable = itemsSource.askEditable() && addToItemsSourceEl.isAtLeastSelected(1);
		editableEl.setVisible(showEditable);
	}

	private String getQuestionNames() {
		return itemsToCopy.stream()
				.map(item -> item.getTitle())
				.collect(Collectors.joining(", "));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addToItemsSourceEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<QuestionItem> copyItems = qpoolService.copyItems(getIdentity(), itemsToCopy);
		addToItemsSource(copyItems);
		logAudit(copyItems);
		fireEvent(ureq, new QItemsProcessedEvent(copyItems, itemsToCopy.size()));
	}

	private void addToItemsSource(List<QuestionItem> items) {
		boolean addToItemsSource = addToItemsSourceEl.isAtLeastSelected(1);
		boolean editable = editableEl.isAtLeastSelected(1);
		if (addToItemsSource) {
			itemsSource.addToSource(items, editable);
		}
	}

	private void logAudit(List<QuestionItem> items) {
		for (QuestionItem item: items) {
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.CREATE_QUESTION_ITEM_BY_COPY);
			builder.withAfter(item);
			qpoolService.persist(builder.create());
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
