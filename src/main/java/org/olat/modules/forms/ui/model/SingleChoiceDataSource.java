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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.model.jpa.CalculatedLong;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.SingleChoice;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceDataSource implements CountDataSource, BarSeriesDataSource {

	private final SingleChoice singleChoice;
	private final SessionFilter filter;
	
	private EvaluationFormReportDAO reportDAO;
	
	public SingleChoiceDataSource(SingleChoice singleChoice, SessionFilter filter) {
		super();
		this.singleChoice = singleChoice;
		this.filter = filter;
		this.reportDAO = CoreSpringFactory.getImpl(EvaluationFormReportDAO.class);
	}

	@Override
	public List<CountResult> getResponses() {
		Map<String, Long> identToCount = loadIdentToCount();
		List<CountResult> countResults = new ArrayList<>(identToCount.size());
		
		for (Choice choice: singleChoice.getChoices().asList()) {
			Long count = identToCount.get(choice.getId());
			long value = count != null? (long) count: 0;
			String name = choice.getValue();
			CountResult countResult = new CountResult(name, value);
			countResults.add(countResult);
		}
		return countResults;
	}

	@Override
	public BarSeries getBarSeries() {
		Map<String, Long> identToCount = loadIdentToCount();
		
		BarSeries series = new BarSeries("o_eva_bar");
		for (Choice choice: singleChoice.getChoices().asList()) {
			Long count = identToCount.get(choice.getId());
			double value = count != null? (double) count: 0;
			Comparable<?> category = choice.getValue();
			series.add(value, category);
		}
		return series;
	}

	private Map<String, Long> loadIdentToCount() {
		String responseIdentifier = singleChoice.getId();
		List<CalculatedLong> counts = reportDAO.getCountByStringuifideResponse(responseIdentifier, filter);
		Map<String, Long> identToValue = counts.stream()
				.collect(Collectors.toMap(CalculatedLong::getIdentifier, CalculatedLong::getValue));
		return identToValue;
	}

}
