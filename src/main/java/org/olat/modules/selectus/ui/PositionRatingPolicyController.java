/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.PolicyLink;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionProfessorship;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionRatingPolicyController extends FormBasicController implements Activateable2 {

	private FormLayoutContainer pointsLayout;
	private FormLayoutContainer documentsLayout;
	private SpacerElement documentsLayoutSpacer;
	
	private Position position;
	
	@Autowired
	private RecruitingModule recruitingModule;

	public PositionRatingPolicyController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl);
		this.position = position;
		initForm(ureq);
	}
	
	public PositionRatingPolicyController(UserRequest ureq, WindowControl wControl, Form rootForm, Position position) {
		super(ureq, wControl);
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.position = position;
		initForm(ureq);
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_rating_policy_form");
		
		String page = velocity_root + "/rating_policy_ratings.html";
		FormLayoutContainer ratingLayout = FormLayoutContainer.createCustomFormLayout("ratings", getTranslator(), page);
		ratingLayout.setRootForm(mainForm);
		formLayout.add(ratingLayout);
		ratingLayout.setLabel("edit.rating.policy.ratings", null);
		ratingLayout.contextPut("abstentionEnabled", recruitingModule.isRatingAbstentionEnabled());
		
		SpacerElement pointsSpacer = uifactory.addSpacerElement("sep1", formLayout, false);
		pointsSpacer.setElementCssClass("o_sel_spacer_points");
		
		String pointsPage = velocity_root + "/rating_policy_points.html";
		pointsLayout = FormLayoutContainer.createCustomFormLayout("points", getTranslator(), pointsPage);
		pointsLayout.setElementCssClass("o_sel_rating_policy_points");
		pointsLayout.setRootForm(mainForm);
		formLayout.add(pointsLayout);
		pointsLayout.setLabel("edit.rating.policy.points", null);
		pointsLayout.contextPut("focusEnabled", Boolean.valueOf(recruitingModule.isRatingPolicyFocusEnabled()));
		pointsLayout.contextPut("potentialCandidatesEnabled", Boolean.valueOf(recruitingModule.isRatingPolicyPotentialCandidates()));
		pointsLayout.contextPut("professorshipTypeGenericExplanationEnabled", Boolean.valueOf(recruitingModule.isRatingPolicyProfessorshipTypeGenericExplanationEnabled()));

		updateProfessorship();

		documentsLayoutSpacer = uifactory.addSpacerElement("sep2", formLayout, false);
		documentsLayoutSpacer.setElementCssClass("o_sel_spacer_docs");

		String documentsPage = velocity_root + "/rating_policy_documents.html";
		documentsLayout = FormLayoutContainer.createCustomFormLayout("documents", getTranslator(), documentsPage);
		documentsLayout.setRootForm(mainForm);
		formLayout.add(documentsLayout);
		documentsLayout.setLabel("edit.rating.policy.documents", null);
		updatePolicyDocuments();
	}
	
	private void updateProfessorship() {
		if(recruitingModule.isProfessorshipTypeEnabled()) {
			String professorship = position.getProfessorship();
			if(!StringHelper.containsNonWhitespace(professorship)) {
				professorship = PositionProfessorship.any.name();
			}
			pointsLayout.contextPut("professorship", professorship);
		} else {
			pointsLayout.contextPut("professorship", "disabled");
		}
	}
	
	private void updatePolicyDocuments() {
		List<PolicyDocument> documents = new ArrayList<>();
		appendPolicyDocuments(position.getPolicyLink1(), documents);
		appendPolicyDocuments(position.getPolicyLink2(), documents);
		appendPolicyDocuments(position.getPolicyLink3(), documents);
		appendPolicyDocuments(position.getPolicyLink4(), documents);
		documentsLayout.contextPut("documentList", documents);
		documentsLayout.setVisible(!documents.isEmpty());
		documentsLayoutSpacer.setVisible(!documents.isEmpty());
	}
	
	private void appendPolicyDocuments(PolicyLink link, List<PolicyDocument> documents) {
		if(link == null) return;
		if(StringHelper.containsNonWhitespace(link.getUrl())) {
			documents.add(new PolicyDocument(link));
		}
	}
	
	public void updatePosition(Position position) {
		this.position = position;
		updateProfessorship();
		updatePolicyDocuments();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	public class PolicyDocument {
		private final PolicyLink link;
		
		public PolicyDocument(PolicyLink link) {
			this.link = link;
		}
		
		public String getLabel() {
			return StringHelper.containsNonWhitespace(link.getLabel()) ? link.getLabel() : link.getUrl();
		}
		
		public String getUrl() {
			return link.getUrl();
		}
		
		public String getCssClass() {
			String url = link.getUrl();
			String cssClass = CSSHelper.createFiletypeIconCssClassFor(url);
			if(StringHelper.containsNonWhitespace(cssClass)) {
				return cssClass;
			}
			return "";
		}
	}
}