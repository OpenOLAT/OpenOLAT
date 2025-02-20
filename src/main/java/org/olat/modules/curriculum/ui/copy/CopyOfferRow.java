/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.copy;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.id.Organisation;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;

/**
 * 
 * Initial date: 18 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyOfferRow {
	
	private final String type;
	private final OfferAndAccessCopy offerAndInfos;
	private final List<Organisation> organisations;
	
	private DateChooser validFromToEl;
	
	public CopyOfferRow(OfferAndAccessCopy offerAndInfos, List<Organisation> organisations, String type) {
		this.type = type;
		this.offerAndInfos = offerAndInfos;
		this.organisations = organisations;
	}
	
	public String getType() {
		return type;
	}

	public Offer getOffer() {
		return offerAndInfos.getOffer();
	}
	
	public OfferAccess getOfferAccess() {
		return offerAndInfos.getOfferAccess();
	}
	
	public OfferAndAccessCopy getOfferAndInfos() {
		return offerAndInfos;
	}

	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public DateChooser getValidFromToEl() {
		return validFromToEl;
	}

	public void setValidFromToEl(DateChooser validFromToEl) {
		this.validFromToEl = validFromToEl;
	}
}
