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
package org.olat.modules.video.model;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;

/**
 * 
 * Initial date: 29 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchVideoInCollectionParams {
	
	private final Roles roles;
	private final Identity identity;
	
	private String text;
	private OrderBy orderBy;
	private boolean orderByAsc;
	private List<OrganisationRef> organisations;
	
	public SearchVideoInCollectionParams(Identity identity, Roles roles) {
		this.identity = identity;
		this.roles = roles;
	}

	public Identity getIdentity() {
		return identity;
	}

	public Roles getRoles() {
		return roles;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public boolean hasOrganisations() {
		return organisations != null && !organisations.isEmpty();
	}

	public List<OrganisationRef> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<OrganisationRef> organisations) {
		this.organisations = organisations;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isOrderByAsc() {
		return orderByAsc;
	}

	public void setOrderByAsc(boolean orderByAsc) {
		this.orderByAsc = orderByAsc;
	}
	
	public enum OrderBy {
		key,
		automatic,
		title,
		author,
		creationDate,
		launchCounter
	}
}
