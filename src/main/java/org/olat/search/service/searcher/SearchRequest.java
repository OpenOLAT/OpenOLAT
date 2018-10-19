/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.search.service.searcher;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Roles;

/**
 * 
 * Description:<br>
 * Encapsulates the search request input.
 * 
 * <P>
 * Initial Date:  03.06.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class SearchRequest implements Serializable {

	private static final long serialVersionUID = -2886090379563029101L;
	private String queryString;
	private List<String> condQueries;
	private Long identityId;
	private Roles roles;
	private Locale locale;
	private boolean doHighlighting;
	private int firstResult;
	private int maxResults;
  
	public SearchRequest() {
		//default constructor
	}
  
	public SearchRequest(String queryString, List<String> condQueries, Long identityId, Roles roles, Locale locale,
			int firstResult, int maxResults, boolean doHighlighting) {
		super();
		this.queryString = queryString;
		this.condQueries = condQueries;
		this.firstResult = firstResult;
		this.maxResults = maxResults;
		this.identityId = identityId;
		this.roles = roles;
		this.locale = locale;
		this.doHighlighting = doHighlighting;
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

	public boolean isDoHighlighting() {
		return doHighlighting;
	}

	public void setDoHighlighting(boolean doHighlighting) {
		this.doHighlighting = doHighlighting;
	}

	public Long getIdentityId() {
		return identityId;
	}

	public void setIdentityId(Long identityId) {
		this.identityId = identityId;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public List<String> getCondQueries() {
		return condQueries;
	}

	public void setCondQueries(List<String> condQueries) {
		this.condQueries = condQueries;
	}

	public Roles getRoles() {
		return roles;
	}
	
	public void setRoles(Roles roles) {
		this.roles = roles;
	}
  
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public String toString() {
		//dummy impl
		return "queryString: " + queryString + " identityId: " + identityId + " roles: " + roles + " doHighlighting: " + doHighlighting;
	}
}
