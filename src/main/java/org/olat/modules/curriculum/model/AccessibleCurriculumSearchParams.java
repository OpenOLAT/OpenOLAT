/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum.model;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.curriculum.CurriculumRef;

/**
 * Initial date: 21 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class AccessibleCurriculumSearchParams {

	private final IdentityRef identity;
	private boolean includeImplementationOwnership;
	private List<Long> curriculumKeys;

	public AccessibleCurriculumSearchParams(IdentityRef identity) {
		this.identity = identity;
	}

	public IdentityRef getIdentity() {
		return identity;
	}

	public boolean isIncludeImplementationOwnership() {
		return includeImplementationOwnership;
	}

	public void setIncludeImplementationOwnership(boolean includeImplementationOwnership) {
		this.includeImplementationOwnership = includeImplementationOwnership;
	}

	public List<Long> getCurriculumKeys() {
		return curriculumKeys;
	}

	public void setCurriculumKeys(List<Long> curriculumKeys) {
		this.curriculumKeys = List.copyOf(curriculumKeys);
	}

	public void setCurriculum(CurriculumRef curriculum) {
		this.curriculumKeys = List.of(curriculum.getKey());
	}

	public void setCurriculums(List<? extends CurriculumRef> curriculums) {
		this.curriculumKeys = curriculums.stream().map(CurriculumRef::getKey).toList();
	}

}
