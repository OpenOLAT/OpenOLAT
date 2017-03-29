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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private TeacherRollCallController rollCallCtrl;
	
	private final RepositoryEntry entry;
	
	@Autowired
	private LectureService lectureService;
	
	public TeacherOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		this.toolbarPanel = toolbarPanel;
		
		mainVC = createVelocityContainer("teacher_view");
		
		List<LectureBlock> blocks = lectureService.getLectureBlocks(entry, getIdentity());
		if(blocks.size() > 0) {
			doOpenRollCall(ureq, blocks.get(0));
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void doOpenRollCall(UserRequest ureq, LectureBlock block) {
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), block);
		listenTo(rollCallCtrl);
		mainVC.put("lecture", rollCallCtrl.getInitialComponent());
	}


	
}
