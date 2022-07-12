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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.lti13.manager.LTI13IDGenerator;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomProfileEditController extends FormBasicController {

    private static final String[] onKeys = new String[]{ "on" };

    private ZoomProfile zoomProfile;
    private final String clientId;
    private final String token;

    private TextElement profileNameEl;
    private TextElement ltiKeyEl;
    private TextElement mailDomainsEl;
    private MultipleSelectionElement studentsCanHostEl;
    private StaticTextElement clientEl;
    private StaticTextElement tokenEl;

    @Autowired
    private LTI13IDGenerator idGenerator;

    @Autowired
    private ZoomManager zoomManager;

    public ZoomProfileEditController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        clientId = idGenerator.newId();
        token = idGenerator.newId().substring(0, 8);
        initForm(ureq);
    }

    public ZoomProfileEditController(UserRequest ureq, WindowControl wControl, ZoomProfile zoomProfile) {
        super(ureq, wControl);
        this.zoomProfile = zoomProfile;
        clientId = zoomProfile.getLtiTool().getClientId();
        token = zoomProfile.getToken();
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormDescription("zoom.profile.description");
        String profileName = zoomProfile == null ? null : zoomProfile.getName();
        profileNameEl = uifactory.addTextElement("zoom.profile.name", "zoom.profile.name", 255, profileName, formLayout);
        profileNameEl.setMandatory(true);

        String ltiKey = zoomProfile == null ? null : zoomProfile.getLtiKey();
        ltiKeyEl = uifactory.addTextElement("zoom.profile.ltiKey", "zoom.profile.ltiKey", 255, ltiKey, formLayout);
        ltiKeyEl.setHelpTextKey("zoom.profile.ltiKey.help", null);
        ltiKeyEl.setMandatory(true);

        String mailDomains = zoomProfile == null ? null : zoomProfile.getMailDomains();
        mailDomainsEl = uifactory.addTextElement("zoom.profile.mailDomains", "zoom.profile.mailDomains", 1024, mailDomains, formLayout);

        boolean studentsCanHost = zoomProfile == null ? false : zoomProfile.isStudentsCanHost();
        String[] checkboxValues = new String[] { getTranslator().translate("zoom.profile.enabledInZoomApp") };
        studentsCanHostEl = uifactory.addCheckboxesHorizontal("zoom.profile.studentsCanHost",
                "zoom.profile.studentsCanHost", formLayout, onKeys, checkboxValues);
        studentsCanHostEl.select(onKeys[0], studentsCanHost);

        clientEl = uifactory.addStaticTextElement("zoom.profile.clientId", clientId, formLayout);
        clientEl.setHelpTextKey("zoom.profile.clientId.help", null);
        clientEl.setElementCssClass("text-muted");

        tokenEl = uifactory.addStaticTextElement("zoom.profile.token", token, formLayout);
        tokenEl.setHelpTextKey("zoom.profile.token.help", new String[0]);
        tokenEl.setElementCssClass("text-muted");

        String toolUrl = zoomProfile == null ? "" : zoomProfile.getLtiTool().getToolUrl();
        uifactory.addStaticTextElement("zoom.profile.toolUrl", toolUrl, formLayout)
                .setElementCssClass("text-muted");

        FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
        formLayout.add("buttons", buttons);
        uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());
        uifactory.addFormSubmitButton("save", buttons);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        if (zoomProfile == null) {
            String name = profileNameEl.getValue();
            String ltiKey = ltiKeyEl.getValue();
            zoomProfile = zoomManager.createProfile(name, ltiKey, clientId, token);
        } else {
            zoomProfile.setName(profileNameEl.getValue());
            zoomProfile.setLtiKey(ltiKeyEl.getValue());
        }

        zoomProfile.setMailDomains(mailDomainsEl.getValue());
        zoomProfile.setStudentsCanHost(studentsCanHostEl.isAtLeastSelected(1));
        zoomProfile = zoomManager.updateProfile(zoomProfile);

        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }
}
