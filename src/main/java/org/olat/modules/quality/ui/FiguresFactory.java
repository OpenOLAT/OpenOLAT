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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.ui.QualityUIFactory.formatTopic;

import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.FiguresBuilder;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.QualityUIContextsBuilder.Attribute;
import org.olat.modules.quality.ui.QualityUIContextsBuilder.UIContext;

/**
 * 
 * Initial date: 4 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FiguresFactory {
	
	public static Figures createOverviewFigures(QualityDataCollection dataCollection,
			QualityDataCollectionView dataCollectionView, Locale locale) {
		QualityService qualityService = CoreSpringFactory.getImpl(QualityService.class);
		Translator translator = Util.createPackageTranslator(QualityMainController.class, locale);

		FiguresBuilder builder = FiguresBuilder.builder();
		QualityExecutorParticipationSearchParams countSearchParams = new QualityExecutorParticipationSearchParams();
		countSearchParams.setDataCollectionRef(dataCollection);
		Long participationCount = qualityService.getExecutorParticipationCount(countSearchParams);
		builder.withNumberOfParticipations(participationCount);
		builder.addCustomFigure(translator.translate("data.collection.figures.title"), dataCollectionView.getTitle());
		builder.addCustomFigure(translator.translate("data.collection.figures.topic"), formatTopic(dataCollectionView, locale));
		if (StringHelper.containsNonWhitespace(dataCollectionView.getPreviousTitle())) {
			builder.addCustomFigure(translator.translate("data.collection.figures.previous.title"), dataCollectionView.getPreviousTitle());
		}
		
		QualityUIContextsBuilder.builder(dataCollection, locale)
				.addAttribute(Attribute.ROLE)
				.addAttribute(Attribute.COURSE)
				.addAttribute(Attribute.CURRICULUM_ELEMENTS)
				.addAttribute(Attribute.TAXONOMY_LEVELS)
				.build()
				.getUiContexts()
				.stream()
				.map(UIContext::getKeyValues)
				.flatMap(List::stream)
				.forEach(kv -> builder.addCustomFigure(kv.getKey(), kv.getValue()));
		
		String period = EvaluationFormFormatter.period(dataCollectionView.getStart(), dataCollectionView.getDeadline(), locale);
		builder.addCustomFigure(translator.translate("data.collection.figures.period"), period);
		return builder.build();
	}

}
