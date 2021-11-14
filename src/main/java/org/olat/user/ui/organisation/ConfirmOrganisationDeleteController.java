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
package org.olat.user.ui.organisation;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 26 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmOrganisationDeleteController extends FormBasicController {
	
	private FormLink deleteButton;
	private SingleSelection organisationAltEl;
	
	private final boolean proposeOrganisationAlternative;
	private final OrganisationRow organisationRow;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryService repositoryService;
	
	public ConfirmOrganisationDeleteController(UserRequest ureq, WindowControl wControl, OrganisationRow organisationRow) {
		super(ureq, wControl, "confirm_delete_organisation");
		this.organisationRow = organisationRow;
		
		CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
		searchParams.setOrganisations(Collections.singletonList(organisationRow));
		
		List<Curriculum> curriculums = curriculumService.getCurriculums(searchParams);
		List<RepositoryEntry> entries = repositoryService.getRepositoryEntryByOrganisation(organisationRow);
		proposeOrganisationAlternative = !curriculums.isEmpty() || !entries.isEmpty();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("organisationName", StringHelper.escapeHtml(organisationRow.getDisplayName()));
		}
		
		if(proposeOrganisationAlternative) {
			List<Organisation> organisations = organisationService.getOrganisations();
			Collections.sort(organisations, new OrganisationNameComparator(getLocale()));
			for(Iterator<Organisation> it=organisations.iterator(); it.hasNext(); ) {
				Organisation organisation = it.next();
				if(organisation == null || organisationRow.getKey().equals(organisation.getKey())) {
					it.remove();
				}
			}
			
			String[] organisationKeys = new String[organisations.size() + 1];
			String[] organisationValues = new String[organisations.size() + 1];
			organisationKeys[0] = "";
			organisationValues[0] = translate("no.replacement");
			for(int i=organisations.size() + 1; i-->1;) {
				Organisation organisation = organisations.get(i-1);
				organisationKeys[i] = organisation.getKey().toString();
				organisationValues[i] = organisation.getDisplayName();
			}
			organisationAltEl = uifactory.addDropdownSingleselect("organisation.replacement", formLayout, organisationKeys, organisationValues);
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(organisationAltEl != null) {
			organisationAltEl.clearError();
			if(!organisationAltEl.isOneSelected()) {
				organisationAltEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			doDelete();
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doDelete() {
		OrganisationRef organisationAlt = null;
		if(organisationAltEl != null && StringHelper.isLong(organisationAltEl.getSelectedKey())) {
			String selectedKey = organisationAltEl.getSelectedKey();
			organisationAlt = new OrganisationRefImpl(Long.valueOf(selectedKey));
		}
		organisationService.deleteOrganisation(organisationRow, organisationAlt);
	}
}
