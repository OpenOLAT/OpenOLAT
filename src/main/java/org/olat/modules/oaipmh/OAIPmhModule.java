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


import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class OAIPmhModule extends AbstractSpringModule {

    private static final Logger log = Tracing.createLoggerFor(OAIPmhModule.class);
    private static final String CONFIG_OAI_ENABLED = "oai.enabled";
    private static final String CONFIG_LICENSE_ALLOW = "oai.license.allow";
    private static final String CONFIG_LICENSE_RESTRICT = "oai.license.restrict";
    private static final String CONFIG_API_TAXONOMY = "oai.api.type.taxonomy";
    private static final String CONFIG_API_ORGANISATION = "oai.api.type.organisation";
    private static final String CONFIG_API_LICENSE = "oai.api.type.license";
    private static final String CONFIG_API_LEARNINGRESOURCE = "oai.api.type.learningResource";
    private static final String CONFIG_API_RELEASE = "oai.api.type.release";
    private static final String CONFIG_SEARCHENGINE_ENABLED = "oai.searchengine.enabled";
    private static final String CONFIG_SELECTED_LICENSE_RESTRICTIONS = "oai.license.selectedRestrictions";

    @Value("${oai.enabled}")
    private boolean enabled;
    @Value("${oai.license.allow}")
    private boolean licenseAllow;
    @Value("${oai.license.restrict}")
    private boolean licenseRestrict;
    @Value("${oai.api.type.taxonomy}")
    private boolean apiTypeTaxonomy;
    @Value("${oai.api.type.organisation}")
    private boolean apiTypeOrganisation;
    @Value("${oai.api.type.license}")
    private boolean apiTypeLicense;
    @Value("${oai.api.type.learningResource}")
    private boolean apiTypeLearningResource;
    @Value("${oai.api.type.release}")
    private boolean apiTypeRelease;
    @Value("${oai.searchengine.enabled}")
    private boolean searchEngineEnabled;

    @Value("${oai.license.selectedRestrictions}")
    private String licenseSelectedRestrictions;


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
            licenseAllow = "true".equals(enabledObj);
        }
        enabledObj = getStringPropertyValue(CONFIG_LICENSE_RESTRICT, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            licenseRestrict = "true".equals(enabledObj);
        }
        enabledObj = getStringPropertyValue(CONFIG_API_TAXONOMY, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            apiTypeTaxonomy = "true".equals(enabledObj);
        }
        enabledObj = getStringPropertyValue(CONFIG_API_ORGANISATION, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            apiTypeOrganisation = "true".equals(enabledObj);
        }
        enabledObj = getStringPropertyValue(CONFIG_API_LICENSE, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            apiTypeLicense = "true".equals(enabledObj);
        }
        enabledObj = getStringPropertyValue(CONFIG_API_LEARNINGRESOURCE, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            apiTypeLearningResource = "true".equals(enabledObj);
        }
        enabledObj = getStringPropertyValue(CONFIG_API_RELEASE, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            apiTypeRelease = "true".equals(enabledObj);
        }
        enabledObj = getStringPropertyValue(CONFIG_SEARCHENGINE_ENABLED, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            searchEngineEnabled = "true".equals(enabledObj);
        }
        enabledObj = getStringPropertyValue(CONFIG_SELECTED_LICENSE_RESTRICTIONS, true);
        if (StringHelper.containsNonWhitespace(enabledObj)) {
            licenseSelectedRestrictions = enabledObj;
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

    public boolean isLicenseAllow() {
        return licenseAllow;
    }

    public void setLicenseAllow(boolean licenseAllow) {
        this.licenseAllow = licenseAllow;
        setStringProperty(CONFIG_LICENSE_ALLOW, Boolean.toString(licenseAllow), true);
    }

    public boolean isLicenseRestrict() {
        return licenseRestrict;
    }

    public void setLicenseRestrict(boolean licenseRestrict) {
        this.licenseRestrict = licenseRestrict;
        setStringProperty(CONFIG_LICENSE_RESTRICT, Boolean.toString(licenseRestrict), true);
    }

    public boolean isApiTypeTaxonomy() {
        return apiTypeTaxonomy;
    }

    public void setApiTypeTaxonomy(boolean apiTypeTaxonomy) {
        this.apiTypeTaxonomy = apiTypeTaxonomy;
        setStringProperty(CONFIG_API_TAXONOMY, Boolean.toString(apiTypeTaxonomy), true);
    }

    public boolean isApiTypeOrganisation() {
        return apiTypeOrganisation;
    }

    public void setApiTypeOrganisation(boolean apiTypeOrganisation) {
        this.apiTypeOrganisation = apiTypeOrganisation;
        setStringProperty(CONFIG_API_ORGANISATION, Boolean.toString(apiTypeOrganisation), true);
    }

    public boolean isApiTypeLicense() {
        return apiTypeLicense;
    }

    public void setApiTypeLicense(boolean apiTypeLicense) {
        this.apiTypeLicense = apiTypeLicense;
        setStringProperty(CONFIG_API_LICENSE, Boolean.toString(apiTypeLicense), true);
    }

    public boolean isApiTypeLearningResource() {
        return apiTypeLearningResource;
    }

    public void setApiTypeLearningResource(boolean apiTypeLearningResource) {
        this.apiTypeLearningResource = apiTypeLearningResource;
        setStringProperty(CONFIG_API_LEARNINGRESOURCE, Boolean.toString(apiTypeLearningResource), true);
    }

    public boolean isApiTypeRelease() {
        return apiTypeRelease;
    }

    public void setApiTypeRelease(boolean apiTypeRelease) {
        this.apiTypeRelease = apiTypeRelease;
        setStringProperty(CONFIG_API_RELEASE, Boolean.toString(apiTypeRelease), true);
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
}
