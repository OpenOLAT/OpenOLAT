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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.modules.oaipmh.dataprovider.model.ItemIdentifier;

/**
 * @author Development @ Lyncode
 * @version 3.1.0
 */
public class ListItemIdentifiersResult {
	private boolean hasMore;
	private List<ItemIdentifier> results;
	private int totalResults = -1;

	public ListItemIdentifiersResult(boolean hasMoreResults, List<ItemIdentifier> results) {
		this.hasMore = hasMoreResults;
		this.results = results;
	}

	public ListItemIdentifiersResult(boolean hasMoreResults, List<ItemIdentifier> results, int totalResults) {
		this.hasMore = hasMoreResults;
		this.results = results;
		this.totalResults = totalResults;
	}

	public ListItemIdentifiersResult(boolean hasMoreResults, List<ItemIdentifier> results, String setSpec, Date from, Date until) {
		List<ItemIdentifier> resultList = new ArrayList<>();

		if (from != null && until != null) {
			resultList = results.stream().filter(l -> l.getDatestamp().after(from) && l.getDatestamp().before(until)).collect(Collectors.toList());
		} else if (from != null) {
			resultList = results.stream().filter(l -> l.getDatestamp().after(from)).collect(Collectors.toList());
		} else if (until != null) {
			resultList = results.stream().filter(l -> l.getDatestamp().before(until)).collect(Collectors.toList());
		}

		if (setSpec != null && from != null && until != null) {
			resultList = results.stream().filter(l -> l.getDatestamp().after(from) && l.getDatestamp().before(until)
					&& l.getSets().stream().allMatch(s -> s.getSpec().equals(setSpec))).collect(Collectors.toList());
		} else if (setSpec != null && from != null) {
			resultList = results.stream().filter(l -> l.getDatestamp().after(from)
					&& l.getSets().stream().allMatch(s -> s.getSpec().equals(setSpec))).collect(Collectors.toList());
		} else if (setSpec != null && until != null) {
			resultList = results.stream().filter(l -> l.getDatestamp().before(until)
					&& l.getSets().stream().allMatch(s -> s.getSpec().equals(setSpec))).collect(Collectors.toList());
		} else if (setSpec != null) {
			resultList = results.stream().filter(l -> l.getSets().stream().allMatch(s -> s.getSpec().equals(setSpec))).collect(Collectors.toList());
		}

		this.hasMore = hasMoreResults;
		this.results = resultList;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public List<ItemIdentifier> getResults() {
		return results;
	}

	public boolean hasTotalResults() {
		return this.totalResults > 0;
	}

	public int getTotal() {
		return this.totalResults;
	}
}
