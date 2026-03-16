/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.core.id.context.ContextEntry;

import org.olat.modules.selectus.model.PositionRef;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SelectPositionLightEvent extends Event {
	
	public static final String SELECT_POSITION = "select";
	
	private static final long serialVersionUID = -9175339857739198033L;
	
	private final PositionRef position;
	private List<ContextEntry> activation;
	
	public SelectPositionLightEvent(PositionRef position) {
		this(position, null);
	}
	
	public SelectPositionLightEvent(PositionRef position, List<ContextEntry> activation) {
		super(SELECT_POSITION);
		this.position = position;
		this.activation = activation;
	}

	public PositionRef getPosition() {
		return position;
	}
	
	public List<ContextEntry> getActivation() {
		return activation;
	}
}
