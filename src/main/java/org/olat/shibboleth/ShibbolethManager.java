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
package org.olat.shibboleth;

import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.User;

/**
 * This manager handles the interaction between Shibboleth and OpenOLAT.
 *
 * Initial date: 19.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ShibbolethManager {

	/**
	 * Create and persist an OpenOLAT user and synchronize the Shibboleth user
	 * attribute with the OpenOLAT user properties. The new user is added with
	 * the role user. Required Attributes have to be checked before this method.
	 *
	 * @param username
	 * @param shibbolethUniqueID
	 * @param language
	 * @param shibbolethAttributes
	 * @return
	 */
	public Identity createAndPersistUser(String username, String shibbolethUniqueID, String language, Map<String, String> shibbolethAttributes);

	/**
	 * Synchronize the Shibboleth user attributes to the OpenOLAT user
	 * properties and persist the user in the database.
	 *
	 * @param identity
	 * @param shibbolethAttributes
	 */
	public void syncUser(Identity identity, Map<String, String> shibbolethAttributes);

	/**
	 * Synchronize the Shibboleth attributes to the user properties. The
	 * attribute mapping is defined in the ShibboletModule. Attributes are added
	 * and overwritten, if Shibboleth delivers data. If Shibboleth delivers no
	 * value for an attribute the appropriate user property will be deleted.
	 *
	 * @param user
	 * @param shibboletAttributes
	 * @return
	 */
	public User syncAttributes(User user, Map<String, String> shibboiethAttributes);

	/**
	 * This methods gets the ShibbolethAttributeHandler for the attribute with
	 * the given attributeName and parses the attributeValue.
	 *
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	public String parseShibbolethAttribute(String attributeName, String attributeValue);

}
