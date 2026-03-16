/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 21.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewPositionSavedEvent extends Event {

	private static final long serialVersionUID = 2266892754911810310L;
	public static final String NEW_POSITION = "new-position-saved";
	
	public NewPositionSavedEvent() {
		super(NEW_POSITION);
	}

}
