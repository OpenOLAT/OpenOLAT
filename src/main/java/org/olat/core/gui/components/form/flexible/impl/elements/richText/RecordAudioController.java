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
package org.olat.core.gui.components.form.flexible.impl.elements.richText;

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
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.gta.ui.AVDoneEvent;

/**
 * Initial date: 2023-09-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RecordAudioController extends BasicController {

	private final VelocityContainer mainVC;
	private final VFSContainer targetContainer;
	private final AVCreationController creationController;
	private SetFileNameController setFileNameController;

	public RecordAudioController(UserRequest ureq, WindowControl wControl, VFSContainer targetContainer) {
		super(ureq, wControl);

		this.targetContainer = targetContainer;

		AVConfiguration config = new AVConfiguration();
		config.setMode(AVConfiguration.Mode.audio);
		config.setAudioRendererActive(true);

		creationController = new AVCreationController(ureq, wControl, config);
		listenTo(creationController);

		mainVC = createVelocityContainer("record-audio");
		mainVC.put("component", creationController.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);

		if (creationController == source) {
			if (event instanceof AVCreationEvent) {
				doSetFileName(ureq);
			}
		} else if (setFileNameController == source) {
			if (event == Event.DONE_EVENT) {
				VFSLeaf recording = creationController.moveUploadFileTo(targetContainer, setFileNameController.getFileName());
				fireEvent(ureq, new AVDoneEvent(recording));
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	private void doSetFileName(UserRequest ureq) {
		setFileNameController = new SetFileNameController(ureq, getWindowControl(), targetContainer,
				creationController.getFileName());
		listenTo(setFileNameController);
		mainVC.put("component", setFileNameController.getInitialComponent());
	}
}
