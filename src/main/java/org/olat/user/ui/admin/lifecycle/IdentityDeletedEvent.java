package org.olat.user.ui.admin.lifecycle;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 5 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityDeletedEvent extends Event {

	private static final long serialVersionUID = -4395608344592354576L;
	public static final String IDENTITY_DELETED = "identity-deleted";
	
	public IdentityDeletedEvent() {
		super(IDENTITY_DELETED);
	}

}
