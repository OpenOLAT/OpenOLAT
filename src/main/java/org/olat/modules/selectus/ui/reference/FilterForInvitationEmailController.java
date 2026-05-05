/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

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
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.mail.InvitationVariables;
import org.olat.modules.selectus.model.references.ReferenceSearchParameters;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 7 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FilterForInvitationEmailController extends StepFormBasicController {
	
	private static final String[] referenceStatusKeys = new String[] {
			ReferenceStatus.notSent.name(), ReferenceStatus.sentAwaiting.name(),
			ReferenceStatus.late.name(), ReferenceStatus.submitted.name()
	};
	
	private MultipleSelectionElement statusEl;
	private MultipleSelectionElement referenceTypeEl;

	private final InvitationVariables emailVar;
	
	@Autowired
	private RecruitingService recruitingService;
	
	public FilterForInvitationEmailController(UserRequest ureq, WindowControl wControl,
			InvitationVariables emailVar, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.emailVar = emailVar;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.invitation.filter.description");
		
		Position position = emailVar.getPosition();
		SelectionValues referenceTypePK = new SelectionValues();
		if(position.isRefereeRecommendationEnabled()) {
			referenceTypePK.add(SelectionValues.entry(ReferenceType.recommendation.name(), translate("referees")));
		}
		if(position.isExpertRecommendationEnabled()) {
			referenceTypePK.add(SelectionValues.entry(ReferenceType.expert.name(), translate("experts")));
		}
		if(position.isComparativeAssessmentExpertEnabled()) {
			referenceTypePK.add(SelectionValues.entry(ReferenceType.comparativeAssessmentExpert.name(), translate("comparative.experts")));
		}
		referenceTypeEl = uifactory.addCheckboxesVertical("references.type", formLayout, referenceTypePK.keys(), referenceTypePK.values(), 1);
		referenceTypeEl.setVisible(referenceTypePK.size() > 1);

		String[] referenceStatusValues = new String[referenceStatusKeys.length];
		for(int i=referenceStatusKeys.length; i-->0; ) {
			referenceStatusValues[i] = translate("reference.status.".concat(referenceStatusKeys[i]));
		}
		statusEl = uifactory.addCheckboxesVertical("references.status", formLayout, referenceStatusKeys, referenceStatusValues, 1);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ReferenceSearchParameters params = new ReferenceSearchParameters();
		params.setApplications(emailVar.getApplications());
		params.setPosition(emailVar.getPosition());
		
		if(referenceTypeEl.isVisible()) {
			Collection<String> selectedTypes = referenceTypeEl.getSelectedKeys();
			List<ReferenceType> types = selectedTypes.stream()
					.map(ReferenceType::valueOf).collect(Collectors.toList());
			params.setTypes(types);
		}
		
		Collection<String> selectedStatus = statusEl.getSelectedKeys();
		List<ReferenceStatus> status = selectedStatus.stream()
				.map(ReferenceStatus::valueOf).collect(Collectors.toList());
		params.setStatus(status);

		List<Reference> references = recruitingService.getReferences(params);
		emailVar.setRows(references);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
