package org.olat.core.gui.components;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;

public interface ComponentEventListener {
	
	/**
	 * The event method will be called when a listener is added to a source and
	 * the source fires an event
	 * 
	 * @param ureq The user request
	 * @param source The component who fired the event
	 * @param event The event
	 */
	public void dispatchEvent(UserRequest ureq, Component source, Event event);

}
