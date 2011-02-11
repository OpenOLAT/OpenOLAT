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
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.core.util.prefs;

import org.olat.core.id.Identity;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21.06.2006 <br>
 *
 * @author Felix Jost
 */
public interface PreferencesStorage {
	
	/**
	 * 
	 * @param identity the identiy whose preferences should be loaded
	 * @param useTransientPreferences if true, an empty preference will be return and calls to save() have no effect (e.g. for guest accounts)
	 * @return the preferences for the user
	 */
	public Preferences getPreferencesFor(Identity identity, boolean useTransientPreferences);
}
