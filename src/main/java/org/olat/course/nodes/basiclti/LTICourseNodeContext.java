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
package org.olat.course.nodes.basiclti;

import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.ims.lti.LTIContext;

/**
 * 
 * Initial date: 13.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTICourseNodeContext implements LTIContext {
	
	private final String roles;
	private final String sourcedId;
	private final String backMapperUri;
	private final String outcomeMapperUri;
	private final CourseNode courseNode;
	private final CourseEnvironment courseEnv;
	
	private final String customProperties;
	
	private String target;
	private String width;
	private String height;
	
	public LTICourseNodeContext(CourseEnvironment courseEnv, CourseNode courseNode,
			String roles, String sourcedId, String backMapperUri, String outcomeMapperUri,
			String customProperties, String target, String width, String height) {
		this.roles = roles;
		this.sourcedId = sourcedId;
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		this.backMapperUri = backMapperUri;
		this.outcomeMapperUri = outcomeMapperUri;
		this.customProperties = customProperties;
		this.target = target;
		this.width = width;
		this.height = height;
	}

	@Override
	public String getSourcedId() {
		return sourcedId;
	}

	@Override
	public String getTalkBackMapperUri() {
		return backMapperUri;
	}
	
	@Override
	public String getOutcomeMapperUri() {
		return outcomeMapperUri;
	}

	@Override
	public String getResourceId() {
		return courseNode.getIdent();
	}

	@Override
	public String getResourceTitle() {
		return courseNode.getShortTitle();
	}

	@Override
	public String getResourceDescription() {
		return courseNode.getLongTitle();
	}

	@Override
	public String getContextId() {
		return courseEnv.getCourseResourceableId().toString();
	}

	@Override
	public String getContextTitle() {
		return courseEnv.getCourseTitle();
	}

	@Override
	public String getRoles(Identity identity) {
		return roles;
	}
	
	@Override
	public String getCustomProperties() {
		return customProperties;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public String getPreferredWidth() {
		return width;
	}

	@Override
	public String getPreferredHeight() {
		return height;
	}

	@Override
	public String getUserId(Identity identity) {
		return identity.getUser().getKey().toString();
	}
}