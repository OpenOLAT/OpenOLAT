/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 18.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationChangeEvent extends Event {

	private static final long serialVersionUID = 8821962676010266544L;

	public ApplicationChangeEvent() {
		super("application-changed");
	}

}
