package org.olat.search.service.searcher;

import java.io.Serializable;
import java.util.List;

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
	
  private String queryString;
  private List<String> condQueries;
  private Long identityId;
  private Roles roles;
  private boolean doHighlighting;
  private int firstResult;
  private int maxResults;
  
  public SearchRequest() {
  	//default constructor
  }
  
	public SearchRequest(String queryString, List<String> condQueries, Long identityId, Roles roles, int firstResult, int maxResults,
			boolean doHighlighting) {
		super();
		this.queryString = queryString;
		this.condQueries = condQueries;
		this.firstResult = firstResult;
		this.maxResults = maxResults;
		this.identityId = identityId;
		this.roles = roles;
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
  
	@Override
	public String toString() {
		//dummy impl
		return "queryString: " + queryString + " identityId: " + identityId + " roles: " + roles + " doHighlighting: " + doHighlighting;
	}
}
