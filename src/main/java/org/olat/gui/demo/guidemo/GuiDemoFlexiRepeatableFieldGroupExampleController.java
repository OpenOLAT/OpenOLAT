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
package org.olat.gui.demo.guidemo;

import static org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupColumnWidth.content;
import static org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupColumnWidth.full;
import static org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupColumnWidth.m;
import static org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupColumnWidth.s;
import static org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupColumnWidth.xs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupColumnWidth;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupController;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupRow;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;

/**
 * One example of the repeatable field group in the GUI demo.
 *
 * Initial date: 9 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class GuiDemoFlexiRepeatableFieldGroupExampleController extends FormBasicController
		implements RepeatableFieldGroupDelegate<GuiDemoFlexiRepeatableFieldGroupExampleController.DemoRow> {
	
	private static final String[] ROLE_KEYS = { "author", "coach", "participant" };
	
	private FormLink openModalLink;
	private RepeatableFieldGroupController<DemoRow> groupCtrl;
	
	private CloseableModalController cmc;
	private GuiDemoFlexiRepeatableFieldGroupExampleController modalCtrl;
	
	private final Example example;
	
	public GuiDemoFlexiRepeatableFieldGroupExampleController(UserRequest ureq, WindowControl wControl, Example example) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.example = example;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("guidemo.rfg." + example.name() + ".desc");
		if(example == Example.modal) {
			openModalLink = uifactory.addFormLink("guidemo.rfg.modal.open", formLayout, Link.BUTTON);
			return;
		}
		
		groupCtrl = new RepeatableFieldGroupController<>(ureq, getWindowControl(), mainForm, getTranslator(),
				translate(example.objectI18nKey), translate(example.addI18nKey), this);
		listenTo(groupCtrl);
		if(example == Example.narrow) {
			FormLayoutContainer narrowCont = FormLayoutContainer.createCustomFormLayout("narrow", getTranslator(), velocity_root + "/guidemo-rfg-narrow.html");
			formLayout.add(narrowCont);
			narrowCont.add("group", groupCtrl.getInitialFormItem());
		} else {
			formLayout.add("group", groupCtrl.getInitialFormItem());
		}
		
		Date today = DateUtils.setTime(ureq.getRequestTimestamp(), 0, 0, 0);
		for(int i = 0; i < example.initialRows; i++) {
			initRow(groupCtrl.addRow(), i, today);
		}
		groupCtrl.setMinRows(example.minRows);
		
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	private void initRow(DemoRow row, int index, Date today) {
		for(FormItem item:row.getFormItems()) {
			if(item instanceof DateChooser dateEl && dateEl.isTimeOnly()) {
				dateEl.setDate(DateUtils.setTime(today, item.getName().endsWith("_start") ? 9 : 12, 0, 0));
			} else if(item instanceof DateChooser dateEl) {
				Date start = DateUtils.addDays(today, index);
				if(dateEl.isDateChooserTimeEnabled()) {
					start = DateUtils.setTime(start, 9, 0, 0);
				}
				dateEl.setDate(start);
				if(dateEl.isSecondDate()) {
					dateEl.setSecondDate(dateEl.isSameDay() ? DateUtils.setTime(start, 12, 0, 0) : DateUtils.addDays(start, 2));
				}
			} else if(item instanceof TextElement textEl && textEl.isMandatory() && example != Example.contacts) {
				textEl.setValue(translate("guidemo.rfg.value1") + " " + (index + 1));
			}
		}
	}
	
	@Override
	public DemoRow createRow(String id, FormItemContainer container) {
		List<FormItem> items = new ArrayList<>();
		switch(example) {
			case basic -> {
				items.add(addDate(id + "_date", "guidemo.rfg.date", container));
				items.add(addText(id + "_value1", "guidemo.rfg.label1", true, container));
				items.add(addText(id + "_value2", "guidemo.rfg.label2", false, container));
			}
			case appointments, narrow -> {
				items.add(addDate(id + "_date", "guidemo.rfg.date", container));
				items.add(addTime(id + "_start", "guidemo.rfg.start", container));
				items.add(addTime(id + "_end", "guidemo.rfg.end", container));
				items.add(addText(id + "_location", "guidemo.rfg.location", false, container));
				items.add(uifactory.addTextAreaElement(id + "_description", "guidemo.rfg.description", 1000, 2, -1, true, false, null, container));
			}
			case dates -> {
				items.add(addDateVariant(id + "_date", "guidemo.rfg.date", false, false, false, container));
				items.add(addDateVariant(id + "_range", "guidemo.rfg.date.range", true, false, false, container));
				items.add(addDateVariant(id + "_datetime", "guidemo.rfg.date.time", false, true, false, container));
				items.add(addDateVariant(id + "_datetimerange", "guidemo.rfg.date.time.range", true, true, false, container));
				items.add(addDateVariant(id + "_sameday", "guidemo.rfg.date.same.day", true, true, true, container));
				items.add(addTime(id + "_time", "guidemo.rfg.time", container));
			}
			case contacts -> {
				items.add(addText(id + "_firstname", "guidemo.rfg.firstname", true, container));
				items.add(addText(id + "_lastname", "guidemo.rfg.lastname", true, container));
				items.add(addText(id + "_email", "guidemo.rfg.email", true, container));
				items.add(addText(id + "_phone", "guidemo.rfg.phone", false, container));
				String[] roleValues = new String[ROLE_KEYS.length];
				for(int i = 0; i < ROLE_KEYS.length; i++) {
					roleValues[i] = translate("guidemo.rfg.role." + ROLE_KEYS[i]);
				}
				items.add(uifactory.addDropdownSingleselect(id + "_role", "guidemo.rfg.role", container, ROLE_KEYS, roleValues));
			}
			case numbered -> {
				items.add(addText(id + "_text", "guidemo.rfg.text", true, container));
				items.add(uifactory.addIntegerElement(id + "_number", "guidemo.rfg.number", 1, container));
			}
			case modal -> {
				//
			}
		}
		return new DemoRow(items);
	}
	
	private DateChooser addDate(String name, String i18nLabel, FormItemContainer container) {
		DateChooser dateEl = uifactory.addDateChooser(name, i18nLabel, null, container);
		dateEl.setMandatory(true);
		return dateEl;
	}
	
	private DateChooser addDateVariant(String name, String i18nLabel, boolean secondDate, boolean time, boolean sameDay, FormItemContainer container) {
		DateChooser dateEl = uifactory.addDateChooser(name, i18nLabel, null, container);
		dateEl.setSecondDate(secondDate);
		dateEl.setSeparator("guidemo.rfg.to");
		dateEl.setDateChooserTimeEnabled(time);
		dateEl.setSameDay(sameDay);
		return dateEl;
	}
	
	private DateChooser addTime(String name, String i18nLabel, FormItemContainer container) {
		DateChooser timeEl = addDate(name, i18nLabel, container);
		timeEl.setTimeOnly(true);
		return timeEl;
	}
	
	private TextElement addText(String name, String i18nLabel, boolean mandatory, FormItemContainer container) {
		TextElement textEl = uifactory.addTextElement(name, i18nLabel, 100, "", container);
		textEl.setMandatory(mandatory);
		return textEl;
	}
	
	@Override
	public void copyRow(DemoRow source, DemoRow target) {
		for(int i = 0; i < source.getFormItems().size(); i++) {
			FormItem sourceItem = source.getFormItems().get(i);
			FormItem targetItem = target.getFormItems().get(i);
			if(sourceItem instanceof DateChooser sourceEl && targetItem instanceof DateChooser targetEl) {
				targetEl.setDate(sourceEl.getDate());
				targetEl.setSecondDate(sourceEl.getSecondDate());
			} else if(sourceItem instanceof TextElement sourceEl && targetItem instanceof TextElement targetEl) {
				targetEl.setValue(sourceEl.getValue());
			} else if(sourceItem instanceof SingleSelection sourceEl && targetItem instanceof SingleSelection targetEl && sourceEl.isOneSelected()) {
				targetEl.select(sourceEl.getSelectedKey(), true);
			}
		}
	}
	
	@Override
	public String getColumnCssClass(int column) {
		return example.widths[column].getCssClass();
	}
	
	@Override
	public boolean validateRow(DemoRow row) {
		boolean allOk = true;
		for(FormItem item:row.getFormItems()) {
			if(!item.isVisible() || item.hasError()) {
				continue;
			}
			
			if(item instanceof DateChooser dateEl && dateEl.isMandatory() && dateEl.getDate() == null) {
				dateEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			} else if(item instanceof TextElement textEl && !(item instanceof DateChooser)) {
				if(textEl.isMandatory() && !StringHelper.containsNonWhitespace(textEl.getValue())) {
					textEl.setErrorKey("form.legende.mandatory");
					allOk = false;
				} else if(textEl.getName().endsWith("_email") && !MailHelper.isValidEmailAddress(textEl.getValue())) {
					textEl.setErrorKey("guidemo.rfg.email.invalid");
					allOk = false;
				}
			}
		}
		return allOk;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(modalCtrl);
		removeAsListenerAndDispose(cmc);
		modalCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == openModalLink) {
			doOpenModal(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		showInfo("guidemo.rfg.saved", Integer.toString(groupCtrl.getRows().size()));
	}
	
	private void doOpenModal(UserRequest ureq) {
		modalCtrl = new GuiDemoFlexiRepeatableFieldGroupExampleController(ureq, getWindowControl(), Example.appointments);
		listenTo(modalCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), modalCtrl.getInitialComponent(),
				translate("guidemo.rfg.modal.tab"));
		listenTo(cmc);
		cmc.activate();
	}
	
	public enum Example {
		basic(3, 1, "guidemo.rfg.object", "guidemo.rfg.add", content, m, m),
		appointments(2, 1, "guidemo.rfg.object", "guidemo.rfg.add", content, content, content, m, full),
		dates(2, 1, "guidemo.rfg.entry", "guidemo.rfg.entry.add", content, content, content, content, content, content),
		contacts(1, 1, "guidemo.rfg.contact", "guidemo.rfg.contact.add", m, m, m, s, m),
		numbered(7, 2, "guidemo.rfg.entry", "guidemo.rfg.entry.add", m, xs),
		narrow(2, 1, "guidemo.rfg.object", "guidemo.rfg.add", content, content, content, m, full),
		modal(0, 0, null, null);
		
		private final int initialRows;
		private final int minRows;
		private final String objectI18nKey;
		private final String addI18nKey;
		private final RepeatableFieldGroupColumnWidth[] widths;
		
		private Example(int initialRows, int minRows, String objectI18nKey, String addI18nKey, RepeatableFieldGroupColumnWidth... widths) {
			this.initialRows = initialRows;
			this.minRows = minRows;
			this.objectI18nKey = objectI18nKey;
			this.addI18nKey = addI18nKey;
			this.widths = widths;
		}
	}
	
	public record DemoRow(List<FormItem> items) implements RepeatableFieldGroupRow {
		
		@Override
		public List<FormItem> getFormItems() {
			return items;
		}
	}
	
}
