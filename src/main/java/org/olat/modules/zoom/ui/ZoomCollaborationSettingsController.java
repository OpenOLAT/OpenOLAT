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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomCollaborationSettingsController extends FormBasicController {

    private final BusinessGroup businessGroup;

    private SingleSelection profileEl;
    private FormSubmit submit;

    @Autowired
    private ZoomManager zoomManager;

    public ZoomCollaborationSettingsController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
        super(ureq, wControl);
        this.businessGroup = businessGroup;
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("collaboration.access.title");

        ZoomManager.KeysAndValues profiles = zoomManager.getProfilesAsKeysAndValues();
        profileEl = uifactory.addDropdownSingleselect("zoom.profile", formLayout, profiles.keys, profiles.values);
        ZoomConfig zoomConfig = zoomManager.getConfig(null, null, businessGroup);

        String profileKey = zoomConfig.getProfile().getKey().toString();
        profileEl.select(profileKey, true);
        if (!profileEl.isOneSelected() && !profiles.isEmpty()) {
            profileEl.select(profiles.keys[0], true);
        }

        // Create submit button
        submit = uifactory.addFormSubmitButton("submit", formLayout);
        submit.setElementCssClass("o_sel_collaboration_zoom_save");
    }

    public void setEnabled(boolean enabled) {
        submit.setVisible(enabled);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        if (profileEl.isOneSelected()) {
            ZoomProfile zoomProfile = zoomManager.getProfile(profileEl.getSelectedKey());
            ZoomConfig zoomConfig = zoomManager.getConfig(null, null, businessGroup);
            zoomManager.recreateConfig(zoomConfig, null, null, businessGroup, zoomProfile);
        }
        fireEvent(ureq, Event.DONE_EVENT);
    }
}
