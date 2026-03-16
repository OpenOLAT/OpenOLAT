/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.gui.control.Event;

import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 26 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewPositionEvent extends Event {

	private static final long serialVersionUID = 3418848901921552389L;
	public static final String NEW_POSITION = "new-position";
	
	private Position newPosition;
	
	public NewPositionEvent(Position newPosition) {
		super(NEW_POSITION);
		this.newPosition = newPosition;
	}
	
	public Position getNewPosition() {
		return newPosition;
	}
}
