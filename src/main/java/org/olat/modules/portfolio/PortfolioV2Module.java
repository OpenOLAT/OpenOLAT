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
package org.olat.modules.portfolio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.portfolio.handler.BinderTemplateHandler;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("portfolioV2Module")
public class PortfolioV2Module extends AbstractSpringModule implements ConfigOnOff {
	
	public static final String ENTRY_POINT_TOC = "toc";
	public static final String ENTRY_POINT_ENTRIES = "entries";

	private static final String PORTFOLIO_ENABLED = "portfoliov2.enabled";
	private static final String PORTFOLIO_LEARNER_CAN_CREATE_BINDERS = "portfoliov2.learner.can.create.binders";
	private static final String PORTFOLIO_CAN_CREATE_BINDERS_FROM_TEMPLATE = "portfoliov2.can.create.binders.from.template";
	private static final String PORTFOLIO_CAN_CREATE_BINDERS_FROM_COURSE = "portfoliov2.can.create.binders.from.course";
	private static final String PORTFOLIO_BINDER_ENTRY_POINT = "portfoliov2.binder.entry.point";
	
	private static final String PORTFOLIO_OVERVIEW_ENABLED = "portfoliov2.overview.enabled";
	private static final String PORTFOLIO_OVERVIEW_COMMENTS_ENABLED = "portfoliov2.overview.comments.enabled";
	private static final String PORTFOLIO_ENTRIES_ENABLED = "portfoliov2.entries.enabled";
	private static final String PORTFOLIO_ENTRIES_LIST_ENABLED = "portfoliov2.entries.list.enabled";
	private static final String PORTFOLIO_ENTRIES_TABLE_ENABLED = "portfoliov2.entries.table.enabled";
	private static final String PORTFOLIO_ENTRIES_COMMENTS_ENABLED = "portfoliov2.entries.comments.enabled";
	private static final String PORTFOLIO_ENTRIES_SEARCH_ENABLED = "portfoliov2.entries.search.enabled";
	private static final String PORTFOLIO_ENTRIES_TIMELINE_ENABLED = "portfoliov2.entries.timeline.enabled";
	private static final String PORTFOLIO_HISTORY_ENABLED = "portfoliov2.history.enabled";
	
	private static final String PORTFOLIO_TAXONOMY_LINKING_ENABLED = "portfoliov2.taxonomy.linking.enabled"; 
	private static final String PORTFOLIO_LINKED_TAXONOMIES = "portfoliov2.linked.taxonomies";
	
	private static final Logger log = Tracing.createLoggerFor(PortfolioV2Module.class);
	
	
	@Value("${portfoliov2.enabled:true}")
	private boolean enabled;
	@Value("${portfoliov2.learner.can.create.binders:true}")
	private boolean learnerCanCreateBinders;
	@Value("${portfoliov2.can.create.binders.from.template:true}")
	private boolean canCreateBindersFromTemplate;
	@Value("${portfoliov2.can.create.binders.from.course:true}")
	private boolean canCreateBindersFromCourse;
	@Value("${portfoliov2.binder.entry.point:toc}")
	private String binderEntryPoint;
	
	@Value("${portfoliov2.overview.enabled:true}")
	private boolean overviewEnabled;
	@Value("${portfoliov2.overview.comments.enabled:true}")
	private boolean overviewCommentsEnabled;
	@Value("${portfoliov2.entries.enabled:true}")
	private boolean entriesEnabled;
	@Value("${portfoliov2.entries.list.enabled:true}")
	private boolean entriesListEnabled;
	@Value("${portfoliov2.entries.table.enabled:true}")
	private boolean entriesTableEnabled;
	@Value("${portfoliov2.entries.comments.enabled:true}")
	private boolean entriesCommentsEnabled;
	@Value("${portfoliov2.entries.search.enabled:true}")
	private boolean entriesSearchEnabled;
	@Value("${portfoliov2.entries.timeline.enabled:true}")
	private boolean entriesTimelineEnabled;
	@Value("${portfoliov2.history.enabled:true}")
	private boolean historyEnabled;
	
	@Value("${portfoliov2.taxonomy.linking.enabled:false}")
	private boolean taxonomyLinkingEnabled;
	@Value("${portfoliov2.linked.taxonomies}")
	private String linkedTaxonomies;
	
	@Autowired
	private TaxonomyDAO taxonomyDAO;
	
	@Autowired
	public PortfolioV2Module(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(PORTFOLIO_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String learnerCanCreateBindersObj = getStringPropertyValue(PORTFOLIO_LEARNER_CAN_CREATE_BINDERS, true);
		if(StringHelper.containsNonWhitespace(learnerCanCreateBindersObj)) {
			learnerCanCreateBinders = "true".equals(learnerCanCreateBindersObj);
		}
		
		String overviewEnabledObj = getStringPropertyValue(PORTFOLIO_OVERVIEW_ENABLED, true);
		if(StringHelper.containsNonWhitespace(overviewEnabledObj)) {
			overviewEnabled = "true".equals(overviewEnabledObj);
		}
		
		String overviewCommentsEnabledObj = getStringPropertyValue(PORTFOLIO_OVERVIEW_COMMENTS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(overviewCommentsEnabledObj)) {
			overviewCommentsEnabled = "true".equals(overviewCommentsEnabledObj);
		}
		
		String entriesEnabledObj = getStringPropertyValue(PORTFOLIO_ENTRIES_ENABLED, true);
		if(StringHelper.containsNonWhitespace(entriesEnabledObj)) {
			entriesEnabled = "true".equals(entriesEnabledObj);
		}
		
		String entriesListEnabledObj = getStringPropertyValue(PORTFOLIO_ENTRIES_LIST_ENABLED, true);
		if(StringHelper.containsNonWhitespace(entriesListEnabledObj)) {
			entriesListEnabled = "true".equals(entriesListEnabledObj);
		}
		
		String entriesTableEnabledObj = getStringPropertyValue(PORTFOLIO_ENTRIES_TABLE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(entriesTableEnabledObj)) {
			entriesTableEnabled = "true".equals(entriesTableEnabledObj);
		}
		
		String entriesCommentsEnabledObj = getStringPropertyValue(PORTFOLIO_ENTRIES_COMMENTS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(entriesCommentsEnabledObj)) {
			entriesCommentsEnabled = "true".equals(entriesCommentsEnabledObj);
		}
		
		String entriesSearchEnabledObj = getStringPropertyValue(PORTFOLIO_ENTRIES_SEARCH_ENABLED, true);
		if(StringHelper.containsNonWhitespace(entriesSearchEnabledObj)) {
			entriesSearchEnabled = "true".equals(entriesSearchEnabledObj);
		}
		
		String entriesTimelineEnabledObj = getStringPropertyValue(PORTFOLIO_ENTRIES_TIMELINE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(entriesTimelineEnabledObj)) {
			entriesTimelineEnabled = "true".equals(entriesTimelineEnabledObj);
		}
		
		String historyEnabledObj = getStringPropertyValue(PORTFOLIO_HISTORY_ENABLED, true);
		if(StringHelper.containsNonWhitespace(historyEnabledObj)) {
			historyEnabled = "true".equals(historyEnabledObj);
		}
		
		String taxonomyLinkingEnabledObj = getStringPropertyValue(PORTFOLIO_TAXONOMY_LINKING_ENABLED, true);
		if(StringHelper.containsNonWhitespace(taxonomyLinkingEnabledObj)) {
			taxonomyLinkingEnabled = "true".equals(taxonomyLinkingEnabledObj);
		}
		
		String enabledTaxonomiesObj = getStringPropertyValue(PORTFOLIO_LINKED_TAXONOMIES, true);
		if (StringHelper.containsNonWhitespace(enabledTaxonomiesObj)) {
			linkedTaxonomies = enabledTaxonomiesObj;
		}
		
		RepositoryHandlerFactory.registerHandler(new BinderTemplateHandler(), 40);
		NewControllerFactory.getInstance().addContextEntryControllerCreator("BinderInvitation",
				new BinderInvitationContextEntryControllerCreator());	
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Binder",
				new BinderContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("PortfolioV2",
				new BinderContextEntryControllerCreator());	
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(PORTFOLIO_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isLearnerCanCreateBinders() {
		return learnerCanCreateBinders;
	}

	public void setLearnerCanCreateBinders(boolean learnerCanCreateBinders) {
		this.learnerCanCreateBinders = learnerCanCreateBinders;
		setStringProperty(PORTFOLIO_LEARNER_CAN_CREATE_BINDERS, Boolean.toString(learnerCanCreateBinders), true);
	}
	
	public boolean isCanCreateBindersFromTemplate() {
		return canCreateBindersFromTemplate;
	}

	public void setCanCreateBindersFromTemplate(boolean canCreateBindersFromTemplate) {
		this.canCreateBindersFromTemplate = canCreateBindersFromTemplate;
		setStringProperty(PORTFOLIO_CAN_CREATE_BINDERS_FROM_TEMPLATE, Boolean.toString(canCreateBindersFromTemplate), true);
	}

	public boolean isCanCreateBindersFromCourse() {
		return canCreateBindersFromCourse;
	}

	public void setCanCreateBindersFromCourse(boolean canCreateBindersFromCourse) {
		this.canCreateBindersFromCourse = canCreateBindersFromCourse;
		setStringProperty(PORTFOLIO_CAN_CREATE_BINDERS_FROM_COURSE, Boolean.toString(canCreateBindersFromCourse), true);
	}

	public String getBinderEntryPoint() {
		return binderEntryPoint;
	}

	public void setBinderEntryPoint(String binderEntryPoint) {
		this.binderEntryPoint = binderEntryPoint;
		setStringProperty(PORTFOLIO_BINDER_ENTRY_POINT, binderEntryPoint, true);
	}

	public boolean isOverviewEnabled() {
		return overviewEnabled;
	}

	public void setOverviewEnabled(boolean overviewEnabled) {
		this.overviewEnabled = overviewEnabled;
		setStringProperty(PORTFOLIO_OVERVIEW_ENABLED, Boolean.toString(overviewEnabled), true);
	}

	public boolean isOverviewCommentsEnabled() {
		return overviewCommentsEnabled;
	}

	public void setOverviewCommentsEnabled(boolean overviewCommentsEnabled) {
		this.overviewCommentsEnabled = overviewCommentsEnabled;
		setStringProperty(PORTFOLIO_OVERVIEW_COMMENTS_ENABLED, Boolean.toString(overviewCommentsEnabled), true);
	}

	public boolean isEntriesEnabled() {
		return entriesEnabled;
	}

	public void setEntriesEnabled(boolean entriesEnabled) {
		this.entriesEnabled = entriesEnabled;
		setStringProperty(PORTFOLIO_ENTRIES_ENABLED, Boolean.toString(entriesEnabled), true);
	}

	public boolean isEntriesListEnabled() {
		return entriesListEnabled;
	}

	public void setEntriesListEnabled(boolean entriesListEnabled) {
		this.entriesListEnabled = entriesListEnabled;
		setStringProperty(PORTFOLIO_ENTRIES_LIST_ENABLED, Boolean.toString(entriesListEnabled), true);
	}

	public boolean isEntriesTableEnabled() {
		return entriesTableEnabled;
	}

	public void setEntriesTableEnabled(boolean entriesTableEnabled) {
		this.entriesTableEnabled = entriesTableEnabled;
		setStringProperty(PORTFOLIO_ENTRIES_TABLE_ENABLED, Boolean.toString(entriesTableEnabled), true);
	}

	public boolean isEntriesCommentsEnabled() {
		return entriesCommentsEnabled;
	}

	public void setEntriesCommentsEnabled(boolean entriesCommentsEnabled) {
		this.entriesCommentsEnabled = entriesCommentsEnabled;
		setStringProperty(PORTFOLIO_ENTRIES_COMMENTS_ENABLED, Boolean.toString(entriesCommentsEnabled), true);
	}

	public boolean isEntriesSearchEnabled() {
		return entriesSearchEnabled;
	}

	public void setEntriesSearchEnabled(boolean entiresSearchEnabled) {
		this.entriesSearchEnabled = entiresSearchEnabled;
		setStringProperty(PORTFOLIO_ENTRIES_SEARCH_ENABLED, Boolean.toString(entiresSearchEnabled), true);
	}

	public boolean isEntriesTimelineEnabled() {
		return entriesTimelineEnabled;
	}

	public void setEntriesTimelineEnabled(boolean entriesTimelineEnabled) {
		this.entriesTimelineEnabled = entriesTimelineEnabled;
		setStringProperty(PORTFOLIO_ENTRIES_TIMELINE_ENABLED, Boolean.toString(entriesTimelineEnabled), true);
	}

	public boolean isHistoryEnabled() {
		return historyEnabled;
	}

	public void setHistoryEnabled(boolean historyEnabled) {
		this.historyEnabled = historyEnabled;
		setStringProperty(PORTFOLIO_HISTORY_ENABLED, Boolean.toString(historyEnabled), true);
	}
	
	public boolean isTaxonomyLinkingEnabled() {
		return taxonomyLinkingEnabled;
	}
	
	public void setTaxonomyLinkingEnabled(boolean taxonomyLinkingEnabled) {
		this.taxonomyLinkingEnabled = taxonomyLinkingEnabled;
		setStringProperty(PORTFOLIO_TAXONOMY_LINKING_ENABLED, Boolean.toString(taxonomyLinkingEnabled), true);
	}
	
	public List<Taxonomy> getLinkedTaxonomies() {
		if (!StringHelper.containsNonWhitespace(linkedTaxonomies)) {
			return null;
		}
		
		String[] taxonomies = linkedTaxonomies.replaceAll(" ", "").split(",");
		List<Taxonomy> taxonomyList = new ArrayList<>();
		
		for (String taxonomyString : taxonomies) {
			try {
				Long taxonomyKey = Long.valueOf(taxonomyString);
				Taxonomy taxonomy = taxonomyDAO.loadByKey(taxonomyKey);
				
				if (taxonomy != null) {
					if (taxonomyList.contains(taxonomy)) {
						log.warn("Misconfigured taxonomies detected: " + taxonomyString + " was added multiple times and should be removed from portfoliov2.enabled.taxonomies");
					} else {
						taxonomyList.add(taxonomy);
					}
				} else {
					log.warn("Misconfigured taxonomies detected: " + taxonomyString + " does not exist and should be removed from portfoliov2.enabled.taxonomies");
				}
			} catch (Exception e) {
				log.warn("Misconfigured taxonomies detected: " + taxonomyString + " needs to be removed from portfoliov2.enabled.taxonomies");
			}
		}
		
		return taxonomyList;
	}
	
	public void setLinkedTaxonomies(Collection<String> collection) {
		if (collection == null) {
			return;
		}
		
		String linkedTaxonomies = collection.stream().collect(Collectors.joining(","));
		
		this.linkedTaxonomies = linkedTaxonomies;
		setStringProperty(PORTFOLIO_LINKED_TAXONOMIES, linkedTaxonomies, true);
	}
	
	public boolean isTaxonomyLinked(Long taxonomyKey) {
		return linkedTaxonomies.contains(taxonomyKey.toString());
	}
}
