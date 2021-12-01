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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.archiver.ArchiveResource;
import org.olat.course.assessment.bulk.BulkAssessmentToolController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * This specialized list of assessed identities has 2 bulk actions, download
 * and bulk assessment. 
 * 
 * Initial date: 18 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TAIdentityListCourseNodeController extends IdentityListCourseNodeController {
	
	private FormLink downloadButton;
	
	public TAIdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, stackPanel, courseEntry, courseNode, coachCourseEnv, toolContainer, assessmentCallback, true);
	}

	@Override
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		if(!coachCourseEnv.isCourseReadOnly()) {
			BulkAssessmentToolController bulkAssessmentTollCtrl = new BulkAssessmentToolController(ureq, getWindowControl(),
					getCourseEnvironment(), courseNode, canEditUserVisibility);
			listenTo(bulkAssessmentTollCtrl);
			formLayout.put("bulk.assessment", bulkAssessmentTollCtrl.getInitialComponent());	
		}
		
		downloadButton = uifactory.addFormLink("bulk.download.title", formLayout, Link.BUTTON);

		super.initMultiSelectionTools(ureq, formLayout);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(downloadButton == source) {
			doDownload(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void doDownload(UserRequest ureq) {
		OLATResource courseOres = getCourseRepositoryEntry().getOlatResource();
		AssessmentToolOptions asOptions = getOptions();
		
		ArchiveOptions options = new ArchiveOptions();
		options.setGroup(asOptions.getGroup());
		options.setIdentities(asOptions.getIdentities());
		
		ArchiveResource resource = new ArchiveResource(courseNode, courseOres, options, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
}
