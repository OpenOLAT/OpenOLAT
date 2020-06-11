package org.olat.ims.qti21.ui.report;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 11 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchEvent extends Event {

	private static final long serialVersionUID = -6345403305741867680L;

	public static final String SEARCH_EVENT = "search-question-test";
	
	private final String author;
	private final String searchString;
	
	public SearchEvent(String searchString, String author) {
		super(SEARCH_EVENT);
		this.author = author;
		this.searchString = searchString;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getSearchString() {
		return searchString;
	}

}
