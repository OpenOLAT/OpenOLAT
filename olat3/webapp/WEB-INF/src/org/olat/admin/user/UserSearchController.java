/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.admin.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.ajax.autocompletion.AutoCompleterController;
import org.olat.core.gui.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.control.state.ControllerState;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date:  Jul 29, 2003
 *
 * @author Felix Jost, Florian Gnaegi
 * 
 * <pre>
 * Comment:  
 * Subworkflow that allows the user to search for a user and choose the user from 
 * the list of users that match the search criteria. Users can be searched by
 * <ul>
 * <li />
 * Username
 * <li />
 * First name
 * <li />
 * Last name
 * <li />
 * Email address
 * </ul>
 * 
 * </pre>
 * 
 * Events:<br>
 *         Fires a SingleIdentityChoosenEvent when an identity has been chosen
 *         which contains the choosen identity<br>
 *         Fires a MultiIdentityChoosenEvent when multiples identities have been
 *         chosen which contains the choosen identities<br>
 *         <p>
 *         Optionally set the useMultiSelect boolean to true which allows to
 *         select multiple identities from within the search results.
 */
public class UserSearchController extends BasicController {
	// Needs PACKAGE and VELOCITY_ROOT because DeletableUserSearchController extends UserSearchController and re-use translations
	private static final String PACKAGE = UserSearchController.class.getPackage().getName();
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);
	
	private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
	private static final String ACTION_MULTISELECT_CHOOSE = "msc";
	
	private VelocityContainer myContent;
	private Panel searchPanel;
	private UserSearchForm searchform;
	private TableController tableCtr;
	private TableGuiConfiguration tableConfig;
	private UserTableDataModel tdm;
	private List<Identity> foundIdentities = new ArrayList<Identity>();
	private boolean useMultiSelect = false;
	
	private AutoCompleterController autocompleterC;
	private String actionKeyChoose;
	private Map<String, String> userPropertiesSearch;
	private boolean isAdministrativeUser;
	private Link backLink;

	private static final String STATE_SEARCHFORM = "searchform";
	private static final String STATE_RESULTS = "results";
	
	public static final String ACTION_KEY_CHOOSE = "action.choose";
	public static final String ACTION_KEY_CHOOSE_FINISH = "action.choose.finish";
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 */
	public UserSearchController(UserRequest ureq, WindowControl wControl, boolean cancelbutton) {
		this(ureq, wControl, cancelbutton, false, false);
	}

	/**
	 * @param ureq
	 * @param windowControl
	 * @param cancelbutton
	 * @param userMultiSelect
	 * @param statusEnabled
	 * @param actionKeyChooseFinish
	 */
	public UserSearchController(UserRequest ureq, WindowControl windowControl, boolean cancelbutton, boolean userMultiSelect, boolean statusEnabled, String actionKeyChooseFinish) {
		this(ureq, windowControl, cancelbutton, userMultiSelect, statusEnabled);
		this.actionKeyChoose = actionKeyChooseFinish;
	}

	/**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 * @param userMultiSelect
	 * @param statusEnabled
	 */
	public UserSearchController(UserRequest ureq, WindowControl wControl, boolean cancelbutton, boolean userMultiSelect, boolean statusEnabled) {
		super(ureq, wControl);
		this.useMultiSelect = userMultiSelect;
		this.actionKeyChoose = ACTION_KEY_CHOOSE;
	  // Needs PACKAGE and VELOCITY_ROOT because DeletableUserSearchController extends UserSearchController and re-use translations
		Translator pT = UserManager.getInstance().getPropertyHandlerTranslator( new PackageTranslator(PACKAGE, ureq.getLocale()) );	
		myContent = new VelocityContainer("olatusersearch", VELOCITY_ROOT + "/usersearch.html", pT, this);
		backLink = LinkFactory.createButton("btn.back", myContent, this);
		
		searchPanel = new Panel("usersearchPanel");
		searchPanel.addListener(this);
		myContent.put("usersearchPanel", searchPanel);

		if (ureq.getUserSession()==null) {
			logError("UserSearchController<init>: session is null!", null);
		} else if (ureq.getUserSession().getRoles()==null) {
			logError("UserSearchController<init>: roles is null!", null);
		}
		boolean isAdmin = ureq.getUserSession().getRoles().isOLATAdmin(); 
		
		searchform = new UserSearchForm(ureq, wControl, isAdmin, cancelbutton);
		listenTo(searchform);
		
		searchPanel.setContent(searchform.getInitialComponent());
	
		myContent.contextPut("noList","false");			
		myContent.contextPut("showButton","false");
		
		boolean ajax = Windows.getWindows(ureq).getWindowManager().isAjaxEnabled();
		final Locale loc = ureq.getLocale();
		if (ajax) {
			// insert a autocompleter search
			ListProvider provider = new ListProvider() {
				/**
				 * @see org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider#getResult(java.lang.String,
				 *      org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver)
				 */
				public void getResult(String searchValue, ListReceiver receiver) {
					Map<String, String> userProperties = new HashMap<String, String>();
					// We can only search in mandatory User-Properties due to problems
					// with hibernate query with join and not existing rows
					userProperties.put(UserConstants.FIRSTNAME, searchValue);
					userProperties.put(UserConstants.LASTNAME, searchValue);
					userProperties.put(UserConstants.EMAIL, searchValue);
					// Search in all fileds -> non intersection search
					List<Identity> res = searchUsers(searchValue,	userProperties, false);
					int maxEntries = 15;
					boolean hasMore = false;
					for (Iterator<Identity> it_res = res.iterator(); (hasMore=it_res.hasNext()) && maxEntries > 0;) {
						maxEntries--;
						Identity ident = it_res.next();
						User u = ident.getUser();
						String key = ident.getKey().toString();
						String displayKey = ident.getName();
						String first = u.getProperty(UserConstants.FIRSTNAME, loc);
						String last = u.getProperty(UserConstants.LASTNAME, loc);
						String displayText = last + " " + first;
						receiver.addEntry(key, displayKey, displayText, CSSHelper.CSS_CLASS_USER);
					}					
					if(hasMore){
						receiver.addEntry(".....",".....");
					}
				}
			};
			autocompleterC = new AutoCompleterController(ureq, getWindowControl(), provider, null, isAdmin, 60, 3, null);
			listenTo(autocompleterC);
			myContent.put("autocompletionsearch", autocompleterC.getInitialComponent());
		}
		
		tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.user.found"));
		tableConfig.setDownloadOffered(false);// no download because user should not download user-list
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myContent.getTranslator());
		listenTo(tableCtr);
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = (roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());
		
		
		putInitialPanel(myContent);
		setState(STATE_SEARCHFORM);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {		
			myContent.contextPut("noList","false");			
			myContent.contextPut("showButton","false");
			searchPanel.popContent();
			setState(STATE_SEARCHFORM);
		} 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				if (te.getActionId().equals(ACTION_SINGLESELECT_CHOOSE)) {
					int rowid = te.getRowId();
					Identity foundIdentity = (Identity)tdm.getObject(rowid);
					foundIdentities.add(foundIdentity);
					// Tell parentController that a subject has been found
					fireEvent(ureq, new SingleIdentityChosenEvent(foundIdentity));
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
					foundIdentities = tdm.getObjects(tmse.getSelection());
					fireEvent(ureq, new MultiIdentityChosenEvent(foundIdentities));
				}
			}
		} else if (source == autocompleterC) {
			EntriesChosenEvent ece = (EntriesChosenEvent)event;
			List res = ece.getEntries();
			// if we get the event, we have a result or an incorrect selection see OLAT-5114 -> check for empty
			String mySel = res.isEmpty() ? null : (String) res.get(0);
			if (( mySel == null) || mySel.trim().equals("")) {
				getWindowControl().setWarning(translate("error.search.form.notempty"));
				return;
			}
			Long key = -1l; // default not found
			try {
				key = Long.valueOf(mySel);				
				if (key > 0) {
					Identity chosenIdent = BaseSecurityManager.getInstance().loadIdentityByKey(key);
					// No need to check for null, exception is thrown when identity does not exist which really 
					// should not happen at all. 
					// Tell that an identity has been chosen
					fireEvent(ureq, new SingleIdentityChosenEvent(chosenIdent));
				}
			} catch (NumberFormatException e) {
				getWindowControl().setWarning(translate("error.no.user.found"));
				return;									
			}
		} else if (source == searchform) {
			if (event == Event.DONE_EVENT) {
				// form validation was ok

				String login = searchform.login.getValue();
				// build user fields search map
				Map<String, String> userPropertiesSearch = new HashMap<String, String>();				
				for (UserPropertyHandler userPropertyHandler : searchform.userPropertyHandlers) {
					if (userPropertyHandler == null) continue;
					FormItem ui = searchform.propFormItems.get(userPropertyHandler.getName());
					String uiValue = userPropertyHandler.getStringValue(ui);
					if (StringHelper.containsNonWhitespace(uiValue)) {
						userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
						getLogger().info("Search property:" + userPropertyHandler.getName() + "=" + uiValue);
					}
				}
				if (userPropertiesSearch.isEmpty()) userPropertiesSearch = null;
				
				tableCtr = new TableController(tableConfig, ureq, getWindowControl(), myContent.getTranslator());
				listenTo(tableCtr);
				
				List<Identity> users = searchUsers(login,	userPropertiesSearch, true);
				if (!users.isEmpty()) {
					tdm = new UserTableDataModel(users, ureq.getLocale(), isAdministrativeUser);
					// add the data column descriptors
					tdm.addColumnDescriptors(tableCtr, null);
					// add the action columns
					if (useMultiSelect) {
						// add multiselect action
						tableCtr.addMultiSelectAction(this.actionKeyChoose, ACTION_MULTISELECT_CHOOSE);
					} else {
						// add single column selec action
						tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", myContent
								.getTranslator().translate("action.choose")));
					}
					tableCtr.setTableDataModel(tdm);
					tableCtr.setMultiSelect(useMultiSelect);
					searchPanel.pushContent(tableCtr.getInitialComponent());
					myContent.contextPut("showButton","true");
					setState(STATE_RESULTS);
				} else {
					getWindowControl().setInfo(translate("error.no.user.found"));
					setState(STATE_SEARCHFORM);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
		
	}
	
	protected void adjustState(ControllerState cstate, UserRequest ureq) {
		String state = cstate.getSerializedState();
		if (state.equals(STATE_SEARCHFORM)) {
			// we should and can adjust to the searchform
			searchPanel.popContent();
			setState(STATE_SEARCHFORM);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// Child controllers auto-disposed by basic controller
	}

	/**
	 * Can be overwritten by subclassen to search other users or filter users.
	 * @param login
	 * @param userPropertiesSearch
	 * @return
	 */
	protected List<Identity> searchUsers(String login, Map<String, String> userPropertiesSearch, boolean userPropertiesAsIntersectionSearch) {
	  return BaseSecurityManager.getInstance().getVisibleIdentitiesByPowerSearch(
			(login.equals("") ? null : login),
			userPropertiesSearch, userPropertiesAsIntersectionSearch,	// in normal search fields are intersected
			null, null, null, null, null);
	}
}


/**
 * <pre>
 *
 * Initial Date:  Jul 29, 2003
 *
 * @author gnaegi
 * 
 * Comment:  
 * The user search form
 * </pre>
 */
class UserSearchForm extends FormBasicController {
	
	private final boolean isAdmin, cancelButton;
	private FormLink searchButton;
	
	protected TextElement login;
	protected List<UserPropertyHandler> userPropertyHandlers;
	protected Map <String,FormItem>propFormItems;
	
	
	/**
	 * @param name
	 * @param cancelbutton
	 * @param isAdmin if true, no field must be filled in at all, otherwise
	 *          validation takes place
	 */

	public UserSearchForm(UserRequest ureq, WindowControl wControl, boolean isAdmin, boolean cancelButton) {
		super(ureq, wControl);
		
		this.isAdmin = isAdmin;
		this.cancelButton = cancelButton;
	
		initForm(ureq);
	}

	
	@Override
	@SuppressWarnings("unused")
	public boolean validateFormLogic (UserRequest ureq) {
		// override for admins
		if (isAdmin) return true;
		
		
		boolean filled = !login.isEmpty();
		StringBuffer  full = new StringBuffer(login.getValue().trim());  
		FormItem lastFormElement = login;
		
		// DO NOT validate each user field => see OLAT-3324
		// this are custom fields in a Search Form
		// the same validation logic can not be applied
		// i.e. email must be searchable and not about getting an error like
		// "this e-mail exists already"
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem ui = propFormItems.get(userPropertyHandler.getName());
				String uiValue = userPropertyHandler.getStringValue(ui);
				// add value for later non-empty search check
				if (StringHelper.containsNonWhitespace(uiValue)) {
					full.append(uiValue.trim());
					filled = true;
				}else{
					//its an empty field
					filled = filled || false;
				}
				lastFormElement = ui;
		}

		// Don't allow searches with * or %  or @ chars only (wild cards). We don't want
		// users to get a complete list of all OLAT users this easily.
		String fullString = full.toString();
		boolean onlyStar= fullString.matches("^[\\*\\s@\\%]*$");

		if (!filled || onlyStar) {
			// set the error message
			lastFormElement.setErrorKey("error.search.form.notempty", null);
			return false;
		}
		if ( fullString.contains("**") ) {
			lastFormElement.setErrorKey("error.search.form.no.wildcard.dublicates", null);
			return false;
		}		
		int MIN_LENGTH = 4;
		if ( fullString.length() < MIN_LENGTH ) {
			lastFormElement.setErrorKey("error.search.form.to.short", null);
			return false;
		}
		
		return true;
	}

	

	@Override
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		
		login = uifactory.addTextElement("login", "search.form.login", 128, "", formLayout);
		
		
		UserManager um = UserManager.getInstance();
		
		Translator tr = Util.createPackageTranslator(
				UserPropertyHandler.class,
				getLocale(), 
				getTranslator()
		);
		
		userPropertyHandlers = um.getUserPropertyHandlersFor(
				getClass().getCanonicalName(), isAdmin
		);
		
		propFormItems = new HashMap<String,FormItem>();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			
			FormItem fi = userPropertyHandler.addFormItem(
					getLocale(), null, getClass().getCanonicalName(), false, formLayout
			);
			fi.setTranslator(tr);

			propFormItems.put(userPropertyHandler.getName(), fi);
		}
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);

		// Don't use submit button, form should not be marked as dirty since this is
		// not a configuration form but only a search form (OLAT-5626)
		searchButton = uifactory.addFormLink("submit.search", buttonGroupLayout, Link.BUTTON);
		searchButton.addActionListener(this, FormEvent.ONCLICK);
		if (cancelButton) {
			uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, @SuppressWarnings("unused") FormEvent event) {
		if (source == searchButton) {
			source.getRootForm().submit(ureq);			
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void doDispose() {
		//
	}

}