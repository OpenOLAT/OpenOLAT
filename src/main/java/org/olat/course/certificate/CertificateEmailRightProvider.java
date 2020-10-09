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
package org.olat.course.certificate;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.RightProvider;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.certificate.ui.CertificateController;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 24.04.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class CertificateEmailRightProvider implements RightProvider {

	public static final String RELATION_RIGHT = "certificateEmail";

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
		return UserRelationRightsOrder.CertificateEmailRight.ordinal();
	}

	@Override
	public Collection<OrganisationRoles> getOrganisationRoles() {
		return Collections.emptyList();
	}

	@Override
	public int getOrganisationPosition() {
		return -1;
	}

	@Override
	public String getTranslatedName(Locale locale) {
		Translator translator = Util.createPackageTranslator(CertificateController.class, locale);
		return translator.translate("relation.right.email");
	}

}
