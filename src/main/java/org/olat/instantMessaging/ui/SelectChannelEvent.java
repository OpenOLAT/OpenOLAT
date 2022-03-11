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
package org.olat.instantMessaging.ui;

import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 1 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectChannelEvent extends Event {
	
	private static final long serialVersionUID = 4180926105872099355L;

	public static final String SELECT_CHANNEL = "select-channel";
	
	private OLATResourceable ores;
	private String resSubPath;
	private String channel;
	
	public SelectChannelEvent(OLATResourceable ores, String resSubPath, String channel) {
		super(SELECT_CHANNEL);
		this.ores = ores;
		this.resSubPath = resSubPath;
		this.channel = channel;
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
}
