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
package org.olat.course.archiver;

import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArchiveResource implements MediaResource {
	
	private static final OLog log = Tracing.createLoggerFor(ArchiveResource.class);
	
	private final Locale locale;
	private final String encoding = "UTF-8";
	private final BusinessGroup group;
	private final CourseNode courseNode;
	private final OLATResourceable courseOres;
	
	public ArchiveResource(CourseNode courseNode, OLATResourceable courseOres,
			BusinessGroup group, Locale locale) {
		this.group = group;
		this.locale = locale;
		this.courseNode = courseNode;
		this.courseOres = courseOres;
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
		
		String label = courseNode.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".zip";
		String urlEncodedLabel = StringHelper.urlEncodeISO88591(label);
		hres.setHeader("Content-Disposition","attachment; filename=\"" + urlEncodedLabel + "\"");			
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		ZipOutputStream zout = null;
		try {
			zout = new ZipOutputStream(hres.getOutputStream());
			zout.setLevel(9);
			ICourse course = CourseFactory.loadCourse(courseOres);
			courseNode.archiveNodeData(locale, course, group, zout, encoding);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(zout);
		}
	}

	@Override
	public void release() {
		//
	}
}
