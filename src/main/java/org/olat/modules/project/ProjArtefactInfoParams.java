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
package org.olat.modules.project;

/**
 * 
 * Initial date: 16 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjArtefactInfoParams {
	
	public static final ProjArtefactInfoParams ALL = of(true, true, true);
	public static final ProjArtefactInfoParams MEMBERS = of(true, false, false);
	public static final ProjArtefactInfoParams TAGS = of(false, false, true);
	
	private final boolean members;
	private final boolean numReferences;
	private final boolean tags;
	
	public static ProjArtefactInfoParams of(boolean members, boolean numReferences, boolean tags) {
		return new ProjArtefactInfoParams(members, numReferences, tags);
	}
	
	private ProjArtefactInfoParams(boolean members, boolean numReferences, boolean tags) {
		this.members = members;
		this.numReferences = numReferences;
		this.tags = tags;
	}

	public boolean isMembers() {
		return members;
	}
	
	public boolean isNumReferences() {
		return numReferences;
	}
	
	public boolean isTags() {
		return tags;
	}

}
