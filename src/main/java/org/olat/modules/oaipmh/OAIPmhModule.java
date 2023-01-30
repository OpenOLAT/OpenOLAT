/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.oaipmh;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.lyncode.builder.ListBuilder;
import org.olat.NewControllerFactory;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class OAIPmhModule extends AbstractSpringModule {

	private static final String CONFIG_OAI_ENABLED = "oai.enabled";
	private static final String CONFIG_LICENSE_ALLOW = "oai.license.allow";
	private static final String CONFIG_LICENSE_RESTRICT = "oai.license.restrict";
	private static final String CONFIG_SET_TAXONOMY = "oai.set.type.taxonomy";
	private static final String CONFIG_SET_ORGANISATION = "oai.set.type.organisation";
	private static final String CONFIG_SET_LICENSE = "oai.set.type.license";
	private static final String CONFIG_SET_LEARNINGRESOURCE = "oai.set.type.learningResource";
	private static final String CONFIG_SET_RELEASE = "oai.set.type.release";
	private static final String CONFIG_SEARCHENGINE_ENABLED = "oai.searchengine.enabled";
	private static final String CONFIG_SELECTED_LICENSE_RESTRICTIONS = "oai.license.selectedRestrictions";
	private static final String CONFIG_OAI_IDENTIFIER_FORMAT = "oai.identifier.format";

	@Value("${oai.enabled}")
	private boolean enabled;
	@Value("${oai.license.allow}")
	private boolean licenseAllowOnly;
	@Value("${oai.license.restrict}")
	private boolean licenseSpecificRestrict;
	@Value("${oai.set.type.taxonomy}")
	private boolean setTypeTaxonomy;
	@Value("${oai.set.type.organisation}")
	private boolean setTypeOrganisation;
	@Value("${oai.set.type.license}")
	private boolean setTypeLicense;
	@Value("${oai.set.type.learningResource}")
	private boolean setTypeLearningResource;
	@Value("${oai.set.type.release}")
	private boolean setTypeRelease;
	@Value("${oai.searchengine.enabled}")
	private boolean searchEngineEnabled;
	@Value("${oai.license.selectedRestrictions}")
	private String licenseSelectedRestrictions;
	@Value("${oai.identifier.format}")
	private String identifierFormat;

	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;


	public OAIPmhModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj;

		enabledObj = getStringPropertyValue(CONFIG_OAI_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_LICENSE_ALLOW, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			licenseAllowOnly = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_LICENSE_RESTRICT, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			licenseSpecificRestrict = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_SET_TAXONOMY, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			setTypeTaxonomy = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_SET_ORGANISATION, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			setTypeOrganisation = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_SET_LICENSE, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			setTypeLicense = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_SET_LEARNINGRESOURCE, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			setTypeLearningResource = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_SET_RELEASE, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			setTypeRelease = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_SEARCHENGINE_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			searchEngineEnabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_SELECTED_LICENSE_RESTRICTIONS, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			licenseSelectedRestrictions = enabledObj;
		}
		enabledObj = getStringPropertyValue(CONFIG_OAI_IDENTIFIER_FORMAT, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			identifierFormat = enabledObj;
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(CONFIG_OAI_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isLicenseAllowOnly() {
		return licenseAllowOnly;
	}

	public void setLicenseAllowOnly(boolean licenseAllowOnly) {
		this.licenseAllowOnly = licenseAllowOnly;
		setStringProperty(CONFIG_LICENSE_ALLOW, Boolean.toString(licenseAllowOnly), true);
	}

	public boolean isLicenseSpecificRestrict() {
		return licenseSpecificRestrict;
	}

	public void setLicenseSpecificRestrict(boolean licenseSpecificRestrict) {
		this.licenseSpecificRestrict = licenseSpecificRestrict;
		setStringProperty(CONFIG_LICENSE_RESTRICT, Boolean.toString(licenseSpecificRestrict), true);
	}

	public boolean isSetTypeTaxonomy() {
		return setTypeTaxonomy;
	}

	public void setSetTypeTaxonomy(boolean setTypeTaxonomy) {
		this.setTypeTaxonomy = setTypeTaxonomy;
		setStringProperty(CONFIG_SET_TAXONOMY, Boolean.toString(setTypeTaxonomy), true);
	}

	public boolean isSetTypeOrganisation() {
		return setTypeOrganisation;
	}

	public void setSetTypeOrganisation(boolean setTypeOrganisation) {
		this.setTypeOrganisation = setTypeOrganisation;
		setStringProperty(CONFIG_SET_ORGANISATION, Boolean.toString(setTypeOrganisation), true);
	}

	public boolean isSetTypeLicense() {
		return setTypeLicense;
	}

	public void setSetTypeLicense(boolean setTypeLicense) {
		this.setTypeLicense = setTypeLicense;
		setStringProperty(CONFIG_SET_LICENSE, Boolean.toString(setTypeLicense), true);
	}

	public boolean isSetTypeLearningResource() {
		return setTypeLearningResource;
	}

	public void setSetTypeLearningResource(boolean setTypeLearningResource) {
		this.setTypeLearningResource = setTypeLearningResource;
		setStringProperty(CONFIG_SET_LEARNINGRESOURCE, Boolean.toString(setTypeLearningResource), true);
	}

	public boolean isSetTypeRelease() {
		return setTypeRelease;
	}

	public void setSetTypeRelease(boolean setTypeRelease) {
		this.setTypeRelease = setTypeRelease;
		setStringProperty(CONFIG_SET_RELEASE, Boolean.toString(setTypeRelease), true);
	}

	public boolean isSearchEngineEnabled() {
		return searchEngineEnabled;
	}

	public void setSearchEngineEnabled(boolean searchEngineEnabled) {
		this.searchEngineEnabled = searchEngineEnabled;
		setStringProperty(CONFIG_SEARCHENGINE_ENABLED, Boolean.toString(searchEngineEnabled), true);
	}

	public List<String> getLicenseSelectedRestrictions() {
		List<String> licenseRestrictions = new ArrayList<>();

		if (StringHelper.containsNonWhitespace(licenseSelectedRestrictions)) {
			List<String> lrArr = Arrays.stream(
							licenseSelectedRestrictions.split("[,]"))
					.map(l -> l.replace("[", "").replace("]", "").replace(" ", "")).toList();
			for (String lR : lrArr) {
				if (StringHelper.containsNonWhitespace(lR)) {
					licenseRestrictions.add(lR);
				}
			}
		}
		return licenseRestrictions;
	}

	public void setLicenseSelectedRestrictions(List<String> licenseSelectedRestrictions) {
		this.licenseSelectedRestrictions = licenseSelectedRestrictions.toString();
		setStringProperty(CONFIG_SELECTED_LICENSE_RESTRICTIONS, licenseSelectedRestrictions.toString(), true);
	}

	public String getIdentifierFormat() {
		return identifierFormat;
	}

	public void setIdentifierFormat(String identifierFormat) {
		this.identifierFormat = identifierFormat;
	}

	public ListBuilder<String> getSetSpecByRepositoryEntry(
			RepositoryEntry repositoryEntry,
			ResourceLicense license,
			List<Organisation> organisationList) {
		ListBuilder<String> setSpec = new ListBuilder<>();

		if (isSetTypeTaxonomy()) {
			List<String> taxonomyLevels =
					repositoryEntry.getTaxonomyLevels().stream().map(t -> t.getTaxonomyLevel().getMaterializedPathIdentifiers()).toList();

			for (String taxonomyLevel : taxonomyLevels) {
				setSpec.add("taxon:" + taxonomyLevel.substring(1, taxonomyLevel.length() - 1));
			}
		}
		if (isSetTypeOrganisation()) {
			String organisation = Arrays.stream(OrganisationUIFactory.createSelectionValues(organisationList).values()).findFirst().get().replace(" ", "");

			setSpec.add("org:" + organisation);
		}
		if (isSetTypeLicense()) {
			if (license != null && license.getLicenseType().isOerLicense()) {
				setSpec.add("license:" + license.getLicenseType().getName());
			}
		}
		if (isSetTypeLearningResource()) {
			String type = NewControllerFactory.translateResourceableTypeName(repositoryEntry.getOlatResource().getResourceableTypeName(), Locale.getDefault());
			setSpec.add("type:" + type);
		}
		if (isSetTypeRelease()) {
			for (Offer offer : getOffers(repositoryEntry.getOlatResource())) {
				List<OfferAccess> offerAccess = acService.getOfferAccess(offer, true);
				for (OfferAccess access : offerAccess) {
					AccessMethodHandler handler = acModule.getAccessMethodHandler(access.getMethod().getType());
					setSpec.add("offering:" + handler.getType());
				}
			}
		}

		return setSpec;
	}

	public boolean isIndexingRestricted(ResourceLicense license) {
		return ((isLicenseAllowOnly() &&
				(license == null || license.getLicenseType().getName().equals("no.license")))
				|| (isLicenseSpecificRestrict() &&
				(license == null || !getLicenseSelectedRestrictions().contains(license.getLicenseType().getKey().toString()))));
	}

	public List<Offer> getOffers(OLATResource olatResource) {
		return acService.findOfferByResource(olatResource, true, null, null);
	}
}
