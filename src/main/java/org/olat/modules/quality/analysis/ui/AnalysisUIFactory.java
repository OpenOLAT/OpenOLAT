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
package org.olat.modules.quality.analysis.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.EvaluationFormFormatter;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.TemporalGroupBy;

/**
 * 
 * Initial date: 19 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class AnalysisUIFactory {
	
	private static final String ALL_RUBRICS_KEY = "-1";
	private static final NumberFormat PLUS_MINUS_TWO_DECIMAL = new DecimalFormat("+#0.00;-#");
	private static final NumberFormat PLUS_MINUS_ONE_DECIMAL = new DecimalFormat("+#0.0;-#");
	
	static long toLongOrZero(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}
	
	static String formatAvg(Double value) {
		return EvaluationFormFormatter.formatDouble(value);
	}
	
	public static String formatDiffAbsolute(Double value) {
		if (value == null || Double.isNaN(value)) return "";
		
		return PLUS_MINUS_TWO_DECIMAL.format(value.doubleValue());
	}

	static String formatDiffRelative(Double value) {
		if (value == null || Double.isNaN(value)) return "";
		
		return PLUS_MINUS_ONE_DECIMAL.format(value.doubleValue() * 100) + "%";
	}
	
	static String formatYearPart(int yearPart) {
		return String.format("%02d", yearPart);
	}
	
	static String formatSliderLabel(Slider slider) {
		return EvaluationFormFormatter.formatSliderLabel(slider);
	}

	static String translateRole(Translator translator, QualityContextRole role) {
		switch (role) {
		case owner: return translator.translate("filter.context.role.owner");
		case coach: return translator.translate("filter.context.role.coach");
		case participant: return translator.translate("filter.context.role.participant");
		case none: return translator.translate("filter.context.role.none");
		default: return role.toString();
		}
	}

	static String getKey(GroupBy groupBy) {
		return groupBy.name();
	}
	
	static GroupBy getGroupBy(String key) {
		return GroupBy.valueOf(key);
	}
	
	static KeyValues getGroupByKeyValues(Translator translator, AvailableAttributes availableAttributes) {
		OrganisationModule organisationModule = CoreSpringFactory.getImpl(OrganisationModule.class);
		CurriculumModule curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		
		KeyValues keyValues = new KeyValues();
		if (availableAttributes.isTopicIdentity()) {
			addEntry(translator, keyValues, GroupBy.TOPIC_IDENTITY);
		}
		if (availableAttributes.isTopicOrganisation() && organisationModule.isEnabled()) {
			addEntry(translator, keyValues, GroupBy.TOPIC_ORGANISATION);
		}
		if (availableAttributes.isTopicCurriculum() && curriculumModule.isEnabled()) {
			addEntry(translator, keyValues, GroupBy.TOPIC_CURRICULUM);
		}
		if (availableAttributes.isTopicCurriculumElement() && curriculumModule.isEnabled()) {
			addEntry(translator, keyValues, GroupBy.TOPIC_CURRICULUM_ELEMENT);
		}
		if (availableAttributes.isTopicRepository()) {
			addEntry(translator, keyValues, GroupBy.TOPIC_REPOSITORY);
		}
		if (availableAttributes.isContextExecutorOrganisation() && organisationModule.isEnabled()) {
			addEntry(translator, keyValues, GroupBy.CONTEXT_ORGANISATION);
		}
		if (availableAttributes.isContextCurriculum() && curriculumModule.isEnabled()) {
			addEntry(translator, keyValues, GroupBy.CONTEXT_CURRICULUM);
		}
		if (availableAttributes.isContextCurriculumElement() && curriculumModule.isEnabled()) {
			addEntry(translator, keyValues, GroupBy.CONTEXT_CURRICULUM_ELEMENT);
		}
		if (availableAttributes.isContextCurriculumOrganisation() && curriculumModule.isEnabled()) {
			addEntry(translator, keyValues, GroupBy.CONTEXT_CURRICULUM_ORGANISATION);
		}
		if (availableAttributes.isContextTaxonomyLevel()) {
			addEntry(translator, keyValues, GroupBy.CONTEXT_TAXONOMY_LEVEL);
		}
		if (availableAttributes.isContextLocation()) {
			addEntry(translator, keyValues, GroupBy.CONTEXT_LOCATION);
		}
		if (availableAttributes.isDataCollection()) {
			addEntry(translator, keyValues, GroupBy.DATA_COLLECTION);
		}
		return keyValues;
	}

	static private void addEntry(Translator translator, KeyValues keyValues, GroupBy groupBy) {
		keyValues.add(KeyValues.entry(getKey(groupBy), translator.translate(groupBy.i18nKey())));
	}

	static String getKey(TemporalGroupBy groupBy) {
		return groupBy.name();
	}
	
	static TemporalGroupBy getTemporalGroupBy(String key) {
		return TemporalGroupBy.valueOf(key);
	}

	static KeyValues getTemporalGroupByKeyValues(Translator translator) {
		KeyValues keyValues = new KeyValues();
		for (TemporalGroupBy groupBy : TemporalGroupBy.values()) {
			keyValues.add(entry(getKey(groupBy), translator.translate(groupBy.i18nKey())));
		}
		return keyValues;
	}
	
	static String getKey(TrendDifference trendDifference) {
		return trendDifference.name();
	}
	
	static TrendDifference getTrendDifference(String key) {
		return TrendDifference.valueOf(key);
	}

	static KeyValues getTrendDifferenceKeyValues(Translator translator) {
		KeyValues keyValues = new KeyValues();
		for (TrendDifference groupBy : TrendDifference.values()) {
			keyValues.add(entry(getKey(groupBy), translator.translate(groupBy.i18nKey())));
		}
		return keyValues;
	}
	
	static String getKey(Rubric rubric) {
		return rubric.getId();
	}
	
	static boolean isAllRubricsKey(String key) {
		return ALL_RUBRICS_KEY.equals(key);
	}
	
	static String getRubricId(String key) {
		return key;
	}

	static KeyValues getRubricKeyValue(Translator translator, List<Rubric> rubrics) {
		KeyValues keyValues = new KeyValues();
		boolean identicaRubrics = areIdenticalRubrics(rubrics);
		if (identicaRubrics) {
			keyValues.add(entry(ALL_RUBRICS_KEY, translator.translate("trend.rubric.index.all")));
		}
		for (int i = 0; i < rubrics.size(); i++) {
			Rubric rubric = rubrics.get(i);
			keyValues.add(entry(getKey(rubric), getRubricName(translator, rubric, i)));
		}
		return keyValues;
	}
	
	static boolean areIdenticalRubrics(List<Rubric> rubrics) {
		Rubric master = rubrics.get(0);
		for (int i = 1; i < rubrics.size(); i++) {
			Rubric rubric = rubrics.get(i);
			if (!equalsNull(master.getLowerBoundInsufficient(), rubric.getLowerBoundInsufficient())
					|| !equalsNull(master.getLowerBoundNeutral(), rubric.getLowerBoundNeutral())
					|| !equalsNull(master.getLowerBoundSufficient(), rubric.getLowerBoundSufficient())
					|| !equalsNull(master.getUpperBoundInsufficient(), rubric.getUpperBoundInsufficient())
					|| !equalsNull(master.getUpperBoundNeutral(), rubric.getUpperBoundNeutral())
					|| !equalsNull(master.getUpperBoundSufficient(), rubric.getUpperBoundSufficient())
					|| master.getScaleType() != rubric.getScaleType()
					|| master.getSliderType() != rubric.getSliderType()
					|| master.getSteps() != rubric.getSteps()
					|| master.getStart() != rubric.getStart()
					|| master.getEnd() != rubric.getEnd()
					|| master.isStartGoodRating() != master.isStartGoodRating()
					) {
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("null")
	private static boolean equalsNull(Object o1, Object o2) {
		if (o1 == null && o2 == null) return true;
		if (o1 == null && o2 != null) return false;
		if (o1 != null && o2 == null) return false;
		return o1.equals(o2);
	}

	private static String getRubricName(Translator translator, Rubric rubric, int i) {
		String index = String.valueOf(i + 1);
		String rubricName = rubric.getName();
		return rubric.getNameDisplays().contains(NameDisplay.report) && StringHelper.containsNonWhitespace(rubricName)
				? translator.translate("trend.rubric.index.name", new String[] {index, rubricName})
				: translator.translate("trend.rubric.index", new String[] {index});
	}

}
