/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool.ui.datasource;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableNumericalRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableNumericalRangeFilter.NumericalRange;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractItemsSource implements QuestionItemsSource {
	
	public static final String FILTER_TITLE = "title";
	public static final String FILTER_TOPIC = "topic";
	public static final String FILTER_KEYWORDS = "keywords";
	public static final String FILTER_COVERAGE = "coverage";
	public static final String FILTER_ADD_INFOS = "add.infos";
	public static final String FILTER_LANGUAGE = "language";
	public static final String FILTER_OWNER = "author";
	public static final String FILTER_TAXONOMYLEVEL_FIELD = "taxonomy.level.field";
	public static final String FILTER_TAXONOMYLEVEL_PATH = "taxonomy.level.path";
	
	public static final String FILTER_EDU_CONTEXT = "edu.context";
	public static final String FILTER_TYPE = "type";
	public static final String FILTER_ASSESSMENT_TYPE = "assessment.type";
	public static final String FILTER_STATUS = "status";
	public static final String FILTER_EDITOR = "editor";
	public static final String FILTER_FORMAT = "format";
	public static final String FILTER_LICENSE = "license";
	public static final String FILTER_MAX_SCORE = "license";
	
	@Autowired
	protected QPoolService qpoolService;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;
	
	public AbstractItemsSource() {
		CoreSpringFactory.autowireObject(this);
	}
	
	public void addFilters(SearchQuestionItemParams params, String searchString, List<FlexiTableFilter> filters) {
		params.setSearchString(searchString);
		
		String title = getTextFilterValue(filters, FILTER_TITLE);
		params.setTitle(title);
		String topic = getTextFilterValue(filters, FILTER_TOPIC);
		params.setTopic(topic);
		String keywords = getTextFilterValue(filters, FILTER_KEYWORDS);
		params.setKeywords(keywords);
		String coverage = getTextFilterValue(filters, FILTER_COVERAGE);
		params.setCoverage(coverage);
		String additionalInfos = getTextFilterValue(filters, FILTER_ADD_INFOS);
		params.setInformations(additionalInfos);
		String language = getTextFilterValue(filters, FILTER_LANGUAGE);
		params.setLanguage(language);
		String owner = getTextFilterValue(filters, FILTER_OWNER);
		params.setOwner(owner);
		
		QEducationalContext eduContext = getContextFilterValue(filters, FILTER_EDU_CONTEXT);
		params.setLevel(eduContext);
		List<QItemType> itemTypes = getItemTypesFilterValue(filters, FILTER_TYPE);
		params.setItemTypes(itemTypes);
		String assessmentType = getTextFilterValue(filters, FILTER_ASSESSMENT_TYPE);
		params.setAssessmentType(assessmentType);
		List<QuestionStatus> status = getStatusFilterValue(filters, FILTER_STATUS);
		params.setQuestionStatus(status);
		String editor = getTextFilterValue(filters, FILTER_EDITOR);
		params.setEditor(editor);
		String format = getTextFilterValue(filters, FILTER_FORMAT);
		params.setFormat(format);
		LicenseType licenseType = getLicenseTypeFilterValue(filters, FILTER_LICENSE);
		params.setLicenseType(licenseType);
		List<TaxonomyLevelRef> taxonomyLevels = getTaxonomyFieldFilterValue(filters, FILTER_TAXONOMYLEVEL_FIELD);
		params.setTaxonomyLevels(taxonomyLevels);
		String taxonomyLevelPath = getTextFilterValue(filters, FILTER_TAXONOMYLEVEL_PATH);
		params.setLikeTaxonomyLevelPath(taxonomyLevelPath);
		
		NumericalRange maxScoreRange = getNumericalRangeFilterValue(filters, FILTER_MAX_SCORE);
		if(maxScoreRange != null) {
			params.setMaxScoreFrom(maxScoreRange.getStart());
			params.setMaxScoreTo(maxScoreRange.getEnd());
		} else {
			params.setMaxScoreFrom(null);
			params.setMaxScoreTo(null);
		}
	}
	
	private String getTextFilterValue(List<FlexiTableFilter> filters, String filterId) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterId);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter
				&& StringHelper.containsNonWhitespace(extendedFilter.getValue())) {
			return extendedFilter.getValue();
		}
		return null;
	}
	
	private List<TaxonomyLevelRef> getTaxonomyFieldFilterValue(List<FlexiTableFilter> filters, String filterId) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterId);
		if (filter instanceof FlexiTableMultiSelectionFilter extendedFilter) {
			List<String> values = extendedFilter.getValues();
			if(values != null && !values.isEmpty()) {
				List<TaxonomyLevelRef> levels = new ArrayList<>();
				for(String value:values) {
					levels.add(new TaxonomyLevelRefImpl(Long.valueOf(value)));
				}
				return levels;
			}
		}
		return null;
	}
	
	private QEducationalContext getContextFilterValue(List<FlexiTableFilter> filters, String filterId) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterId);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter
				&& StringHelper.containsNonWhitespace(extendedFilter.getValue())) {
			return MetaUIFactory.getContextByKey(extendedFilter.getValue(), qpoolService);
		}
		return null;
	}
	
	private List<QItemType> getItemTypesFilterValue(List<FlexiTableFilter> filters, String filterId) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterId);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter
				&& StringHelper.containsNonWhitespace(extendedFilter.getValue())) {
			return MetaUIFactory.getQItemTypeByKey(extendedFilter.getValues(), qpoolService);
		}
		return List.of();
	}
	
	private List<QuestionStatus> getStatusFilterValue(List<FlexiTableFilter> filters, String filterId) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterId);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter
				&& StringHelper.containsNonWhitespace(extendedFilter.getValue())) {
			return extendedFilter.getValues().stream()
					.map(QuestionStatus::valueOf).toList();
		}
		return null;
	}
	
	private LicenseType getLicenseTypeFilterValue(List<FlexiTableFilter> filters, String filterId) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterId);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter
				&& StringHelper.containsNonWhitespace(extendedFilter.getValue())) {
			LicenseSelectionConfig config = LicenseUIFactory.createLicenseSelectionConfig(licenseHandler);
			return config.getLicenseType(extendedFilter.getValue());
		}
		return null;
	}
	
	private NumericalRange getNumericalRangeFilterValue(List<FlexiTableFilter> filters, String filterId) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterId);
		if (filter instanceof FlexiTableNumericalRangeFilter extendedFilter) {
			return extendedFilter.getNumericalRange();
		}
		return null;
	}
}
