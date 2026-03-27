/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.organisation;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.OrganisationUnit;

/**
 * 
 * Initial date: 18 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MergeOrganisationUnitsController extends FormBasicController {
	
	private SingleSelection unitsEl;
	
	private final List<OrganisationUnit> orgUnits;
	
	@Autowired
	private RecruitingService erFrontendManager;
	
	public MergeOrganisationUnitsController(UserRequest ureq, WindowControl wControl, List<OrganisationUnit> orgUnits) {
		super(ureq, wControl);
		this.orgUnits = orgUnits;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("merge.organisation.unit.explain");
		
		String[] theKeys = new String[orgUnits.size()];
		String[] theValues = new String[orgUnits.size()];
		for(int i=orgUnits.size(); i-->0; ) {
			theKeys[i] = orgUnits.get(i).getKey().toString();
			theValues[i] = orgUnits.get(i).getName();
		}
		unitsEl = uifactory.addRadiosVertical("units", "", formLayout, theKeys, theValues);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("merge", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		unitsEl.clearError();
		if(!unitsEl.isOneSelected()) {
			unitsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		OrganisationUnit targetUnit = null;
		String selectedUnitKey = unitsEl.getSelectedKey();
		List<OrganisationUnit> unitsToMerge = new ArrayList<>(orgUnits.size()); 
		for(OrganisationUnit unit:orgUnits) {
			if(unit.getKey().toString().equals(selectedUnitKey)) {
				targetUnit = unit;
			} else {
				unitsToMerge.add(unit);
			}
		}
		
		if(targetUnit != null) {
			//TODO selectus load mail settings erFrontendManager.mergeOrganisationUnits(unitsToMerge, targetUnit);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
