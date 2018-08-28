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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewController extends AbstractTeacherOverviewController implements TooledController {
	
	private final TooledStackedPanel toolbarPanel;

	private final RepositoryEntry entry;
	private final RepositoryEntryLectureConfiguration entryConfig;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;

	public TeacherOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry entry, boolean admin, boolean defaultShowAllLectures) {
		super(ureq, wControl, admin, "Lectures::" + entry.getKey(), false, true, defaultShowAllLectures);
		this.entry = entry;
		entryConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		this.toolbarPanel = toolbarPanel;
		toolbarPanel.addListener(this);
		setBreadcrumbPanel(toolbarPanel);
		loadModel(null);
	}

	@Override
	public void initTools() {
		toolbarPanel.addTool(allTeachersSwitch, Align.right);
	}

	@Override
	protected List<LectureBlockRow> getRows(LecturesBlockSearchParameters searchParams) {
		Identity filterByTeacher = ((Boolean)allTeachersSwitch.getUserObject()).booleanValue() ? null : getIdentity();
		List<LectureBlockWithTeachers> blocksWithTeachers = lectureService
				.getLectureBlocksWithTeachers(entry, filterByTeacher, searchParams);
		
		// only show the start button if 
		List<LectureBlockRow> rows = new ArrayList<>(blocksWithTeachers.size());
		if(ConfigurationHelper.isRollCallEnabled(entryConfig, lectureModule)) {
			for(LectureBlockWithTeachers blockWithTeachers:blocksWithTeachers) {
				LectureBlock block = blockWithTeachers.getLectureBlock();
				
				StringBuilder teachers = new StringBuilder(32);
				List<Identity> teacherList = blockWithTeachers.getTeachers();

				String separator = translate("user.fullname.separator");
				for(Identity teacher:blockWithTeachers.getTeachers()) {
					if(teachers.length() > 0) teachers.append(" ").append(separator).append(" ");
					teachers.append(userManager.getUserDisplayName(teacher));
				}
				
				rows.add(new LectureBlockRow(block, entry.getDisplayname(), entry.getExternalRef(), teachers.toString(), teacherList.contains(getIdentity())));
			}
		}
		return rows;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//reload table... first
		super.event(ureq, source, event);
		
		if(source instanceof TeacherRollCallController) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				stackPanel.popUpToController(this);
			}
		}
	}
}
