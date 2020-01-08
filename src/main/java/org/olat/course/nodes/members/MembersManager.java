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
package org.olat.course.nodes.members;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 23 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MembersManager {
	
	public List<Long> getOwnersKeys(RepositoryEntryRef re);
	
	public List<Identity> getOwners(RepositoryEntryRef re);
	
	public List<Long> getCoachesKeys(RepositoryEntryRef re, ModuleConfiguration moduleConfiguration);
	
	public List<Identity> getCoaches(RepositoryEntryRef re, ModuleConfiguration moduleConfiguration);
	
	public List<Long> getParticipantsKeys(RepositoryEntryRef re, ModuleConfiguration moduleConfiguration);
	
	public List<Identity> getParticipants(RepositoryEntryRef re, ModuleConfiguration moduleConfiguration);

}
