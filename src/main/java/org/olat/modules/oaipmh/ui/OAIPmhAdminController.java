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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OAIPmhAdminController extends FormBasicController {

    private static final LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);

    private static final String[] keys = new String[]{"on"};

    private FormToggle oaiPmhEl;
    private MultipleSelectionElement licenseEl;
    private MultipleSelectionElement apiTypeEl;
    private MultipleSelectionElement searchEnginePublishEl;
    private MultiSelectionFilterElement licenseSelectionEl;

    @Autowired
    private OAIPmhModule oaiPmhModule;

    public OAIPmhAdminController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl, LAYOUT_VERTICAL);

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        FormLayoutContainer oaipmhCont = FormLayoutContainer.createDefaultFormLayout("oaipmh", getTranslator());
        oaipmhCont.setFormTitle(translate("oai.title"));
        oaipmhCont.setFormInfo(translate("oaipmh.desc"));
        oaipmhCont.setFormInfoHelp("manual_admin/administration/Modules_OAI/");
        oaipmhCont.setFormContextHelp("manual_admin/administration/Modules_OAI/");
        formLayout.add(oaipmhCont);

        oaiPmhEl = uifactory.addToggleButton("oaipmh.module", translate("oaipmh.module"), "&nbsp;&nbsp;", oaipmhCont, null, null);

        if (oaiPmhModule.isEnabled()) {
            oaiPmhEl.toggleOn();
        } else {
            oaiPmhEl.toggleOff();
        }

        oaiPmhEl.addActionListener(FormEvent.ONCHANGE);

        String endpointUrl = "<span class='o_copy_code o_nowrap'><input type='text' value='" + Settings.getServerContextPathURI() + "/oaipmh" + "' onclick='this.select()'/></span>";
        uifactory.addStaticTextElement("oaipmh.endpoint.uri", endpointUrl, oaipmhCont);

        // license restrictions
        FormLayoutContainer licenseCont = FormLayoutContainer.createDefaultFormLayout("oaiLicense", getTranslator());
        licenseCont.setFormTitle(translate("license.title"));
        formLayout.add(licenseCont);

        String[] licenseKeys = new String[]{"oai.license.allow", "oai.license.restrict"};
        String[] licenseValues = new String[]{translate("license.allow"), translate("license.restrict")};

        licenseEl = uifactory.addCheckboxesVertical("license.label", licenseCont, licenseKeys, licenseValues, 1);
        licenseEl.select(licenseKeys[0], oaiPmhModule.isLicenseAllow());
        licenseEl.select(licenseKeys[1], oaiPmhModule.isLicenseRestrict());
        licenseEl.addActionListener(FormEvent.ONCHANGE);

        List<LicenseType> license = licenseService.loadLicenseTypes();

        SelectionValues licenseSV = new SelectionValues();

        license.forEach(l -> licenseSV.add(new SelectionValues.SelectionValue(l.getKey().toString(), l.getName())));

        licenseSelectionEl = uifactory.addCheckboxesFilterDropdown("license.selected", "license.selected", licenseCont, getWindowControl(), licenseSV);
        licenseSelectionEl.setVisible(licenseEl.isKeySelected("oai.license.restrict"));

        oaiPmhModule.getLicenseSelectedRestrictions().forEach(lr -> licenseSelectionEl.select(lr, true));

        // api configuration
        FormLayoutContainer apiCont = FormLayoutContainer.createDefaultFormLayout("oaiApi", getTranslator());
        apiCont.setFormTitle(translate("api.title"));
        formLayout.add(apiCont);

        String[] apiTypeKeys = new String[]{
                "oai.api.type.taxonomy",
                "oai.api.type.organisation",
                "oai.api.type.license",
                "oai.api.type.learningResource",
                "oai.api.type.release"};
        String[] apiTypeValues = new String[]{
                translate("api.type.taxonomy"),
                translate("api.type.organisation"),
                translate("api.type.license"),
                translate("api.type.learningResource"),
                translate("api.type.release")};

        apiTypeEl = uifactory.addCheckboxesVertical("api.label", apiCont, apiTypeKeys, apiTypeValues, 1);
        apiTypeEl.select(apiTypeKeys[0], oaiPmhModule.isApiTypeTaxonomy());
        apiTypeEl.select(apiTypeKeys[1], oaiPmhModule.isApiTypeOrganisation());
        apiTypeEl.select(apiTypeKeys[2], oaiPmhModule.isApiTypeLicense());
        apiTypeEl.select(apiTypeKeys[3], oaiPmhModule.isApiTypeLearningResource());
        apiTypeEl.select(apiTypeKeys[4], oaiPmhModule.isApiTypeRelease());

        // search engine
        FormLayoutContainer searchEngineCont = FormLayoutContainer.createDefaultFormLayout("oaiSearchEngine", getTranslator());
        searchEngineCont.setRootForm(mainForm);
        searchEngineCont.setFormTitle(translate("searchEngine.title"));
        formLayout.add(searchEngineCont);

        searchEnginePublishEl = uifactory.addCheckboxesHorizontal("searchEngine.label", searchEngineCont, keys, new String[]{translate("searchEngine.enable")});
        searchEnginePublishEl.select(keys[0], oaiPmhModule.isSearchEngineEnabled());

        FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
        buttonsCont.setRootForm(mainForm);
        searchEngineCont.add(buttonsCont);
        uifactory.addFormSubmitButton("save", buttonsCont);
        uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
    }

    private void saveSelectedLicenseRestrictions() {
        List<String> selectedLicenseRestrictions = licenseSelectionEl.getSelectedKeys().stream().toList();
        oaiPmhModule.setLicenseSelectedRestrictions(selectedLicenseRestrictions);
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == licenseEl) {
            if (licenseEl.isKeySelected("oai.license.restrict")) {
                licenseSelectionEl.setVisible(true);
            } else {
                oaiPmhModule.setLicenseSelectedRestrictions(new ArrayList<>());
                licenseSelectionEl.uncheckAll();
                licenseSelectionEl.setVisible(false);
            }
        }

        super.formInnerEvent(ureq, source, event);
    }


    @Override
    protected void formOK(UserRequest ureq) {
        oaiPmhModule.setEnabled(oaiPmhEl.isOn());

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
        if (apiTypeEl.isEnabled()) {
            oaiPmhModule.setApiTypeTaxonomy(apiTypeEl.isKeySelected("oai.api.type.taxonomy"));
            oaiPmhModule.setApiTypeOrganisation(apiTypeEl.isKeySelected("oai.api.type.organisation"));
            oaiPmhModule.setApiTypeLicense(apiTypeEl.isKeySelected("oai.api.type.license"));
            oaiPmhModule.setApiTypeLearningResource(apiTypeEl.isKeySelected("oai.api.type.learningResource"));
            oaiPmhModule.setApiTypeRelease(apiTypeEl.isKeySelected("oai.api.type.release"));
        }
        if (searchEnginePublishEl.isEnabled()) {
            oaiPmhModule.setSearchEngineEnabled(searchEnginePublishEl.isSelected(0));
        }
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }
}
