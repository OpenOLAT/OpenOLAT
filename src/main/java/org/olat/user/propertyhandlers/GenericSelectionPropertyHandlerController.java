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
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.user.propertyhandlers.ui.UsrPropHandlerCfgController;


/**
 * 
 * This is the Config-Controller for the GenericSelectionPropertyHandler. It
 * provides a GUI for the Handler-Configuration
 * 
 * <P>
 * Initial Date: 30.08.2011 <br>
 * 
 * @author strentini
 */
public class GenericSelectionPropertyHandlerController extends FormBasicController implements UsrPropHandlerCfgController {

	private static final String OPTFIELD_PREFIX = "optfield.";
	private static final String OPTFIELD_TRSLBL_PREFIX = "optfield.trslbl.";
	private static final String OPTFIELD_TRS_PREFIX = "optfield.trs.";
	private static final String OPTFIELD_RMV_PREFIX = "optfield.rmv.";

	private FormLayoutContainer hcFlc;
	private SingleSelection modeRadio;
	private FormLink bttAdd;
	private StaticTextElement txtError;

	private List<String> optionFieldNames;

	// list of i18n keys to remove from the overlay, after form submit
	private List<String> i18nKeysToDelete;

	private CloseableModalController cmc;
	private SingleKeyTranslatorController singleKeyTrsCtrl;

	// the handler this controller actually configures
	private GenericSelectionPropertyHandler handler;

	public GenericSelectionPropertyHandlerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		initForm(ureq);
	}

	@Override
	public void setHandlerToConfigure(UserPropertyHandler handler) {
		this.handler = (GenericSelectionPropertyHandler) handler;

		if (this.handler.isMultiSelect()) {
			modeRadio.select(modeRadio.getKey(1), true);
		} else {
			modeRadio.select(modeRadio.getKey(0), true);
		}

		// the option-fields
		for (String key : this.handler.getSelectionKeys()) {
			addOptionField(key);
		}

		hcFlc.contextPut("optionfields", optionFieldNames);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		optionFieldNames = new ArrayList<>();
		i18nKeysToDelete = new ArrayList<>();

		hcFlc = FormLayoutContainer.createCustomFormLayout("hcForm", getTranslator(), velocity_root
				+ "/genericSelectionPropertyController.html");
		formLayout.add(hcFlc);

		// the radio
		String[] keys = { "gsphc.issingle", "gsphc.ismulti" };
		String[] values = { translate(keys[0]), translate(keys[1]) };
		modeRadio = uifactory.addRadiosHorizontal("moderadio", hcFlc, keys, values);

		bttAdd = uifactory.addFormLink("gsphc.addoption", hcFlc, Link.BUTTON_SMALL);

		txtError = uifactory.addStaticTextElement("txtError", null,"Kein Fehler", formLayout);
		txtError.setVisible(false);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("ok_cancel", getTranslator());
		buttonLayout.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		formLayout.add(buttonLayout);
	}

	/**
	 * adds an option field to the form (one row in the table)
	 * 
	 * @param val
	 */
	private void addOptionField(String val) {
		int teNumber = optionFieldNames.size() + 1;
		TextElement te = uifactory.addTextElement(OPTFIELD_PREFIX + teNumber, null, 10, "", hcFlc);
		te.setValue(val);

		if (StringHelper.containsNonWhitespace(val)) {
			uifactory.addStaticTextElement(OPTFIELD_TRSLBL_PREFIX + teNumber, null, "(" + translate(val) + ")", hcFlc);
		}

		FormLink tl = uifactory.addFormLink(OPTFIELD_TRS_PREFIX + teNumber, "gsphc.translate", null, hcFlc, Link.LINK);
		FormLink re = uifactory.addFormLink(OPTFIELD_RMV_PREFIX + teNumber, "gsphc.remove", null, hcFlc, Link.BUTTON_XSMALL);
		re.setUserObject(te);
		tl.setUserObject(te);
		optionFieldNames.add(String.valueOf(teNumber));
		hcFlc.contextPut("optionfields", optionFieldNames);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		super.propagateDirtinessToContainer(fiSrc, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source.equals(bttAdd)) {
			addOptionField("");
		} else if (source instanceof FormLink link
				&& source.getUserObject() instanceof TextElement te) {
			if (source.getName().startsWith(OPTFIELD_TRS_PREFIX)) {
				if (StringHelper.containsNonWhitespace(te.getValue())) {
					doTranslate(ureq, te);
				} else {
					te.setErrorKey("gsphc.translate");
					te.showLabel(true);
				}
			} else if (source.getName().startsWith(OPTFIELD_RMV_PREFIX)) {
				if(optionFieldNames.size() > 1) {
					optionFieldNames.remove(te.getName().replace(OPTFIELD_PREFIX, ""));
					i18nKeysToDelete.add(te.getValue());
					hcFlc.contextPut("optionfields", optionFieldNames);
				}
			}
		}
	}
	
	private void doTranslate(UserRequest ureq, TextElement te) {
		removeAsListenerAndDispose(singleKeyTrsCtrl);
		removeAsListenerAndDispose(cmc);
		
		singleKeyTrsCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), te.getValue(), GenericSelectionPropertyHandler.class);
		listenTo(singleKeyTrsCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), singleKeyTrsCtrl.getInitialComponent(),
				true, translate("gsphc.translate") + ":: " + te.getValue());
		cmc.activate();
		listenTo(cmc);
		
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void closeTranslatorCallout() {
		if (cmc != null) {
			removeAsListenerAndDispose(cmc);
			cmc = null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == singleKeyTrsCtrl) {
			closeTranslatorCallout();
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		txtError.setVisible(false);
		if( optionFieldNames.isEmpty()) {
			txtError.setValue(translate("form.legende.mandatory"));
			txtError.setVisible(true);
			allOk &= false;
		} else {
			for (int i = 0; i < optionFieldNames.size(); i++) {
				TextElement te = (TextElement) hcFlc.getFormComponent(OPTFIELD_PREFIX + optionFieldNames.get(i));
				String textValue = te.getValue();
				
				te.clearError();
				if (StringHelper.containsNonWhitespace(textValue)) {
					String translatedValue = I18nManager.getInstance().getLocalizedString(GenericSelectionPropertyHandler.class.getPackage().getName(), textValue, null, getLocale(), true, true);
					if(translatedValue == null) {
						te.setErrorKey("error.missing.translation");
						txtError.setValue("Please translate all values");
						txtError.setVisible(true);
						allOk &= false;
					}
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// save the data to config
		boolean m = modeRadio.isSelected(1);
		handler.setMultiSelect(m);
		List<String> selectionKeys = new ArrayList<>();

		for (int i = 0; i < optionFieldNames.size(); i++) {
			TextElement te = (TextElement) hcFlc.getFormComponent(OPTFIELD_PREFIX + optionFieldNames.get(i));
			String textValue = te.getValue();
			if (StringHelper.containsNonWhitespace(textValue)) selectionKeys.add(textValue);
		}
		String[] selectionKeysArray = new String[selectionKeys.size()];
		handler.setSelectionKeys(selectionKeys.toArray(selectionKeysArray));
		handler.saveConfig();

		// remove i18nKeys that are no longer used (prevent orphans)
		if(singleKeyTrsCtrl==null){
			singleKeyTrsCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(),"", GenericSelectionPropertyHandler.class);
		}
		for (String key : i18nKeysToDelete) {
			singleKeyTrsCtrl.deleteI18nKey(key);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}
}
