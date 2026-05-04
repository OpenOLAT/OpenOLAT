/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.attributes.SeparatorConfiguration;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 16 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditAdditionalSeparatorAttributeController extends FormBasicController implements PositionEditAdditionalAttributeController {
	
	private static final String WITH_LINE = "withline";
	private static final String WITHOUT_LINE = "withoutline";
	
	
	private SingleSelection displayEl;

	private PositionAttributeDefinition attributeDefinition;
	private SeparatorConfiguration configuration;

	public PositionEditAdditionalSeparatorAttributeController(UserRequest ureq, WindowControl wControl, PositionAttributeDefinition attributeDefinition) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.attributeDefinition = attributeDefinition;
		configuration = attributeDefinition.getConfiguration(SeparatorConfiguration.class);
		if(configuration == null) {
			configuration = new SeparatorConfiguration();
			configuration.setWithLine(true);
		}
		initForm(ureq);
	}
	
	@Override
	public PositionAttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues keyValues = new SelectionValues();
		keyValues.add(SelectionValues.entry(WITH_LINE, translate("custom.attribute.separator.display.w")));
		keyValues.add(SelectionValues.entry(WITHOUT_LINE, translate("custom.attribute.separator.display.wo")));
		displayEl = uifactory.addRadiosVertical("custom.attribute.separator.display", formLayout, keyValues.keys(), keyValues.values());
		String selectedKey = configuration.isWithLine() ? WITH_LINE : WITHOUT_LINE;
		displayEl.select(selectedKey, true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayEl.clearError();
		if(!displayEl.isOneSelected()) {
			displayEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String selectedKey = displayEl.getSelectedKey();
		configuration.setWithLine(WITH_LINE.equals(selectedKey));
		attributeDefinition.setConfiguration(configuration);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
