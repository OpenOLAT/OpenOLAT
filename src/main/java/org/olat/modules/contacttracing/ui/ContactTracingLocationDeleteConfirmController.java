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

import java.util.List;

import org.olat.core.gui.*;
import org.olat.core.gui.components.form.flexible.*;
import org.olat.core.gui.components.form.flexible.impl.*;
import org.olat.core.gui.control.*;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationDeleteConfirmController extends FormBasicController {

    private List<ContactTracingLocation> deleteLocations;

    @Autowired
    private ContactTracingManager contactTracingManager;

    public ContactTracingLocationDeleteConfirmController(UserRequest ureq, WindowControl wControl, List<ContactTracingLocation> deleteLocations) {
        super(ureq, wControl);

        this.deleteLocations = deleteLocations;

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // TODO ADD FORM

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        buttonLayout.setRootForm(mainForm);
        formLayout.add(buttonLayout);

        uifactory.addFormCancelButton("contact.tracing.location.delete.confirm.cancel", buttonLayout, ureq, getWindowControl());
        uifactory.addFormSubmitButton("contact.tracing.location.delete.confirm.submit", buttonLayout);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        contactTracingManager.deleteLocations(deleteLocations);

        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void doDispose() {

    }
}
