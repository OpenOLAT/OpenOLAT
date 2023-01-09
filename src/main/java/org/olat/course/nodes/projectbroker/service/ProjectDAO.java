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
package org.olat.course.nodes.projectbroker.service;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Project queries must migrate here.
 * 
 * 
 * Initial date: 01.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjectDAO {
	@Autowired
	private DB dbInstance;
	
	/**
	 * The method load the project or return null if not found. The query fetch
	 * all dependencies but the custom fields.
	 * 
	 * @param projectKey
	 * @return The project
	 */
	public Project loadProject(Long projectKey) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select project from pbrokerproject as project ")
		  .append(" left join fetch project.projectGroup pGroup")
		  .append(" left join fetch pGroup.baseGroup bGroup")
		  .append(" left join fetch project.candidateGroup cGroup")
		  .append(" left join fetch project.projectBroker pBroker")
		  .append(" where project.key=:projectKey");
		
		List<Project> projects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Project.class)
				.setParameter("projectKey", projectKey)
				.getResultList();
		return projects == null || projects.isEmpty() ? null : projects.get(0);
	}

}
