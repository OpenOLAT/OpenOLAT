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
package org.olat.admin.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.model.FindNamedIdentityCollection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UsermanagerUserBulkSearchForm extends FormBasicController {
	
	private FormLink searchButton;
	private TextAreaElement searchEl;
	
	private final List<Organisation> manageableOrganisations;
	
	@Autowired
	private BaseSecurity securityManager;
	
	public UsermanagerUserBulkSearchForm(UserRequest ureq, WindowControl wControl,
			List<Organisation> manageableOrganisations) {
		super(ureq, wControl);
		this.manageableOrganisations = new ArrayList<>(manageableOrganisations);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchEl = uifactory.addTextAreaElement("users.list", "users.list", -1, 12, 60, false, false, "", formLayout);
		searchEl.setLineNumbersEnbaled(true);
		// Don't use submit button, form should not be marked as dirty since this is
		// not a configuration form but only a search form (OLAT-5626)
		searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON);
		searchButton.addActionListener(FormEvent.ONCLICK);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	/**
	 * The method calculate the list of identities.
	 * 
	 * @return A deduplicated list of identities.
	 */
	public List<Identity> getUserList() {
		String lines = searchEl.getValue();
		List<String> identList = getLines(lines);
		FindNamedIdentityCollection identityCollection = securityManager.findAndCollectIdentitiesBy(identList, manageableOrganisations);
		Set<Identity> identities = new HashSet<>();
		if(identityCollection.getUnique() != null && !identityCollection.getUnique().isEmpty()) {
			identities.addAll(identityCollection.getUnique());
		}
		if(identityCollection.getAmbiguous() != null && !identityCollection.getAmbiguous().isEmpty()) {
			identities.addAll(identityCollection.getAmbiguous());
		}
		return new ArrayList<>(identities);
	}
	
	private List<String> getLines(String inp) {
		List<String> identList = new ArrayList<>();
		String[] lines = inp.split("\r?\n");
		for (int i = 0; i < lines.length; i++) {
			String username = lines[i].trim();
			if(username.length() > 0) {
				identList.add(username);
			}
		}
		return identList;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(searchButton == source) {
			fireEvent (ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
}
