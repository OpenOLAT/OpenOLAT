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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.feedback.ApplicationsFeedbackConfigurationImpl;

/**
 * 
 * Initial date: 21 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ApplicationsFeedbackConfigurationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ApplicationsFeedbackConfiguration createFeedbackConfiguration(String configurationName, Position position) {
		ApplicationsFeedbackConfigurationImpl config = new ApplicationsFeedbackConfigurationImpl();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setConfigurationName(configurationName);
		config.setPosition(position);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public ApplicationsFeedbackConfiguration loadFeedbackConfigurationByKey(Long configurationKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select config from rappsfeedback as config")
		  .append(" where config.key=:configurationKey");
		
		List<ApplicationsFeedbackConfiguration> configurations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationsFeedbackConfiguration.class)
				.setParameter("configurationKey", configurationKey)
				.getResultList();
		return configurations == null || configurations.isEmpty() ? null : configurations.get(0);
	}
	
	public ApplicationsFeedbackConfiguration updateFeedbackConfiguration(ApplicationsFeedbackConfiguration configuration) {
		configuration.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(configuration);
	}
	
	public int deleteFeedbackConfigurations(PositionRef position) {
		String q = "delete from rappsfeedback as config where config.position.key=:positionKey";
		return dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("positionKey", position.getKey())
			.executeUpdate();
	}
	
	
	
	public List<ApplicationsFeedbackConfiguration> getFeedbackConfigurations(PositionRef position) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select config from rappsfeedback as config")
		  .append(" where config.position.key=:positionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationsFeedbackConfiguration.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public boolean hasFeedbackConfigurationEnabled(PositionRef position) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select config.key from rappsfeedback as config")
		  .append(" where config.position.key=:positionKey and config.enabled=true");
		
		List<Long> configKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("positionKey", position.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return configKeys != null && !configKeys.isEmpty()
				&& configKeys.get(0) != null && configKeys.get(0).longValue() > 0;
	}

}
