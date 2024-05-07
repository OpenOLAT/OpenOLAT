/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.folder.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.avrecorder.AVConfiguration;
import org.olat.core.gui.avrecorder.AVCreationController;
import org.olat.core.gui.avrecorder.AVCreationEvent;
import org.olat.core.gui.avrecorder.AVVideoQuality;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 1 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RecordAVController extends BasicController {
	
	private static final AVVideoQuality videoQuality = AVVideoQuality.medium;
	private static final long recordingLengthLimitInMs = 600 * 1000;

	private final AVCreationController creationCtrl;
	
	private final VFSContainer currentContainer;
	private VFSLeaf createdLeaf;

	public RecordAVController(UserRequest ureq, WindowControl wControl, VFSContainer currentContainer, boolean audio) {
		super(ureq, wControl);
		this.currentContainer = currentContainer;
		
		AVConfiguration config = new AVConfiguration();
		config.setRecordingLengthLimit(recordingLengthLimitInMs);
		if (audio) {
			config.setMode(AVConfiguration.Mode.audio);
			config.setAudioRendererActive(true);
		} else {
			config.setVideoQuality(videoQuality);
		}

		creationCtrl = new AVCreationController(ureq, wControl, config);
		listenTo(creationCtrl);

		putInitialPanel(creationCtrl.getInitialComponent());
	}

	public VFSLeaf getCreatedLeaf() {
		return createdLeaf;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);

		if (creationCtrl == source) {
			if (event instanceof AVCreationEvent) {
				createdLeaf = creationCtrl.moveUploadFileTo(currentContainer, creationCtrl.getFileName());
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

}
