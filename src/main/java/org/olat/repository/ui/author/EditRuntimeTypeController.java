/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.author;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditRuntimeTypeController extends FormBasicController {

	private SingleSelection runtimeTypeEl;
	
	private RepositoryEntry entry;
	private final int numOfOffers;
	private final boolean hasUserManager;
	private final RepositoryService.RuntimeTypesAndCheckDetails allowedRuntimeTypesAndDetails;
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	public EditRuntimeTypeController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "editruntimetype", Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		this.entry = entry;
		hasUserManager = repositoryService.hasUserManaged(entry);
		numOfOffers = acService.findOfferByResource(entry.getOlatResource(), true, null, null).size();
		allowedRuntimeTypesAndDetails = repositoryService.allowedRuntimeTypes(entry);
		initForm(ureq);
	}
	
	public RepositoryEntry getRepositoryEntry() {
		return entry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_edit_runtime_type_form");
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("r_info", translate("change.runtime.type.info"));
			layoutCont.contextPut("r_info_help_url", "manual_user/learningresources/Access_configuration/");
		}
		
		StringBuilder warnings = new StringBuilder();
		switch (allowedRuntimeTypesAndDetails.checkDetails()) {
			case ltiDeploymentExists -> warnings.append("<p>").append(translate("change.runtime.type.warning.ltiDeploymentExists")).append("</p>");
			case participantExists -> warnings.append("<p>").append(translate("change.runtime.type.warning.participantExists")).append("</p>");
			case coachExists -> warnings.append("<p>").append(translate("change.runtime.type.warning.coachExists")).append("</p>");
			case offerExists -> warnings.append("<p>").append(translate("change.runtine.type.warning.offerExists")).append("</p>");
			case curriculumElementExists -> warnings.append("<p>").append(translate("change.runtime.type.warning.curriculumElementExists")).append("</p>");
			case groupWithOffersOrLtiExists -> warnings.append("<p>").append(translate("change.runtime.type.warning.groupWithOffersOrLtiExists")).append("</p>");
			case groupWithOtherCoursesExists -> warnings.append("<p>").append(translate("change.runtime.type.warning.groupWithOtherCoursesExists")).append("</p>");
			case isTemplate -> warnings.append("<p>").append(translate("change.runtime.type.warning.isTemplate")).append("</p>");
			case lectureEnabled -> warnings.append("<p>").append(translate("change.runtime.type.warning.lectureEnabled")).append("</p>");
			case wrongState, ok -> { /* Do nothing */}
		}
		if (warnings.isEmpty()) {
			if(hasUserManager) {
				warnings.append("<p>").append(translate("change.runtime.type.warning")).append("</p>");
			}
			if(numOfOffers > 0) {
				warnings.append("<p>").append(translate("change.runtime.type.warning.offers", Integer.toString(numOfOffers))).append("</p>");
			}
		}
		if(!warnings.isEmpty()) {
			setFormTranslatedWarning(warnings.toString());
		}

		Set<RepositoryEntryRuntimeType> possibleRuntimeTypes = repositoryService.getPossibleRuntimeTypes(entry);
		if(entry.getRuntimeType() != null) {
			possibleRuntimeTypes.add(entry.getRuntimeType());
		}

		SelectionValues runtimeTypeKV = new SelectionValues();
		if (possibleRuntimeTypes.contains(RepositoryEntryRuntimeType.embedded)) {
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.embedded.name(),
					translate("runtime.type." + RepositoryEntryRuntimeType.embedded.name() + ".title"),
					translate("runtime.type." + RepositoryEntryRuntimeType.embedded.name() + ".desc"),
					"o_icon o_icon_link", null, !hasUserManager));
		}
		if (possibleRuntimeTypes.contains(RepositoryEntryRuntimeType.standalone)) {
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.standalone.name(),
					translate("runtime.type." + RepositoryEntryRuntimeType.standalone.name() + ".title"),
					translate("runtime.type." + RepositoryEntryRuntimeType.standalone.name() + ".desc"),
					"o_icon o_icon_people", null,
					allowedRuntimeTypesAndDetails.runtimeTypes().contains(RepositoryEntryRuntimeType.standalone)));
		}
		if (possibleRuntimeTypes.contains(RepositoryEntryRuntimeType.curricular)) {
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.curricular.name(),
					translate("runtime.type." + RepositoryEntryRuntimeType.curricular.name() + ".title"),
					translate("runtime.type." + RepositoryEntryRuntimeType.curricular.name() + ".desc"),
					"o_icon o_icon_curriculum", null,
					allowedRuntimeTypesAndDetails.runtimeTypes().contains(RepositoryEntryRuntimeType.curricular)));
		}
		if (possibleRuntimeTypes.contains(RepositoryEntryRuntimeType.template)) {
			
			
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.template.name(),
					translate("runtime.type." + RepositoryEntryRuntimeType.template.name() + ".title"),
					translate("runtime.type." + RepositoryEntryRuntimeType.template.name() + ".desc"),
					"o_icon o_icon_template", null,
					allowedRuntimeTypesAndDetails.runtimeTypes().contains(RepositoryEntryRuntimeType.template)));
		}
		

		runtimeTypeEl = uifactory.addCardSingleSelectHorizontal("cif.runtime.type", "cif.runtime.type", formLayout, runtimeTypeKV);
		for (int i = 0; i < runtimeTypeKV.size(); i++) {
			runtimeTypeEl.setEnabled(i, runtimeTypeKV.enabledStates()[i]);
		}

		if(entry.getRuntimeType() != null) {
			runtimeTypeEl.select(entry.getRuntimeType().name(), true);
		}

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		runtimeTypeEl.clearError();
		if(!runtimeTypeEl.isOneSelected()) {
			runtimeTypeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		RepositoryEntryRuntimeType runtimeType = RepositoryEntryRuntimeType.valueOf(runtimeTypeEl.getSelectedKey());
		entry = repositoryManager.setRuntimeType(entry, runtimeType);
		
		// Embedded hasn't any offers
		if(runtimeType == RepositoryEntryRuntimeType.embedded) {
			List<Offer> deletedOfferList = acService.findOfferByResource(entry.getOlatResource(), true, null, null);
			for(Offer offerToDelete:deletedOfferList) {
				acService.deleteOffer(offerToDelete);
			}
			dbInstance.commit();
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
