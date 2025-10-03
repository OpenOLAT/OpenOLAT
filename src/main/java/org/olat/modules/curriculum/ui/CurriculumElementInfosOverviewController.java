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
package org.olat.modules.curriculum.ui;


import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 16, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfosOverviewController extends BasicController {
	
	@Autowired
	private CurriculumService  curriculumService;

	public CurriculumElementInfosOverviewController(UserRequest ureq, WindowControl wControl, CurriculumElement element, int numLectureBlocks) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("curriculum_element_overview");
		putInitialPanel(mainVC);
		
		Formatter formatter = Formatter.getInstance(getLocale());
		StringBuilder dates = new StringBuilder();
		if (element.getBeginDate() != null) {
			dates.append(formatter.formatDate(element.getBeginDate()));
		}
		if (element.getEndDate() != null) {
			if (!dates.isEmpty()) dates.append(" \u2013 ");
			dates.append(formatter.formatDate(element.getEndDate()));
		}
		mainVC.contextPut("period", dates.toString());
		
		mainVC.contextPut("location", element.getLocation());
		mainVC.contextPut("expenditureOfWork", element.getExpenditureOfWork());
		mainVC.contextPut("mainLanguage", element.getMainLanguage());
		mainVC.contextPut("participants", CurriculumHelper.getParticipantRange(getTranslator(), null, element.getMaxParticipants(), false));
		
		if (numLectureBlocks > 0) {
			String numEvents = numLectureBlocks == 1
					? translate("num.of.event", String.valueOf(numLectureBlocks))
					: translate("num.of.events", String.valueOf(numLectureBlocks));
			mainVC.contextPut("numEvents", numEvents);
		}
		
		// Taxonomy levels
		String taxonomyLevelTags = TaxonomyUIFactory.getTags(getTranslator(), curriculumService.getTaxonomy(element));
		mainVC.contextPut("taxonomyLevelTags", taxonomyLevelTags);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
