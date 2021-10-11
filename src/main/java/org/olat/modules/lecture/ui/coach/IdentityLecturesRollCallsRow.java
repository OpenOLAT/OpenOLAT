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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;

/**
 * 
 * Initial date: 29 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityLecturesRollCallsRow {
	
	private final Identity identity;
	private final ImmunityProofLevel immunoStatus;
	private FormLink tools;
	
	private List<IdentityLecturesRollCallPart> parts = new ArrayList<>();
	
	public IdentityLecturesRollCallsRow(Identity identity, ImmunityProofLevel immunoStatus) {
		this.identity = identity;
		this.immunoStatus = immunoStatus;
	}
	
	public String getIdentityName() {
		return identity.getName();
	}
	
	public Long getIdentityKey() {
		return identity.getKey();
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public ImmunityProofLevel getImmunoStatus() {
		return immunoStatus;
	}

	public FormLink getTools() {
		return tools;
	}

	public void setTools(FormLink tools) {
		this.tools = tools;
	}
	
	public void add(IdentityLecturesRollCallPart part) {
		parts.add(part);
	}
	
	/**
	 * @return The list of lecture blocks which the identity attendee
	 */
	public List<LectureBlock> getLectureBlocks() {
		List<LectureBlock> blocks = new ArrayList<>(parts.size());
		for(IdentityLecturesRollCallPart part:parts) {
			if(part.isParticipate()) {
				blocks.add(part.getLectureBlock());
			}
		}
		return blocks;
	}
	
	public List<LectureBlockRollCall> getRollCalls() {
		List<LectureBlockRollCall> rollCalls = new ArrayList<>(parts.size());
		for(IdentityLecturesRollCallPart part:parts) {
			if(part.isParticipate() && part.getRollCall() != null) {
				rollCalls.add(part.getRollCall());
			}
		}
		return rollCalls;
	}
	
	public IdentityLecturesRollCallPart getPart(int index) {
		IdentityLecturesRollCallPart part = null;
		if(index >= 0 && index < parts.size()) {
			part = parts.get(index);
		}
		return part;
	}
}
