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
package org.olat.modules.forms.ui.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.model.jpa.CalculatedLong;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.MultipleChoice;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceBarSeriesDataSource implements BarSeriesDataSource {

	private final MultipleChoice multipleChoice;
	private final List<? extends EvaluationFormSessionRef> sessions;
	
	private EvaluationFormReportDAO reportDAO;
	
	public MultipleChoiceBarSeriesDataSource(MultipleChoice multipleChoice, List<? extends EvaluationFormSessionRef> sessions) {
		super();
		this.multipleChoice = multipleChoice;
		this.sessions = sessions;
		this.reportDAO = CoreSpringFactory.getImpl(EvaluationFormReportDAO.class);
	}

	@Override
	public BarSeries getBarSeries() {
		String responseIdentifier = multipleChoice.getId();
		List<CalculatedLong> counts = reportDAO.getCountByStringuifideResponse(responseIdentifier, sessions);
		Map<String, Long> identToValue = counts.stream()
				.collect(Collectors.toMap(CalculatedLong::getIdentifier, CalculatedLong::getValue));
		
		OWASPAntiSamyXSSFilter xssFilter = new OWASPAntiSamyXSSFilter();
		BarSeries series = new BarSeries("o_eva_bar");
		// predefined values
		for (Choice choice: multipleChoice.getChoices().asList()) {
			Long count = identToValue.remove(choice.getId());
			double value = count != null? (double) count: 0;
			Comparable<?> category = xssFilter.filter(choice.getValue());
			series.add(value, category);
		}
		// other values
		for (String otherValue: identToValue.keySet()) {
			Long count = identToValue.get(otherValue);
			double value = count != null? (double) count: 0;
			Comparable<?> category = xssFilter.filter(otherValue);
			series.add(value, category);
		}
		return series;
	}

}
