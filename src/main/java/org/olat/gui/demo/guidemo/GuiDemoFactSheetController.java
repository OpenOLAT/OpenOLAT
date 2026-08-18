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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.factsheet.Fact;
import org.olat.core.gui.components.factsheet.FactSheet;
import org.olat.core.gui.components.factsheet.FactSheetFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.util.ComponentList;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 *
 * Initial date: 12 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoFactSheetController extends BasicController {

	public GuiDemoFactSheetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("guidemo-factsheet");
		putInitialPanel(mainVC);

		FactSheet factSheet = FactSheetFactory.createFactSheet("factSheet", mainVC);
		factSheet.setTitle(translate("factsheet.sheet.title"));
		factSheet.setFacts(createFacts());
	}

	private List<Fact> createFacts() {
		List<Fact> facts = new ArrayList<>();
		facts.add(FactSheetFactory.createFact("o_icon_lifecycle_date", translate("factsheet.fact.period.label"),
				translate("factsheet.fact.period.value"), translate("factsheet.fact.period.subvalue")));
		facts.add(FactSheetFactory.createFact("o_icon_expenditure", translate("factsheet.fact.effort.label"),
				translate("factsheet.fact.effort.value"), translate("factsheet.fact.effort.subvalue")));
		facts.add(FactSheetFactory.createFact("o_icon_location", translate("factsheet.fact.location.label"),
				translate("factsheet.fact.location.value"), translate("factsheet.fact.location.subvalue")));
		facts.add(FactSheetFactory.createFact("o_icon_language", translate("factsheet.fact.language.label"),
				translate("factsheet.fact.language.value")));
		facts.add(FactSheetFactory.createFact("o_icon_num_participants", translate("factsheet.fact.participants.label"),
				translate("factsheet.fact.participants.value"), translate("factsheet.fact.participants.subvalue")));
		facts.add(FactSheetFactory.createFact("o_icon_certificate", translate("factsheet.fact.certificate.label"),
				translate("factsheet.fact.certificate.value")));
		facts.add(FactSheetFactory.createFact("o_icon_coins", translate("factsheet.fact.price.label"),
				translate("factsheet.fact.price.value"), translate("factsheet.fact.price.subvalue")));
		facts.add(FactSheetFactory.createFact("o_icon_graduate", translate("factsheet.fact.coaches.label"),
				translate("factsheet.fact.coaches.value"), translate("factsheet.fact.coaches.subvalue")));
		facts.add(FactSheetFactory.createFact("o_icon_group", translate("factsheet.fact.groups.label"),
				createGroupLinks()));
		facts.add(FactSheetFactory.createFact("o_icon_status_in_progress", translate("factsheet.fact.progress.label"),
				createProgressBar()));
		return facts;
	}

	private Component createGroupLinks() {
		// The info page can show more than one group. A fact value is a single
		// component, so the group links are wrapped in one ComponentList.
		// Group names are user data, hence NONTRANSLATED.
		List<Component> groupLinks = List.of(
				createGroupLink("group1", translate("factsheet.fact.groups.value1")),
				createGroupLink("group2", translate("factsheet.fact.groups.value2")));
		return new ComponentList("groups", groupLinks);
	}

	private Link createGroupLink(String name, String groupName) {
		Link groupLink = LinkFactory.createCustomLink(name, name, "group", groupName, Link.LINK | Link.NONTRANSLATED, null, this);
		groupLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		return groupLink;
	}

	private Component createProgressBar() {
		return new ProgressBar("progress", 100, 65, 100, "%");
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link && "group".equals(link.getCommand())) {
			showInfo("factsheet.fact.groups.clicked");
		}
	}

}
