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
package org.olat.modules.project.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.avrecorder.AVConfiguration;
import org.olat.core.gui.avrecorder.AVCreationController;
import org.olat.core.gui.avrecorder.AVCreationEvent;
import org.olat.core.gui.avrecorder.AVVideoQuality;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-11-07<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ProjRecordAVController extends BasicController {

	private static final AVVideoQuality videoQuality = AVVideoQuality.medium;
	private static final long recordingLengthLimitInMs = 600 * 1000;
	private final ProjProject project;
	private final VelocityContainer mainVC;

	private final AVCreationController creationController;
	private ProjRecordAVDetailsController detailsController;

	@Autowired
	private ProjectService projectService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	protected ProjRecordAVController(UserRequest ureq, WindowControl wControl, ProjProject project, boolean audio) {
		super(ureq, wControl);
		this.project = project;

		AVConfiguration config = new AVConfiguration();
		config.setRecordingLengthLimit(recordingLengthLimitInMs);
		if (audio) {
			config.setMode(AVConfiguration.Mode.audio);
			config.setAudioRendererActive(true);
		} else {
			config.setVideoQuality(videoQuality);
		}

		creationController = new AVCreationController(ureq, wControl, config);
		listenTo(creationController);

		mainVC = createVelocityContainer("av_wrapper");
		mainVC.put("component", creationController.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);

		if (creationController == source) {
			if (event instanceof AVCreationEvent) {
				doSetPageTwo(ureq);
			}
		} else if (detailsController == source) {
			if (event == Event.DONE_EVENT) {
				try {
					FileInputStream fileInputStream = new FileInputStream(creationController.getRecordedFile());
					ProjFile projFile = projectService.createFile(getIdentity(), project, detailsController.getFileName(),
							fileInputStream, true);
					if (projFile != null) {
						projectService.updateTags(getIdentity(), projFile.getArtefact(), detailsController.getTagDisplayValues());
						VFSMetadata vfsMetadata = projFile.getVfsMetadata();
						detailsController.updateVfsMetdata(vfsMetadata);
						vfsRepositoryService.updateMetadata(vfsMetadata);
						if (vfsRepositoryService.getItemFor(vfsMetadata) instanceof VFSLeaf vfsLeaf) {
							creationController.triggerConversionIfNeeded(vfsLeaf);
						}
					}

				} catch (FileNotFoundException e) {
					logError("Failed to store recording: {}", e);
				}
			}
			fireEvent(ureq, event);
		}
	}

	private void doSetPageTwo(UserRequest ureq) {
		String fileName = creationController.getFileName();
		detailsController = new ProjRecordAVDetailsController(ureq, getWindowControl(), project, fileName);
		listenTo(detailsController);
		mainVC.put("component", detailsController.getInitialComponent());
	}
}
