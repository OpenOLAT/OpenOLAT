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
package org.olat.modules.grade;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 
 * Initial date: 17 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeSystemSearchParams {
	
	private Collection<Long> keys;
	private String identifier;
	private boolean enabledOnly;

	public Collection<Long> getKeys() {
		return keys;
	}
	
	public void setGradeSystem(GradeSystemRef gradeSystem) {
		this.keys = Collections.singletonList(gradeSystem.getKey());
	}

	public void setGradeSystems(Collection<GradeSystemRef> gradeSystems) {
		this.keys = gradeSystems.stream().map(GradeSystemRef::getKey).collect(Collectors.toList());
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isEnabledOnly() {
		return enabledOnly;
	}

	public void setEnabledOnly(boolean enabledOnly) {
		this.enabledOnly = enabledOnly;
	}
	
}
