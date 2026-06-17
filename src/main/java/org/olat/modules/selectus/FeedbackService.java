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
package org.olat.modules.selectus;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.model.ReferenceStatus;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface FeedbackService {
	
	public List<ApplicationsFeedbackConfiguration> getOrCreateApplicationsFeedbackConfigurations(String defaultName, Position position);
	
	public List<ApplicationsFeedbackConfiguration> getApplicationsFeedbackConfigurations(PositionRef position);
	
	public ApplicationsFeedbackConfiguration getApplicationsFeedbackConfiguration(ApplicationsFeedbackConfiguration configuration);
	
	public boolean hasFeedbackConfigurationEnabled(PositionRef position);
	
	public ApplicationsFeedbackConfiguration updateApplicationsFeedbackConfiguration(ApplicationsFeedbackConfiguration configuration);
	
	public List<ApplicationFeedback> addFeedbacksMembers(List<ApplicationLight> apps, List<Identity> members, Date deadline, ApplicationsFeedbackConfiguration configuration);
	
	public List<ApplicationFeedback> getApplicationFeedbacks(ApplicationRef application);
	
	public List<ApplicationFeedback> getApplicationsFeedbacks(List<? extends ApplicationRef> applications);
	
	/**
	 * @param status The status (mandatory)
	 * @param limitDeadline The deadline (mandatory)
	 * @return All the feedbacks with the deadline before the specified date and with the specified status.
	 */
	public List<ApplicationFeedback> searchApplicationsFeedbacks(ReferenceStatus status, Date limitDeadline);
	
	public ApplicationFeedback getApplicationFeedback(ApplicationFeedback feedback);
	
	public List<ApplicationFeedback> getApplicationFeedbacks(IdentityRef identity);
	
	public boolean hasApplicationFeedbacks(IdentityRef identity);
	
	public List<ApplicationFeedback> getApplicationsFeedbacks(PositionRef position);

	public boolean isApplicationFeedbackEnabled(Position position, Application application);

	public ApplicationFeedback updateApplicationFeedback(ApplicationFeedback feedback);
	
	public void deleteApplicationFeedback(ApplicationFeedback feedback);
	
	public PublicFeedback getPublicFeedback(String firstName, String lastName, String email,
			String externalid, String externalRef, Application application);
	
	public PublicFeedback updatePublicFeedback(PublicFeedback feedback);
	
	public void deletePublicFeedback(PublicFeedback feedback);
	
	public List<PublicFeedback> getPublicFeedbacks(ApplicationRef application);
	
	public String getPublicFeedbackLink(Application application);

	public boolean isPublicFeedbackEnabled(Position position, Application application);

}
