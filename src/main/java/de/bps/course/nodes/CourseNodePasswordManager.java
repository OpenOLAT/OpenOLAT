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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes;

import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;

public interface CourseNodePasswordManager {
	
	/**
	 * returns the answer container
	 * @param identity
	 * @return
	 */
	public String getAnswer(IdentityEnvironment identityEnv, Long courseId, String nodeIdentifier);
	
	public void removeAnswerContainerFromCache(Identity identity);

	/**
	 * updates inputted password
	 * @param identity
	 * @param nodeIdentifier
	 * @param courseId
	 * @param value
	 */
	public void updatePwd(IdentityEnvironment identityEnv, String nodeIdentifier, Long courseId, String value);

	
}
