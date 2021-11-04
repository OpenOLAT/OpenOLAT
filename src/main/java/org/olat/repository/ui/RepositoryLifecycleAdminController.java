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
package org.olat.repository.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntryLifeCycleValue;
import org.olat.repository.RepositoryEntryLifeCycleValue.RepositoryEntryLifeCycleUnit;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryLifecycleAdminController extends FormBasicController {
	
	private String[] onKeys = new String[]{ "on" };
	private static final String[] unitKeys = new String[]{
			RepositoryEntryLifeCycleUnit.day.name(), RepositoryEntryLifeCycleUnit.week.name(),
			RepositoryEntryLifeCycleUnit.month.name(), RepositoryEntryLifeCycleUnit.year.name()
		};
	
	private MultipleSelectionElement notificationEl;
	private MultipleSelectionElement toCloseEl;
	private MultipleSelectionElement toDeleteEl;
	private MultipleSelectionElement toDefinitivelyDeleteEl;
	private TextElement closeValueEl;
	private TextElement deleteValueEl;
	private TextElement definitivelyDeleteValueEl;
	private SingleSelection closeUnitEl;
	private SingleSelection deleteUnitEl;
	private SingleSelection definitivelyDeleteUnitEl;
	private FormLayoutContainer closeRuleCont;
	private FormLayoutContainer deleteRuleCont;
	private FormLayoutContainer definitivelyDeleteRuleCont;
	
	@Autowired
	private RepositoryModule repositoryModule;
	
	public RepositoryLifecycleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer lifecycleCont = FormLayoutContainer.createDefaultFormLayout("leave", getTranslator());
		lifecycleCont.setFormTitle(translate("repository.admin.lifecycle.title"));
		formLayout.add(lifecycleCont);
		lifecycleCont.setRootForm(mainForm);
		
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		String page = Util.getPackageVelocityRoot(this.getClass()) + "/date_rule.html";
		String[] unitValues = new String[] {
				translate(RepositoryEntryLifeCycleUnit.day.name()), translate(RepositoryEntryLifeCycleUnit.week.name()),
				translate(RepositoryEntryLifeCycleUnit.month.name()), translate(RepositoryEntryLifeCycleUnit.year.name())
			};
		
		initCloseForm(id, page, unitValues, lifecycleCont);
		initDeleteForm(id, page, unitValues, lifecycleCont);
		initDefinitivelyDeleteForm(id, page, unitValues, lifecycleCont);
		
		FormLayoutContainer notificationsCont = FormLayoutContainer.createDefaultFormLayout("notis", getTranslator());
		notificationsCont.setFormTitle(translate("repository.admin.lifecycle.notifications.title"));
		formLayout.add(notificationsCont);
		notificationsCont.setRootForm(mainForm);
		
		boolean notification = repositoryModule.isLifecycleNotificationByCloseDeleteEnabled();
		String[] notificationValues = new String[] { translate("repository.admin.lifecycle.notifications.enabled") };
		notificationEl = uifactory.addCheckboxesHorizontal("repository.admin.lifecycle.notifications", notificationsCont, onKeys, notificationValues);
		notificationEl.addActionListener(FormEvent.ONCHANGE);
		if(notification) {
			notificationEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		notificationsCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void initCloseForm(String id, String page, String[] unitValues, FormLayoutContainer lifecycleCont) {
		RepositoryEntryLifeCycleValue autoCloseValue = repositoryModule.getLifecycleAutoCloseValue();
		String[] toCloseValues = new String[] { translate("change.to.close.text") };
		toCloseEl = uifactory.addCheckboxesHorizontal("change.to.close", lifecycleCont, onKeys, toCloseValues);
		toCloseEl.addActionListener(FormEvent.ONCHANGE);
		if(autoCloseValue != null) {
			toCloseEl.select(onKeys[0], true);
		}
		
		closeRuleCont = FormLayoutContainer.createCustomFormLayout("close.".concat(id), lifecycleCont.getTranslator(), page);
		closeRuleCont.contextPut("ruleMsg", translate("after.course.end"));
		closeRuleCont.setLabel(null, null);
		closeRuleCont.setVisible(toCloseEl.isAtLeastSelected(1));
		closeRuleCont.contextPut("prefix", "clo");
		lifecycleCont.add(closeRuleCont);
		closeRuleCont.setRootForm(mainForm);
		
		String currentCloseValue = autoCloseValue == null ? null : Integer.toString(autoCloseValue.getValue());
		closeValueEl = uifactory.addTextElement("clo-value", null, 128, currentCloseValue, closeRuleCont);
		closeValueEl.setDomReplacementWrapperRequired(false);
		closeValueEl.setDisplaySize(3);

		closeUnitEl = uifactory.addDropdownSingleselect("clo-unit", null, closeRuleCont, unitKeys, unitValues, null);
		closeUnitEl.setDomReplacementWrapperRequired(false);
		selectUnitEl(closeUnitEl, autoCloseValue);
	}
	
	private void initDeleteForm(String id, String page, String[] unitValues, FormLayoutContainer lifecycleCont) {
		RepositoryEntryLifeCycleValue autoDeleteValue = repositoryModule.getLifecycleAutoDeleteValue();
		String[] toDeleteValues = new String[] { translate("change.to.delete.text") };
		toDeleteEl = uifactory.addCheckboxesHorizontal("change.to.delete", lifecycleCont, onKeys, toDeleteValues);
		toDeleteEl.addActionListener(FormEvent.ONCHANGE);
		if(autoDeleteValue != null) {
			toDeleteEl.select(onKeys[0], true);
		}
		
		deleteRuleCont = FormLayoutContainer.createCustomFormLayout("delete.".concat(id), lifecycleCont.getTranslator(), page);
		deleteRuleCont.contextPut("ruleMsg", translate("after.course.end"));
		deleteRuleCont.setLabel(null, null);
		deleteRuleCont.setVisible(toDeleteEl.isAtLeastSelected(1));
		deleteRuleCont.contextPut("prefix", "del");
		lifecycleCont.add(deleteRuleCont);
		deleteRuleCont.setRootForm(mainForm);
		
		String currentDeleteValue = autoDeleteValue == null ? null : Integer.toString(autoDeleteValue.getValue());
		deleteValueEl = uifactory.addTextElement("del-value", null, 128, currentDeleteValue, deleteRuleCont);
		deleteValueEl.setDomReplacementWrapperRequired(false);
		deleteValueEl.setDisplaySize(3);

		deleteUnitEl = uifactory.addDropdownSingleselect("del-unit", null, deleteRuleCont, unitKeys, unitValues, null);
		deleteUnitEl.setDomReplacementWrapperRequired(false);
		selectUnitEl(deleteUnitEl, autoDeleteValue);
	}
	
	private void initDefinitivelyDeleteForm(String id, String page, String[] unitValues, FormLayoutContainer lifecycleCont) {
		RepositoryEntryLifeCycleValue autoDefinitivelyDeleteValue = repositoryModule.getLifecycleAutoDefinitivelyDeleteValue();
		String[] toDeleteValues = new String[] { translate("change.to.delete.definitively.text") };
		toDefinitivelyDeleteEl = uifactory.addCheckboxesHorizontal("change.to.delete.definitively", lifecycleCont, onKeys, toDeleteValues);
		toDefinitivelyDeleteEl.addActionListener(FormEvent.ONCHANGE);
		if(autoDefinitivelyDeleteValue != null) {
			toDefinitivelyDeleteEl.select(onKeys[0], true);
		}
		
		definitivelyDeleteRuleCont = FormLayoutContainer.createCustomFormLayout("def-delete.".concat(id), lifecycleCont.getTranslator(), page);
		definitivelyDeleteRuleCont.contextPut("ruleMsg", translate("after.course.deletion"));
		definitivelyDeleteRuleCont.setLabel(null, null);
		definitivelyDeleteRuleCont.setVisible(toDefinitivelyDeleteEl.isAtLeastSelected(1));
		definitivelyDeleteRuleCont.contextPut("prefix", "def-del");
		lifecycleCont.add(definitivelyDeleteRuleCont);
		definitivelyDeleteRuleCont.setRootForm(mainForm);
		
		String currentDeleteValue = autoDefinitivelyDeleteValue == null ? null : Integer.toString(autoDefinitivelyDeleteValue.getValue());
		definitivelyDeleteValueEl = uifactory.addTextElement("def-del-value", null, 128, currentDeleteValue, definitivelyDeleteRuleCont);
		definitivelyDeleteValueEl.setDomReplacementWrapperRequired(false);
		definitivelyDeleteValueEl.setDisplaySize(3);

		definitivelyDeleteUnitEl = uifactory.addDropdownSingleselect("def-del-unit", null, definitivelyDeleteRuleCont, unitKeys, unitValues, null);
		definitivelyDeleteUnitEl.setDomReplacementWrapperRequired(false);
		selectUnitEl(definitivelyDeleteUnitEl, autoDefinitivelyDeleteValue);
	}
	
	private void selectUnitEl(SingleSelection unitEl, RepositoryEntryLifeCycleValue currentUnit) {
		boolean selected = false;
		if(currentUnit != null && currentUnit.getUnit() != null) {
			String unit = currentUnit.getUnit().name();
			for(String unitKey:unitKeys) {
				if(unit.equals(unitKey)) {
					unitEl.select(unitKey, true);
					selected = true;
				}
			}
		}
		if(!selected) {
			unitEl.select(unitKeys[1], true);	
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateFormLogic(toCloseEl, closeValueEl, closeUnitEl);
		allOk &= validateFormLogic(toDeleteEl, deleteValueEl, deleteUnitEl);
		allOk &= validateFormLogic(toDefinitivelyDeleteEl, definitivelyDeleteValueEl, definitivelyDeleteUnitEl);
		
		RepositoryEntryLifeCycleValue autoClose = getValue(toCloseEl, closeValueEl, closeUnitEl);
		RepositoryEntryLifeCycleValue autoDelete = getValue(toDeleteEl, deleteValueEl, deleteUnitEl);
		if(autoDelete != null && autoClose != null && autoDelete.compareTo(autoClose) <= 0) {
			deleteValueEl.setErrorKey("error.lifecycle.after", null);
			allOk &= false;
		}

		return allOk;
	}
	
	protected boolean validateFormLogic(MultipleSelectionElement enableEl, TextElement textEl, SingleSelection unitEl) {
		boolean allOk = true;
		enableEl.clearError();
		textEl.clearError();
		unitEl.clearError();
		
		if(enableEl.isAtLeastSelected(1)) {
			String value = textEl.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Integer.parseInt(value);
				} catch (NumberFormatException e) {
					textEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} else {
				textEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			if(!unitEl.isOneSelected()) {
				textEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(toCloseEl == source) {
			closeRuleCont.setVisible(toCloseEl.isAtLeastSelected(1));
		} else if(toDeleteEl == source) {
			deleteRuleCont.setVisible(toDeleteEl.isAtLeastSelected(1));
		} else if(toDefinitivelyDeleteEl == source) {
			definitivelyDeleteRuleCont.setVisible(toDefinitivelyDeleteEl.isAtLeastSelected(1));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String autoClose = getStringValue(toCloseEl, closeValueEl, closeUnitEl);
		repositoryModule.setLifecycleAutoClose(autoClose);
		String autoDelete = getStringValue(toDeleteEl, deleteValueEl, deleteUnitEl);
		repositoryModule.setLifecycleAutoDelete(autoDelete);
		String autoDefinitivelyDelete = getStringValue(toDefinitivelyDeleteEl, definitivelyDeleteValueEl, definitivelyDeleteUnitEl);
		repositoryModule.setLifecycleAutoDefinitivelyDelete(autoDefinitivelyDelete);
		boolean notification = notificationEl.isAtLeastSelected(1);
		repositoryModule.setLifecycleNotificationByCloseDeleteEnabled(notification);
	}
	
	private String getStringValue(MultipleSelectionElement enableEl, TextElement textEl, SingleSelection unitEl) {
		RepositoryEntryLifeCycleValue val = getValue(enableEl, textEl, unitEl);
		return val == null ? "" : val.toString();
	}
	
	private RepositoryEntryLifeCycleValue getValue(MultipleSelectionElement enableEl, TextElement textEl, SingleSelection unitEl) {
		if(enableEl.isAtLeastSelected(1)) {
			try {
				String value = textEl.getValue();
				String unit = unitEl.getSelectedKey();
				return new RepositoryEntryLifeCycleValue(Integer.parseInt(value), RepositoryEntryLifeCycleUnit.valueOf(unit));
			} catch (NumberFormatException e) {
				logError("", e);
			}
		}
		return null;
	}
}