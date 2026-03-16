/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.mail.InvitationVariables;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectForInvitationEmailController extends AbstractInvitationEmailController {
	
	private final InvitationVariables emailVar;

	public SelectForInvitationEmailController(UserRequest ureq, WindowControl wControl,
			InvitationVariables emailVar, StepsRunContext runContext, Form form) {
		super(ureq, wControl, emailVar.getRows(), emailVar.getSortKey(), runContext, form);
		this.emailVar = emailVar;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.invitation.select.description");
		super.initForm(formLayout, listener, ureq);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setElementCssClass("o_sel_reference_list");
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selectedIndexSet = tableEl.getMultiSelectedIndex();
		List<Reference> selectedReferences = new ArrayList<>(selectedIndexSet.size());
		for(Integer selectedIndex:selectedIndexSet) {
			ReferenceInvitationRow ref = dataModel.getObject(selectedIndex.intValue());
			if(ref != null) {
				selectedReferences.add(ref.getReference());
			}
		}
		
		emailVar.setSelectedReferences(selectedReferences);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}