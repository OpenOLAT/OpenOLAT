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

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.manager.GTAResultsExport;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;

/**
 * 
 * Initial date: 3 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupBulkDownloadResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(GroupBulkDownloadResource.class);
	private static final String encoding = "UTF-8";
	
	private final Locale locale;
	private final List<BusinessGroup> groups;
	private final GTACourseNode courseNode;
	private final OLATResourceable courseOres;
	
	public GroupBulkDownloadResource(GTACourseNode courseNode, OLATResourceable courseOres,
			List<BusinessGroup> groups, Locale locale) {
		this.groups = groups;
		this.locale = locale;
		this.courseNode = courseNode;
		this.courseOres = courseOres;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding(encoding);
		} catch (Exception e) {
			log.error("", e);
		}
		
		String label = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date()) + ".zip";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);

		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			ICourse course = CourseFactory.loadCourse(courseOres);
			GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);

			if(courseNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
				List<Identity> assessableIdentities = CoreSpringFactory.getImpl(BusinessGroupService.class)
						.getMembers(groups, GroupRoles.participant.name());
				String courseTitle = course.getCourseTitle();
				String fileName = ExportUtil.createFileNameWithTimeStamp(courseTitle, "xlsx");
				GTAResultsExport export = new GTAResultsExport(course, courseNode, locale);
				export.export(fileName, assessableIdentities, groups, zout);
			}

			TaskList taskList = gtaManager.getTaskList(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode);
			for(BusinessGroup businessGroup:groups) {
				courseNode.archiveNodeData(course, businessGroup, taskList, "", zout);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
}
