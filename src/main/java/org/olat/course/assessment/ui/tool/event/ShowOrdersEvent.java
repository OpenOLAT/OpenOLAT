package org.olat.course.assessment.ui.tool.event;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.core.id.context.ContextEntry;

/**
 * 
 * Initial date: 21 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShowOrdersEvent extends Event {
	
	private static final long serialVersionUID = -6467406959698840743L;
	
	public static final String SHOW_ORDERS = "show-orders";
	
	private List<ContextEntry> entries;
	
	public ShowOrdersEvent() {
		super(SHOW_ORDERS);
	}
	
	public ShowOrdersEvent(List<ContextEntry> entries) {
		super(SHOW_ORDERS);
		this.entries = entries;
	}
	
	public List<ContextEntry> getEntries() {
		return entries;
	}
}
