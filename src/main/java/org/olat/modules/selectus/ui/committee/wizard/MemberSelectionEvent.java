/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 23 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberSelectionEvent extends Event {

	private static final long serialVersionUID = 1225210814589602480L;

	public static final String SELECT_MEMBER = "search-select-m";
	
	private final Identity identity;
	 
	 public MemberSelectionEvent(Identity identity) {
		 super(SELECT_MEMBER);
		 this.identity = identity;
	 }

	public Identity getIdentity() {
		return identity;
	}
}
