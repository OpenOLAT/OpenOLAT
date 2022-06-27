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
package org.olat.resource.accesscontrol;

import org.olat.core.id.Organisation;

/**
 * Map the relation between an offer and an organisation.
 * - Bookable, GUI visible offers (with a method) must have at least one organisation.
 * - Bookable, not visible offers (e.g. auto booking) must not have on organisation.
 * - Open access offers must have an organisation.
 * - Gust access offers must not have an organisation.
 * 
 * Initial date: 22.04.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface OfferToOrganisation {
	
	public Offer getOffer();
	
	public Organisation getOrganisation();

}
