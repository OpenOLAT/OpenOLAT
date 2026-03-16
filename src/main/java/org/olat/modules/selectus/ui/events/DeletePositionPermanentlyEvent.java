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
 * Initial date: 1 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletePositionPermanentlyEvent extends Event {

	private static final long serialVersionUID = 3418848901921552389L;
	public static final String DELETE_POSITION = "delete-position-permanently";
	
	private Position position;
	
	public DeletePositionPermanentlyEvent(Position position) {
		super(DELETE_POSITION);
		this.position = position;
	}
	
	public Position getPosition() {
		return position;
	}
}
