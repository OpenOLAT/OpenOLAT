/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.forms.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.forms.EvaluationFormStatistic;
import org.olat.modules.forms.Figure;
import org.olat.modules.forms.Figures;

/**
 * 
 * Initial date: Sep 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FiguresFactory {
	
	public final static List<Figure> createFigures(Translator translator, Figures customFigures, EvaluationFormStatistic statistic) {
		List<Figure> allFigures = new ArrayList<>();
		
		if (customFigures != null) {
			allFigures.addAll(customFigures.getCustomFigures());
		}
		
		long numOfDoneSessions = statistic.getNumOfDoneSessions();
		if (customFigures != null && customFigures.getNumberOfPublicParticipations() != null) {
			numOfDoneSessions -= customFigures.getNumberOfPublicParticipations().longValue();
		}
		if (customFigures != null && customFigures.getNumberOfParticipations() != null) {
			long numberOfParticipations = customFigures.getNumberOfParticipations().longValue();
			
			double percent = numberOfParticipations > 0
					? (double)numOfDoneSessions / numberOfParticipations * 100.0d
					: 0.0;
			long percentRounded = Math.round(percent);
			
			String[] args = new String[] {
					String.valueOf(numOfDoneSessions),
					String.valueOf(numberOfParticipations),
					String.valueOf(percentRounded)
			};
			String numberSessions = translator.translate("report.overview.figures.number.done.session.of", args);
			allFigures.add(new Figure(translator.translate("report.overview.figures.number.done.session.percent"),
					numberSessions));
		} else {
			String numberSessions = String.valueOf(numOfDoneSessions);
			allFigures.add(new Figure(translator.translate("report.overview.figures.number.done.session"),
					numberSessions));
		}
		
		if (customFigures != null && customFigures.getNumberOfPublicParticipations() != null) {
			allFigures.add(new Figure(translator.translate("report.overview.figures.number.public.participations"),
					String.valueOf(customFigures.getNumberOfPublicParticipations())));
		}
		
		String submissionPeriod = EvaluationFormFormatter.period(statistic.getFirstSubmission(),
				statistic.getLastSubmission(), translator.getLocale());
		allFigures.add(
				new Figure(translator.translate("report.overview.figures.submission.period"), submissionPeriod));
		allFigures.add(new Figure(translator.translate("report.overview.figures.average.duration"),
				EvaluationFormFormatter.duration(statistic.getAverageDuration())));
		
		return allFigures;
	}

}
