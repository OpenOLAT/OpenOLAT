package org.olat.modules.portfolio.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.portfolio.Page;

/**
 * 
 * Initial date: 18.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageRemoved extends Event {

	private static final long serialVersionUID = -7075556842338612328L;

	public static final String PAGE_REMOVED = "pfpage-removed";
	
	private final Page page;
	
	public PageRemoved(Page page) {
		super(PAGE_REMOVED);
		this.page = page;
	}

	public Page getPage() {
		return page;
	}
}
