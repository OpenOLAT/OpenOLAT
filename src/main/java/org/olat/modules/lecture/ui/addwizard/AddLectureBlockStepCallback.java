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
package org.olat.modules.lecture.ui.addwizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddLectureBlockStepCallback implements StepRunnerCallback {

	private final AddLectureContext addLectureCtxt;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	public AddLectureBlockStepCallback(AddLectureContext addLectureCtxt) {
		CoreSpringFactory.autowireObject(this);
		this.addLectureCtxt = addLectureCtxt;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		RepositoryEntry entry = addLectureCtxt.getEntry();
		LectureBlock lectureBlock = addLectureCtxt.getLectureBlock();
		CurriculumElement curriculumElement = addLectureCtxt.getCurriculumElement();
		
		StringBuilder audit = new StringBuilder(); 
		
		List<Group> selectedGroups = new ArrayList<>();
		if(curriculumElement != null) {
			curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
			selectedGroups.add(curriculumElement.getGroup());
		}
		if(entry != null) {
			Group defGroup = repositoryService.getDefaultGroup(entry);
			selectedGroups.add(defGroup);
		}
		
		lectureBlock = lectureService.save(lectureBlock, selectedGroups);
		
		List<Identity> selectedTeachers = addLectureCtxt.getTeachers();
		for(Identity teacher:selectedTeachers) {
			lectureService.addTeacher(lectureBlock, teacher);
		}

		String afterxml = lectureService.toAuditXml(lectureBlock);
		LectureBlockAuditLog.Action action = LectureBlockAuditLog.Action.createLectureBlock;
		lectureService.auditLog(action, null, afterxml, audit.toString(), lectureBlock, null,
				entry, curriculumElement, null, ureq.getIdentity());
		dbInstance.commit();

		lectureService.syncCalendars(lectureBlock);

		return StepsMainRunController.DONE_MODIFIED;
	}
}
