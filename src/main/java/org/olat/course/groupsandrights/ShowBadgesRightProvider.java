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
package org.olat.course.groupsandrights;

import java.util.Locale;

import org.olat.basesecurity.RightProvider;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;

import org.springframework.stereotype.Component;

/**
 * Initial date: 2023-08-10<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */

@Component
public class ShowBadgesRightProvider implements RightProvider {

	public static final String RELATION_RIGHT = "showBadges";


	@Override
	public String getRight() {
		return RELATION_RIGHT;
	}

	@Override
	public RightProvider getParent() {
		return null;
	}

	@Override
	public boolean isUserRelationsRight() {
		return true;
	}

	@Override
	public int getUserRelationsPosition() {
		return UserRelationRightsOrder.ShowBadgesRight.ordinal();
	}

	@Override
	public int getOrganisationPosition() {
		return OrganisationRightsOrder.ShowBadgesRight.ordinal();
	}

	@Override
	public String getTranslatedName(Locale locale) {
		Translator translator = Util.createPackageTranslator(OpenBadgesUIFactory.class, locale);
		return translator.translate("rights.show.badges");
	}
}
