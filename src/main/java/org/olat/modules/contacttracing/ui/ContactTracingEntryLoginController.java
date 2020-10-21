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
package org.olat.modules.contacttracing.ui;

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.manager.ContactTracingManagerImpl;

/**
 * Initial date: 20.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingEntryLoginController extends FormBasicController {

    private final ContactTracingLocation location;

    private FormLink registerAsGuestLink;

    public ContactTracingEntryLoginController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location) {
        super(ureq, wControl, LAYOUT_DEFAULT);

        this.location = location;

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("contact.tracing.general.title");
        setFormDescription("contact.tracing.registration.login.description");

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        buttonLayout.setRootForm(mainForm);
        formLayout.add(buttonLayout);

        uifactory.addFormSubmitButton("contact.tracing.registration.login.account", buttonLayout);
        registerAsGuestLink = uifactory.addFormLink("contact.tracing.registration.login.guest", buttonLayout, Link.BUTTON);
        uifactory.addFormCancelButton("contact.tracing.registration.login.cancel", buttonLayout, ureq, getWindowControl());
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == registerAsGuestLink) {
            // Login as guest
        }
    }

    @Override
    protected void formOK(UserRequest ureq) {
        // Redirect to contact tracing
        String redirectURL = new StringBuilder()
                .append(Settings.getServerContextPathURI())
                .append("/auth/")
                .append(ContactTracingManagerImpl.CONTACT_TRACING_CONTEXT_KEY)
                .append("/")
                .append(location.getKey())
                .toString();

        //DispatcherModule.redirectTo(ureq.getHttpResp(), redirectURL);
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        // Redirect to default dispatcher
        DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
    }

    @Override
    protected void doDispose() {

    }
}
