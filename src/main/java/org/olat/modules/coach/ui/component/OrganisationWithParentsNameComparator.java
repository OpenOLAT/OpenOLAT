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
package org.olat.modules.coach.ui.component;

import java.util.Comparator;
import java.util.Locale;

import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.id.OrganisationNameComparator;

/**
 * 
 * Initial date: 15 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationWithParentsNameComparator implements Comparator<OrganisationWithParents> {

	private final OrganisationNameComparator comparator;
	
	public OrganisationWithParentsNameComparator(Locale locale) {
		comparator = new OrganisationNameComparator(locale);
	}
	
	@Override
	public int compare(OrganisationWithParents o1, OrganisationWithParents o2) {
		return comparator.compare(o1.getOrganisation(), o2.getOrganisation());
	}
}
