package org.olat.search.ui;

import org.apache.commons.collections.map.LRUMap;
import org.olat.core.commons.services.search.SearchResults;

class SearchLRUCache extends LRUMap {

	public SearchLRUCache() {
		super(5);
	}
	
	@Override
	public SearchResults get(Object key) {
		SearchEntry searchEntry = (SearchEntry)super.get(key);
		if(searchEntry != null) {
			if(searchEntry.isUpToDate()) {
				return searchEntry.getSearchResults();
			}
			remove(key);
		}
		return null;
	}

	@Override
	public Object put(Object key, Object value) {
		return super.put(key, new SearchEntry((SearchResults)value));
	}

	public class SearchEntry {
		private final SearchResults results;
		private long timestamp = System.currentTimeMillis();
		
		public SearchEntry(SearchResults results) {
			this.results = results;
		}
		
		public SearchResults getSearchResults() {
			return results;
		}
		
		public boolean isUpToDate() {
			return System.currentTimeMillis() - timestamp < 300000;
		}
	}
}
