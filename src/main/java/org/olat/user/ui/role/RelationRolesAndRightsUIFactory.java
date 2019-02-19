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
package org.olat.user.ui.role;

import java.util.Locale;

import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRightProvider;
import org.olat.basesecurity.RelationRole;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.user.UserModule;

/**
 * 
 * Initial date: 29 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RelationRolesAndRightsUIFactory {
	
	public static final String TRANS_ROLE_PREFIX = "relation.role.";
	
	public static String getTranslatedRight(RelationRight right, Locale locale) {
		IdentityRelationshipService relationshipService = CoreSpringFactory.getImpl(IdentityRelationshipService.class);
		RelationRightProvider provider = relationshipService.getRelationRightProvider(right);
		return provider != null? provider.getTranslatedName(locale): "???";
	}
	
	public static String getTranslatedRole(RelationRole role, Locale locale) {
		Translator translator = Util.createPackageTranslator(UserModule.class, locale);
		return getTranslatedRole(translator, role);
	}

	public static String getTranslatedRole(Translator translator, RelationRole role) {
		String translatedRole = translator.translate(TRANS_ROLE_PREFIX + role.getKey());
		if (translatedRole.length() > 256 || translatedRole.startsWith(RelationRolesAndRightsUIFactory.TRANS_ROLE_PREFIX)) {
			translatedRole = role.getRole();
		}
		return translatedRole;
	}
	
}
