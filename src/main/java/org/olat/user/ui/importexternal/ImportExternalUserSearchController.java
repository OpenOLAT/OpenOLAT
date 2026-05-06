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
package org.olat.user.ui.importexternal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.OAuth2Tokens;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.modules.selectus.model.ExternalUserResults;
import org.olat.modules.teams.manager.MicrosoftGraphDAO;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.importexternal.ImportExternalUserSearchDataModel.ImportCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportExternalUserSearchController extends FormBasicController {

	public static final int OFFSET_INDEX = 10000;
	public static final String USER_PROPS = "org.olat.ldap.ui.ImportExternalUserSearchController";
	
	private TextElement emailEl;
	private FormLink selectButton;
	private FlexiTableElement tableEl;
	private ImportExternalUserSearchDataModel dataModel;
	
	private Identity importedIdentity;
	private final boolean withSearchField;
	private final boolean externalUsersOnly;
	private final boolean withMultiSelection;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private Set<String> excludedProviders;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LDAPLoginModule ldapModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	@Autowired
	private WebDAVAuthManager webdavAuthManager;
	@Autowired
	private MicrosoftGraphDAO microsoftGraphDao;
	
	/**
	 * Search LDAP / Azure for users.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public ImportExternalUserSearchController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, true, false, true, true);
	}

	public ImportExternalUserSearchController(UserRequest ureq, WindowControl wControl,
			boolean withSearchField, boolean withMultiSelection, boolean externalUserOnly, boolean forImport) {
		super(ureq, wControl, "search");
		this.withSearchField = withSearchField;
		this.externalUsersOnly = externalUserOnly;
		this.withMultiSelection = withMultiSelection;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS, true);
		initForm(ureq);
		if(forImport) {
			flc.contextPut("msg", translate("import.ldap.user.explain"));
		} else {
			flc.contextPut("msg", translate("search.ldap.user.explain"));
		}
		
		if(oauthLoginModule.isAzureAdfsEnabled() && oauthLoginModule.isAzureLookupEnabled() && ureq.getUserSession().getOAuth2Tokens() == null) {
			flc.contextPut("warningToken", translate("warning.azure.without.token"));
		}
		
		Set<String> providersToExclude = new HashSet<>();
		providersToExclude.addAll(webdavAuthManager.getProviderNames());
		providersToExclude.add("RSS-OLAT");
		providersToExclude.add("REST");
		excludedProviders = Set.copyOf(providersToExclude);
	}
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param searchString
	 */
	public ImportExternalUserSearchController(UserRequest ureq, WindowControl wControl, String searchString, boolean forImport) {
		this(ureq, wControl, false, false, false, forImport);
		initForm(ureq);
		doSearch(ureq, searchString);
	}
	
	public Identity getImportedIdentity() {
		return importedIdentity;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		emailEl = uifactory.addTextElement("email", "email", 255, "", formLayout);
		emailEl.setDomReplacementWrapperRequired(false);
		emailEl.setFocus(true);
		emailEl.setVisible(withSearchField);
		
		FormSubmit searchButton = uifactory.addFormSubmitButton("search", "search", formLayout);
		searchButton.setVisible(withSearchField);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCols.username));
		
		int colPos = OFFSET_INDEX;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			boolean defVisible = userManager.isMandatoryUserProperty(USER_PROPS, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(defVisible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colPos++, true, userPropertyHandler.getName()));
		}

		if(!externalUsersOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCols.authenticationProvider,
				new ImportExternalProviderCellRenderer(getTranslator())));
		}
		if(!withMultiSelection) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		}
		
		dataModel = new ImportExternalUserSearchDataModel(columnsModel, userPropertyHandlers, getLocale());
	
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 24, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(withMultiSelection);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("table.user.empty")
				.build());
		tableEl.setSelectAllEnable(withMultiSelection);
		tableEl.setAndLoadPersistedPreferences(ureq, "import-ldap-search");
		
		selectButton = uifactory.addFormLink("select", formLayout, Link.BUTTON);
		selectButton.setVisible(false);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		emailEl.clearError();
		if(emailEl.isVisible() && !StringHelper.containsNonWhitespace(emailEl.getValue())) {
			emailEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se
					&& "select".equals(se.getCommand())) {
				Identity selectedIdentity = dataModel.getObject(se.getIndex()).getIdentity();
				fireEvent(ureq, new SingleIdentityChosenEvent(selectedIdentity));
			}	
		} else if(selectButton == source) {
			doMultiSelect(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String searchString = emailEl.getValue();
		if(StringHelper.containsNonWhitespace(searchString)) {
			doSearch(ureq, searchString);
		}
	}
	
	private void doMultiSelect(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if(selectedIndex.isEmpty()) {
			showWarning("warning.atleastone");
		} else {
			List<Identity> identities = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(ImportExternalRow::getIdentity)
				.collect(Collectors.toList());
			fireEvent(ureq, new MultiIdentityChosenEvent(identities));
		}
	}
	
	private void doSearch(UserRequest ureq, String searchString) {
		OAuth2Tokens accessToken = ureq.getUserSession().getOAuth2Tokens();
		ExternalUserResults results = searchUsers(searchString, accessToken, getLocale());
		List<Identity> users = results.getExternalUsers();

		List<ImportExternalRow> rows = users.stream()
				.map(ImportExternalRow::new)
				.collect(Collectors.toList());
		
		Map<Long,ImportExternalRow> rowMap = rows.stream()
				.filter(row -> row.getKey() != null)
				.collect(Collectors.toMap(ImportExternalRow::getKey, Function.identity(), (u, v) -> u));
		
		List<Identity> localIdentities = users.stream()
				.filter(user -> user instanceof IdentityImpl)
				.collect(Collectors.toList());
		
		List<Authentication> authentications = securityManager.getAuthentications(localIdentities);
		for(Authentication authentication:authentications) {
			String provider = authentication.getProvider();
			ImportExternalRow row = rowMap.get(authentication.getIdentity().getKey());
			if(row != null && !excludedProviders.contains(provider)) {
				String currentAuthentication = row.getAuthenticationProvider();
				if(currentAuthentication == null || "OLAT".equals(currentAuthentication)) {
					row.setAuthenticationProvider(provider);
				}
			}
		}
		
		if(externalUsersOnly) {
			rows = rows.stream()
					.filter(row -> row.getIdentity() instanceof TransientIdentity)
					.collect(Collectors.toList());
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		selectButton.setVisible(withMultiSelection && !rows.isEmpty());
		
		if(!results.getExistingUsers().isEmpty()) {
			String existingUsers = String.join(", ", results.getExistingUsers());
			showWarning("warning.existing.users", existingUsers);
		}
	}
	
	private ExternalUserResults searchUsers(String searchString, OAuth2Tokens oauth2Tokens, Locale locale) {
		SearchIdentityParams searchParams = new SearchIdentityParams();
		searchParams.setSearchString(searchString);
		List<Identity> identities = securityManager.getIdentitiesByPowerSearch(searchParams, 0, 2000);

		Set<String> emailDuplicates = new HashSet<>();
		Set<String> usernameDuplicates = new HashSet<>();
		for(Identity identity:identities) {
			String email = identity.getUser().getProperty(UserConstants.EMAIL, locale);
			if(StringHelper.containsNonWhitespace(email)) {
				emailDuplicates.add(email);
			}
			email = identity.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, locale);
			if(StringHelper.containsNonWhitespace(email)) {
				emailDuplicates.add(email);
			}
			String userName = identity.getName();
			if(StringHelper.containsNonWhitespace(userName)) {
				usernameDuplicates.add(userName);
			}
		}
		
		Set<String> existingUsers = new HashSet<>();
		if(oauthLoginModule.isAzureAdfsEnabled() && oauthLoginModule.isAzureLookupEnabled() && oauth2Tokens != null) {
			TeamsErrors errors = new TeamsErrors();
			List<TransientIdentity> azureIdentities = microsoftGraphDao
					.toIdentity(microsoftGraphDao.searchUsers(searchString, oauth2Tokens, errors));
			cleanDuplicates(identities, azureIdentities, emailDuplicates, usernameDuplicates, existingUsers, locale);
		}
		if(ldapModule.isLDAPEnabled() && ldapModule.isLdapLookupEnabled()) {
			List<TransientIdentity> ldapIdentities = ldapManager.search(searchString);
			cleanDuplicates(identities, ldapIdentities, emailDuplicates, usernameDuplicates, existingUsers, locale);
		}
		return new ExternalUserResults(identities, new ArrayList<>(existingUsers));
	}
	
	/**
	 * Remove users which email or user name is the same as someone in OpenOlat
	 */
	private void cleanDuplicates(List<Identity> identities, List<TransientIdentity> foundIdentities,
			Set<String> emailDuplicates, Set<String> usernameDuplicates, Set<String> existingUsers, Locale locale) {
		if(foundIdentities == null || foundIdentities.isEmpty()) return;
		
		for(TransientIdentity ldapIdentity:foundIdentities) {
			String email = ldapIdentity.getUser().getProperty(UserConstants.EMAIL, locale);
			if(StringHelper.containsNonWhitespace(email) && emailDuplicates.contains(email)) {
				existingUsers.add(email);
				continue;
			}
			email = ldapIdentity.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, locale);
			if(StringHelper.containsNonWhitespace(email) && emailDuplicates.contains(email)) {
				existingUsers.add(email);
				continue;
			}
			
			String username = ldapIdentity.getName();
			if(username != null && usernameDuplicates.contains(username)) {
				existingUsers.add(username);
				continue;
			}
			
			identities.add(ldapIdentity);
		}
	}
}
