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
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
 * Initial date: 2 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConversionConfirmationController extends FormBasicController {

	private static final String ADD_TO_SOURCE_KEY = "add.to.source.key";
	private static final String[] addToItemsSourceKeys = new String[] { ADD_TO_SOURCE_KEY };
	private static final String[] editableKeys = new String[] { "editable" };
	
	private SingleSelection formatEl;
	private FormLayoutContainer exampleHelpEl;
	private StaticTextElement questionsEl;
	private MultipleSelectionElement addToItemsSourceEl;
	private MultipleSelectionElement editableEl;
	
	private final Map<String,List<QuestionItemShort>> formatToItems;
	private final QuestionItemsSource itemsSource;
	
	@Autowired
	private QPoolService qpoolService;
	
	public ConversionConfirmationController(UserRequest ureq, WindowControl wControl,
			Map<String,List<QuestionItemShort>> formatToItems, QuestionItemsSource itemsSource) {
		super(ureq, wControl);
		this.formatToItems = formatToItems;
		this.itemsSource = itemsSource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] formatKeys = formatToItems.keySet().toArray(new String[formatToItems.size()]);

		formatEl = uifactory.addDropdownSingleselect("format", "convert.format", formLayout, formatKeys, formatKeys, null);
		formatEl.addActionListener(FormEvent.ONCHANGE);
		if(formatKeys.length > 0) {
			formatEl.select(formatKeys[0], true);
		}
		
		// only info about QTI 1.2 to 2.1 as it's the only option for now
		String page = velocity_root + "/example_conversion.html";
		exampleHelpEl = FormLayoutContainer.createCustomFormLayout("example.help", "example.help", getTranslator(), page);
		formLayout.add(exampleHelpEl);
		
		questionsEl = uifactory.addStaticTextElement("convert.questions", "", formLayout);
		
		String[] addToItemsSourceValues = new String[] { itemsSource.getAskToSourceText(getTranslator()) };
		addToItemsSourceEl = uifactory.addCheckboxesHorizontal("convert.add.to.source", "", formLayout, addToItemsSourceKeys, addToItemsSourceValues);
		addToItemsSourceEl.addActionListener(FormEvent.ONCHANGE);
		addToItemsSourceEl.select(ADD_TO_SOURCE_KEY, itemsSource.askAddToSourceDefault());
		
		String[] editableValues = new String[] { translate("convert.editable") };
		editableEl = uifactory.addCheckboxesHorizontal("convert.editable", "", formLayout, editableKeys, editableValues);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("convert.item", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		updateInfos();
		updateUI();
	}
	
	private void updateInfos() {
		if(formatEl.isOneSelected()) {
			String format = formatEl.getSelectedKey();
			List<QuestionItemShort> items = formatToItems.get(format);
			setFormInfo("convert.item.msg", new String[]{ Integer.toString(items.size()) });
			questionsEl.setValue(getQuestionNames(items));
		}
	}

	private String getQuestionNames(List<QuestionItemShort> items) {
		return items.stream()
				.map(QuestionItemShort::getTitle)
				.collect(Collectors.joining(", "));
	}
	
	private void updateUI() {
		boolean showAddToSource = itemsSource.askAddToSource();
		addToItemsSourceEl.setVisible(showAddToSource);
		boolean showEditable = itemsSource.askEditable() && addToItemsSourceEl.isAtLeastSelected(1);
		editableEl.setVisible(showEditable);
	}
	
	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		formatEl.clearError();
		if(!formatEl.isOneSelected()) {
			formatEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(formatEl == source) {
			updateInfos();
		} else if(addToItemsSourceEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String format = formatEl.isOneSelected() ? formatEl.getSelectedKey() : null;
		List<QuestionItemShort> itemsToConvert = formatToItems.get(format);
		List<QuestionItem> convertedItems = qpoolService.convertItems(getIdentity(), itemsToConvert, format,
				getLocale());
		addToItemsSource(convertedItems);
		logAudit(convertedItems);
		fireEvent(ureq, new QItemsProcessedEvent(convertedItems, itemsToConvert.size(),
				itemsToConvert.size() - convertedItems.size()));
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
					Action.CREATE_QUESTION_ITEM_BY_CONVERSION);
			builder.withAfter(item);
			qpoolService.persist(builder.create());
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}