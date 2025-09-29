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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CourseReferencesController extends BasicController {
	
	@Autowired
	private CoachingService coachingService;
	
	public CourseReferencesController(UserRequest ureq, WindowControl wControl, CourseStatEntryRow entry, GroupRoles role) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));

		VelocityContainer mainVC = createVelocityContainer("course_references");
		
		List<Curriculum> curriculums = coachingService.getCourseReferences(entry, getIdentity(), role);
		List<Reference> references = new ArrayList<>();
		for(Curriculum curriculum:curriculums) {
			String id = "ref_" + references.size();
			Link link = LinkFactory.createLink(id, id, "ref", curriculum.getDisplayName(), getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromString(CurriculumHelper.getCurriculumBusinessPath(curriculum.getKey()));
			link.setUrl(BusinessControlFactory.getInstance().getAsAuthURIString(entries, true));
			link.setUserObject(curriculum);
			Reference ref = new Reference(curriculum, link);
			references.add(ref);
		}
		
		mainVC.contextPut("references", references);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link link && link.getUserObject() instanceof Curriculum curriculum) {
			this.fireEvent(ureq, Event.CLOSE_EVENT);
			doOpenCurriculum(ureq, curriculum);
		}
	}
	
	private void doOpenCurriculum(UserRequest ureq, Curriculum curriculum) {
		String businessPath = CurriculumHelper.getCurriculumBusinessPath(curriculum.getKey());
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	public record Reference(Curriculum curriculum, Link link) {
		
		public String getDisplayName() {
			return curriculum.getDisplayName();
		}
		
		public String getIdentifier() {
			return curriculum.getIdentifier();
		}
	}
}
