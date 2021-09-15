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
 * Initial date: 15.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofCreateConfirmController extends FormBasicController {
	
	private static final String[] CONFIRMATION_KEYS = new String[]{"on"};

    private MultipleSelectionElement confirmationEl;

	public ImmunityProofCreateConfirmController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl, LAYOUT_VERTICAL);

        setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormWarning("confirm.date.already.exists");
        
        confirmationEl = uifactory.addCheckboxesHorizontal("confirm.date.label", formLayout, CONFIRMATION_KEYS, new String[]{ translate("confirm.date.value" )});

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
        buttonLayout.setRootForm(mainForm);
        formLayout.add(buttonLayout);

        uifactory.addFormSubmitButton("confirm.date.override", buttonLayout);
        uifactory.addFormCancelButton("confirm.date.keep", buttonLayout, ureq, getWindowControl());
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
