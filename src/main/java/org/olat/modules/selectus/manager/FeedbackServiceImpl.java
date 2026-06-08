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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
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
@Service
public class FeedbackServiceImpl implements FeedbackService {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private PublicFeedbackDAO publicFeedbackDao;
	@Autowired
	private ApplicationFeedbackDAO applicationFeedbackDao;
	@Autowired
	private ApplicationsFeedbackConfigurationDAO applicationsFeedbackConfigurationDao;

	@Override
	public List<ApplicationsFeedbackConfiguration> getOrCreateApplicationsFeedbackConfigurations(String defaultName, Position position) {
		List<ApplicationsFeedbackConfiguration> configurations = applicationsFeedbackConfigurationDao.getFeedbackConfigurations(position);
		if(configurations.isEmpty()) {
			ApplicationsFeedbackConfiguration defaultConfig = applicationsFeedbackConfigurationDao.createFeedbackConfiguration(defaultName, position);
			dbInstance.commit();
			
			configurations = new ArrayList<>();
			configurations.add(defaultConfig);
		}
		return configurations;
	}
	
	@Override
	public List<ApplicationsFeedbackConfiguration> getApplicationsFeedbackConfigurations(PositionRef position) {
		return applicationsFeedbackConfigurationDao.getFeedbackConfigurations(position);
	}

	@Override
	public ApplicationsFeedbackConfiguration getApplicationsFeedbackConfiguration(ApplicationsFeedbackConfiguration configuration) {
		return applicationsFeedbackConfigurationDao.loadFeedbackConfigurationByKey(configuration.getKey());
	}

	@Override
	public boolean hasFeedbackConfigurationEnabled(PositionRef position) {
		return applicationsFeedbackConfigurationDao.hasFeedbackConfigurationEnabled(position);
	}

	@Override
	public ApplicationsFeedbackConfiguration updateApplicationsFeedbackConfiguration(ApplicationsFeedbackConfiguration configuration) {
		return applicationsFeedbackConfigurationDao.updateFeedbackConfiguration(configuration);
	}
	
	@Override
	public List<ApplicationFeedback> addFeedbacksMembers(List<ApplicationLight> apps, List<Identity> members, Date deadline, ApplicationsFeedbackConfiguration configuration) {
		List<Long> applicationKeys = apps.stream()
				.map(ApplicationLight::getKey)
				.collect(Collectors.toList());
		
		List<ApplicationFeedback> feedbacks = new ArrayList<>();
		List<Application> applications = applicationDao.loadApplicationsByKeyForRelations(applicationKeys);
		for(Identity member:members) {
			for(Application app:applications) {
				if(!applicationFeedbackDao.hasFeedback(member, app, configuration)) {
					ApplicationFeedback feedback = applicationFeedbackDao.createFeedback(member, app, deadline, configuration);
					feedbacks.add(feedback);
				}
			}
		}
		
		return feedbacks;
	}

	@Override
	public List<ApplicationFeedback> getApplicationFeedbacks(ApplicationRef application) {
		return applicationFeedbackDao.loadByApplication(application);
	}

	@Override
	public List<ApplicationFeedback> searchApplicationsFeedbacks(ReferenceStatus status, Date limitDeadline) {
		return applicationFeedbackDao.searchApplicationFeedback(status, limitDeadline);
	}

	@Override
	public List<ApplicationFeedback> getApplicationsFeedbacks(List<? extends ApplicationRef> applications) {
		return applicationFeedbackDao.loadByApplications(applications);
	}

	@Override
	public List<ApplicationFeedback> getApplicationsFeedbacks(PositionRef position) {
		return applicationFeedbackDao.loadByPosition(position);
	}

	@Override
	public List<ApplicationFeedback> getApplicationFeedbacks(IdentityRef identity) {
		return applicationFeedbackDao.loadByMember(identity);
	}

	@Override
	public ApplicationFeedback getApplicationFeedback(ApplicationFeedback feedback) {
		return applicationFeedbackDao.loadByKey(feedback);
	}

	@Override
	public boolean hasApplicationFeedbacks(IdentityRef identity) {
		return applicationFeedbackDao.hasFeedbackOpen(identity);
	}
	
	@Override
	public ApplicationFeedback updateApplicationFeedback(ApplicationFeedback feedback) {
		return applicationFeedbackDao.updateFeedback(feedback);
	}

	@Override
	public void deleteApplicationFeedback(ApplicationFeedback feedback) {
		feedback = applicationFeedbackDao.loadByKey(feedback);
		if(feedback != null) {
			applicationFeedbackDao.deleteFeedback(feedback);
		}
	}

	@Override
	public boolean isApplicationFeedbackEnabled(Position position, Application application) {
		 return recruitingModule.isMembersFeedbackEnabled()
				 && applicationFeedbackDao.hasFeedbackOpen(application)
				 && hasFeedbackConfigurationEnabled(position);
	}

	@Override
	public PublicFeedback getPublicFeedback(String firstName, String lastName, String email,
			String externalId, String externalRef, Application application) {
		PublicFeedback feedback = publicFeedbackDao.getFeedbackBy(application, email, externalId);
		if(feedback == null) {
			feedback = publicFeedbackDao.createFeedback(firstName, lastName, email, externalId, externalRef, application);
			dbInstance.commit();
		}
		return feedback;
	}

	@Override
	public PublicFeedback updatePublicFeedback(PublicFeedback feedback) {
		return publicFeedbackDao.updatePublicFeedback(feedback);
	}
	
	@Override
	public void deletePublicFeedback(PublicFeedback feedback) {
		publicFeedbackDao.deletePublicFeedback(feedback);
	}

	@Override
	public List<PublicFeedback> getPublicFeedbacks(ApplicationRef application) {
		return publicFeedbackDao.getFeedbacks(application);
	}

	@Override
	public String getPublicFeedbackLink(Application application) {
		String serverUri = Settings.getServerContextPathURI();
		if(!serverUri.endsWith("/")) {
			serverUri += "/";
		}
		serverUri += "publicfeedback/" + application.getPublicFeedbackKey();
		return serverUri;
	}

	@Override
	public boolean isPublicFeedbackEnabled(Position position, Application application) {
		return recruitingModule.isPublicFeedbackEnabled()
				&& position.isPublicFeedbackEnabled()
				&& (application == null || application.isPublicFeedbackEnabled());
	}
}
