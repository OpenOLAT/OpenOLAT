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
package org.olat.modules.lecture.ui;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditLectureController extends FormBasicController {
	
	private TextElement titleEl;
	private TextElement descriptionEl;
	private TextElement preparationEl;
	private TextElement locationEl;
	private DateChooser startDateEl, endDateEl;
	private SingleSelection teacherEl;
	
	private RepositoryEntry entry;
	private LectureBlock lectureBlock;
	
	private List<Identity> teachers;
	private String[] teacherKeys, teacherValues;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BaseSecurity securityManager;
	
	public EditLectureController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, LectureBlock lectureBlock) {
		super(ureq, wControl);
		this.entry = entry;
		this.lectureBlock = lectureBlock;
		if(lectureBlock != null && lectureBlock.getKey() != null) {
			teachers = lectureService.getTeachers(lectureBlock);
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = lectureBlock == null ? null : lectureBlock.getTitle();
		titleEl = uifactory.addTextElement("title", "lecture.title", 128, title, formLayout);
		
		List<Identity> coaches = repositoryService.getMembers(entry, GroupRoles.coach.name());
		teacherKeys = new String[coaches.size()];
		teacherValues = new String[coaches.size()];
		for(int i=coaches.size(); i-->0; ) {
			teacherKeys[i] = coaches.get(i).getKey().toString();
			teacherValues[i] = userManager.getUserDisplayName(coaches.get(i));
		}
		teacherEl = uifactory.addDropdownSingleselect("teacher", "lecture.teacher", formLayout, teacherKeys, teacherValues, null);
		if(teachers != null && teachers.size() > 0) {
			String currentTeacherKey = teachers.get(0).getKey().toString();
			for(String teacherKey:teacherKeys) {
				if(currentTeacherKey.equals(teacherKey)) {
					teacherEl.select(currentTeacherKey, true);
				}
			}
		}

		String description = lectureBlock == null ? "" : lectureBlock.getDescription();
		descriptionEl = uifactory.addTextAreaElement("lecture.descr", 4, 72, description, formLayout);
		String preparation = lectureBlock == null ? "" : lectureBlock.getPreparation();
		preparationEl = uifactory.addTextAreaElement("lecture.preparation", 4, 72, preparation, formLayout);
		String location = lectureBlock == null ? "" : lectureBlock.getLocation();
		locationEl = uifactory.addTextElement("location", "lecture.location", 128, location, formLayout);
		Date startDate = lectureBlock == null ? null : lectureBlock.getStartDate();
		startDateEl = uifactory.addDateChooser("lecture.start", startDate, formLayout);
		Date endDate = lectureBlock == null ? null : lectureBlock.getEndDate();
		endDateEl = uifactory.addDateChooser("lecture.end", endDate, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(lectureBlock == null) {
			lectureBlock = lectureService.createLectureBlock(entry);
		}
		lectureBlock.setTitle(titleEl.getValue());
		lectureBlock.setDescription(descriptionEl.getValue());
		lectureBlock.setPreparation(preparationEl.getValue());
		lectureBlock.setLocation(locationEl.getValue());
		lectureBlock.setStartDate(startDateEl.getDate());
		lectureBlock.setEndDate(endDateEl.getDate());
		lectureBlock = lectureService.save(lectureBlock);
		
		if(teacherEl.isOneSelected()) {
			Long identityKey = new Long(teacherEl.getSelectedKey());
			Identity newTeacher = securityManager.loadIdentityByKey(identityKey);
			lectureService.addTeacher(lectureBlock, newTeacher);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}