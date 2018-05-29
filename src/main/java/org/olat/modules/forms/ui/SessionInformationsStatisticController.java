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
package org.olat.modules.forms.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.ui.ReportHelper.Legend;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionInformationsStatisticController extends FormBasicController {
	
	private final List<? extends EvaluationFormSession> sessions;
	private final ReportHelper reportHelper;
	
	@Autowired
	private EvaluationFormManager evluationFormManager;

	public SessionInformationsStatisticController(UserRequest ureq, WindowControl wControl,
			List<? extends EvaluationFormSessionRef> sessionRefs, ReportHelper reportHelper) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.sessions = loadSessions(sessionRefs);
		this.reportHelper = reportHelper;
		initForm(ureq);
	}

	private List<? extends EvaluationFormSession> loadSessions(List<? extends EvaluationFormSessionRef> sessionRefs) {
		if (hasOnlySessions(sessionRefs)) {
			return sessionRefs.stream().map(ref -> (EvaluationFormSession)ref).collect(Collectors.toList());
		}
		return evluationFormManager.loadSessionsByKey(sessionRefs, 0, -1);
	}
	
	private boolean hasOnlySessions(List<? extends EvaluationFormSessionRef> sessionRefs) {
		for (EvaluationFormSessionRef ref: sessionRefs) {
			if (!(ref instanceof EvaluationFormSession)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String numSesions = String.valueOf(sessions.size());
		int anonymous = 0;
		for (EvaluationFormSession ref : sessions) {
			Legend legend = reportHelper.getLegend(ref);
			if (legend.isAnonymous()) {
				anonymous++;
			}
		}
		String numAnonymousSession = String.valueOf(anonymous);
		String numberOfParticipants = translate("session.informations.report.number.participants",
				new String[] { numSesions, numAnonymousSession });
		uifactory.addStaticTextElement("sis_" + CodeHelper.getRAMUniqueID(), null, numberOfParticipants, formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
