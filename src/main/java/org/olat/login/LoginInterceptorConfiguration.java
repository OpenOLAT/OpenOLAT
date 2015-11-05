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
package org.olat.login;

import java.util.Map;

import org.olat.core.gui.control.creator.AutoCreator;

/**
 * 
 * Initial date: 04.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LoginInterceptorConfiguration implements Comparable<LoginInterceptorConfiguration> {
	
	public static final String CONTROLLER_KEY = "controller";
	public static final String FORCEUSER_KEY = "forceUser";
	public static final String REDOTIMEOUT_KEY = "redoTimeout";
	public static final String I18NINTRO_KEY = "i18nIntro"; 
	public static final String ORDER_KEY = "order";
	
	private final AutoCreator creator;
	private final boolean forceUser;
	private final Long redoTimeout;
	private final String i18nIntroKey;
	private final int order;
	
	public LoginInterceptorConfiguration(Map<String,Object> config) {
		creator = (AutoCreator)config.get(CONTROLLER_KEY);
		if(config.containsKey(FORCEUSER_KEY)) {
			forceUser = Boolean.valueOf(config.get(FORCEUSER_KEY).toString());
		} else {
			forceUser = false;
		}
		
		if (config.containsKey(REDOTIMEOUT_KEY)) {
			redoTimeout = Long.parseLong((String)config.get(REDOTIMEOUT_KEY));
		} else {
			redoTimeout = null;
		}
		
		i18nIntroKey = (String)config.get(I18NINTRO_KEY);
		if (config.containsKey(ORDER_KEY)) {
			order = Integer.parseInt(config.get(ORDER_KEY).toString());
		} else {
			order = 1;
		}
	}

	public AutoCreator getCreator() {
		return creator;
	}

	public boolean isForceUser() {
		return forceUser;
	}

	public Long getRedoTimeout() {
		return redoTimeout;
	}

	public String getI18nIntroKey() {
		return i18nIntroKey;
	}

	public int getOrder() {
		return order;
	}

	@Override
	public int compareTo(LoginInterceptorConfiguration o) {
		return order - o.order;
	}
}
