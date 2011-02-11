package org.olat.admin.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;

public interface UserSearchUIService {
	/**
	 * creates a user search workflow
	 * 
	 * @param withCancelButton true, if a cancel button should be offered, false otherwise
	 * @param ureq the userrequest
	 * @param wControl the windowcontrol
	 * @return a usersearch controller
	 */
	public Controller createUserSearch(boolean withCancelButton, UserRequest ureq, WindowControl wControl);

	/**
	 * returns the chosen identity
	 * 
	 * @param event the received event
	 * @return the chosen Identity or null if the workflow was cancelled
	 */
	public Identity getChosenUser(Event event);

}
