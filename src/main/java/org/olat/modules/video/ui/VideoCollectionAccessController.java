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
package org.olat.modules.video.ui;

import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.video.VideoManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class VideoCollectionAccessController extends FormBasicController {
	
	private FormToggle videoCollectionEl;
	private ObjectSelectionElement organisationsEl;
	
	private final boolean readOnly;
	private final RepositoryEntry entry;
	
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public VideoCollectionAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("video.collection.access.title");
		
		videoCollectionEl = uifactory.addToggleButton("video.collection.access", "video.collection.access", translate("on"), translate("off"), formLayout);
		videoCollectionEl.toggle(entry.isVideoCollection());
		videoCollectionEl.addActionListener(FormEvent.ONCHANGE);
		
		Roles roles = ureq.getUserSession().getRoles();
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				videoManager.getVideoOrganisations(entry),
				() -> organisationService.getOrganisations(getIdentity(), roles,
						OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author));
		organisationsEl = uifactory.addObjectSelectionElement("organisations", "video.collection.organisations", formLayout,
				getWindowControl(), true, organisationSource);
		organisationsEl.setVisible(organisationModule.isEnabled());
		organisationsEl.setEnabled(!readOnly);
		
		String levels = TaxonomyUIFactory.getTags(getTranslator(), repositoryService.getTaxonomy(entry));
		StaticTextElement taxonomyEl = uifactory.addStaticTextElement("video.collection.taxonomy", "video.collection.taxonomy", levels, formLayout);
		taxonomyEl.setDomWrapperElement(DomWrapperElement.div);
		taxonomyEl.setVisible(StringHelper.containsNonWhitespace(levels));
		
		FormLayoutContainer layoutCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", "save", layoutCont);
	}
	
	private void updateUI() {
		organisationsEl.setVisible(organisationModule.isEnabled() && videoCollectionEl.isOn());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		organisationsEl.clearError();
		if(organisationsEl.isVisible() && organisationsEl.getSelectedKeys().isEmpty()) {
			organisationsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk; 
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(videoCollectionEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public boolean isVideoCollectionEnabled() {
		return videoCollectionEl.isOn();
	}
	
	public List<Organisation> getVideoOrganisations() {
		return organisationService.getOrganisation(OrganisationSelectionSource.toRefs(organisationsEl.getSelectedKeys()));
	}
}
