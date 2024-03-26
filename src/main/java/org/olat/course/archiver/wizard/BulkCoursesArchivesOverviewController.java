/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.archiver.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Formatter;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkCoursesArchivesOverviewController extends StepFormBasicController {
	
	private final CourseArchiveOptions archiveOptions;
	private final BulkCoursesArchivesContext archiveContext;
	
	public BulkCoursesArchivesOverviewController(UserRequest ureq, WindowControl wControl,
			BulkCoursesArchivesContext archiveContext, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.archiveContext = archiveContext;
		archiveOptions = archiveContext.getArchiveOptions();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int numOfCompleteArchive = 0;
		int numOfPartialArchive = 0;
		List<RepositoryEntry> entries = archiveContext.getEntries();
		for(RepositoryEntry entry:entries) {
			ArchiveType type = archiveContext.getArchiveTypesForEntries().get(entry);
			if(type == ArchiveType.COMPLETE) {
				numOfCompleteArchive++;
			} else if(type == ArchiveType.PARTIAL) {
				numOfPartialArchive++;
			}
		}
		uifactory.addStaticTextElement("number.entries", Integer.toString(numOfCompleteArchive + numOfPartialArchive), formLayout);

		String archiveType;
		if(numOfCompleteArchive > 0 && numOfPartialArchive == 0) {
			archiveType = translate("archive.types.complete");
		} else if(numOfCompleteArchive == 0 && numOfPartialArchive > 0) {
			archiveType = translate("archive.types.partial");
		} else if(numOfCompleteArchive > 0 && numOfPartialArchive > 0) {
			archiveType = translate("archive.types.complete") + " / " + translate("archive.types.partial");
		} else {
			archiveType = "";
		}
		uifactory.addStaticTextElement("archive.types", archiveType, formLayout);

		if(archiveOptions.isLogFilesUsers()) {
			String adminOnly = translate("access.admin.only");
			uifactory.addStaticTextElement("access.admin", adminOnly, formLayout);
		}
		
		int timeInMilliSec = getEstimatedTime();
		String duration = Formatter.formatDuration(timeInMilliSec);
		uifactory.addStaticTextElement("archive.estimated.time", duration, formLayout);
	}
	
	private int getEstimatedTime() {
		int time = 0;
		for(RepositoryEntry entry:archiveContext.getEntries()) {
			time += getEstimatedTime(entry);
		}
		return time;
	}
	
	private int getEstimatedTime(RepositoryEntry courseEntry) {
		int time = 0;
		final ICourse course = CourseFactory.loadCourse(courseEntry);
		final List<CourseNode> nodes = new ArrayList<>();
		new TreeVisitor(node -> {
			if(node instanceof CourseNode cNode && CourseArchiveContext.acceptCourseElement(cNode)) {
				nodes.add(cNode);
			}
		}, course.getRunStructure().getRootNode(), false).visitAll();
		
		ArchiveOptions nodeOptions = new ArchiveOptions();
		nodeOptions.setDoer(getIdentity());
		nodeOptions.setWithPdfs(archiveOptions.isResultsWithPDFs());
		for(CourseNode node:nodes) {
			time += node.estimatedArchivedTime(course, nodeOptions);
		}
		return time;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}

}
