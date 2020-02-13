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
package org.olat.modules.grading.ui.wizard;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 28 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignGrader {
	
	private Identity grader;
	private RepositoryEntry entry;
	private final List<GraderToIdentity> currentGrader;
	
	public AssignGrader(RepositoryEntry entry) {
		this.entry = entry;
		currentGrader = null;
	}
	
	public AssignGrader(RepositoryEntry entry, List<GraderToIdentity> currentGrader) {
		this.entry = entry;
		this.currentGrader = currentGrader;
	}

	public Identity getGrader() {
		return grader;
	}

	public void setGrader(Identity grader) {
		this.grader = grader;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	public List<GraderToIdentity> getCurrentGrader() {
		return currentGrader;
	}
}
