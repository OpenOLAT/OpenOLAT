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
package org.olat.modules.opencast;

import java.util.HashMap;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIManager;

/**
 * 
 * Initial date: 10.09.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpencastBBBRecordingContext implements LTIContext {
	
	private static final String LTI_ROLE = "Learner";
	private static final String HEIGTH = "auto";
	private static final String WIDTH = "auto";
	private static final String TARGET = LTIDisplayOptions.fullscreen.name();
	private static final String CUSTOM_TOOL = "tool";
	
	private final String identifier;

	public OpencastBBBRecordingContext(String identifier) {
		this.identifier = identifier;
	}

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
	public String getRoles(Identity identity) {
		return LTI_ROLE;
	}

	@Override
	public String getCustomProperties() {
		Map<String, String> customProps = new HashMap<>();
		
		customProps.put(CUSTOM_TOOL, "play/" + identifier);
		
		return CoreSpringFactory.getImpl(LTIManager.class).joinCustomProps(customProps);
	}

	@Override
	public String getTarget() {
		return TARGET;
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
		return CoreSpringFactory.getImpl(BaseSecurityManager.class).findAuthenticationName(identity);
	}

}
