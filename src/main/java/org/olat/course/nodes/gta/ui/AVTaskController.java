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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.avrecorder.AVConfiguration;
import org.olat.core.gui.avrecorder.AVCreationController;
import org.olat.core.gui.avrecorder.AVCreationEvent;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.gta.model.Solution;
import org.olat.course.nodes.gta.model.TaskDefinition;

import java.util.List;

/**
 * Initial date: 2022-09-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVTaskController extends BasicController implements Activateable2 {

	private final VelocityContainer mainVC;

	private TaskDefinition task;
	private final VFSContainer tasksContainer;
	private final List<TaskDefinition> existingDefinitions;

	private final AVCreationController creationController;
	private AVTaskDefinitionController taskDefinitionController;

	public AVTaskController(UserRequest ureq, WindowControl wControl, VFSContainer tasksContainer,
							List<TaskDefinition> existingDefinitions, boolean audioOnly) {
		super(ureq, wControl);

		this.tasksContainer = tasksContainer;
		this.existingDefinitions = existingDefinitions;

		AVConfiguration config = new AVConfiguration();
		if (audioOnly) {
			config.setMode(AVConfiguration.Mode.audio);
		}
		creationController = new AVCreationController(ureq, wControl, config);
		listenTo(creationController);

		mainVC = createVelocityContainer("av_wrapper");
		mainVC.put("component", creationController.getInitialComponent());

		putInitialPanel(mainVC);
	}

	public TaskDefinition getTask() {
		return task;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);

		if (creationController == source) {
			if (event instanceof AVCreationEvent) {
				doSetTaskDefinition(ureq);
			}
		} else if (taskDefinitionController == source) {
			if (event == Event.DONE_EVENT) {
				creationController.moveUploadFileTo(tasksContainer, task.getFilename());
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	private void doSetTaskDefinition(UserRequest ureq) {
		task = new TaskDefinition();
		task.setFilename(creationController.getFileName());

		taskDefinitionController = new AVTaskDefinitionController(ureq, getWindowControl(), task, tasksContainer,
				existingDefinitions);
		listenTo(taskDefinitionController);

		mainVC.put("component", taskDefinitionController.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
	}

	public Solution getSolution() {
		Solution solution = new Solution();
		solution.setFilename(task.getFilename());
		solution.setTitle(task.getTitle());
		return solution;
	}
}
