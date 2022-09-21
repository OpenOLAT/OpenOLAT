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
import org.olat.core.gui.avrecorder.AVConfiguration;
import org.olat.core.gui.avrecorder.AVCreationController;
import org.olat.core.gui.avrecorder.AVCreationEvent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.gta.model.Solution;

import java.io.File;

/**
 * Initial date: 2022-09-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVSampleSolutionController extends BasicController {

	private final VelocityContainer mainVC;
	private final File solutionDir;
	private final VFSContainer solutionContainer;
	private final AVCreationController creationController;
	private AVSampleSolutionDetailsController sampleSolutionDetailsController;

	public AVSampleSolutionController(UserRequest ureq, WindowControl wControl, File solutionDir,
									  VFSContainer solutionContainer, boolean audioOnly) {
		super(ureq, wControl);

		this.solutionDir = solutionDir;
		this.solutionContainer = solutionContainer;

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

	public File getRecordedFile() {
		return creationController.getRecordedFile();
	}

	public Solution getSolution() {
		return sampleSolutionDetailsController.getSolution();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);

		if (creationController == source) {
			if (event instanceof AVCreationEvent) {
				doSetSampleSolutionDetails(ureq);
			}
		} else if (sampleSolutionDetailsController == source) {
			if (event == Event.DONE_EVENT) {
				creationController.moveUploadFileTo(solutionContainer);
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	private void doSetSampleSolutionDetails(UserRequest ureq) {
		sampleSolutionDetailsController = new AVSampleSolutionDetailsController(ureq, getWindowControl(), solutionDir, creationController.getRecordedFileName());
		listenTo(sampleSolutionDetailsController);
		mainVC.put("component", sampleSolutionDetailsController.getInitialComponent());
	}
}
