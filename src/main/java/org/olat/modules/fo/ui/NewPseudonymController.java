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
package org.olat.modules.fo.ui;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
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
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Pseudonym;
import org.olat.modules.fo.manager.ForumManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewPseudonymController extends FormBasicController {

	private TextElement pseudonymEl, passwordEl;
	
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public NewPseudonymController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		pseudonymEl = uifactory.addTextElement("pseudonym", "new.pseudonym.label", 128, "", formLayout);
		pseudonymEl.setElementCssClass("o_sel_forum_alias");
		
		passwordEl = uifactory.addPasswordElement("password", "new.password.label", 128, "", formLayout);
		passwordEl.setElementCssClass("o_sel_forum_alias_pass");
		passwordEl.setAutocomplete("new-password");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("create.pseudonym", buttonsCont);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		pseudonymEl.clearError();
		passwordEl.clearError();
		String value = pseudonymEl.getValue();
		String password = passwordEl.getValue();
		
		if(StringHelper.containsNonWhitespace(value)) {
			List<Pseudonym> pseudonyms = forumManager.getPseudonyms(value);
			if(pseudonyms.size() > 0) {
				pseudonymEl.setErrorKey("error.pseudonym", null);
				allOk &= false;
			} else if(forumManager.isPseudonymInUseInForums(value)) {
				pseudonymEl.setErrorKey("error.pseudonym", null);
				allOk &= false;
			}
			
			if(allOk) {
				List<IdentityShort> sameValues = securityManager.searchIdentityShort(value, 2);
				if(sameValues.size() > 0) {
					pseudonymEl.setErrorKey("error.pseudonym", null);
					allOk &= false;
				}
			}
		} else {
			pseudonymEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(!StringHelper.containsNonWhitespace(password)) {
			passwordEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String value = pseudonymEl.getValue();
		String password = passwordEl.getValue();
		forumManager.createProtectedPseudonym(value, password);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
