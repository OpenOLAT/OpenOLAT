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
package org.olat.course.nodes.ta;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.archiver.ArchiveResource;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkDownloadToolController extends BasicController {
	
	private final Link downloadButton;

	private final ArchiveOptions options;
	private final OLATResource courseOres;
	private final TACourseNode courseNode;
	
	public BulkDownloadToolController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			AssessmentToolOptions asOptions, TACourseNode courseNode) {
		super(ureq, wControl);
		this.options = new ArchiveOptions();
		this.options.setGroup(asOptions.getGroup());
		this.options.setIdentities(asOptions.getIdentities());
		this.courseNode = courseNode;
		courseOres = courseEnv.getCourseGroupManager().getCourseResource();
		
		downloadButton = LinkFactory.createButton("bulk.download.title", null, this);
		downloadButton.setTranslator(getTranslator());
		putInitialPanel(downloadButton);
		getInitialComponent().setSpanAsDomReplaceable(true); // override to wrap panel as span to not break link layout 
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(downloadButton == source) {
			doDownload(ureq);
		}
	}
	
	private void doDownload(UserRequest ureq) {
		ArchiveResource resource = new ArchiveResource(courseNode, courseOres, options, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
}
