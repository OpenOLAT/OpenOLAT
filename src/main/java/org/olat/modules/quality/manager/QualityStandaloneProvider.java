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
package org.olat.modules.quality.manager;

import static org.olat.modules.quality.QualityExecutorParticipationStatus.PARTICIPATING;
import static org.olat.modules.quality.QualityExecutorParticipationStatus.READY;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormStandaloneProvider;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityExecutorParticipationStatus;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.ExecutionHeaderController;
import org.olat.modules.quality.ui.QualityMainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityStandaloneProvider implements EvaluationFormStandaloneProvider {
	
	@Autowired
	private QualityService qualityService;

	@Override
	public boolean accept(OLATResourceable ores) {
		return QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME.equals(ores.getResourceableTypeName());
	}

	@Override
	public boolean isExecutable(EvaluationFormParticipation participation) {
		// Locale does not matter. We are inly interested in the status.
		QualityExecutorParticipation executorParticipation = getExecutorParticipation(participation, Locale.ENGLISH);
		if (executorParticipation != null) {
			QualityExecutorParticipationStatus executionStatus = executorParticipation.getExecutionStatus();
			if (READY.equals(executionStatus) || PARTICIPATING.equals(executionStatus)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Controller getExecutionHeader(UserRequest ureq, WindowControl wControl, EvaluationFormParticipation participation) {
		QualityExecutorParticipation executorParticipation = getExecutorParticipation(participation, ureq.getUserSession().getLocale());
		return new ExecutionHeaderController(ureq, wControl, executorParticipation);
	}
	
	private QualityExecutorParticipation getExecutorParticipation(EvaluationFormParticipation participation, Locale locale) {
		Translator translator = Util.createPackageTranslator(QualityMainController.class, locale);
		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setParticipationRef(participation);
		List<QualityExecutorParticipation> executorParticipations = qualityService.loadExecutorParticipations(translator, searchParams, 0, -1);
		if (executorParticipations.size() == 1) {
			return executorParticipations.get(0);
		}
		return null;
	}

	@Override
	public boolean hasBusinessPath(EvaluationFormParticipation participation) {
		return true;
	}

	@Override
	public String getBusinessPath(EvaluationFormParticipation participation) {
		return "[QualitySite:0][quality:0][my:0][execution:" + participation.getKey() +"]";
	}

}
