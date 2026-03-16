/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 15 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RequestPersistEvent extends Event {

	private static final long serialVersionUID = 421175821058801805L;
	public static final String REQUEST_PERSIST = "request-persist";
	
	public RequestPersistEvent() {
		super(REQUEST_PERSIST);
	}

}
