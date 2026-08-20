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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.factsheet.Fact;
import org.olat.core.gui.components.factsheet.FactSheet;
import org.olat.core.gui.components.factsheet.FactSheetFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumElementInfosController;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InfoPageFactsController extends BasicController {

	private boolean hasContent;

	public InfoPageFactsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, int numLectureBlocks) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));

		List<Fact> facts = new ArrayList<>();
		addFact(facts, "o_icon_lifecycle_date", "cif.dates", period(entry.getLifecycle()));
		addEventsFact(facts, numLectureBlocks);
		addFact(facts, "o_icon_location", "cif.location", entry.getLocation());
		addFact(facts, "o_icon_graduate", "cif.authors", entry.getAuthors());
		addFact(facts, "o_icon_language", "cif.mainLanguage", entry.getMainLanguage());
		addFact(facts, "o_icon_expenditure", "cif.expenditureOfWork", entry.getExpenditureOfWork());

		init(facts);
	}

	public InfoPageFactsController(UserRequest ureq, WindowControl wControl, CurriculumElement element, int numLectureBlocks) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(),
				Util.createPackageTranslator(CurriculumElementInfosController.class, ureq.getLocale())));

		List<Fact> facts = new ArrayList<>();
		addFact(facts, "o_icon_lifecycle_date", "cif.dates", Formatter.getInstance(getLocale()).formatPeriod(element.getBeginDate(), element.getEndDate()));
		addEventsFact(facts, numLectureBlocks);
		addFact(facts, "o_icon_location", "cif.location", element.getLocation());
		addFact(facts, "o_icon_graduate", "cif.authors", element.getAuthors());
		addFact(facts, "o_icon_language", "cif.mainLanguage", element.getMainLanguage());
		addFact(facts, "o_icon_expenditure", "cif.expenditureOfWork", element.getExpenditureOfWork());
		addFact(facts, "o_icon_num_participants", "curriculum.element.participants",
				CurriculumHelper.getParticipantRange(getTranslator(), null, element.getMaxParticipants(), false));

		init(facts);
	}

	private String period(RepositoryEntryLifecycle lifecycle) {
		if (lifecycle == null) {
			return null;
		}
		if (!lifecycle.isPrivateCycle()) {
			return StringHelper.containsNonWhitespace(lifecycle.getSoftKey()) ? lifecycle.getSoftKey() : lifecycle.getLabel();
		}
		return Formatter.getInstance(getLocale()).formatPeriod(lifecycle.getValidFrom(), lifecycle.getValidTo());
	}

	private void addEventsFact(List<Fact> facts, int numLectureBlocks) {
		if (numLectureBlocks > 0) {
			String numEvents = numLectureBlocks == 1
					? translate("num.of.event", String.valueOf(numLectureBlocks))
					: translate("num.of.events", String.valueOf(numLectureBlocks));
			addFact(facts, "o_icon_events", "cif.events", numEvents);
		}
	}

	private void addFact(List<Fact> facts, String iconCss, String labelI18nKey, String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			facts.add(FactSheetFactory.createFact(iconCss, translate(labelI18nKey), value));
		}
	}

	public boolean hasContent() {
		return hasContent;
	}

	private void init(List<Fact> facts) {
		hasContent = !facts.isEmpty();
		if (!hasContent) {
			putInitialPanel(new Panel("empty"));
		} else {
			FactSheet factSheet = FactSheetFactory.createFactSheet("factSheet", null);
			factSheet.setTitle(translate("details.facts"));
			factSheet.setFacts(facts);
			putInitialPanel(factSheet);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
