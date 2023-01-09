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
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */

package org.olat.user.propertyhandlers.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.control.Event;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertiesConfigImpl;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * Description:<br>
 * Provides Functionality to modify the UserPropertyConfig Object (from the
 * UserManager). The UserPropertyConfig-Object is modified with our own config
 * which can be edited through an admin-gui
 * 
 * <P>
 * Initial Date: 24.08.2011 <br>
 * 
 * @author strentini
 */
@Service
public class UsrPropCfgManager extends AbstractSpringModule implements GenericEventListener {

	private static final Logger log = Tracing.createLoggerFor(UsrPropCfgManager.class);
	/*
	 * these properties (the handlers) cannot be deactivated. OLAT depends on them
	 */
	private static final String[] NON_DEACTIVABLE_PROPERTIES = { "firstName", "lastName", "email", "emchangeKey", "emailDisabled" };
	
	/*
	 * properties (the handlers) that must be mandatory in all contexts.
	 */
	private static final String[] MUST_BE_MANDATORY_PROPERTIES = {"firstName", "lastName","email"};
	
	/* 
	 * contexts that are an exception for "MUST-BE-MANDATORIY-PROPERTIES" ;-)
	 * explanation: there are some contexts where the propertyhandlers are used to render search-fields. in these contexts/Forms
	 * none of the properties must be mandatory. 
	 * (I know, it's a hack...) 
	 * 
	 */
	private static final String[] EXCEPTIONAL_CONTEXTS_FOR_MUSTBE_MANDATORY_PROPERTIES = {
			"org.olat.admin.user.UsermanagerUserSearchForm",
			"org.olat.admin.user.UserSearchForm",
			"org.olat.user.HomePageConfig"
		};
                            
	private static final String CONF_KEY_ACTUPROP = "activeuserpropertyhandlers";
	private static final String CONF_KEY_DACTUPROP = "deactiveuserpropertyhandlers";
	private static final String CONF_KEY_PROPGROUP = "_group";
	private static final String CONF_KEY_CONTPREFIX = "_hndl_";
	private static final String CONF_KEY_CONT_MANDATORY_PREFIX = "mandatory";
	private static final String CONF_KEY_CONT_ADMINONLY_PREFIX = "adminonly";
	private static final String CONF_KEY_CONT_USERREADONLY_PREFIX = "usrreadonly";

	private static final String PROP_DELIMITER = ",";


	private UsrPropCfgObject cfgObject;

	private final List<UserPropertyHandler> allUserPropertyHandlersFromXML;

	private final UserManager userManager;

	@Autowired
	public UsrPropCfgManager(CoordinatorManager coordinatorManager, UserManager userManager) {
		super(coordinatorManager, "com.frentix.olat.admin.userproperties.UsrPropCfgManager");
		this.userManager = userManager;
		allUserPropertyHandlersFromXML = userManager.getUserPropertiesConfig().getAllUserPropertyHandlers();
	}

	@Override
	public void init() {
		getUserPropertiesConfigObject();
		//
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	/**
	 * returns the current UserPropertiesConfigObject (on first invocation, the
	 * cfgObject is loaded from persistedProperties)
	 * 
	 * @return
	 */
	public UsrPropCfgObject getUserPropertiesConfigObject() {
		if (cfgObject == null) {
			loadModifiedUserPropertiesConfig(createPropertiesFromPersistedProperties());
		}
		return cfgObject;
	}

	/**
	 * Resets the UserCongfig to the given Preset
	 * 
	 * @param properties
	 */
	public void resetToPresetConfig(Properties properties) {
		loadModifiedUserPropertiesConfig(properties);
		savePersistedProperties();
	}

	/**
	 * loads our persisted Config and manipulates the Config of the user-manager
	 * (which comes from xml-config)
	 */
	private void loadModifiedUserPropertiesConfig(Properties props) {
		log.info("loading modified UserPropertiesConfig");

		UserPropertiesConfig usrMngConfig = userManager.getUserPropertiesConfig();

		cfgObject = new UsrPropCfgObject(allUserPropertyHandlersFromXML, usrMngConfig.getUserPropertyUsageContexts());

		// now manipulate this cfgObject according to our own config ( from the
		// persistedProperties)
		List<String> val_activeHandlers = Arrays.asList(props.getProperty(CONF_KEY_ACTUPROP, "").split(PROP_DELIMITER));
		List<String> val_dactiveHandlers = Arrays.asList(props.getProperty(CONF_KEY_DACTUPROP, "").split(PROP_DELIMITER));
		for (UserPropertyHandler handler : cfgObject.getPropertyHandlers()) {
			//modify the groupName of the handler
			String groupName = props.getProperty(handler.getName() + CONF_KEY_PROPGROUP,null);
			if(groupName != null){
				handler.setGroup(groupName);
			}
			//either it is set as an active property, or the property can't be deactivated (email, firstname, lastname, etc.)
			if (val_activeHandlers.contains(handler.getName()) || !UsrPropCfgManager.canBeDeactivated(handler)) {
				cfgObject.setHandlerAsActive(handler, true);
			} else if (!val_dactiveHandlers.contains(handler.getName())) {
				// this is a new handler (not yet in our own config)
				// -->set it as active
				// (note: if you delete persistedProperties-conf-file, all handlers are
				// "new" and therefore should be active)
				log.debug("UserPropertyHandler {} unknown in config, set Property as active.", handler.getName());
				cfgObject.setHandlerAsActive(handler, true);
			}
		}

		// handle contexts (these are the contexts from xml)
		for (Entry<String, UserPropertyUsageContext> ctxEntry : cfgObject.getUsageContexts().entrySet()) {
			UserPropertyUsageContext ctx = ctxEntry.getValue();
			String contextName = ctxEntry.getKey();

			List<UserPropertyHandler> ctx_allHandlers = new ArrayList<>();
			Set<UserPropertyHandler> ctx_mandHandlers = new HashSet<>();
			Set<UserPropertyHandler> ctx_adminonlyHandlers = new HashSet<>();
			Set<UserPropertyHandler> ctx_usrreadonlyHandlers = new HashSet<>();

			String handlerNameInConfig = props.getProperty(contextName, null);
			if (handlerNameInConfig == null) {// our config doesn't know this context,
																				// leave it as is!
				log.debug("UserPropertyUsageContext {} unknown in config, leave Context untouched.", contextName);
				continue;
			}
			// this list from the persistedProperties has the correct order of handlers!
			List<String> val_handlers = Arrays.asList(props.getProperty(contextName + CONF_KEY_CONTPREFIX, "").split(PROP_DELIMITER));
			
			List<String> val_mandatoryHandlers = Arrays.asList(props.getProperty(contextName + CONF_KEY_CONTPREFIX + CONF_KEY_CONT_MANDATORY_PREFIX, "")
					.split(PROP_DELIMITER));
			List<String> val_adminonlyHandlers = Arrays.asList(props.getProperty(contextName + CONF_KEY_CONTPREFIX + CONF_KEY_CONT_ADMINONLY_PREFIX, "")
					.split(PROP_DELIMITER));
			List<String> val_userreadonlyHandlers = Arrays.asList(props.getProperty(
					contextName + CONF_KEY_CONTPREFIX + CONF_KEY_CONT_USERREADONLY_PREFIX, "").split(PROP_DELIMITER));

			for (UserPropertyHandler handler : cfgObject.getPropertyHandlers()) {
				String handlerName = handler.getName();
				if (val_handlers.contains(handlerName) && cfgObject.isActiveHandler(handler)) {
					ctx_allHandlers.add(handler);
					//either it is set as mandatory in config or it is one of the property where mandatory must be enforced :) 
					if (val_mandatoryHandlers.contains(handlerName) || !UsrPropCfgManager.canBeOptionalInContext(handler,contextName)) {
						ctx_mandHandlers.add(handler);
					}
					if (val_adminonlyHandlers.contains(handlerName)) {
						ctx_adminonlyHandlers.add(handler);
					}
					if (val_userreadonlyHandlers.contains(handlerName)) {
						ctx_usrreadonlyHandlers.add(handler);
					}
				}
			}
			ctx.setPropertyHandlers(restoreCorrectHandlerOrderWithinContext(ctx_allHandlers,val_handlers));
			ctx.setMandatoryProperties(ctx_mandHandlers);
			ctx.setAdminViewOnlyProperties(ctx_adminonlyHandlers);
			ctx.setUserViewReadOnlyProperties(ctx_usrreadonlyHandlers);
		}

		// create new modified userPropertiesConfig for UserManager
		setUserManagerProperties();
	}
	
	/**
	 * This method restores the correct property-order within the list of handlers.
	 * The order is correctly stored in persistedProperties (as list of names -> orderedNameList).
	 * 
	 * 
	 * @param allHandlers the handlers to sort
	 * @param orderedNameList the list of handlerNames in the correct order
	 * @return Returns the list of PropertyHandlers but with the correct order 
	 */
	private List<UserPropertyHandler> restoreCorrectHandlerOrderWithinContext(List<UserPropertyHandler> allHandlers, List<String> orderedNameList){
		Map<String,UserPropertyHandler> nameToPropertyMap = new HashMap<>();
		for(UserPropertyHandler handler:allHandlers) {
			nameToPropertyMap.put(handler.getName(), handler);
		}
		
		// this list will be returned. contains all handlers from "allHandlers" in sorted order
		List<UserPropertyHandler> sortedHandlersList = new ArrayList<>(allHandlers.size());
		for(String name:orderedNameList) {
			UserPropertyHandler handler = nameToPropertyMap.remove(name);
			if(handler != null) {
				sortedHandlersList.add(handler);
			}
		}

		for (UserPropertyHandler handler : allHandlers) {
			if(nameToPropertyMap.containsKey(handler.getName())) {
				sortedHandlersList.add(handler);
			}
		}
		return sortedHandlersList;
	}

	/**
	 * saves the current UserPropertiesConfig to persistedProperties and updates
	 * the UserManager with the current config
	 */
	public void saveUserPropertiesConfig() {
		log.info("saving modified UserPropertiesConfig");

		// save our config to persistedProperties
		savePersistedProperties();

		// make a new userpropertiesconfig and set it in the userManager
		setUserManagerProperties();
	}

	/**
	 * updates the userManager with our current config
	 */
	private void setUserManagerProperties() {
		UserPropertiesConfigImpl upConfig = new UserPropertiesConfigImpl();
		List<UserPropertyHandler> handlers = new ArrayList<>();
		for (UserPropertyHandler handler : cfgObject.getPropertyHandlers()) {
			if (cfgObject.isActiveHandler(handler)) handlers.add(handler);
		}
		upConfig.setUserPropertyHandlers(handlers);
		upConfig.setUserPropertyUsageContexts(cfgObject.getUsageContexts());
		userManager.setUserPropertiesConfig(upConfig);
	}

	/**
	 * saves our config to persistedProperties
	 */
	private void savePersistedProperties() {
		StringBuilder sbActiveHandlers = new StringBuilder();
		StringBuilder sbDeactiveHandlers = new StringBuilder();

		// the propertyhandlers
		for (UserPropertyHandler handler : cfgObject.getPropertyHandlers()) {
			// save the group of each handler
			setStringProperty(handler.getName() + CONF_KEY_PROPGROUP, handler.getGroup(), false);
			if (cfgObject.isActiveHandler(handler)) {
				sbActiveHandlers.append(handler.getName() + PROP_DELIMITER);
			} else {
				sbDeactiveHandlers.append(handler.getName() + PROP_DELIMITER);
			}
		}
		setStringProperty(CONF_KEY_ACTUPROP, sbActiveHandlers.toString(), false);
		setStringProperty(CONF_KEY_DACTUPROP, sbDeactiveHandlers.toString(), false);

		// the contexts
		for (Entry<String, UserPropertyUsageContext> ctxEntry : cfgObject.getUsageContexts().entrySet()) {
			StringBuilder sbHandlers = new StringBuilder();
			StringBuilder sbMandatoryHandlers = new StringBuilder();
			StringBuilder sbAdminonlyHandlers = new StringBuilder();
			StringBuilder sbUsrreadonlyHandlers = new StringBuilder();

			// now loop over the handlers in the current context
			UserPropertyUsageContext ctx = ctxEntry.getValue();
			for (UserPropertyHandler handler : ctx.getPropertyHandlers()) {
				if (cfgObject.isActiveHandler(handler)) {
					sbHandlers.append(handler.getName() + PROP_DELIMITER);
					log.debug("{} has active handler :{}", ctxEntry.getKey(), handler.getName());
				}

				if (ctx.isMandatoryUserProperty(handler)) {
					sbMandatoryHandlers.append(handler.getName() + PROP_DELIMITER);
				}
				if (ctx.isForAdministrativeUserOnly(handler)) {
					sbAdminonlyHandlers.append(handler.getName() + PROP_DELIMITER);
				}
				if (ctx.isUserViewReadOnly(handler)) {
					sbUsrreadonlyHandlers.append(handler.getName() + PROP_DELIMITER);
				}
			}

			setStringProperty(ctxEntry.getKey(), "1", false);
			setStringProperty(ctxEntry.getKey() + CONF_KEY_CONTPREFIX, sbHandlers.toString(), false);
			setStringProperty(ctxEntry.getKey() + CONF_KEY_CONTPREFIX + CONF_KEY_CONT_MANDATORY_PREFIX,
					sbMandatoryHandlers.toString(), false);
			setStringProperty(ctxEntry.getKey() + CONF_KEY_CONTPREFIX + CONF_KEY_CONT_ADMINONLY_PREFIX,
					sbAdminonlyHandlers.toString(), false);
			setStringProperty(ctxEntry.getKey() + CONF_KEY_CONTPREFIX + CONF_KEY_CONT_USERREADONLY_PREFIX,
					sbUsrreadonlyHandlers.toString(), false);
		}

		// now persist
		savePropertiesAndFireChangedEvent();
	}

	/**
	 * small helper method that checks if given propertyHandler can be
	 * deactivated. See UsrPropCfgtableController.NON_DEACTIVABLE_PROPERTIES
	 * 
	 * @param propertyHandler
	 * @return
	 */
	public static boolean canBeDeactivated(UserPropertyHandler propertyHandler) {
		String name = propertyHandler.getName();
		for (String key : NON_DEACTIVABLE_PROPERTIES) {
			if (key.equals(name)) return false;
		}
		return true;
	}
	
	/**
	 * small helper method that cecks if given propertyHandler can be optional (i.e. "non-mandatory") in any context.
	 * UsrPropCfgtableController.MUST_BE_MANDATORY_PROPERTIES
	 * 
	 * @param propertyHandler the propertyHandler to check
	 * @param contextClassName the name of the context to check for. (there is a special special case, where "must-be-mandatory-properties" can be non-mandatory)
	 * @return
	 */
	public static boolean canBeOptionalInContext(UserPropertyHandler propertyHandler, String contextClassName){
		// check for the exception of the rule ^ 
		if(contextClassName != null) {
			for(String exceptionalContext : EXCEPTIONAL_CONTEXTS_FOR_MUSTBE_MANDATORY_PROPERTIES){
				if(exceptionalContext.equals(contextClassName))return true;
			}
		}
		
		String name = propertyHandler.getName();
		for(String key : MUST_BE_MANDATORY_PROPERTIES){
			if(key.equals(name))return false;
		}
		return true;
	}

	@Override
	public void event(Event event) {
		// nothing to do
	}
}
