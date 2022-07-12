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
package org.olat.course.nodes.zoom;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import static org.olat.modules.zoom.ZoomManager.ApplicationType.courseElement;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomEditConfig extends FormBasicController {

    private RepositoryEntry courseEntry;
    private String subIdent;

    private SingleSelection profileEl;
    private FormSubmit submit;

    @Autowired
    private ZoomManager zoomManager;

    public ZoomEditConfig(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, String subIdent) {
        super(ureq, wControl);
        this.courseEntry = courseEntry;
        this.subIdent = subIdent;
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("pane.tab.zoomConfig");

        zoomManager.initializeConfig(courseEntry, subIdent, null, courseElement);

        ZoomManager.KeysAndValues profiles = zoomManager.getProfilesAsKeysAndValues();
        profileEl = uifactory.addDropdownSingleselect("zoom.profile", formLayout, profiles.keys, profiles.values);
        ZoomConfig config = zoomManager.getConfig(courseEntry, subIdent, null);

        String profileKey = config.getProfile().getKey().toString();
        profileEl.select(profileKey, true);
        if (!profileEl.isOneSelected() && !profiles.isEmpty()) {
            profileEl.select(profiles.keys[0], true);
        }

        // Create submit button
        submit = uifactory.addFormSubmitButton("save", formLayout);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        if (profileEl.isOneSelected()) {
            ZoomProfile profile = zoomManager.getProfile(profileEl.getSelectedKey());
            ZoomConfig config = zoomManager.getConfig(courseEntry, subIdent, null);
            zoomManager.recreateConfig(config, courseEntry, subIdent, null, profile);
        }
        fireEvent(ureq, Event.DONE_EVENT);
    }
}
