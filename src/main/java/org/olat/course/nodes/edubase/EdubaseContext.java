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
package org.olat.course.nodes.edubase;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIManager;
import org.olat.modules.edubase.EdubaseManager;

/**
 *
 * Initial date: 17.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseContext implements LTIContext {

	private static final String CUSTOM_END_PAGE = "end_page";
	private static final String CUSTOM_APPLICATION = "application";
	private static final String LTI_ROLE = "Learner";
	private static final String HEIGTH = "auto";
	private static final String WIDTH = "auto";
	private static final LTIDisplayOptions TARGET = LTIDisplayOptions.iframe;

	private final Integer pageTo;
	private final IdentityEnvironment identityEnvironment;

	public EdubaseContext(IdentityEnvironment identityEnvironment, Integer pageTo) {
		this.identityEnvironment = identityEnvironment;
		this.pageTo = pageTo;
	}

	@Override
	public String getSourcedId() {
		return null;
	}

	@Override
	public String getTalkBackMapperUri() {
		return "http://openolat.org";
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
	public String getRoles(Identity identity) {
		return LTI_ROLE;
	}

	@Override
	public String getCustomProperties() {
		Map<String, String> customProps = new HashMap<>();

		Identity identity = identityEnvironment.getIdentity();
		String applicationUrl =  CoreSpringFactory.getImpl(EdubaseManager.class).getApplicationUrl(identity);
		if (StringHelper.containsNonWhitespace(applicationUrl)) {
			customProps.put(CUSTOM_APPLICATION, applicationUrl);
		}
		if (pageTo != null) {
			customProps.put(CUSTOM_END_PAGE, Integer.toString(pageTo));
		}

		return CoreSpringFactory.getImpl(LTIManager.class).joinCustomProps(customProps);
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

	@Override
	public String getUserId(Identity identity) {
		return CoreSpringFactory.getImpl(EdubaseManager.class).getUserId(identityEnvironment);
	}

}
