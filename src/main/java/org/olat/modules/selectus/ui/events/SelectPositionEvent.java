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
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SelectPositionEvent extends Event {
	
	public static final String SELECT_POSITION = "select";
	
	private static final long serialVersionUID = -9175339857739198033L;
	
	private final boolean edit;
	private final Position position;
	
	public SelectPositionEvent(Position position) {
		this(position, false);
	}
	
	public SelectPositionEvent(Position position, boolean edit) {
		super(SELECT_POSITION);
		this.edit = edit;
		this.position = position;
	}
	
	public boolean isEdit() {
		return edit;
	}

	public Position getPosition() {
		return position;
	}
}
