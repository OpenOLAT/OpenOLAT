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
package org.olat.course.assessment.model;

import java.util.Date;
import java.util.List;

/**
 * 
 * Initial date: 06.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SearchAssessmentModeParams {
	
	private Date dateTo;
	private Date dateFrom;
	private String idAndRefs;
	private String name;
	
	private String externalId;
	private Boolean managed;
	private Boolean withExternalId;
	
	private Boolean running;
	
	private Long repositoryEntryKey;

	private List<String> allowedModeStatus;

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date date) {
		this.dateFrom = date;
	}
	
	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date date) {
		this.dateTo = date;
	}
	
	public String getIdAndRefs() {
		return idAndRefs;
	}

	public void setIdAndRefs(String id) {
		this.idAndRefs = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Boolean getManaged() {
		return managed;
	}

	public void setManaged(Boolean managed) {
		this.managed = managed;
	}

	public Boolean getWithExternalId() {
		return withExternalId;
	}

	public void setWithExternalId(Boolean withExternalId) {
		this.withExternalId = withExternalId;
	}

	public Boolean getRunning() {
		return running;
	}

	public void setRunning(Boolean running) {
		this.running = running;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public void setRepositoryEntryKey(Long repositoryEntryKey) {
		this.repositoryEntryKey = repositoryEntryKey;
	}

	public List<String> getAllowedModeStatus() {
		return allowedModeStatus;
	}

	/**
	 * If certain status is not allowed:
	 * Setting this param will consider by "or" mode.endWithFollowupTime>=current_date inside the query
	 *
	 * @param allowedModeStatus list of allowedModeStatus as strings, those will be considered for the result
	 */
	public void setAllowedModeStatus(List<String> allowedModeStatus) {
		this.allowedModeStatus = allowedModeStatus;
	}
}
