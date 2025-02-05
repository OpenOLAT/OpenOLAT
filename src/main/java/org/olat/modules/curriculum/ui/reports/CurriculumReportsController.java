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
package org.olat.modules.curriculum.ui.reports;

import java.util.List;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ui.ExportsListSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.coach.ui.manager.ReportTemplatesController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumManagerRootController;

/**
 * 
 * Initial date: 29 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumReportsController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	
	private final CurriculumReportsListController generatedReportsCtrl;
	private final ReportTemplatesController reportTemplatesCtrl;
	
	public CurriculumReportsController(UserRequest ureq, WindowControl wControl,
			List<Curriculum> adminsCurriculum,  Curriculum curriculum, CurriculumElement curriculumElement,
			ArchiveType type, int titleSize) {

		
		super(ureq, wControl, Util.createPackageTranslator(CurriculumManagerRootController.class, ureq.getLocale()));
		mainVC = createVelocityContainer("manager_reports");
		mainVC.contextPut("titleSize", titleSize);
		
		reportTemplatesCtrl = new CurriculumReportTemplatesController(ureq, wControl, curriculum, curriculumElement, type);
		listenTo(reportTemplatesCtrl);
		mainVC.put("report.templates", reportTemplatesCtrl.getInitialComponent());

		ExportsListSettings settings = new ExportsListSettings(true);
		generatedReportsCtrl = new CurriculumReportsListController(ureq, wControl,
				adminsCurriculum, curriculum, curriculumElement, type, settings);
		listenTo(generatedReportsCtrl);
		mainVC.put("generated.reports", generatedReportsCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//		
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(reportTemplatesCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				generatedReportsCtrl.loadModel();
			}
		}
	}
}
