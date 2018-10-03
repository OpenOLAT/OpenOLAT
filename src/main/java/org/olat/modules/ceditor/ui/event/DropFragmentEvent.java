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
package org.olat.modules.ceditor.ui.event;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 12 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DropFragmentEvent extends Event {

	private static final long serialVersionUID = 7464855877445936717L;
	public static final String DROP = "drop-fragment";
	
	private final String dragged;
	private final String source;
	private final String targetCmpId;
	private final String siblingCmpId;
	private final String containerCmpId;
	private final String slotId;
	
	public DropFragmentEvent(String dragged, String sourceCmpId, String targetCmpId, String siblingCmpId, String containerCmpId, String slotId) {
		super(DROP);
		this.dragged = dragged;
		this.source = sourceCmpId;
		this.targetCmpId = targetCmpId;
		this.siblingCmpId = siblingCmpId;
		this.containerCmpId = containerCmpId;
		this.slotId = slotId;
	}
	
	public String getDragged() {
		return dragged;
	}
	
	public String getSource() {
		return source;
	}

	public String getTargetCmpId() {
		return targetCmpId;
	}

	public String getSiblingCmpId() {
		return siblingCmpId;
	}

	public String getContainerCmpId() {
		return containerCmpId;
	}

	public String getSlotId() {
		return slotId;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("dropFragment[dragged=").append(dragged == null ? "" : dragged).append(";")
		  .append("source=").append(source == null ? "" : source).append(";")
		  .append("target=").append(targetCmpId == null ? "" : targetCmpId).append(";")
		  .append("sibling=").append(siblingCmpId == null ? "" : siblingCmpId).append(";")
		  .append("container=").append(containerCmpId == null ? "" : containerCmpId).append(";")
		  .append("slot=").append(slotId == null ? "" : slotId).append("]");
		return sb.toString();
	}
}
