/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.mail;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 29 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenVariablesEvent extends Event {
	
	private static final long serialVersionUID = -588498139624785192L;
	public static final String OPEN_VARIABLES = "open-variables";
	
	public OpenVariablesEvent() {
		super(OPEN_VARIABLES);
	}

}
