package org.olat.core.commons.services.csp.ui.event;

import org.olat.core.commons.services.csp.CSPLog;
import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NextEntryEvent extends Event {

	private static final long serialVersionUID = 2593090663934151479L;

	public static final String NEXT_EVENT = "next-log-entry";
	
	private final CSPLog entry;
	
	public NextEntryEvent(CSPLog entry) {
		super(NEXT_EVENT);
		this.entry = entry;
	}

	public CSPLog getEntry() {
		return entry;
	}
}
