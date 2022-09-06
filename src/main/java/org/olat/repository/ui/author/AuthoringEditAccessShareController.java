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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.ui.CatalogMainController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEditAccessShareController extends FormBasicController {
	
	private static final String[] leaveKeys = new String[]{
			RepositoryEntryAllowToLeaveOptions.atAnyTime.name(),
			RepositoryEntryAllowToLeaveOptions.afterEndDate.name(),
			RepositoryEntryAllowToLeaveOptions.never.name()
		};
	private static final String KEY_PRIVATE = "private";
	private static final String KEY_PUBLIC = "public";
	private static final String KEY_REFERENCE = "reference";
	private static final String KEY_COPY = "copy";
	private static final String KEY_DOWNLOAD = "download";
	private static final String[] accessKey = new String[] { KEY_PRIVATE, KEY_PUBLIC };
	
	private SingleSelection accessEl;
	private FormLayoutContainer repoLinkCont;
	private FormLayoutContainer catalogLinksCont;
	private FormLink showMicrositeLinks;
	private SingleSelection leaveEl;
	private SingleSelection statusEl;
	private MultipleSelectionElement organisationsEl;
	private SelectionElement authorCanEl;
	
	private final boolean status;
	private final boolean embbeded;
	private final boolean readOnly;
	private RepositoryEntry entry;
	private List<Organisation> repositoryEntryOrganisations;
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ACService acService;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private TaxonomyModule taxonomyModule;
	
	public AuthoringEditAccessShareController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		embbeded = false;
		status = false;
		
		initForm(ureq);
		validateOfferAvailable();
	}
	
	public AuthoringEditAccessShareController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = false;
		embbeded = true;
		status = true;
		
		initForm(ureq);
	}
	
	public boolean isPublicVisible() {
		return accessEl.isOneSelected() && accessEl.isKeySelected(KEY_PUBLIC);
	}
	
	public boolean canCopy() {
		return authorCanEl.isKeySelected(KEY_COPY);
	}

	public boolean canReference() {
		return authorCanEl.isKeySelected(KEY_REFERENCE);
	}

	public boolean canDownload() {
		return authorCanEl.isKeySelected(KEY_DOWNLOAD);
	}
	
	public RepositoryEntryStatusEnum getEntryStatus() {
		return RepositoryEntryStatusEnum.valueOf(statusEl.getSelectedKey());
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public RepositoryEntryAllowToLeaveOptions getSelectedLeaveSetting() {
		RepositoryEntryAllowToLeaveOptions setting;
		if(leaveEl.isOneSelected()) {
			setting = RepositoryEntryAllowToLeaveOptions.valueOf(leaveEl.getSelectedKey());
		} else {
			setting = RepositoryEntryAllowToLeaveOptions.atAnyTime;
		}
		return setting;
	}
	
	public List<Organisation> getSelectedOrganisations() {
		if(organisationsEl == null || !organisationsEl.isVisible()) {
			return repositoryEntryOrganisations;
		}
		
		List<Organisation> organisations = new ArrayList<>();

		Set<String> organisationKeys = organisationsEl.getKeys();
		Collection<String> selectedOrganisationKeys = organisationsEl.getSelectedKeys();

		Set<String> currentOrganisationKeys = new HashSet<>();
		for(Iterator<Organisation> it=organisations.iterator(); it.hasNext(); ) {
			String key = it.next().getKey().toString();
			currentOrganisationKeys.add(key);
			if(organisationKeys.contains(key) && !selectedOrganisationKeys.contains(key)) {
				it.remove();
			}
		}

		for(String selectedOrganisationKey:selectedOrganisationKeys) {
			if(!currentOrganisationKeys.contains(selectedOrganisationKey)) {
				Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(Long.valueOf(selectedOrganisationKey)));
				if(organisation != null) {
					organisations.add(organisation);
				}
			}
		}

		return organisations;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("details.access");
		setFormContextHelp("manual_user/course_create/Access_configuration/#access-configuration");
		formLayout.setElementCssClass("o_sel_repo_access_configuration");
		
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		initStatus(generalCont);
		statusEl.setVisible(status);
		statusEl.setEnabled(!readOnly);
		
		String[] accessValues = new String[] {
				getAccessTranslatedValue("rentry.access.type.private", "rentry.access.type.private.explain", "o_icon-fw o_icon_locked"),
				getAccessTranslatedValue("rentry.access.type.public", "rentry.access.type.public.explain", "o_icon-fw o_icon_unlocked")
		};
		accessEl = uifactory.addRadiosVertical("entry.access.type", "rentry.access.type", generalCont, accessKey, accessValues);
		accessEl.setEnabled(!readOnly);
		if (embbeded || catalogModule.isEnabled()) {
			accessEl.addActionListener(FormEvent.ONCHANGE);
		}
		if(entry.isPublicVisible()) {
			accessEl.select(KEY_PUBLIC, true);
		} else {
			accessEl.select(KEY_PRIVATE, true);
		}
		
		repoLinkCont = FormLayoutContainer.createCustomFormLayout("catalogLinks", getTranslator(), velocity_root + "/repo_links.html");
		repoLinkCont.setLabel("cif.repo.link", null);
		repoLinkCont.setRootForm(mainForm);
		generalCont.add("repoLink", repoLinkCont);
		String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
		repoLinkCont.contextPut("repoLink", new ExtLink(entry.getKey().toString(), url, null));
		
		catalogLinksCont = FormLayoutContainer.createCustomFormLayout("catalogLinks", getTranslator(), velocity_root + "/catalog_links.html");
		catalogLinksCont.setLabel("cif.catalog.links", null);
		catalogLinksCont.setRootForm(mainForm);
		generalCont.add("catalogLinks", catalogLinksCont);
		
		showMicrositeLinks = uifactory.addFormLink("show.additional", "nodeConfigForm.show.additional", null, catalogLinksCont, Link.LINK);
		showMicrositeLinks.setIconLeftCSS("o_icon o_icon-lg o_icon_open_togglebox");
		
		updateCatalogLinksUI();
		
		initLeaveOption(generalCont);
		
		uifactory.addSpacerElement("author.config", generalCont, false);
		
		UserSession usess = ureq.getUserSession();
		initFormOrganisations(generalCont, usess);
		organisationsEl.setVisible(organisationModule.isEnabled());
		organisationsEl.setEnabled(!readOnly);
		
		final boolean managedSettings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.settings);
		boolean closedOrDeleted = entry.getEntryStatus() == RepositoryEntryStatusEnum.closed
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.trash
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.deleted;
		boolean supportsDownload = handlerFactory.getRepositoryHandler(entry).supportsDownload();
		
		SelectionValues canSV = new SelectionValues();
		canSV.add(SelectionValues.entry(KEY_REFERENCE, translate("cif.canReference")));
		canSV.add(SelectionValues.entry(KEY_COPY, translate("cif.canCopy")));
		if (supportsDownload) {
			canSV.add(SelectionValues.entry(KEY_DOWNLOAD, translate("cif.canDownload")));
		}
		authorCanEl = uifactory.addCheckboxesVertical("cif.author.can", generalCont, canSV.keys(), canSV.values(), 1);
		authorCanEl.setEnabled(!managedSettings && !closedOrDeleted && !readOnly);
		authorCanEl.select(KEY_REFERENCE, entry.getCanReference()); 
		authorCanEl.select(KEY_COPY, entry.getCanCopy()); 
		if (supportsDownload) {
			authorCanEl.select(KEY_DOWNLOAD, entry.getCanDownload()); 
		}

		if(!embbeded && !readOnly) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonsCont.setRootForm(mainForm);
			generalCont.add("buttons", buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}
	
	private String getAccessTranslatedValue(String i18nKey, String explanationI18nKey, String iconCssClass) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("<i class='o_icon o_icon-fq ").append(iconCssClass).append("'> </i> ")
		  .append(translate(i18nKey)).append(" <small>")
		  .append(translate(explanationI18nKey)).append("</small>");
		return sb.toString();
	}
	
	private void initStatus(FormItemContainer formLayout) {
		// make configuration read only when managed by external system
		final boolean managedAccess = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.access);
		final boolean closedOrDeleted = entry.getEntryStatus() == RepositoryEntryStatusEnum.closed
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.trash
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.deleted;
		
		String[] publishedKeys;
		String[] publishedValues;
		if(closedOrDeleted) {
			publishedKeys = new String[] {
					RepositoryEntryStatusEnum.preparation.name(), RepositoryEntryStatusEnum.review.name(),
					RepositoryEntryStatusEnum.coachpublished.name(), RepositoryEntryStatusEnum.published.name(),
					RepositoryEntryStatusEnum.closed.name(), RepositoryEntryStatusEnum.trash.name(),
					RepositoryEntryStatusEnum.deleted.name()
			};
			publishedValues = new String[] {
					translate("cif.status.preparation"), translate("cif.status.review"),
					translate("cif.status.coachpublished"), translate("cif.status.published"),
					translate("cif.status.closed"), translate("cif.status.trash"),
					translate("cif.status.deleted")
			};
		} else {
			publishedKeys = new String[] {
					RepositoryEntryStatusEnum.preparation.name(), RepositoryEntryStatusEnum.review.name(),
					RepositoryEntryStatusEnum.coachpublished.name(), RepositoryEntryStatusEnum.published.name()
			};
			publishedValues = new String[] {
					translate("cif.status.preparation"), translate("cif.status.review"),
					translate("cif.status.coachpublished"), translate("cif.status.published")
			};
		}
		statusEl = uifactory.addDropdownSingleselect("publishedStatus", "cif.publish", formLayout, publishedKeys, publishedValues, null);
		statusEl.setElementCssClass("o_sel_repositoryentry_access_publication");
		statusEl.setEnabled(!managedAccess && !closedOrDeleted);
		statusEl.select(entry.getStatus(), true);
		if (embbeded) {
			statusEl.addActionListener(FormEvent.ONCHANGE);
		}
	}
	
	private void initLeaveOption(FormItemContainer formLayout) {
		String[] leaveValues = new String[]{
				translate("rentry.leave.atanytime"),
				translate("rentry.leave.afterenddate"),
				translate("rentry.leave.never")
		};
		
		final boolean managedLeaving = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.membersmanagement);
		leaveEl = uifactory.addRadiosVertical("entry.leave", "rentry.leave.option", formLayout, leaveKeys, leaveValues);
		boolean found = false;
		for(String leaveKey:leaveKeys) {
			if(leaveKey.equals(entry.getAllowToLeaveOption().name())) {
				leaveEl.select(leaveKey, true);
				found = true;
			}
		}
		if(!found) {
			if(managedLeaving) {
				leaveEl.select(RepositoryEntryAllowToLeaveOptions.never.name(), true);
			} else {
				RepositoryEntryAllowToLeaveOptions defaultOption = repositoryModule.getAllowToLeaveDefaultOption();
				leaveEl.select(defaultOption.name(), true);
			}
		}
		leaveEl.setEnabled(!managedLeaving && !readOnly);
	}
	
	private void initFormOrganisations(FormItemContainer formLayout, UserSession usess) {
		Roles roles = usess.getRoles();
		List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		List<Organisation> organisationList = new ArrayList<>(organisations);

		List<Organisation> reOrganisations = repositoryService.getOrganisations(entry);
		repositoryEntryOrganisations = new ArrayList<>(reOrganisations);
		
		for(Organisation reOrganisation:reOrganisations) {
			if(reOrganisation != null && !organisationList.contains(reOrganisation)) {
				organisationList.add(reOrganisation);
			}
		}
		
		Collections.sort(organisationList, new OrganisationNameComparator(getLocale()));
		
		List<String> keyList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();
		for(Organisation organisation:organisationList) {
			keyList.add(organisation.getKey().toString());
			valueList.add(organisation.getDisplayName());
		}
		organisationsEl = uifactory.addCheckboxesDropdown("organisations", "cif.organisations", formLayout,
				keyList.toArray(new String[keyList.size()]), valueList.toArray(new String[valueList.size()]),
				null, null);
		organisationsEl.setEnabled(!RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.organisations) && !readOnly);
		for(Organisation reOrganisation:reOrganisations) {
			if(keyList.contains(reOrganisation.getKey().toString())) {
				organisationsEl.select(reOrganisation.getKey().toString(), true);
			}
		}
	}
	
	public void validateOfferAvailable() {
		setFormWarning(null);
		if (accessEl.isKeySelected(KEY_PUBLIC)) {
			List<Offer> offers = acService.findOfferByResource(entry.getOlatResource(), true, null, null);
			if (offers.isEmpty()) {
				setFormWarning("error.public.no.offers");
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == accessEl) {
			updateCatalogLinksUI();
			markDirty();
			if (embbeded) {
				fireEvent(ureq, new PublicVisibleEvent(accessEl.isKeySelected(KEY_PUBLIC)));
			}
		} else if (source == statusEl) {
			fireEvent(ureq, new StatusEvent(getEntryStatus()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateCatalogLinksUI() {
		catalogLinksCont.setVisible(catalogModule.isEnabled() && accessEl.isKeySelected(KEY_PUBLIC));
		
		if (catalogLinksCont.isVisible()) {
			String url = Settings.getServerContextPathURI() + "/url/Catalog/0/" + CatalogMainController.ORES_TYPE_SEARCH
					+ "/0/" + CatalogMainController.ORES_TYPE_INFOS + "/" + entry.getKey();
			catalogLinksCont.contextPut("searchLink", new ExtLink(entry.getKey().toString(), url, null));
			
			showMicrositeLinks.setVisible(false);
			if (taxonomyModule.isEnabled()) {
				HashSet<TaxonomyLevel> taxonomyLevels = new HashSet<>(repositoryService.getTaxonomy(entry));
				if (!taxonomyLevels.isEmpty()) {
					showMicrositeLinks.setVisible(true);
					List<ExtLink> taxonomyLinks = new ArrayList<>(taxonomyLevels.size());
					for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
						url = Settings.getServerContextPathURI() + "/url/Catalog/0/" + CatalogMainController.ORES_TYPE_TAXONOMY
								+ "/" + taxonomyLevel.getKey();
						String name = translate("cif.catalog.links.microsite", TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel));
						ExtLink extLink = new ExtLink(taxonomyLevel.getKey().toString(), url, name);
						taxonomyLinks.add(extLink);
						catalogLinksCont.contextPut("taxonomyLinks", taxonomyLinks);
					}
				}
			}
		}
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (organisationsEl != null) {
			organisationsEl.clearError();
			if(organisationsEl.isVisible() && !organisationsEl.isAtLeastSelected(1)) {
				organisationsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		accessEl.clearError();
		if(!accessEl.isOneSelected()) {
			accessEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public static class PublicVisibleEvent extends Event {

		private static final long serialVersionUID = 2663359793325512923L;
		private final boolean publicVisible;
		
		public PublicVisibleEvent(boolean publicVisible) {
			super("public-visible");
			this.publicVisible = publicVisible;
		}
		
		public boolean isPublicVisible() {
			return publicVisible;
		}
	}
	
	public static class StatusEvent extends Event {

		private static final long serialVersionUID = 2757546613805700985L;
		private final RepositoryEntryStatusEnum status;
		
		public StatusEvent(RepositoryEntryStatusEnum status) {
			super("public-visible");
			this.status = status;
		}
		
		public RepositoryEntryStatusEnum getStatus() {
			return status;
		}
	}
	
	public static class ExtLink {
		
		private final String key;
		private final String url;
		private final String name;
		
		public ExtLink(String key, String url, String name) {
			this.key = key;
			this.url = url;
			this.name = name;
		}

		public String getKey() {
			return key;
		}

		public String getUrl() {
			return url;
		}

		public String getName() {
			return name;
		}
		
	}
 }
