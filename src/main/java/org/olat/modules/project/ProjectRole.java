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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * Initial date: 25 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum ProjectRole {
	
	owner,
	leader,
	projectOffice,
	participant,
	supplier,
	client,
	steeringCommitee,
	invitee;
	
	public static final List<ProjectRole> PROJECT_ROLES = Arrays.asList(owner, leader, projectOffice, participant, supplier, client, steeringCommitee);
	public static final List<ProjectRole> ALL = Arrays.asList(ProjectRole.values());
	public static final Set<String> ALL_NAMES = ALL.stream().map(ProjectRole::name).collect(Collectors.toSet());
	
	public static boolean isValueOf(String val) {
		return ALL_NAMES.contains(val);
	}

}
