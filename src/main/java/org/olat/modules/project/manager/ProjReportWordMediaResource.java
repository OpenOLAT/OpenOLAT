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
package org.olat.modules.project.manager;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 25 Sep 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjReportWordMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ProjReportWordMediaResource.class);

	private final ProjReportWordExport wordReportExport;

	public ProjReportWordMediaResource(ProjReportWordExport wordReportExport) {
		this.wordReportExport = wordReportExport;
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
		return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
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
	public void release() {
		//
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
			
			String filename = wordReportExport.getFilename();
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(filename));
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(filename));
			
			wordReportExport.export(hres.getOutputStream());
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
}
