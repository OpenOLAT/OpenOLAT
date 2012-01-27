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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.util.prefs;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21.06.2006 <br>
 *
 * @author Felix Jost
 */
public class PreferencesFactory {
	private static final PreferencesFactory INSTANCE = new PreferencesFactory();
	private PreferencesStorage preferencesStorage;

	private PreferencesFactory() {
		// singleton	
		preferencesStorage = (PreferencesStorage) CoreSpringFactory.getBean("core.preferences.PreferencesStorage");
	}
	
	public static PreferencesFactory getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Factory method to create a preferences object for a given identity. <br />
	 * DO NOT USE THIS METHOD unless you really know what you do! Use the method
	 * ureq.getUserSession().getGuiPreferences(); instead. This will preven double
	 * properties in the database! Only the UserSession.signOn() method should use
	 * this method
	 * 
	 * @param identity
	 * @param useTransientPreferences
	 * @return
	 */
	public Preferences getPreferencesFor(Identity identity, boolean useTransientPreferences) {
		return preferencesStorage.getPreferencesFor(identity, useTransientPreferences);
	}
	
	public PreferencesStorage getStorage() {
		return preferencesStorage;
	}
	
	
}
