/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 26 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FilterStatusController extends StepFormBasicController {
	
	private MultipleSelectionElement filterEl;
	
	private final ContactMembersContext feedbacksContext;
	
	public FilterStatusController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, ContactMembersContext feedbacksContext) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.feedbacksContext = feedbacksContext;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues keyValues = new SelectionValues();
		for(ReferenceStatus status: ReferenceStatus.values()) {
			keyValues.add(SelectionValues.entry(status.name(), translate("reference.status.".concat(status.name()))));
		}
		filterEl = uifactory.addCheckboxesVertical("filter", "filter", formLayout, keyValues.keys(), keyValues.values(), 1);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		Collection<String> selectedStatus = filterEl.getSelectedKeys();
		if(selectedStatus.isEmpty()) {
			feedbacksContext.setSelectedFeedbacks(feedbacksContext.getFeedbacks());
		} else {
			List<ApplicationFeedback> filteredFeedback = feedbacksContext.getFeedbacks().stream()
					.filter(feedback -> selectedStatus.contains(feedback.getReferenceStatus().name()))
					.collect(Collectors.toList());
			feedbacksContext.setSelectedFeedbacks(filteredFeedback);
		}
		
		List<Identity> members = feedbacksContext.getSelectedFeedbacks().stream()
				.map(ApplicationFeedback::getIdentity)
				.distinct()
				.collect(Collectors.toList());
		feedbacksContext.setMembers(members);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
