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
package org.olat.modules.oaipmh.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.validator.routines.UrlValidator;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.modules.oaipmh.OAIService;
import org.olat.repository.ResourceInfoDispatcher;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OAIPmhAdminController extends FormBasicController {

	private static final String OAI_KEY_LICENSE_ALLOW = "oai.license.allow";
	private static final String OAI_KEY_LICENSE_RESTRICT = "oai.license.restrict";
	private static final String OAI_KEY_SEARCHENGINE_BING = "oai.searchengine.bing";
	private static final String OAI_KEY_SEARCHENGINE_CUSTOM_INDEXNOW = "oai.searchengine.custom.indexnow";
	private static final String OAI_KEY_SEARCHENGINE_CUSTOM_SITEMAP = "oai.searchengine.custom.sitemap";
	private static final String OAI_KEY_SEARCHENGINE_GOOGLE = "oai.searchengine.google";
	private static final String OAI_KEY_SEARCHENGINE_YANDEX = "oai.searchengine.yandex";
	private static final String OAI_KEY_SET_TYPE_LEARNING_RESOURCE = "oai.set.type.learningResource";
	private static final String OAI_KEY_SET_TYPE_LICENSE = "oai.set.type.license";
	private static final String OAI_KEY_SET_TYPE_ORGANISATION = "oai.set.type.organisation";
	private static final String OAI_KEY_SET_TYPE_RELEASE = "oai.set.type.release";
	private static final String OAI_KEY_SET_TYPE_TAXONOMY = "oai.set.type.taxonomy";

	private FormLayoutContainer restrictionsCont;
	private FormLayoutContainer apiCont;
	private FormLayoutContainer searchEngineCont;
	private FormLayoutContainer buttonsCont;
	private FormToggle oaiPmhEl;
	private ExternalLinkItem testOaiEndpointLink;
	private FormLink searchEnginePublishLink;
	private StaticTextElement endpointEl;
	private SingleSelection identifierFormatEl;
	private MultipleSelectionElement licenseEl;
	private MultipleSelectionElement setTypeEl;
	private FormToggle searchEnginePublishEl;
	private MultipleSelectionElement searchEngineSitemapEl;
	private MultipleSelectionElement searchEngineIndexnowEl;
	private TextElement googleTextEl;
	private TextElement bingTextEl;
	private TextElement yandexTextEl;
	private TextElement customSitemapTextEl;
	private TextElement customIndexTextEl;
	private MultiSelectionFilterElement licenseSelectionEl;

	@Autowired
	private OAIPmhModule oaiPmhModule;
	@Autowired
	private RepositoryEntryLicenseHandler repositoryEntryLicenseHandler;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private OAIService oaiService;

	public OAIPmhAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("oai.title");
		setFormInfo("oaipmh.desc");
		setFormInfoHelp("manual_admin/administration/Modules_OAI/");
		setFormContextHelp("manual_admin/administration/Modules_OAI/");

		if (oaiPmhModule.getUuid().equals("none")) {
			oaiPmhModule.setUuid(UUID.randomUUID().toString());
		}

		// Render the on/off toggle standalone in it's own container. Other containers are visible depending on module activation
		FormLayoutContainer oaipmhConfigContainer = FormLayoutContainer.createDefaultFormLayout("oaipmhConfigContainer", getTranslator());
		formLayout.add(oaipmhConfigContainer);
		oaiPmhEl = uifactory.addToggleButton("oaipmh.module", translate("oaipmh.module"), "&nbsp;&nbsp;", oaipmhConfigContainer, null, null);
		oaiPmhEl.addActionListener(FormEvent.ONCHANGE);

		if (oaiPmhModule.isEnabled()) {
			oaiPmhEl.toggleOn();
		} else {
			oaiPmhEl.toggleOff();
		}

		// Add endpoint URL
		String endpointUrl = "<span class='o_copy_code o_nowrap'><input type='text' value='" + Settings.getServerContextPathURI() + "/oaipmh" + "' onclick='this.select()'/></span>";
		endpointEl = uifactory.addStaticTextElement("oaipmh.endpoint.uri", endpointUrl, oaipmhConfigContainer);

		String testUrl = Settings.getServerContextPathURI() + "/oaipmh?" + "verb=listRecords";
		testOaiEndpointLink = uifactory.addExternalLink("oaipmh.endpoint.test", testUrl, "_blank", oaipmhConfigContainer);
		testOaiEndpointLink.setName(translate("oaipmh.endpoint.test"));
		testOaiEndpointLink.setCssClass("btn btn-default");

		// Section for API configuration
		apiCont = FormLayoutContainer.createDefaultFormLayout("apiCont", getTranslator());
		apiCont.setFormTitle(translate("api.title"));
		formLayout.add(apiCont);

		SelectionValues identifierFormatSV = new SelectionValues();
		String domain = Settings.getServerDomainName();
		identifierFormatSV.add(entry("oai", translate("oai.identifier.format.oai", domain)));
		identifierFormatSV.add(entry("url", translate("oai.identifier.format.url", domain)));
		identifierFormatEl = uifactory.addRadiosVertical("oai.identifier.format", apiCont, identifierFormatSV.keys(), identifierFormatSV.values());
		identifierFormatEl.select(oaiPmhModule.getIdentifierFormat(), true);

		String[] setTypeKeys = new String[]{
				OAI_KEY_SET_TYPE_TAXONOMY,
				OAI_KEY_SET_TYPE_ORGANISATION,
				OAI_KEY_SET_TYPE_LICENSE,
				OAI_KEY_SET_TYPE_LEARNING_RESOURCE,
				OAI_KEY_SET_TYPE_RELEASE};
		String[] setTypeValues = new String[]{
				translate("set.type.taxonomy"),
				translate("set.type.organisation"),
				translate("set.type.license"),
				translate("set.type.learningResource"),
				translate("set.type.release")};

		setTypeEl = uifactory.addCheckboxesVertical("set.label", apiCont, setTypeKeys, setTypeValues, 1);
		setTypeEl.select(setTypeKeys[0], oaiPmhModule.isSetTypeTaxonomy());
		setTypeEl.select(setTypeKeys[1], oaiPmhModule.isSetTypeOrganisation());
		setTypeEl.select(setTypeKeys[2], oaiPmhModule.isSetTypeLicense());
		setTypeEl.select(setTypeKeys[3], oaiPmhModule.isSetTypeLearningResource());
		setTypeEl.select(setTypeKeys[4], oaiPmhModule.isSetTypeRelease());
		setTypeEl.setAjaxOnly(true); // to fix load after module enable

		// Section for restrictions
		// 1) license restrictions
		restrictionsCont = FormLayoutContainer.createDefaultFormLayout("restrictionsCont", getTranslator());
		restrictionsCont.setFormTitle(translate("license.title"));
		formLayout.add(restrictionsCont);

		String[] licenseKeys = new String[]{OAI_KEY_LICENSE_ALLOW, OAI_KEY_LICENSE_RESTRICT};
		String[] licenseValues = new String[]{translate("license.allow"), translate("license.restrict")};

		licenseEl = uifactory.addCheckboxesVertical("license.label", restrictionsCont, licenseKeys, licenseValues, 1);
		licenseEl.select(licenseKeys[0], oaiPmhModule.isLicenseAllowOnly());
		licenseEl.select(licenseKeys[1], oaiPmhModule.isLicenseSpecificRestrict());
		licenseEl.addActionListener(FormEvent.ONCHANGE);
		licenseEl.setAjaxOnly(true); // to fix load after module enable

		List<LicenseType> license = licenseService.loadActiveLicenseTypes(repositoryEntryLicenseHandler);
		SelectionValues licenseSV = new SelectionValues();
		license.forEach(l -> licenseSV.add(new SelectionValues.SelectionValue(l.getKey().toString(), LicenseUIFactory.translate(l, getLocale()))));

		licenseSelectionEl = uifactory.addCheckboxesFilterDropdown("license.selected", "license.selected", restrictionsCont, getWindowControl(), licenseSV);
		licenseSelectionEl.setVisible(licenseEl.isKeySelected(OAI_KEY_LICENSE_RESTRICT));

		oaiPmhModule.getLicenseSelectedRestrictions().forEach(lr -> licenseSelectionEl.select(lr, true));

		// 2) language restrictions
		//TODO

		// search engine
		searchEngineCont = FormLayoutContainer.createDefaultFormLayout("oaiSearchEngine", getTranslator());
		searchEngineCont.setRootForm(mainForm);
		searchEngineCont.setFormTitle(translate("searchengine.title"));
		searchEngineCont.setFormInfo(translate("searchengine.info"));
		formLayout.add(searchEngineCont);

		searchEnginePublishEl = uifactory.addToggleButton("searchengine.label", translate("searchengine.label"), "&nbsp;&nbsp;", searchEngineCont, null, null);
		searchEnginePublishEl.setHelpText(translate("searchengine.enable"));

		if (oaiPmhModule.isSearchEngineEnabled()) {
			searchEnginePublishEl.toggleOn();
		} else {
			searchEnginePublishEl.toggleOff();
		}

		// TODO /resourceinfo/ -> session cookies abstellen -> möglich?
		// TODO Link -> NoFollow -> for searchengine, möglich?

		String sitemapUrl = ResourceInfoDispatcher.getUrl("sitemap.xml");
		String[] sitemapKeys = new String[]{OAI_KEY_SEARCHENGINE_GOOGLE, OAI_KEY_SEARCHENGINE_CUSTOM_SITEMAP};
		String[] sitemapValues = new String[]{translate("searchengine.url.google"), translate("searchengine.url.custom.sitemap")};

		searchEngineSitemapEl = uifactory.addCheckboxesVertical("searchengine.provider.sitemap", searchEngineCont, sitemapKeys, sitemapValues, 1);
		searchEngineSitemapEl.setHelpTextKey("searchengine.provider.sitemap.help", new String[]{sitemapUrl});
		searchEngineSitemapEl.setHelpUrl("https://developers.google.com/search/docs/crawling-indexing/sitemaps/overview?hl=en");

		googleTextEl = uifactory.addTextElement("searchengine.url.google", 255, oaiPmhModule.getSearchEngineGoogleUrl(), searchEngineCont);
		customSitemapTextEl = uifactory.addTextElement("searchengine.url.custom.sitemap", 255, oaiPmhModule.getSearchEngineCustomSitemapUrl(), searchEngineCont);
		searchEngineSitemapEl.select(sitemapKeys[0], oaiPmhModule.isSearchEngineGoogle());
		searchEngineSitemapEl.select(sitemapKeys[1], oaiPmhModule.isSearchEngineCustomSitemap());

		uifactory.addSpacerElement("spacer", searchEngineCont, false);

		String[] indexnowKeys = new String[]{OAI_KEY_SEARCHENGINE_BING, OAI_KEY_SEARCHENGINE_YANDEX, OAI_KEY_SEARCHENGINE_CUSTOM_INDEXNOW};
		String[] indexnowValues = new String[]{translate("searchengine.url.bing"), translate("searchengine.url.yandex"), translate("searchengine.url.custom.indexnow")};

		searchEngineIndexnowEl = uifactory.addCheckboxesVertical("searchengine.provider.indexnow", searchEngineCont, indexnowKeys, indexnowValues, 1);
		searchEngineIndexnowEl.setHelpUrl("https://www.indexnow.org/index");

		bingTextEl = uifactory.addTextElement("oai.searchengine.bing.url", "oai.searchengine.bing.url", "searchengine.url.bing", 255, oaiPmhModule.getSearchEngineBingUrl(), searchEngineCont);
		yandexTextEl = uifactory.addTextElement("searchengine.url.yandex", 255, oaiPmhModule.getSearchEngineYandexUrl(), searchEngineCont);
		customIndexTextEl = uifactory.addTextElement("searchengine.url.custom.indexnow", 255, oaiPmhModule.getSearchEngineCustomIndexnowUrl(), searchEngineCont);
		searchEngineIndexnowEl.select(indexnowKeys[0], oaiPmhModule.isSearchEngineBing());
		searchEngineIndexnowEl.select(indexnowKeys[1], oaiPmhModule.isSearchEngineYandex());
		searchEngineIndexnowEl.select(indexnowKeys[2], oaiPmhModule.isSearchEngineCustomIndexnow());

		searchEnginePublishLink = uifactory.addFormLink("searchengine.publish", searchEngineCont, Link.BUTTON);

		// Form buttons
		buttonsCont = uifactory.addButtonsFormLayout("buttonsCont", null, searchEngineCont);
		FormLayoutContainer buttonsInnerCont = FormLayoutContainer.createButtonLayout("buttonsInnerCont", getTranslator());
		buttonsCont.add(buttonsInnerCont);
		uifactory.addFormSubmitButton("save", buttonsInnerCont);
		uifactory.addFormCancelButton("cancel", buttonsInnerCont, ureq, getWindowControl());

		// Everything initialized, update visibility of form elements
		updateContainerVisibility();
		updateSearchEngineContainerVisibility();
		// dont
		this.mainForm.setHideDirtyMarkingMessage(true);
	}

	private void saveSelectedLicenseRestrictions() {
		List<String> selectedLicenseRestrictions = licenseSelectionEl.getSelectedKeys().stream().toList();
		oaiPmhModule.setLicenseSelectedRestrictions(selectedLicenseRestrictions);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == oaiPmhEl) {
			// enable / disable entire module and trigger visibility of config form
			oaiPmhModule.setEnabled(oaiPmhEl.isOn());
			updateContainerVisibility();
		} else if (source == searchEnginePublishEl) {
			oaiPmhModule.setSearchEngineEnabled(searchEnginePublishEl.isOn());
			updateSearchEngineContainerVisibility();
		} else if (source == licenseEl) {
			if (licenseEl.isKeySelected(OAI_KEY_LICENSE_RESTRICT)) {
				licenseSelectionEl.setVisible(true);
			} else {
				oaiPmhModule.setLicenseSelectedRestrictions(new ArrayList<>());
				licenseSelectionEl.uncheckAll();
				licenseSelectionEl.setVisible(false);
			}
			if (licenseEl.isAtLeastSelected(1)
					&& !licenseModule.isEnabled(repositoryEntryLicenseHandler)) {
				restrictionsCont.setFormWarning(translate("license.restriction.warning"));
			} else {
				restrictionsCont.setFormWarning(null);
			}
		} else if (source == searchEnginePublishLink) {
			List<String> urlList = new ArrayList<>();
			if (searchEngineSitemapEl.isKeySelected(OAI_KEY_SEARCHENGINE_GOOGLE)
					&& isValidInputUrl(googleTextEl.getValue())) {
				urlList.add(googleTextEl.getValue());
			}
			if (searchEngineSitemapEl.isKeySelected(OAI_KEY_SEARCHENGINE_CUSTOM_SITEMAP)
					&& isValidInputUrl(customSitemapTextEl.getValue())) {
				urlList.add(customSitemapTextEl.getValue());
			}
			if (searchEngineIndexnowEl.isKeySelected(OAI_KEY_SEARCHENGINE_BING)
					&& isValidInputUrl(bingTextEl.getValue())) {
				urlList.add(bingTextEl.getValue());
			}
			if (searchEngineIndexnowEl.isKeySelected(OAI_KEY_SEARCHENGINE_YANDEX)
					&& isValidInputUrl(yandexTextEl.getValue())) {
				urlList.add(yandexTextEl.getValue());
			}
			if (searchEngineIndexnowEl.isKeySelected(OAI_KEY_SEARCHENGINE_CUSTOM_INDEXNOW)
					&& isValidInputUrl(customIndexTextEl.getValue())) {
				urlList.add(customIndexTextEl.getValue());
			}

			if (!urlList.isEmpty()) {
				showInfo("searchengine.response", oaiService.propagateSearchEngines(urlList).toString());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private boolean isValidInputUrl(String url) {
		boolean allOk = false;
		UrlValidator urlValidator = new UrlValidator(new String[]{"https"});

		// check first on ServerDomainName, because we don't want test servers getting indexed
		if (!Settings.getServerDomainName().equals("testing.frentix.com")
				&& StringHelper.containsNonWhitespace(url)
				&& urlValidator.isValid(url)
				&& (url.contains("sitemap") || url.contains("indexnow"))) {
			allOk = true;
		}

		return allOk;
	}

	private void updateSearchEngineContainerVisibility() {
		searchEngineSitemapEl.setVisible(oaiPmhModule.isSearchEngineEnabled());
		searchEngineIndexnowEl.setVisible(oaiPmhModule.isSearchEngineEnabled());
		googleTextEl.setVisible(oaiPmhModule.isSearchEngineEnabled());
		bingTextEl.setVisible(oaiPmhModule.isSearchEngineEnabled());
		customSitemapTextEl.setVisible(oaiPmhModule.isSearchEngineEnabled());
		customIndexTextEl.setVisible(oaiPmhModule.isSearchEngineEnabled());
		yandexTextEl.setVisible(oaiPmhModule.isSearchEngineEnabled());
		searchEnginePublishLink.setVisible(oaiPmhModule.isSearchEngineEnabled());
	}

	/**
	 * helper to enable / disable visibility of form parts containers depending
	 * on module activation
	 */
	private void updateContainerVisibility() {
		// make everything below the module toggle switch visible/invisible
		endpointEl.setVisible(oaiPmhModule.isEnabled());
		endpointEl.getComponent().setDirty(false);
		restrictionsCont.setVisible(oaiPmhModule.isEnabled());
		apiCont.setVisible(oaiPmhModule.isEnabled());
		searchEngineCont.setVisible(oaiPmhModule.isEnabled());
		buttonsCont.setVisible(oaiPmhModule.isEnabled());
		testOaiEndpointLink.setVisible(oaiPmhModule.isEnabled());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		oaiPmhModule.setEnabled(oaiPmhEl.isOn());
		oaiPmhModule.setIdentifierFormat(identifierFormatEl.getSelectedKey());

		if (licenseEl.isEnabled()) {
			oaiPmhModule.setLicenseAllowOnly(licenseEl.isKeySelected(OAI_KEY_LICENSE_ALLOW));
			oaiPmhModule.setLicenseSpecificRestrict(licenseEl.isKeySelected(OAI_KEY_LICENSE_RESTRICT));
			if (licenseEl.isKeySelected(OAI_KEY_LICENSE_RESTRICT)) {
				licenseSelectionEl.setVisible(true);
			} else {
				oaiPmhModule.setLicenseSelectedRestrictions(new ArrayList<>());
				licenseSelectionEl.uncheckAll();
				licenseSelectionEl.setVisible(false);
			}
		}
		if (licenseSelectionEl.isEnabled()) {
			saveSelectedLicenseRestrictions();
		}
		if (setTypeEl.isEnabled()) {
			oaiPmhModule.setSetTypeTaxonomy(setTypeEl.isKeySelected(OAI_KEY_SET_TYPE_TAXONOMY));
			oaiPmhModule.setSetTypeOrganisation(setTypeEl.isKeySelected(OAI_KEY_SET_TYPE_ORGANISATION));
			oaiPmhModule.setSetTypeLicense(setTypeEl.isKeySelected(OAI_KEY_SET_TYPE_LICENSE));
			oaiPmhModule.setSetTypeLearningResource(setTypeEl.isKeySelected(OAI_KEY_SET_TYPE_LEARNING_RESOURCE));
			oaiPmhModule.setSetTypeRelease(setTypeEl.isKeySelected(OAI_KEY_SET_TYPE_RELEASE));
		}
		if (searchEnginePublishEl.isOn()) {
			oaiPmhModule.setSearchEngineBing(searchEngineIndexnowEl.isKeySelected(OAI_KEY_SEARCHENGINE_BING));
			oaiPmhModule.setSearchEngineBingUrl(bingTextEl.getValue());
			oaiPmhModule.setSearchEngineCustomSitemap(searchEngineSitemapEl.isKeySelected(OAI_KEY_SEARCHENGINE_CUSTOM_SITEMAP));
			oaiPmhModule.setSearchEngineCustomSitemapUrl(customSitemapTextEl.getValue());
			oaiPmhModule.setSearchEngineCustomIndexnow(searchEngineIndexnowEl.isKeySelected(OAI_KEY_SEARCHENGINE_CUSTOM_INDEXNOW));
			oaiPmhModule.setSearchEngineCustomIndexnowUrl(customIndexTextEl.getValue());
			oaiPmhModule.setSearchEngineGoogle(searchEngineSitemapEl.isKeySelected(OAI_KEY_SEARCHENGINE_GOOGLE));
			oaiPmhModule.setSearchEngineGoogleUrl(googleTextEl.getValue());
			oaiPmhModule.setSearchEngineYandex(searchEngineIndexnowEl.isKeySelected(OAI_KEY_SEARCHENGINE_YANDEX));
			oaiPmhModule.setSearchEngineYandexUrl(yandexTextEl.getValue());
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
