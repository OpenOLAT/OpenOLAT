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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingConfirmResetController extends FormBasicController {

    private static final String[] CONFIRMATION_KEYS = new String[]{"on"};

    private final String warning;
    private final String confirmation;

    private StaticTextElement warningEl;
    private MultipleSelectionElement confirmationEl;

    public ContactTracingConfirmResetController(UserRequest ureq, WindowControl wControl, String warning, String confirmation) {
        super(ureq, wControl, LAYOUT_DEFAULT);

        this.warning = warning;
        this.confirmation = confirmation;

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        warningEl = uifactory.addStaticTextElement("warning", null, warning, formLayout);
        // warningEl.setElementCssClass("o_error");

        confirmationEl = uifactory.addCheckboxesHorizontal("contact.tracing.reset.confirmation", "contact.tracing.reset.confirmation.label", formLayout, CONFIRMATION_KEYS, new String[]{confirmation});

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("contact.tracing.reset.buttons", getTranslator());
        buttonLayout.setRootForm(mainForm);
        formLayout.add(buttonLayout);

        uifactory.addFormCancelButton("contact.tracing.reset.cancel", buttonLayout, ureq, getWindowControl());
        uifactory.addFormSubmitButton("contact.tracing.reset", buttonLayout);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);
        confirmationEl.clearError();

        if (!confirmationEl.isSelected(0)) {
            allOk = false;
            confirmationEl.setErrorKey("contact.tracing.reset.mandatory", null);
        }

        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        fireEvent(ureq, FormEvent.DONE_EVENT);
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, FormEvent.CANCELLED_EVENT);
    }

    @Override
    protected void doDispose() {

    }
}
