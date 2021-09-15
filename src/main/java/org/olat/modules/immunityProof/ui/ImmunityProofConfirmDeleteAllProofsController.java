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
package org.olat.modules.immunityProof.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.immunityProof.ImmunityProof;
import org.olat.modules.immunityProof.ui.event.ImmunityProofDeleteEvent;

/**
 * Initial date: 15.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofConfirmDeleteAllProofsController extends FormBasicController {

	private static final String[] CONFIRMATION_KEYS = new String[]{"confirm", "notify"};

    private MultipleSelectionElement confirmationEl;

    public ImmunityProofConfirmDeleteAllProofsController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl, LAYOUT_DEFAULT);

        setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
    	setFormWarning("delete.proof.warning");

        String[] values = new String[]{ translate("delete.proof.confirm.value" ), translate("delete.proofs.and.notify")};
        confirmationEl = uifactory.addCheckboxesHorizontal("delete.proof.confirm", formLayout, CONFIRMATION_KEYS, values);

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("reset.buttons", getTranslator());
        buttonLayout.setRootForm(mainForm);
        formLayout.add(buttonLayout);

        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
        uifactory.addFormSubmitButton("delete.all.proofs", buttonLayout);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);
        confirmationEl.clearError();

        if (!confirmationEl.isKeySelected("confirm")) {
            allOk = false;
            confirmationEl.setErrorKey("form.legende.mandatory", null);
        }

        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
    	if (confirmationEl.isKeySelected("notify")) {
    		fireEvent(ureq, ImmunityProofDeleteEvent.DELETE_AND_NOTIFY);
    	} else {
    		fireEvent(ureq, ImmunityProofDeleteEvent.DELETE);
    	}
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, ImmunityProofDeleteEvent.CANCELLED_EVENT);
    }

    @Override
    protected void doDispose() {

    }

}
