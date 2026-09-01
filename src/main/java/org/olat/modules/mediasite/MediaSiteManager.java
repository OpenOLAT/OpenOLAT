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
package org.olat.modules.mediasite;

import org.olat.course.nodes.mediasite.MediaSiteRunController;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public interface MediaSiteManager {

	/**
	 * Parse the module id. Remove the unnecessary part
	 * if someone inserts the whole weblink from the MediaSite website 
	 * 
	 * @param identifier Could be a module id or a URL
	 * @return the parsed module id
	 */
	public String parseAlias(String identifier);
	
	/**
	 * Copies the LTI 1.3 MediaSite configuration from a source tool/deployment/context tree to a target 
	 * tree.
	 *
	 * @param sourceEntry The repository entry (course) from which the configuration should be copied.
	 * @param targetEntry The repository entry (course) to which the configuration should be copied.
	 * @param subIdent The sub-identifier (course node ID) within the source and target repository entry. 
	 * 
	 * @return The unique key of the newly copied LTI 1.3 tool configuration.
	 */
	Long copyLti13MediaSiteConfiguration(RepositoryEntry sourceEntry, RepositoryEntry targetEntry, String subIdent);

	LTI13Context createLti13Context(String targetUrl, LTI13ToolDeployment deployment, RepositoryEntry courseEntry, 
									String subIdent, MediaSiteRunController mediaSiteRunController);

	void deleteLti13MediaSiteConfiguration(RepositoryEntry entry, String ident, Long toolKey);

	/**
	 * Resolves the deployment of a course-local (private-login) MediaSite LTI 1.3 tool: the key
	 * stored explicitly in the course node's module configuration (MediaSiteCourseNode.CONFIG_LTI13_DEPLOYMENT_KEY)
	 * if present, otherwise falls back to the tool's only/first deployment for courses configured
	 * before the key was stored explicitly. Never mutates the given configuration.
	 *
	 * @param config The course node's module configuration.
	 * @param tool The course-local LTI 1.3 tool.
	 * @return The deployment, or null if none can be found.
	 */
	LTI13ToolDeployment resolveCourseDeployment(ModuleConfiguration config, LTI13Tool tool);
}
