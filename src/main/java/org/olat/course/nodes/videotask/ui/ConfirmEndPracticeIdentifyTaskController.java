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
package org.olat.course.nodes.videotask.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.videotask.ui.components.RestartEvent;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoTaskSegmentResult;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmEndPracticeIdentifyTaskController extends FormBasicController {
	
	private FormLink restartButton;
	private FormLink endTaskButton;
	
	private final int maxAttempts;
	private final int currentAttempt;
	private final List<VideoSegment> segmentsList;
	private final List<VideoTaskSegmentResult> results;
	
	public ConfirmEndPracticeIdentifyTaskController(UserRequest ureq, WindowControl wControl,
			List<VideoTaskSegmentResult> results, List<VideoSegment> segmentsList,
			int currentAttempt, int maxAttempts) {
		super(ureq, wControl, "confirm_end_practice_identify");
	
		this.results = results;
		this.segmentsList = segmentsList;
		this.maxAttempts = maxAttempts;
		this.currentAttempt = currentAttempt;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(currentAttempt + 1 <= maxAttempts) {
			String restartText = translate("restart", Integer.toString(currentAttempt + 2));
			restartButton = uifactory.addFormLink("restart", restartText, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			restartButton.setIconLeftCSS("o_icon o_icon_reload");
		}
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			int correct = VideoTaskHelper.correctlyAssignedSegments(segmentsList, results);
			
			String resultMsg = translate("confirm.practice.identify.results", Integer.toString(segmentsList.size()), Integer.toString(correct));
			layoutCont.contextPut("resultMsg", resultMsg);
			
			layoutCont.contextPut("correct", Integer.toString(correct));
			int notCorrect = VideoTaskHelper.incorrectlyAssignedSegments(segmentsList, results);
			layoutCont.contextPut("notCorrect", Integer.toString(notCorrect));
			int notIdentified = VideoTaskHelper.notAssignedSegments(segmentsList, results);
			layoutCont.contextPut("notIdentified", Integer.toString(notIdentified));
		}

		endTaskButton = uifactory.addFormLink("end.task", formLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(restartButton == source) {
			fireEvent(ureq, new RestartEvent());
		} else if(endTaskButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
