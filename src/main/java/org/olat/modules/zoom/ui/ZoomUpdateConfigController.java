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
package org.olat.modules.zoom.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomUpdateConfigController extends FormBasicController {

    private final RepositoryEntry courseEntry;
    private final String subIdent;
    private final BusinessGroup businessGroup;
    private final ZoomManager.ApplicationType applicationType;

    private SingleSelection profileEl;
    private FormSubmit submit;

    @Autowired
    private ZoomManager zoomManager;

    public ZoomUpdateConfigController(UserRequest ureq, WindowControl wControl,
                                      RepositoryEntry courseEntry, String subIdent, BusinessGroup businessGroup,
                                      ZoomManager.ApplicationType applicationType) {
        super(ureq, wControl);
        this.courseEntry = courseEntry;
        this.subIdent = subIdent;
        this.businessGroup = businessGroup;
        this.applicationType = applicationType;
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        if (applicationType == ZoomManager.ApplicationType.courseElement) {
            setFormTitle("zoom.configuration.title"); // to be consistent with containing element
        } else {
            setFormTitle("zoom.configure.title");
        }

        zoomManager.initializeConfig(courseEntry, subIdent, businessGroup, applicationType, getIdentity().getUser());

        ZoomManager.KeysAndValues profiles = zoomManager.getProfilesAsKeysAndValues();
        profileEl = uifactory.addDropdownSingleselect("zoom.profile", formLayout, profiles.keys, profiles.values);
        profileEl.addActionListener(FormEvent.ONCHANGE);
        ZoomConfig zoomConfig = zoomManager.getConfig(courseEntry, subIdent, businessGroup);

        String profileKey = zoomConfig.getProfile().getKey().toString();
        profileEl.select(profileKey, true);
        if (!profileEl.isOneSelected() && !profiles.isEmpty()) {
            profileEl.select(profiles.keys[0], true);
        }
        if (StringHelper.containsNonWhitespace(zoomConfig.getProfile().getMailDomains())) {
            profileEl.setExampleKey("zoom.profile.mailDomains.example", new String[] { zoomConfig.getProfile().getMailDomains() });
        }

        // Create submit button
        submit = uifactory.addFormSubmitButton("submit", formLayout);
        submit.setElementCssClass("o_sel_update_zoom");
    }

    public void setEnabled(boolean enabled) {
        submit.setVisible(enabled);
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        super.formInnerEvent(ureq, source, event);

        if (source == profileEl) {
            ZoomProfile zoomProfile = zoomManager.getProfile(profileEl.getSelectedKey());
            if (StringHelper.containsNonWhitespace(zoomProfile.getMailDomains())) {
                profileEl.setExampleKey("zoom.profile.mailDomains.example", new String[] { zoomProfile.getMailDomains() });
            } else {
                profileEl.setExampleKey(null, null);
            }
        }
    }

    @Override
    protected void formOK(UserRequest ureq) {
        if (profileEl.isOneSelected()) {
            ZoomProfile zoomProfile = zoomManager.getProfile(profileEl.getSelectedKey());
            ZoomConfig zoomConfig = zoomManager.getConfig(null, null, businessGroup);
            if (StringHelper.containsNonWhitespace(zoomProfile.getMailDomains())) {
                profileEl.setExampleKey("zoom.profile.mailDomains.example", new String[] { zoomProfile.getMailDomains() });
            } else {
                profileEl.setExampleKey(null, null);
            }
            zoomManager.recreateConfig(zoomConfig, null, null, businessGroup, zoomProfile);
        }
        fireEvent(ureq, Event.DONE_EVENT);
    }
}
