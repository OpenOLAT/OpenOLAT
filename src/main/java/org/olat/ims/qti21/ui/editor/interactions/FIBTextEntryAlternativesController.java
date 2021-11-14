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
package org.olat.ims.qti21.ui.editor.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;

import uk.ac.ed.ph.jqtiplus.internal.util.StringUtilities;

/**
 * 
 * Initial date: 26 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FIBTextEntryAlternativesController extends FormBasicController {
	
	private TextElement variantsEl;
	private TextElement separatorEl;
	
	public FIBTextEntryAlternativesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		variantsEl = uifactory.addTextElement("fib.alternatives", 255, "", formLayout);
		
		separatorEl = uifactory.addTextElement("fib.alternatives.separator", 1, ";", formLayout);
		separatorEl.setElementCssClass("form-inline");
		separatorEl.setDisplaySize(1);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("add", buttonsCont);
	}
	
	public List<String> getAlternatives() {
		String val = variantsEl.getValue();
		String sep = separatorEl.getValue();
		List<String> variants = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(val) && StringHelper.containsNonWhitespace(sep) && sep.length() == 1) {
			for(StringTokenizer tokenizer = new StringTokenizer(val, sep, false); tokenizer.hasMoreTokens(); ) {
				String variant = tokenizer.nextToken();
				if(StringHelper.containsNonWhitespace(variant)) {
					variants.add(StringUtilities.trim(variant));
				}
			}
		}
		return variants;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateTextElement(variantsEl);
		allOk &= validateTextElement(separatorEl);
		return allOk;
	}
	
	private boolean validateTextElement(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(!StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
