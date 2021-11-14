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
package org.olat.modules.grading.ui;

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters.SearchStatus;
import org.olat.modules.grading.ui.component.IdentityComparator;
import org.olat.modules.grading.ui.component.RepositoryEntryComparator;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentsSearchController extends FormBasicController {
	
	private static final String[] passedKeys = new String[] { "all", "true", "false" };
	
	private FormLink searchButton;
	private TextElement scoreToEl;
	private TextElement scoreFromEl;
	private SingleSelection passedEl;
	private SingleSelection gradersEl;
	private SingleSelection entriesEl;
	private DateChooser gradingDatesEl;
	private MultipleSelectionElement statusEl;
	private SingleSelection referenceEntriesEl;
	private MultipleSelectionElement taxonomyLevelEl;
	
	private final boolean myView;
	private final Identity grader;
	private List<Identity> graders;
	private List<RepositoryEntry> entries;
	private final RepositoryEntry referenceEntry; 
	private List<TaxonomyLevel> allTaxonomyLevels;
	private List<RepositoryEntry> referenceEntries;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryModule repositoryModule;
	
	public AssignmentsSearchController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry referenceEntry, Identity grader, boolean myView, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "assignments_search", rootForm);
		this.grader = grader;
		this.myView = myView;
		this.referenceEntry = referenceEntry;
		initForm(ureq);
		loadSearchLists();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initRightForm(formLayout);
		initLeftForm(formLayout);
		
		searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON);
		searchButton.setElementCssClass("btn-primary");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void initRightForm(FormItemContainer formLayout) {
		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout("right", getTranslator());
		formLayout.add(rightContainer);
		rightContainer.setRootForm(mainForm);
		
		String taxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyTreeKey) && taxonomyModule.isEnabled()) {
			initFormTaxonomy(rightContainer, new TaxonomyRefImpl(Long.valueOf(taxonomyTreeKey)));
		}
		
		SelectionValues empty = new SelectionValues();
		empty.add(SelectionValues.entry("all", translate("show.all")));
		entriesEl = uifactory.addDropdownSingleselect("search.entries", rightContainer,
				empty.keys(), empty.values());

		SelectionValues referenceEntriesKeyValues = new SelectionValues();
		referenceEntriesKeyValues.add(SelectionValues.entry("all", translate("show.all")));
		if(referenceEntry != null) {
			referenceEntriesKeyValues.add(SelectionValues.entry(referenceEntry.getKey().toString(), referenceEntry.getDisplayname()));
		}
		referenceEntriesEl = uifactory.addDropdownSingleselect("search.reference.entries", rightContainer,
				referenceEntriesKeyValues.keys(), referenceEntriesKeyValues.values());
		if(referenceEntry != null) {
			referenceEntriesEl.select(referenceEntry.getKey().toString(), true);
			referenceEntriesEl.setEnabled(false);
		}

		SelectionValues identities = new SelectionValues();
		identities.add(SelectionValues.entry("all", translate("show.all")));
		if(grader != null) {
			identities.add(SelectionValues.entry(grader.getKey().toString(), userManager.getUserDisplayName(grader)));
		}
		gradersEl = uifactory.addDropdownSingleselect("search.graders", rightContainer,
				identities.keys(), identities.values());
		if(grader != null) {
			gradersEl.select(grader.getKey().toString(), true);
			gradersEl.setEnabled(false);
		}
	}
	
	private void initLeftForm(FormItemContainer formLayout) {
		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout("left", getTranslator());
		formLayout.add(leftContainer);
		leftContainer.setRootForm(mainForm);
		
		SelectionValues statusKeys = new SelectionValues();
		for(SearchStatus status: SearchStatus.values()) {
			statusKeys.add(SelectionValues.entry(status.name(), translate("search.status.".concat(status.name()))));
		}
		statusEl = uifactory.addCheckboxesDropdown("status", "search.status", leftContainer, statusKeys.keys(), statusKeys.values());
		statusEl.select(SearchStatus.unassigned.name(), true);
		statusEl.select(SearchStatus.open.name(), true);

		gradingDatesEl = uifactory.addDateChooser("gradingDate", "search.grading.dates", null, leftContainer);
		gradingDatesEl.setSecondDate(true);
		gradingDatesEl.setSeparator("search.grading.dates.sep");

		String scoresPage = this.velocity_root + "/scores.html";
		FormLayoutContainer scoresLayout = FormLayoutContainer.createCustomFormLayout("search.score", getTranslator(), scoresPage);
		leftContainer.add(scoresLayout);
		scoresLayout.setRootForm(mainForm);
		scoresLayout.setLabel("search.scores", null);

		scoreFromEl = uifactory.addTextElement("search.score.from", null, 8, null, scoresLayout);
		scoreFromEl.setDomReplacementWrapperRequired(false);
		scoreFromEl.setDisplaySize(8);
		scoreToEl = uifactory.addTextElement("search.score.to", null, 8, null, scoresLayout);
		scoreToEl.setDomReplacementWrapperRequired(false);
		scoreToEl.setDisplaySize(8);

		String[] passedValues = new String[] { translate("show.all"), translate("passed.true.label"), translate("passed.false.label") };
		passedEl = uifactory.addDropdownSingleselect("search.passed", leftContainer, passedKeys, passedValues);
	}
	
	private void initFormTaxonomy(FormItemContainer formLayout, TaxonomyRef taxonomyRef) {
		allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRef);

		SelectionValues keyValues = new SelectionValues();
		for (TaxonomyLevel level:allTaxonomyLevels) {
			String key = Long.toString(level.getKey());
			ArrayList<String> names = new ArrayList<>();
			addParentNames(names, level);
			Collections.reverse(names);
			String value = String.join(" / ", names);
			keyValues.add(entry(key, value));
		}
		keyValues.sort(VALUE_ASC);
	
		taxonomyLevelEl = uifactory.addCheckboxesDropdown("taxonomyLevels", "search.taxonomy", formLayout,
				keyValues.keys(), keyValues.values(), null, null);
	}
	
	private void addParentNames(List<String> names, TaxonomyLevel level) {
		names.add(level.getDisplayName());
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentNames(names, parent);
		}
	}
	
	private void loadSearchLists() {
		if(referenceEntry == null) {
			entries = gradingService.getEntriesWithGrading(getIdentity());
		} else {
			entries = gradingService.getEntriesWithGrading(referenceEntry);
		}
		Collections.sort(entries, new RepositoryEntryComparator(getLocale()));
		
		SelectionValues entriesKeyValues = new SelectionValues();
		entriesKeyValues.add(SelectionValues.entry("all", translate("show.all")));
		entries.forEach(entry
				-> entriesKeyValues.add(SelectionValues.entry(entry.getKey().toString(), entry.getDisplayname())));
		entriesEl.setKeysAndValues(entriesKeyValues.keys(), entriesKeyValues.values(), null);
		
		if(referenceEntry == null) {
			if(myView) {
				referenceEntries = gradingService.getReferenceRepositoryEntriesAsGrader(getIdentity());
			} else {
				referenceEntries = gradingService.getReferenceRepositoryEntriesWithGrading(getIdentity());
			}
			Collections.sort(referenceEntries, new RepositoryEntryComparator(getLocale()));
			
			SelectionValues referenceKeyValues = new SelectionValues();
			referenceKeyValues.add(SelectionValues.entry("all", translate("show.all")));
			referenceEntries.forEach(entry
					-> referenceKeyValues.add(SelectionValues.entry(entry.getKey().toString(), entry.getDisplayname())));
			referenceEntriesEl.setKeysAndValues(referenceKeyValues.keys(), referenceKeyValues.values(), null);
		}
		
		if(grader == null) {
			if(referenceEntry == null) {
				graders = gradingService.getGraders(getIdentity());
			} else {
				List<GraderToIdentity> graderToIdentities = gradingService.getGraders(referenceEntry);
				graders = graderToIdentities.stream()
						.map(GraderToIdentity::getIdentity)
						.distinct()
						.collect(Collectors.toList());
			}
			Collections.sort(graders, new IdentityComparator());
			
			SelectionValues gradersKeyValues = new SelectionValues();
			gradersKeyValues.add(SelectionValues.entry("all", translate("show.all")));
			graders.forEach(identity
					-> gradersKeyValues.add(SelectionValues.entry(identity.getKey().toString(), userManager.getUserDisplayName(identity))));
			gradersEl.setKeysAndValues(gradersKeyValues.keys(), gradersKeyValues.values(), null);
		}
	}
	
	public List<SearchStatus> getSearchStatus() {
		Collection<String> selectedStatus = statusEl.getSelectedKeys();
		return selectedStatus.stream()
				.map(SearchStatus::valueOf)
				.collect(Collectors.toList());
	}
	
	public void setSearchStatus(SearchStatus status) {
		statusEl.uncheckAll();
		if(status != null && statusEl.getKeys().contains(status.name())) {
			statusEl.select(status.name(), true);
		}
	}
	
	public BigDecimal getScoreFrom() {
		return getScore(scoreFromEl);
	}
	
	public BigDecimal getScoreTo() {
		return getScore(scoreToEl);
	}
	
	private BigDecimal getScore(TextElement el) {
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			try {
				String val = el.getValue().replace(",", ".");
				return new BigDecimal(val);
			} catch (NumberFormatException e) {
				logWarn("Cannot parse:" + el.getValue(), e);
			}
		}
		return null;
	}
	
	public Boolean getPassed() {
		if(passedEl.isOneSelected()) {
			String selectedKey = passedEl.getSelectedKey();
			if("true".equals(selectedKey)) {
				return Boolean.TRUE;
			}
			if("false".equals(selectedKey)) {
				return Boolean.FALSE;
			}
		}
		return null;
	}
	
	public Identity getGrader() {
		if(grader != null) {
			return grader;
		}
		if(graders != null && gradersEl.isOneSelected()
				&& StringHelper.isLong(gradersEl.getSelectedKey())) {
			Long graderKey = Long.valueOf(gradersEl.getSelectedKey());
			return graders.stream().filter(identity -> graderKey.equals(identity.getKey()))
					.findFirst().orElse(null);
		}
		return null;
	}
	
	public void setGrader(Identity identity) {
		if(grader != null || graders == null) return;
		
		String identityKey = identity.getKey().toString();
		for(String graderKey:gradersEl.getKeys()) {
			if(identityKey.equals(graderKey)) {
				gradersEl.select(graderKey, true);
			}
		}
	}
	
	public RepositoryEntry getReferenceEntry() {
		if(referenceEntry != null) {
			return referenceEntry;
		}
		if(referenceEntries != null && referenceEntriesEl.isOneSelected()
				&& StringHelper.isLong(referenceEntriesEl.getSelectedKey())) {
			Long entryKey = Long.valueOf(referenceEntriesEl.getSelectedKey());
			return referenceEntries.stream().filter(entry -> entry.getKey().equals(entryKey))
					.findFirst().orElse(null);
		}
		return null;
	}
	
	public void setReferenceEntry(RepositoryEntry entry) {
		if(referenceEntry != null || referenceEntries == null) return;
		
		String referenceEntryKey = entry.getKey().toString();
		for(String entryKey:referenceEntriesEl.getKeys()) {
			if(referenceEntryKey.equals(entryKey)) {
				referenceEntriesEl.select(entryKey, true);
			}
		}
	}
	
	public RepositoryEntry getEntry() {
		if(entries != null && entriesEl.isOneSelected()
				&& StringHelper.isLong(entriesEl.getSelectedKey())) {
			Long entryKey = Long.valueOf(entriesEl.getSelectedKey());
			return entries.stream().filter(entry -> entry.getKey().equals(entryKey))
					.findFirst().orElse(null);
		}
		return null;
	}
	
	public List<TaxonomyLevel> getTaxonomyLevels() {
		if(taxonomyLevelEl == null || !taxonomyLevelEl.isAtLeastSelected(1)
				|| allTaxonomyLevels == null || allTaxonomyLevels.isEmpty()) {
			return Collections.emptyList();
		}
		
		Map<Long, TaxonomyLevel> levelMap = allTaxonomyLevels.stream()
				.collect(Collectors.toMap(TaxonomyLevel::getKey, Function.identity(), (u, v) -> u));
		return taxonomyLevelEl.getSelectedKeys().stream()
			.map(Long::valueOf)
			.map(levelMap::get)
			.collect(Collectors.toList());
	}
	
	public Date getGradingFrom() {
		return gradingDatesEl.getDate();
	}
	
	public Date getGradingTo() {
		Date to = gradingDatesEl.getSecondDate();
		return CalendarUtils.endOfDay(to);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSearch(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(searchButton == source) {
			doSearch(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		scoreToEl.setValue(null);
		scoreFromEl.setValue(null);
		passedEl.select(passedKeys[0], true);
		gradingDatesEl.setDate(null);
		gradingDatesEl.setSecondDate(null);
		statusEl.uncheckAll();
		statusEl.select(SearchStatus.unassigned.name(), true);
		statusEl.select(SearchStatus.open.name(), true);
		if(taxonomyLevelEl != null) {
			taxonomyLevelEl.uncheckAll();
		}
		entriesEl.select("all", true);
		if(referenceEntriesEl.isEnabled()) {
			referenceEntriesEl.select("all", true);
		}
		if(gradersEl.isEnabled()) {
			gradersEl.select("all", true);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doSearch(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
