/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.instantMessaging;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.ui.ChatViewConfig;
import org.olat.instantMessaging.ui.RosterFormDisplay;

/**
 * 
 * Description:<br>
 * Message to open a new message window. Only use this event with
 * the Single VM message bus with the SingleUserEventCenter!!!
 * 
 * <P>
 * Initial Date:  2 mar. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenInstantMessageEvent extends MultiUserEvent {

	private static final long serialVersionUID = -7767366726634855700L;
	
	private Buddy buddy;
	private boolean vip;
	private boolean persistent;

	private OLATResourceable ores;
	private String resSubPath;
	private String channel;
	
	private ChatViewConfig viewConfig;
	private RosterFormDisplay rosterDisplay;

	public OpenInstantMessageEvent() {
		super("openim");
	}
	
	public OpenInstantMessageEvent(Buddy buddy) {
		this();
		this.buddy = buddy;
	}
	
	public OpenInstantMessageEvent(OLATResourceable ores, String resSubPath, String channel,
			ChatViewConfig viewConfig, boolean vip, boolean persistent, RosterFormDisplay rosterDisplay) {
		this();
		this.ores = OresHelper.clone(ores);
		this.resSubPath = resSubPath;
		this.channel = channel;
		this.viewConfig = viewConfig;
		this.vip = vip;
		this.persistent = persistent;
		this.rosterDisplay = rosterDisplay;
	}

	public Buddy getBuddy() {
		return buddy;
	}

	public OLATResourceable getOres() {
		return ores;
	}
	
	public String getResSubPath() {
		return resSubPath;
	}

	public String getChannel() {
		return channel;
	}

	public ChatViewConfig getViewConfig() {
		return viewConfig;
	}
	
	public boolean isVip() {
		return vip;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public RosterFormDisplay getRosterDisplay() {
		return rosterDisplay;
	}
}
