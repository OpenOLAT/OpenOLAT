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
package org.olat.modules.project.model;

import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteInfo;

/**
 * 
 * Initial date: 19 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteInfoImpl implements ProjNoteInfo {
	
	private ProjNote note;
	private Set<Identity> members;
	private int numReferences;
	
	@Override
	public ProjNote getNote() {
		return note;
	}
	
	public void setNote(ProjNote note) {
		this.note = note;
	}
	
	@Override
	public Set<Identity> getMembers() {
		return members;
	}
	
	public void setMembers(Set<Identity> members) {
		this.members = members;
	}

	@Override
	public int getNumReferences() {
		return numReferences;
	}

	public void setNumReferences(int numReferences) {
		this.numReferences = numReferences;
	}

}
