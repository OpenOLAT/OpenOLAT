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

import static org.olat.core.gui.components.util.KeyValues.VALUE_ASC;
import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExtendedFlexiTableSearchController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.course.nodeaccess.NodeAccessProviderIdentifier;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.RepositoryHandlerFactory.OrderedRepositoryHandler;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.ResourceUsage;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthorSearchController extends FormBasicController implements ExtendedFlexiTableSearchController {

	private static final String[] keys = new String[]{ "my" };
	private static final String[] statusKeys = new String[]{ "all", "active", "closed" };
	private static final String[] usageKeys = new String[]{ ResourceUsage.all.name(), ResourceUsage.used.name(), ResourceUsage.notUsed.name() };
	
	private TextElement id; // only for admins
	private TextElement displayName;
	private TextElement author;
	private TextElement description;
	private MultipleSelectionElement types;
	private MultipleSelectionElement technicalTypeEl;
	private MultipleSelectionElement educationalTypeEl;
	private SingleSelection closedEl;
	private SingleSelection resourceUsageEl;
	private MultipleSelectionElement organisationsEl;
	private MultipleSelectionElement ownedResourcesOnlyEl;
	private MultipleSelectionElement taxonomyLevelPathEl;
	private MultipleSelectionElement licenseEl;
	private FormLink searchButton;
	
	private String[] typeKeys;
	private final boolean cancelAllowed;
	private final TaxonomyRef taxonomyRef;
	private boolean enabled = true;
	private final Map<String,Organisation> organisationMap = new HashMap<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public AuthorSearchController(UserRequest ureq, WindowControl wControl, boolean cancelAllowed) {
		super(ureq, wControl, "search");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.cancelAllowed = cancelAllowed;
		
		String taxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		taxonomyRef = StringHelper.isLong(taxonomyTreeKey)
				? new TaxonomyRefImpl(Long.valueOf(taxonomyTreeKey))
				: null;
				
		initForm(ureq);
	}

	public AuthorSearchController(UserRequest ureq, WindowControl wControl, boolean cancelAllowed, Form form) {
		super(ureq, wControl, LAYOUT_CUSTOM, "search", form);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.cancelAllowed = cancelAllowed;
		
		String taxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		taxonomyRef = StringHelper.isLong(taxonomyTreeKey)
				? new TaxonomyRefImpl(Long.valueOf(taxonomyTreeKey))
				: null;
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);

		// LEFT part of form
		displayName = uifactory.addTextElement("cif_displayname", "cif.displayname", 255, "", leftContainer);
		displayName.setElementCssClass("o_sel_repo_search_displayname");
		displayName.setFocus(true);

		id = uifactory.addTextElement("cif_id", "cif.id", 128, "", leftContainer);
		id.setElementCssClass("o_sel_repo_search_id");
		
		author = uifactory.addTextElement("cif_author", "cif.author.search", 255, "", leftContainer);
		author.setElementCssClass("o_sel_repo_search_author");
		
		description = uifactory.addTextElement("cif_description", "cif.description", 255, "", leftContainer);
		description.setElementCssClass("o_sel_repo_search_description");
		
		if(organisationModule.isEnabled()) {
			initFormOrganisations(leftContainer, ureq.getUserSession());
		}
		
		if (taxonomyRef != null) {
			initFormTaxonomyLevels(leftContainer);
		}
		
		// RIGHT part of form
		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		rightContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);

		List<String> typeList = getResources();
		typeKeys = typeList.toArray(new String[typeList.size()]);
		String[] typeValues = getTranslatedResources(typeList);
		String[] typeCSS = getResourcesCSS(typeList);
		types = uifactory.addCheckboxesDropdown("cif.type", "cif.type", rightContainer, typeKeys, typeValues, null, typeCSS);
		types.setNonSelectedText(translate("table.showall"));
		
		KeyValues technicalTypeKV = new KeyValues();
		for (NodeAccessProviderIdentifier identifier : nodeAccessService.getNodeAccessProviderIdentifer()) {
			String name = identifier.getDisplayName(getLocale());
			technicalTypeKV.add(entry(identifier.getType(), name));
		}
		technicalTypeKV.sort(KeyValues.VALUE_ASC);
		technicalTypeEl = uifactory.addCheckboxesDropdown("cif.technical.type", "cif.technical.type",
				rightContainer, technicalTypeKV.keys(), technicalTypeKV.values());
		technicalTypeEl.setNonSelectedText(translate("table.showall"));
		
		KeyValues educationalTypeKV = new KeyValues();
		repositoryManager.getAllEducationalTypes()
				.forEach(type -> educationalTypeKV.add(entry(type.getKey().toString(), translate(RepositoyUIFactory.getI18nKey(type)))));
		educationalTypeKV.sort(KeyValues.VALUE_ASC);
		educationalTypeEl = uifactory.addCheckboxesDropdown("cif.educational.type", "cif.educational.type",
				rightContainer, educationalTypeKV.keys(), educationalTypeKV.values());
		educationalTypeEl.setNonSelectedText(translate("table.showall"));
		
		if (licenseModule.isEnabled(licenseHandler)) {
			List<LicenseType> activeLicenseTypes = licenseService.loadActiveLicenseTypes(licenseHandler);
			Collections.sort(activeLicenseTypes);
			
			String[] licenseTypeKeys = new String[activeLicenseTypes.size()];
			String[] licenseTypeValues = new String[activeLicenseTypes.size()];
			int counter = 0;
			for (LicenseType licenseType: activeLicenseTypes) {
				licenseTypeKeys[counter] = String.valueOf(licenseType.getKey());
				licenseTypeValues[counter] = LicenseUIFactory.translate(licenseType, getLocale());
				counter++;
			}
			licenseEl = uifactory.addCheckboxesDropdown("cif.license", "cif.license", rightContainer, licenseTypeKeys, licenseTypeValues, null, null);
			licenseEl.setNonSelectedText(translate("table.showall"));
		}

		String[] statusValues = new String[] {
				translate("cif.resources.status.all"),
				translate("cif.resources.status.active"),
				translate("cif.resources.status.closed")
			};
		closedEl = uifactory.addRadiosHorizontal("cif_status", "cif.resources.status", rightContainer, statusKeys, statusValues);
		closedEl.select(statusKeys[1], true);

		String[] usageValues = new String[] {
			translate("cif.owned.resources.usage.all"),
			translate("cif.owned.resources.usage.used"),
			translate("cif.owned.resources.usage.notUsed")
		};
		resourceUsageEl = uifactory.addRadiosHorizontal("cif_used", "cif.owned.resources.usage", rightContainer, usageKeys, usageValues);
		resourceUsageEl.select(usageKeys[0], true);
		
		ownedResourcesOnlyEl = uifactory.addCheckboxesHorizontal("cif_my", "cif.owned.resources.only", rightContainer, keys, new String[]{ "" });
		ownedResourcesOnlyEl.select(keys[0], true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		if(cancelAllowed) {
			uifactory.addFormCancelButton("quick.search", buttonLayout, ureq, getWindowControl());
		}
		searchButton = uifactory.addFormLink("search", buttonLayout, Link.BUTTON);
		searchButton.setCustomEnabledLinkCSS("btn btn-primary");
	}
	
	private void initFormOrganisations(FormItemContainer formLayout, UserSession usess) {
		Roles roles = usess.getRoles();
		List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.principal, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		List<Organisation> organisationList = new ArrayList<>(organisations);
		
		Collections.sort(organisationList, new OrganisationNameComparator(getLocale()));
		
		List<String> keyList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();
		for(Organisation organisation:organisationList) {
			String key = organisation.getKey().toString();
			keyList.add(key);
			valueList.add(organisation.getDisplayName());
			organisationMap.put(key, organisation);
		}

		organisationsEl = uifactory.addCheckboxesDropdown("organisations", "cif.organisations", formLayout,
				keyList.toArray(new String[keyList.size()]), valueList.toArray(new String[valueList.size()]));
		organisationsEl.setVisible(keyList.size() > 1);
	}
	
	private void initFormTaxonomyLevels(FormLayoutContainer formLayout) {
		List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRef);

		KeyValues keyValues = new KeyValues();
		for (TaxonomyLevel level:allTaxonomyLevels) {
			String key = Long.toString(level.getKey());
			ArrayList<String> names = new ArrayList<>();
			addParentNames(names, level);
			Collections.reverse(names);
			String value = String.join(" / ", names);
			keyValues.add(entry(key, value));
		}
		keyValues.sort(VALUE_ASC);
	
		taxonomyLevelPathEl = uifactory.addCheckboxesDropdown("taxonomyLevelPaths", "table.header.taxonomy.paths", formLayout,
				keyValues.keys(), keyValues.values());
	}
	
	private void addParentNames(List<String> names, TaxonomyLevel level) {
		names.add(level.getDisplayName());
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentNames(names, parent);
		}
	}
	
	public void update(SearchEvent se) {
		displayName.setValue(se.getDisplayname());
		id.setValue(se.getId());
		author.setValue(se.getAuthor());
		ownedResourcesOnlyEl.select(keys[0], se.isOwnedResourcesOnly());
		description.setValue(se.getDescription());
		if(se.getResourceUsage() != null) {
			resourceUsageEl.select(se.getResourceUsage().name(), true);
		}
		if(se.getClosed() != null) {
			if(se.getClosed().booleanValue()) {
				closedEl.select(statusKeys[2], true);
			} else {
				closedEl.select(statusKeys[1], true);
			}
		} else {
			closedEl.select(statusKeys[0], true);
		}
		Set<String> selectedTypes = se.getTypes();
		if(selectedTypes != null && !selectedTypes.isEmpty()) {
			for(String typeKey: selectedTypes) {
				types.select(typeKey, true);
			}
		}
		
		Set<String> technicalTypes = se.getTechnicalTypes();
		if (technicalTypes != null && !technicalTypes.isEmpty()) {
			for (String technicalType : technicalTypes) {
				if (technicalTypeEl.getKeys().contains(technicalType)) {
					technicalTypeEl.select(technicalType, true);
				}
			}
		}
		
		Set<Long> educationalTypeKeys = se.getEducationalTypeKeys();
		if (educationalTypeKeys != null && !educationalTypeKeys.isEmpty()) {
			for (Long educationalTypeKey : educationalTypeKeys) {
				String key = educationalTypeKey.toString();
				if (educationalTypeEl.getKeys().contains(key)) {
					educationalTypeEl.select(key, true);
				}
			}
		}
		
		if (licenseModule.isEnabled(licenseHandler)) {
			for (Long licenseTypeKey: se.getLicenseTypeKeys()) {
				String key = String.valueOf(licenseTypeKey);
				licenseEl.select(key, true);
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public List<String> getConditionalQueries() {
		return Collections.emptyList();
	}

	/**
	 * @return Return value of ID field.
	 */
	public String getId() {
		return id.getValue();
	}

	/**
	 * @return Display name filed value.
	 */
	public String getDisplayName() {
		return displayName.getValue();
	}

	/**
	 * @return Author field value.
	 */
	public String getAuthor() {
		return author.getValue();
	}

	/**
	 * @return Description field value.
	 */
	public String getDescription() {
		return description.getValue();
	}

	/**
	 * @return Limiting type selections.
	 */
	public Set<String> getRestrictedTypes() {
		if(types.isAtLeastSelected(1)) {
			return new HashSet<>(types.getSelectedKeys());
		}
		return null;
	}
	
	public Set<String> getTechnicalTypes() {
		if (technicalTypeEl.isAtLeastSelected(1)) {
			return new HashSet<>(technicalTypeEl.getSelectedKeys());
		}
		return null;
	}
	
	public Set<Long> getEducationalTypeKeys() {
		if (educationalTypeEl.isAtLeastSelected(1)) {
			return educationalTypeEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.collect(Collectors.toSet());
		}
		return null;
	}
	
	public boolean isOwnedResourcesOnly() {
		return ownedResourcesOnlyEl.isAtLeastSelected(1);
	}
	
	public ResourceUsage getResourceUsage() {
		if(resourceUsageEl.isOneSelected()) {
			return ResourceUsage.valueOf(resourceUsageEl.getSelectedKey());
		}
		return ResourceUsage.all;
	}
	
	public Boolean getClosed() {
		Boolean status = null;
		if(closedEl.isOneSelected()) {
			int selected = closedEl.getSelected();
			if(selected == 1) {
				status = Boolean.FALSE;
			} else if(selected == 2) {
				status = Boolean.TRUE;
			}
		}
		return status;
	}
	
	public List<OrganisationRef> getEntryOrganisations() {
		List<OrganisationRef> organisations = new ArrayList<>();
		if(organisationsEl != null && organisationsEl.isVisible()) {
			for(String selectedKey:organisationsEl.getSelectedKeys()) {
				OrganisationRef org = organisationMap.get(selectedKey);
				if(org != null) {
					organisations.add(org);
				}
			}
		}
		return organisations;
	}
	
	private List<TaxonomyLevelRef> getTaxonomyLevels() {
		if (taxonomyLevelPathEl != null && taxonomyLevelPathEl.isVisible() && taxonomyLevelPathEl.isAtLeastSelected(1)) {
			return taxonomyLevelPathEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.map(TaxonomyLevelRefImpl::new)
					.collect(Collectors.toList());
		}
		return null;
	}
	
	@Override
	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if(!enabled) return true;
		
		author.clearError();
		if (displayName.isEmpty() && author.isEmpty() && description.isEmpty() && (id != null && id.isEmpty()))	{
			showWarning("cif.error.allempty");
			//return false;
		}
		
		int maxSize = dbInstance.isMySQL() ? 5 : 3;
		if(StringHelper.containsNonWhitespace(author.getValue()) && author.getValue().length() < maxSize) {
			author.setErrorKey("form.error.tooshort", new String[] { Integer.toString(maxSize) });
			//return false;
		}
		
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(enabled) {
			fireSearchEvent(ureq);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if(enabled && source == searchButton) {
			fireSearchEvent(ureq);
		}
	}
	
	private void fireSearchEvent(UserRequest ureq) {
		SearchEvent e = new SearchEvent();
		e.setId(getId());
		e.setAuthor(getAuthor());
		e.setDisplayname(getDisplayName());
		e.setDescription(getDescription());
		e.setTypes(getRestrictedTypes());
		e.setEducationalTypeKeys(getEducationalTypeKeys());
		e.setTechnicalTypes(getTechnicalTypes());
		e.setOwnedResourcesOnly(isOwnedResourcesOnly());
		e.setResourceUsage(getResourceUsage());
		e.setClosed(getClosed());
		e.setEntryOrganisations(getEntryOrganisations());
		e.setTaxonomyLevels(getTaxonomyLevels());
		if (licenseModule.isEnabled(licenseHandler)) {
			Set<Long> licenceKeys = licenseEl.getSelectedKeys().stream().map(Long::valueOf).collect(Collectors.toSet());
			e.setLicenseTypeKeys(licenceKeys);
		}
		fireEvent(ureq, e);
	}
	
	private String[] getTranslatedResources(List<String> resources) {
		List<String> l = new ArrayList<>();
		for(String key: resources){
			if(StringHelper.containsNonWhitespace(key)) {
				l.add(translate(key));
			} else {
				l.add("");
			}
		}
		return l.toArray(new String[0]);
	}
	
	private String[] getResourcesCSS(List<String> resources) {
		List<String> l = new ArrayList<>();
		for(String key: resources){
			if(StringHelper.containsNonWhitespace(key)) {
				l.add("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(key));
			} else {
				l.add("");
			}
		}
		return l.toArray(new String[0]);
	}
	
	private List<String> getResources() {
		List<String> resources = new ArrayList<>();
		for(OrderedRepositoryHandler handler:repositoryHandlerFactory.getOrderRepositoryHandlers()) {
			resources.add(handler.getHandler().getSupportedType());
		}
		return resources;
	}
}