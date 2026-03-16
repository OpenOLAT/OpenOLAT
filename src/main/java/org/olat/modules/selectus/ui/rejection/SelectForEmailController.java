/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rejection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.mail.EmailVariables;

/**
 * 
 * Initial date: 19.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectForEmailController extends AbstractEmailController {

	private final EmailVariables emailVar;
	
	public SelectForEmailController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form,
			EmailVariables emailVar, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, runContext, form, emailVar.getRows(), emailVar.getMailLog(), emailVar.getRatings(),
				emailVar.getCommittee(), emailVar.getPosition(), emailVar.isShowDecisions(), secCallback);
		this.emailVar = emailVar;
		preselect();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setElementCssClass("o_sel_rejection_to_application_list");

		if(applicationsDataModel.getRowCount() > 0) {
			setFormDescription("wizard.mail.select.description");
		}
	}
	
	private void preselect() {
		Set<Integer> selectedRows = new HashSet<>();
		List<ApplicationLight> applications = applicationsDataModel.getObjects();
		if(applications != null && !applications.isEmpty()) {
			for(int i=applications.size(); i-->0; ) {
				selectedRows.add(Integer.valueOf(i));
			}
		}

		if(!selectedRows.isEmpty()) {
			tableEl.setMultiSelectedIndex(selectedRows);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selectedIndexSet = tableEl.getMultiSelectedIndex();
		List<ApplicationLight> selectedApps = new ArrayList<>();
		for(Integer selectedIndex:selectedIndexSet) {
			ApplicationLight obj = applicationsDataModel.getObject(selectedIndex.intValue());
			if(obj != null) {
				selectedApps.add(obj);
			}
		}
		emailVar.setSelectedApps(selectedApps);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}