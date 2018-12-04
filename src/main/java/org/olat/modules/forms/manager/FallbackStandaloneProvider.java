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
package org.olat.modules.forms.manager;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormStandaloneProvider;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FallbackStandaloneProvider implements EvaluationFormStandaloneProvider {

	@Override
	public boolean isFallbackProvider() {
		return true;
	}

	@Override
	public boolean accept(OLATResourceable ores) {
		return false;
	}

	@Override
	public boolean isExecutable(EvaluationFormParticipation participation) {
		return false;
	}

	@Override
	public Controller getExecutionHeader(UserRequest ureq, WindowControl wControl, EvaluationFormParticipation participation) {
		return null;
	}

	@Override
	public boolean hasBusinessPath(EvaluationFormParticipation participation) {
		return false;
	}

	@Override
	public String getBusinessPath(EvaluationFormParticipation participation) {
		return null;
	}

}
