/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.admin.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date: May 5. 2024
 *
 * 
 * Comment: Subworkflow that allows the user to search for a single or multiple
 * users by a single search field. The searchable fields are configurable in the
 * UsermanagerUserSearchController user properties context. <br>
 * In addition, the search will also search for identity keys (if it is a
 * number) and in the users external ID (ID of an external system if generated
 * by a syncher) <br>
 * The search supports multi value search. If multiple search terms are found,
 * the search is performed in all fields using an "or" syntax.
 * 
 * Events:<br>
 * Fires a DONE event whenever something as been entered. Use the getUserList()
 * method to get the found identities.
 * 
 * @author Florian Gnaegi
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class UserAdminQuickSearchController extends FormBasicController {
	
	private TextElement searchEl;
	private FormLink searchLink;
	private List<UserPropertyHandler> userSearchFormPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private IdentityPowerSearchQueries identitySearchQueries;

	/**
	 * Create a quick search controller
	 * @param ureq
	 * @param wControl
	 */
	public UserAdminQuickSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_DEFAULT);	
		init(ureq);
		initForm(ureq);
	}
		
	private void init(UserRequest ureq) {		
		List<UserPropertyHandler> allSearchFormPropertyHandlers = userManager.getUserPropertyHandlersFor(UserAdminQuickSearchController.class.getCanonicalName(), true);
		userSearchFormPropertyHandlers = allSearchFormPropertyHandlers.stream()
				.filter(prop -> !UserConstants.NICKNAME.equals(prop.getName()))// admin. has the login search field
				.collect(Collectors.toList());		
	}

	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer searchCont = FormLayoutContainer.createInputGroupLayout("searchWrapper", getTranslator(), null, null);
		formLayout.add("searchWrapper", searchCont);
		searchCont.setRootForm(mainForm);
		
		searchEl = uifactory.addTextElement("quick.search", "quick.search", 64, null, searchCont);
		searchEl.setAriaLabel("quick.search");
		searchEl.setFocus(true);
		
		searchLink = uifactory.addFormLink("rightAddOn", "", "", searchCont, Link.NONTRANSLATED);
		searchLink.setElementCssClass("input-group-addon");
		searchLink.setCustomEnabledLinkCSS("o_user_quick_search_button o_undecorated");
		searchLink.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		String searchLabel = getTranslator().translate("quick.search");
		searchLink.setLinkTitle(searchLabel);
		searchLink.setI18nKey(searchLabel);
		
		uifactory.addSpacerElement("searchSpacer", formLayout, false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Triggered when pressing return
		String searchValue = searchEl.getValue();
		if(StringHelper.containsNonWhitespace(searchValue)) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		// Triggered when clicking the quick-search link
		if (source == searchLink) {
			String searchValue = searchEl.getValue();
			if(StringHelper.containsNonWhitespace(searchValue)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
		
	/**
	 * Can be called to get the list of users the user searched for
	 * 
	 * @return List of identities. List can be empty, but never NULL. 
	 */
	List<Identity> getUserList() {
		List<Identity> searchResult = new ArrayList<Identity>();
		List<Organisation> searchableOrganisations = organisationService.getOrganisations(getIdentity(), new OrganisationRoles[] { OrganisationRoles.usermanager, OrganisationRoles.rolesmanager,  OrganisationRoles.administrator });
		
		String searchElValue = searchEl.getValue();
		if(StringHelper.containsNonWhitespace(searchElValue)) {
			searchElValue = searchElValue.trim();

			// 1) Support multi-value search: perform a search for each search term separately
			String[] searchValues = searchElValue.split(" ");
			for (String searchValue : searchValues) {
				searchValue = searchValue.trim();
				if(StringHelper.containsNonWhitespace(searchElValue)) {
					// 1) Search in user properties
					Map<String, String> userProperties = new HashMap<>();
					for (UserPropertyHandler handler : userSearchFormPropertyHandlers) {
						userProperties.put(handler.getName(), searchValue);
					}
					
					SearchIdentityParams params = new SearchIdentityParams(searchValue,
							userProperties, false, null, null, null, null, null, null, null, null);			
					params.setOrganisations(searchableOrganisations);					

					searchResult.addAll(identitySearchQueries.getIdentitiesByPowerSearch(params, 0, -1));

					// 2) Search in identity key and external ID					
					// need to have at least one user property in the list to make the power search work, just use an empty one
					userProperties = new HashMap<>();
					userProperties.put(UserConstants.FIRSTNAME, ""); 
					
					params = new SearchIdentityParams(searchValue,
							userProperties, false, null, null, null, null, null, null, null, null);			
					params.setOrganisations(searchableOrganisations);					
					params.setIdAndExternalIds(searchElValue);
					
					searchResult.addAll(identitySearchQueries.getIdentitiesByPowerSearch(params, 0, -1));
				}				
			}
		}
		
		return searchResult;		
	}	


}