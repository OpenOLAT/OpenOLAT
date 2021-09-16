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
package org.olat.modules.immunityproof.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.immunityproof.ImmunityProof;

/**
 * Initial date: 13.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofConfirmResetController extends FormBasicController {
	
	private static final String[] CONFIRMATION_KEYS = new String[]{"on"};

    private MultipleSelectionElement confirmationEl;
    private String resetKey;

    public ImmunityProofConfirmResetController(UserRequest ureq, WindowControl wControl, String resetKey) {
        super(ureq, wControl, LAYOUT_DEFAULT);

        setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
        this.resetKey = resetKey;

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        uifactory.addStaticTextElement("warning", null, translate("mail.reset.warning", new String[] {translate(resetKey)}), formLayout);

        confirmationEl = uifactory.addCheckboxesHorizontal("mail.reset.confirm", formLayout, CONFIRMATION_KEYS, new String[]{ translate("mail.reset.confirm.value" )});

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("reset.buttons", getTranslator());
        buttonLayout.setRootForm(mainForm);
        formLayout.add(buttonLayout);

        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
        uifactory.addFormSubmitButton("reset", buttonLayout);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);
        confirmationEl.clearError();

        if (!confirmationEl.isSelected(0)) {
            allOk = false;
            confirmationEl.setErrorKey("form.legende.mandatory", null);
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
