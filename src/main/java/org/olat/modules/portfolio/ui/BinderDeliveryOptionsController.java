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
package org.olat.modules.portfolio.ui;

import java.util.List;

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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderDeliveryOptions;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderDeliveryOptionsController extends FormBasicController implements Activateable2 {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private MultipleSelectionElement templatesEl;
	private MultipleSelectionElement newEntriesEl;
	private MultipleSelectionElement deleteBinderEl;
	private MultipleSelectionElement mandatoryTemplatesPageEl;
	
	private CloseableModalController deleteOptionCmcCtrl;
	private ConfirmDeleteOptionController deleteOptionCtrl;
	
	private final Binder binder;
	private final boolean readOnly;
	private final BinderDeliveryOptions deliveryOptions;
	
	@Autowired
	private PortfolioService portfolioService;
	
	 
	public BinderDeliveryOptionsController(UserRequest ureq, WindowControl wControl, Binder binder, boolean readOnly) {
		super(ureq, wControl);
		
		this.binder = binder;
		this.readOnly = readOnly;
		deliveryOptions = portfolioService.getDeliveryOptions(binder.getOlatResource());
		 
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/portfolio/Portfolio_template_Administration_and_editing/");
		setFormTitle("portfolio.template.options.title");
		
		newEntriesEl = uifactory.addCheckboxesHorizontal("canAddEntries", "allow.new.entries", formLayout, onKeys, onValues);
		newEntriesEl.setEnabled(!readOnly);
		if(deliveryOptions.isAllowNewEntries()) {
			newEntriesEl.select(onKeys[0], true);
		}
		
		deleteBinderEl = uifactory.addCheckboxesHorizontal("canDeleteBinder", "allow.delete.binder", formLayout, onKeys, onValues);
		deleteBinderEl.addActionListener(FormEvent.ONCHANGE);
		deleteBinderEl.setEnabled(!readOnly);
		if(deliveryOptions.isAllowDeleteBinder()) {
			deleteBinderEl.select(onKeys[0], true);
		}
		
		templatesEl = uifactory.addCheckboxesHorizontal("canTemplates", "allow.templates.folder", formLayout, onKeys, onValues);
		templatesEl.addActionListener(FormEvent.ONCHANGE);
		templatesEl.setEnabled(!readOnly);
		if(deliveryOptions.isAllowTemplatesFolder()) {
			templatesEl.select(onKeys[0], true);
		}
		
		mandatoryTemplatesPageEl = uifactory.addCheckboxesHorizontal("mandatoryTemplates", "allow.templates.mandatory", formLayout, onKeys, onValues);
		mandatoryTemplatesPageEl.setVisible(templatesEl.isAtLeastSelected(1));
		mandatoryTemplatesPageEl.setEnabled(templatesEl.isAtLeastSelected(1) && !readOnly);
		if(!deliveryOptions.isOptionalTemplateForEntry()) {
			mandatoryTemplatesPageEl.select(onKeys[0], true);
		}
		
		if(!readOnly) {
			FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonsLayout.setRootForm(mainForm);
			formLayout.add(buttonsLayout);
			uifactory.addFormSubmitButton("save", buttonsLayout);
			uifactory.addFormCancelButton("cancel", buttonsLayout, ureq, getWindowControl());
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == deleteBinderEl) {
			if(deleteBinderEl.isAtLeastSelected(1)) {
				doConfirmDeleteOption(ureq);
			}
		} else if(source == templatesEl) {
			mandatoryTemplatesPageEl.setVisible(templatesEl.isAtLeastSelected(1));
			mandatoryTemplatesPageEl.setEnabled(templatesEl.isAtLeastSelected(1) && !readOnly);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean allowNewEntries = newEntriesEl.isAtLeastSelected(1);
		deliveryOptions.setAllowNewEntries(allowNewEntries);
		boolean allowDeleteBinder = deleteBinderEl.isAtLeastSelected(1);
		deliveryOptions.setAllowDeleteBinder(allowDeleteBinder);
		boolean allowTemplatesFolder = templatesEl.isAtLeastSelected(1);
		deliveryOptions.setAllowTemplatesFolder(allowTemplatesFolder);
		boolean mandatoryTemplates = mandatoryTemplatesPageEl.isAtLeastSelected(1);
		deliveryOptions.setOptionalTemplateForEntry(!mandatoryTemplates);
		portfolioService.setDeliveryOptions(binder.getOlatResource(), deliveryOptions);
		fireEvent(ureq, new ReloadSettingsEvent());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(deleteOptionCtrl == source) {
			if(event != Event.DONE_EVENT) {
				deleteBinderEl.uncheckAll();
			}
			deleteOptionCmcCtrl.deactivate();
			cleanUp();
		} else if(deleteOptionCmcCtrl == source) {
			deleteBinderEl.uncheckAll();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteOptionCmcCtrl);
		removeAsListenerAndDispose(deleteOptionCtrl);
		deleteOptionCmcCtrl = null;
		deleteOptionCtrl = null;
	}

	private void doConfirmDeleteOption(UserRequest ureq) {
		if(guardModalController(deleteOptionCtrl)) return;
		
		deleteOptionCtrl = new ConfirmDeleteOptionController(ureq, getWindowControl());
		listenTo(deleteOptionCtrl);
		
		String title = translate("confirmation");
		deleteOptionCmcCtrl = new CloseableModalController(getWindowControl(), null, deleteOptionCtrl.getInitialComponent(), true, title, true);
		listenTo(deleteOptionCmcCtrl);
		deleteOptionCmcCtrl.activate();
	}
	
	private class ConfirmDeleteOptionController extends FormBasicController {
		
		private MultipleSelectionElement acknowledgeEl;
		
		public ConfirmDeleteOptionController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "confirm_delete_option");
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			String[] values = new String[] { translate("allow.delete.binder.warning")	 };
			acknowledgeEl = uifactory.addCheckboxesHorizontal("acknowledge", "confirmation", formLayout, onKeys, values);
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
			uifactory.addFormSubmitButton("delete", "ok", formLayout);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			acknowledgeEl.clearError();
			if(!acknowledgeEl.isAtLeastSelected(1)) {
				acknowledgeEl.setErrorKey("form.mandatory.hover", null);
				allOk &= false;
			}
			
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.DONE_EVENT);
		}

		@Override
		protected void formCancelled(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
}
