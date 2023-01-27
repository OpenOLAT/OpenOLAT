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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OAIPmhAdminController extends FormBasicController {

    private static final LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);

    private static final String[] keys = new String[]{"on"};

    private FormLayoutContainer restrictionsCont, apiCont, buttonsCont;
    private FormToggle oaiPmhEl;
    private ExternalLinkItem testOaiEndpointLink;
    private StaticTextElement endpointEl;
    private SingleSelection identifierFormatEl;
    private MultipleSelectionElement licenseEl;
    private MultipleSelectionElement setTypeEl;
    //private MultipleSelectionElement searchEnginePublishEl;
    private MultiSelectionFilterElement licenseSelectionEl;

    @Autowired
    private OAIPmhModule oaiPmhModule;
    @Autowired
    private RepositoryEntryLicenseHandler repositoryEntryLicenseHandler;
    @Autowired
    private LicenseModule licenseModule;

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
                "oai.set.type.taxonomy",
                "oai.set.type.organisation",
                "oai.set.type.license",
                "oai.set.type.learningResource",
                "oai.set.type.release"};
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

        String[] licenseKeys = new String[]{"oai.license.allow", "oai.license.restrict"};
        String[] licenseValues = new String[]{translate("license.allow"), translate("license.restrict")};

        licenseEl = uifactory.addCheckboxesVertical("license.label", restrictionsCont, licenseKeys, licenseValues, 1);
        licenseEl.select(licenseKeys[0], oaiPmhModule.isLicenseAllow());
        licenseEl.select(licenseKeys[1], oaiPmhModule.isLicenseRestrict());
        licenseEl.addActionListener(FormEvent.ONCHANGE);
        licenseEl.setAjaxOnly(true); // to fix load after module enable

        List<LicenseType> license = licenseService.loadActiveLicenseTypes(repositoryEntryLicenseHandler);

        SelectionValues licenseSV = new SelectionValues();

        license.forEach(l -> licenseSV.add(new SelectionValues.SelectionValue(l.getKey().toString(), LicenseUIFactory.translate(l, getLocale()))));


        licenseSelectionEl = uifactory.addCheckboxesFilterDropdown("license.selected", "license.selected", restrictionsCont, getWindowControl(), licenseSV);
        licenseSelectionEl.setVisible(licenseEl.isKeySelected("oai.license.restrict"));

        oaiPmhModule.getLicenseSelectedRestrictions().forEach(lr -> licenseSelectionEl.select(lr, true));

        // 2) language restrictions
        //TODO
        

        /* TODO Not implemented yet
        // search engine
        FormLayoutContainer searchEngineCont = FormLayoutContainer.createDefaultFormLayout("oaiSearchEngine", getTranslator());
        searchEngineCont.setRootForm(mainForm);
        searchEngineCont.setFormTitle(translate("searchEngine.title"));
        formLayout.add(searchEngineCont);

        searchEnginePublishEl = uifactory.addCheckboxesHorizontal("searchEngine.label", searchEngineCont, keys, new String[]{translate("searchEngine.enable")});
        searchEnginePublishEl.select(keys[0], oaiPmhModule.isSearchEngineEnabled());*/

        buttonsCont = FormLayoutContainer.createDefaultFormLayout("buttonsCont", getTranslator());
        formLayout.add(buttonsCont);
        FormLayoutContainer buttonsInnerCont = FormLayoutContainer.createButtonLayout("buttonsInnerCont", getTranslator());
//        buttonsInnerCont.setRootForm(mainForm);
        buttonsCont.add(buttonsInnerCont);
        uifactory.addFormSubmitButton("save", buttonsInnerCont);
        uifactory.addFormCancelButton("cancel", buttonsInnerCont, ureq, getWindowControl());

        // Everything initialized, update visibility of form elements
    	updateContainerVisibility();
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
	    } else if (source == licenseEl) {
            if (licenseEl.isKeySelected("oai.license.restrict")) {
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
        }
        super.formInnerEvent(ureq, source, event);
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
        buttonsCont.setVisible(oaiPmhModule.isEnabled());
        testOaiEndpointLink.setVisible(oaiPmhModule.isEnabled());
    }

    @Override
    protected void formOK(UserRequest ureq) {
        oaiPmhModule.setEnabled(oaiPmhEl.isOn());
        oaiPmhModule.setIdentifierFormat(identifierFormatEl.getSelectedKey());

        if (licenseEl.isEnabled()) {
            oaiPmhModule.setLicenseAllow(licenseEl.isKeySelected("oai.license.allow"));
            oaiPmhModule.setLicenseRestrict(licenseEl.isKeySelected("oai.license.restrict"));
            if (licenseEl.isKeySelected("oai.license.restrict")) {
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
            oaiPmhModule.setSetTypeTaxonomy(setTypeEl.isKeySelected("oai.set.type.taxonomy"));
            oaiPmhModule.setSetTypeOrganisation(setTypeEl.isKeySelected("oai.set.type.organisation"));
            oaiPmhModule.setSetTypeLicense(setTypeEl.isKeySelected("oai.set.type.license"));
            oaiPmhModule.setSetTypeLearningResource(setTypeEl.isKeySelected("oai.set.type.learningResource"));
            oaiPmhModule.setSetTypeRelease(setTypeEl.isKeySelected("oai.set.type.release"));
        }
        /*if (searchEnginePublishEl.isEnabled()) {
            oaiPmhModule.setSearchEngineEnabled(searchEnginePublishEl.isSelected(0));
        }*/
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }
}
