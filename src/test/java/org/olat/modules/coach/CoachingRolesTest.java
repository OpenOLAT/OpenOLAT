/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * 
 * Initial date: 4 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachingRolesTest {
	
	@Test
	public void intersect() {
		List<CoachingRoles> owners = CoachingRoles.intersect(List.of(CoachingRoles.owner, CoachingRoles.coach), List.of(CoachingRoles.owner));
		Assertions.assertThat(owners)
			.containsExactlyInAnyOrder(CoachingRoles.owner);
		
		List<CoachingRoles> coachs = CoachingRoles.intersect(List.of(CoachingRoles.coach), List.of(CoachingRoles.coach, CoachingRoles.mastercoach));
		Assertions.assertThat(coachs)
			.containsExactlyInAnyOrder(CoachingRoles.coach);
		
		List<CoachingRoles> coachsAndMasterCoachs = CoachingRoles.intersect(List.of(CoachingRoles.coach, CoachingRoles.mastercoach), List.of(CoachingRoles.coach, CoachingRoles.mastercoach));
		Assertions.assertThat(coachsAndMasterCoachs)
			.containsExactlyInAnyOrder(CoachingRoles.coach, CoachingRoles.mastercoach);
	}

}
