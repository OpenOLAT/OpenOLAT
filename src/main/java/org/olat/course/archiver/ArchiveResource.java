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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArchiveResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ArchiveResource.class);
	
	private static final String encoding = "UTF-8";
	private final Locale locale;
	private final ArchiveOptions options;
	private final List<CourseNode> courseNodes;
	private final OLATResourceable courseOres;
	
	public ArchiveResource(CourseNode courseNode, OLATResourceable courseOres,
			ArchiveOptions options, Locale locale) {
		this.options = options;
		this.locale = locale;
		this.courseNodes = Collections.singletonList(courseNode);
		this.courseOres = courseOres;
	}
	
	public ArchiveResource(List<CourseNode> courseNodes, OLATResourceable courseOres,
			ArchiveOptions options, Locale locale) {
		this.options = options;
		this.locale = locale;
		this.courseNodes = courseNodes;
		this.courseOres = courseOres;
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
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
		
		String courseNodeName = StringHelper.transformDisplayNameToFileSystemName(courseNodes.get(0).getShortName());
		if(courseNodes.size() > 1) {
			courseNodeName += "_and_more";
		}
		String label = courseNodeName
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date()) + ".zip";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		Set<String> usedPath = new HashSet<>();
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			ICourse course = CourseFactory.loadCourse(courseOres);
			for(CourseNode courseNode:courseNodes) {
				String nodePath = "";
				if(courseNodes.size() > 1) {
					nodePath = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName());
					if(!StringHelper.containsNonWhitespace(nodePath)) {
						nodePath = courseNode.getType() + "_" + courseNode.getIdent();
					}
					if(usedPath.contains(nodePath)) {
						nodePath += "_" + courseNode.getIdent();
					}
					usedPath.add(nodePath);
				}
				courseNode.archiveNodeData(locale, course, options, zout, nodePath, encoding);
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
