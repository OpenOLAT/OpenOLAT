/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import java.util.List;

import org.olat.core.gui.control.Event;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  3 mar. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionApplicationEvent extends Event {
	
	private static final long serialVersionUID = 9180409849893266046L;
	
	public static final String NEXT = "next";
	public static final String PREVIOUS = "previous";
	public static final PositionApplicationEvent ALL = new PositionApplicationEvent("all");
	
	private Long appKey;
	private List<Long> sortedAppKeys;

	public PositionApplicationEvent(String cmd) {
		super(cmd);
	}
	
	public PositionApplicationEvent(String cmd, Long appKey, List<Long> sortedAppKeys) {
		super(cmd);
		this.appKey = appKey;
		this.sortedAppKeys = sortedAppKeys;
	}
	
	public Long getAppKey() {
		return appKey;
	}

	public List<Long> getSortedAppKeys() {
		return sortedAppKeys;
	}

	@Override
	public int hashCode() {
		return getCommand().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PositionApplicationEvent) {
			PositionApplicationEvent e = (PositionApplicationEvent)obj;
			return getCommand() != null && getCommand().equals(e.getCommand());
		}
		return false;
	}
}
