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
package org.olat.course.nodes.mediasite;

import org.olat.core.id.Identity;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIDisplayOptions;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteContext implements LTIContext {
	
	private static final String LTI_ROLE = "Learner";
	private static final String HEIGTH = "auto";
	private static final String WIDTH = "auto";
	private static final LTIDisplayOptions TARGET = LTIDisplayOptions.iframe;

	@Override
	public String getSourcedId() {
		return null;
	}

	@Override
	public String getTalkBackMapperUri() {
		return null;
	}

	@Override
	public String getOutcomeMapperUri() {
		return null;
	}

	@Override
	public String getResourceId() {
		return null;
	}

	@Override
	public String getResourceTitle() {
		return null;
	}

	@Override
	public String getResourceDescription() {
		return null;
	}

	@Override
	public String getContextId() {
		return null;
	}

	@Override
	public String getContextTitle() {
		return null;
	}

	@Override
	public String getUserId(Identity identity) {
		return identity.getUser().getKey().toString();
	}

	@Override
	public String getRoles(Identity identity) {
		return LTI_ROLE;
	}

	@Override
	public String getCustomProperties() {
		return null;
	}

	@Override
	public String getTarget() {
		return TARGET.toString();
	}

	@Override
	public String getPreferredWidth() {
		return WIDTH;
	}

	@Override
	public String getPreferredHeight() {
		return HEIGTH;
	}

}
