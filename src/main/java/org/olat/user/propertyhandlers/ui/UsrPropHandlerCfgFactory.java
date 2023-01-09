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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * This factory will return a controller for a given UserPropertyHandler which
 * allows for additional configuration.
 * 
 * Note: not yet fully implemented; see FXOLAT-239
 * 
 * <P>
 * Initial Date: 26.08.2011 <br>
 * 
 * @author strentini
 */
@Service("UsrPropHandlerCfgFactory")
public class UsrPropHandlerCfgFactory extends AbstractSpringModule {
	private static final Logger log = Tracing.createLoggerFor(UsrPropHandlerCfgFactory.class);
	private static final String PROP_HNDLCFG_PREFIX = "handlerconfig_";

	@Autowired(required=false) @Qualifier("propertyHandlerControllerMap")
	private HashMap<String, AutoCreator> handlerControllerMap;


	@Autowired
	public UsrPropHandlerCfgFactory(CoordinatorManager coordinatorManager) {
		super(coordinatorManager, "com.frentix.olat.admin.userproperties.UsrPropHandlerCfgFactory");
	}

	@Override
	public void init() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	/**
	 * save the given configuration String for the specified handler.
	 * 
	 * @param handler the UserPropertyHandler to save the config for
	 * @param configValue a serialized configuration for the handler
	 */
	public void saveConfigForHandler(UserPropertyHandler handler, Map<String,String> configMap) {
		String configValue = serializeConfig(configMap);
		setStringProperty(PROP_HNDLCFG_PREFIX + handler.getName(), configValue, true);
	}

	/**
	 * load the configuration for the given handler
	 * 
	 * @param handler the UserPropertyHandler to load the config for
	 * @return
	 */
	public Map<String,String> loadConfigForHandler(UserPropertyHandler handler) {
		String config =  getStringPropertyValue(PROP_HNDLCFG_PREFIX + handler.getName(), true);
		return deserializeConfig(config);
	}
	

	/**
	 * Returns true if there exists an additional handlerConfig-Controller for the
	 * given PropertyHandler
	 * 
	 * @param handler
	 * @return
	 */
	public boolean hasConfig(UserPropertyHandler handler) {
		return handlerControllerMap != null && handlerControllerMap.containsKey(handler.getClass().getName());
	}

	/**
	 * returns the configController for the given PropertyHandler. If no
	 * controller available, null is returned instead! Always check first with
	 * <code>hasConfig()</code>
	 * 
	 * @param ureq
	 * @param wControl
	 * @param handler
	 * @return
	 */
	public UsrPropHandlerCfgController getConfigController(UserRequest ureq, WindowControl wControl, UserPropertyHandler handler) {
		if (!hasConfig(handler)) return null;
		AutoCreator creator = handlerControllerMap.get(handler.getClass().getName());
		UsrPropHandlerCfgController ctrl =  (UsrPropHandlerCfgController) creator.createController(ureq, wControl);
		ctrl.setHandlerToConfigure(handler);
		return ctrl;
	}
	
	private static Map<String, String> deserializeConfig(String handlerConfig) {
		Map<String,String> conf = new HashMap<>();
		
		try {
			if(StringHelper.containsNonWhitespace(handlerConfig)) {
				JSONObject jsonObject = new JSONObject(handlerConfig);
				Iterator<String> jsonIterator = jsonObject.keys();
				while(jsonIterator.hasNext()){
					String key = jsonIterator.next();
					conf.put(key, jsonObject.getString(key));
				}
			}
		} catch (JSONException e) {
			log.error("", e);
		}
		return conf;
	}

	private static String serializeConfig(Map<String, String> map) {
		JSONObject jsonObject = new JSONObject();
		
		for (Entry<String, String> entry : map.entrySet()) {
			try {
				jsonObject.put(entry.getKey(), entry.getValue());
			} catch (JSONException e) {
				log.error("", e);
			}
		}
		return jsonObject.toString();
	}
}
