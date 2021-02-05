package org.olat.modules.portfolio.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.portfolio.Page;

/**
 * 
 * Initial date: 3 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageSelectionEvent extends Event {

	private static final long serialVersionUID = 3559566708817370123L;
	public static final String SELECT_PAGE = "pf-select-page";
	
	private Page page;
	
	public PageSelectionEvent(Page page) {
		super(SELECT_PAGE);
		this.page = page;
	}

	public Page getPage() {
		return page;
	}
	
}
