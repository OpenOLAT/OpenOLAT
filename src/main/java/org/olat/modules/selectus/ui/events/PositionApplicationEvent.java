/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
