/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SearchElement;
import org.olat.core.gui.components.form.flexible.elements.SearchVariant;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.SearchFormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.modules.coach.ui.component.SearchEvent;

/**
 * 
 * Initial date: 6 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CoachMainSearchHeaderController extends FormBasicController {
	
	private SearchElement searchEl;
	private FormLink searchUsersLink;
	
	private final boolean userSearchAllowed;
	
	public CoachMainSearchHeaderController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "coaching_search_field");
		

		Roles roles = ureq.getUserSession().getRoles();
		userSearchAllowed = roles.isAdministrator() || roles.isLearnResourceManager() || roles.isPrincipal();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchEl = uifactory.addSearchElement("searchWrapper", SearchVariant.LARGE, formLayout);
		searchEl.setPlaceholderKey("search.header.placeholder.coach");

		searchUsersLink = uifactory.addFormLink("coaching.search.users", "coaching.search.users", null, formLayout, Link.LINK);
		searchUsersLink.setVisible(userSearchAllowed);
	}
	
	public String getSearchString() {
		return searchEl.getValue();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(searchEl == source && event instanceof SearchFormEvent sfe
				&& !SearchFormEvent.RESET.equals(sfe.getCommand())) {
			fireEvent(ureq, new SearchEvent(SearchEvent.SEARCH, searchEl.getValue()));
		} else if(searchUsersLink == source) {
			fireEvent(ureq, new SearchEvent(SearchEvent.SEARCH_USERS));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new SearchEvent(SearchEvent.SEARCH, searchEl.getValue()));
	}
}
