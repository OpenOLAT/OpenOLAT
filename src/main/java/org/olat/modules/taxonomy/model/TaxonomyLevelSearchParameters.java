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
package org.olat.modules.taxonomy.model;

import java.util.List;

/**
 * 
 * Initial date: 11 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelSearchParameters {
	
	private String quickSearch;
	private Boolean allowedAsSubject;
	private List<Long> taxonomyKeys;

	public String getQuickSearch() {
		return quickSearch;
	}

	public void setQuickSearch(String search) {
		quickSearch = search;
	}
	
	public Boolean isAllowedAsSubject() {
		return allowedAsSubject;
	}
	
	public void setAllowedAsSubject(Boolean allowedAsSubject) {
		this.allowedAsSubject = allowedAsSubject;
	}
	
	public void setTaxonomyKeys(List<Long> taxonomyKeys) {
		this.taxonomyKeys = taxonomyKeys;
	}
	
	public List<Long> getTaxonomyKeys() {
		return taxonomyKeys;
	}
	
}
