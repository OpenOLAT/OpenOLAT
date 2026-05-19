/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.roommanagement.model;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.roommanagement.BuildingRef;
import org.olat.modules.roommanagement.RoomStatus;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SearchRoomParameters {

	private String searchString;
	private String exactExternalId;
	private String exactExternalRef;
	private List<RoomStatus> status;
	private BuildingRef building;
	private Integer minSeats;
	private Integer maxSeats;
	private Long organisationKey;
	private Date availableFrom;
	private Date availableTo;
	/** When set, apply the org-scoped visibility predicate for this identity. */
	private IdentityRef identity;
	private int firstResult = 0;
	private int maxResults = 0;

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public String getExactExternalId() {
		return exactExternalId;
	}

	public void setExactExternalId(String exactExternalId) {
		this.exactExternalId = exactExternalId;
	}

	public String getExactExternalRef() {
		return exactExternalRef;
	}

	public void setExactExternalRef(String exactExternalRef) {
		this.exactExternalRef = exactExternalRef;
	}

	public List<RoomStatus> getStatus() {
		return status;
	}

	public void setStatus(List<RoomStatus> status) {
		this.status = status;
	}

	public BuildingRef getBuilding() {
		return building;
	}

	public void setBuilding(BuildingRef building) {
		this.building = building;
	}

	public Integer getMinSeats() {
		return minSeats;
	}

	public void setMinSeats(Integer minSeats) {
		this.minSeats = minSeats;
	}

	public Integer getMaxSeats() {
		return maxSeats;
	}

	public void setMaxSeats(Integer maxSeats) {
		this.maxSeats = maxSeats;
	}

	public Long getOrganisationKey() {
		return organisationKey;
	}

	public void setOrganisationKey(Long organisationKey) {
		this.organisationKey = organisationKey;
	}

	public Date getAvailableFrom() {
		return availableFrom;
	}

	public void setAvailableFrom(Date availableFrom) {
		this.availableFrom = availableFrom;
	}

	public Date getAvailableTo() {
		return availableTo;
	}

	public void setAvailableTo(Date availableTo) {
		this.availableTo = availableTo;
	}

	public IdentityRef getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityRef identity) {
		this.identity = identity;
	}

	public int getFirstResult() {
		return firstResult;
	}

	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public SearchRoomParameters withoutPagination() {
		SearchRoomParameters copy = new SearchRoomParameters();
		copy.searchString = this.searchString;
		copy.exactExternalId = this.exactExternalId;
		copy.exactExternalRef = this.exactExternalRef;
		copy.status = this.status;
		copy.building = this.building;
		copy.minSeats = this.minSeats;
		copy.maxSeats = this.maxSeats;
		copy.organisationKey = this.organisationKey;
		copy.availableFrom = this.availableFrom;
		copy.availableTo = this.availableTo;
		copy.identity = this.identity;
		return copy;
	}
}
