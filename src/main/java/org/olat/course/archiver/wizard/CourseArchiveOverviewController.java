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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoyUIFactory;

/**
 * 
 * Initial date: 16 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveOverviewController extends StepFormBasicController {
	
	private TextElement titleEl;
	private StaticTextElement filenameEl;

	private final CourseArchiveOptions archiveOptions;
	private final CourseArchiveContext archiveContext;
	
	public CourseArchiveOverviewController(UserRequest ureq, WindowControl wControl,
			CourseArchiveContext archiveContext, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.archiveContext = archiveContext;
		archiveOptions = archiveContext.getArchiveOptions();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		RepositoryEntry courseEntry = archiveContext.getCourseEntry();
		String title = Formatter.addReference(getTranslator(),
				StringHelper.escapeHtml(StringHelper.escapeHtml(courseEntry.getDisplayname())),
				StringHelper.escapeHtml(StringHelper.escapeHtml(courseEntry.getExternalRef())),
				"o_icon_" + RepositoyUIFactory.getIconCssClass(courseEntry));
		uifactory.addStaticTextElement("course.displayname", title, formLayout);
		
		String archiveType;
		if(archiveOptions.getArchiveType() == ArchiveType.COMPLETE) {
			archiveType = translate("archive.types.complete");
		} else if(archiveOptions.getArchiveType() == ArchiveType.PARTIAL) {
			archiveType = translate("archive.types.partial");
		} else {
			archiveType = "";
		}
		uifactory.addStaticTextElement("archive.types", archiveType, formLayout);
		
		String name = CourseArchiveExportTask.getArchiveName(archiveContext.getCourseEntry(),
				archiveOptions.getArchiveType(), getLocale());
		titleEl = uifactory.addTextElement("archive.name", "archive.name", 128, name, formLayout);
		titleEl.addActionListener(FormEvent.ONCHANGE);
		
		filenameEl = uifactory.addStaticTextElement("archive.filename", "archive.filename", name, formLayout);
		cleanUpFilename();
		
		if(archiveOptions.getArchiveType() == ArchiveType.PARTIAL) {
			String archiveObjects = getArchiveObjects();
			uifactory.addStaticTextElement("archive.objects", "archive.objects", archiveObjects, formLayout);
		}
		
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
		RepositoryEntry courseEntry = archiveContext.getCourseEntry();
		final ICourse course = CourseFactory.loadCourse(courseEntry);
		final List<CourseNode> nodes;
		if(archiveContext.getCourseNodes() == null) {
			nodes = new ArrayList<>();
			new TreeVisitor(node -> {
				if(node instanceof CourseNode cNode && CourseArchiveContext.acceptCourseElement(cNode)) {
					nodes.add(cNode);
				}
			}, course.getRunStructure().getRootNode(), false).visitAll();
		} else {
			nodes = archiveContext.getCourseNodes();
		}
		
		ArchiveOptions nodeOptions = new ArchiveOptions();
		nodeOptions.setDoer(getIdentity());
		nodeOptions.setWithPdfs(archiveOptions.isResultsWithPDFs());
		for(CourseNode node:nodes) {
			time += node.estimatedArchivedTime(course, nodeOptions);
		}
		return time;
	}
	
	private String getArchiveObjects() {
		if(archiveContext.getCourseNodes() == null || archiveContext.getCourseNodes().isEmpty()) {
			return CourseArchiveExportTask.getDescription(archiveOptions, archiveContext.getCourseEntry(), getLocale());
		}
		return CourseArchiveExportTask.getDescription(archiveOptions, archiveContext.getCourseNodes(), getLocale());
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(fiSrc != titleEl) {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == titleEl) {
			cleanUpFilename();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void cleanUpFilename() {
		String cleanUpName = CourseArchiveExportTask.getFilename(titleEl.getValue());
		filenameEl.setValue(cleanUpName);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		archiveOptions.setTitle(titleEl.getValue());
		archiveOptions.setFilename(filenameEl.getValue());
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
