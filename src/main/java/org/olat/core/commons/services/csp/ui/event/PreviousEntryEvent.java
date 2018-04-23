package org.olat.core.commons.services.csp.ui.event;

import org.olat.core.commons.services.csp.CSPLog;
import org.olat.core.gui.control.Event;

/**
 * å
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PreviousEntryEvent extends Event {

	private static final long serialVersionUID = -7672843364001117384L;

	public static final String PREVIOUS_EVENT = "previous-log-entry";
	
	private final CSPLog entry;
	
	public PreviousEntryEvent(CSPLog entry) {
		super(PREVIOUS_EVENT);
		this.entry = entry;
	}

	public CSPLog getEntry() {
		return entry;
	}
}
