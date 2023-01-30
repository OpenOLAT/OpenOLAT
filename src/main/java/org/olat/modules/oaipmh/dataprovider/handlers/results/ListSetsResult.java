/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.handlers.results;

import java.util.List;

import org.olat.modules.oaipmh.dataprovider.model.Set;

/**
 * @author Development @ Lyncode
 * @version 3.1.0
 */
public class ListSetsResult {
	private boolean hasMore;
	private List<Set> results;
	private int total = -1;

	public ListSetsResult(boolean hasMoreResults, List<Set> results) {
		this.hasMore = hasMoreResults;
		this.results = results;
	}

	public ListSetsResult(boolean hasMoreResults, List<Set> results, int total) {
		this.hasMore = hasMoreResults;
		this.results = results;
		this.total = total;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public List<Set> getResults() {
		return results;
	}

	public boolean hasTotalResults() {
		return this.total > 0;
	}

	public int getTotalResults() {
		return this.total;
	}
}
