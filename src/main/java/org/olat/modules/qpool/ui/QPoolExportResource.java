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
package org.olat.modules.qpool.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QPoolService;

/**
 * 
 * Initial date: 11.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QPoolExportResource  implements MediaResource {
	
	private static final OLog log = Tracing.createLoggerFor(QPoolExportResource.class);
	
	private String encoding;
	private final QuestionItemShort item;
	
	public QPoolExportResource(String encoding, QuestionItemShort item) {
		this.encoding = encoding;
		this.item = item;
	}

	@Override
	public String getContentType() {
		return "application/octet-stream";
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
		
		String label = item.getTitle();
		String file = StringHelper.transformDisplayNameToFileSystemName(label) + ".zip";
		hres.setHeader("Content-Disposition","attachment; filename=\"" + StringHelper.urlEncodeISO88591(file) + "\"");			
		hres.setHeader("Content-Description",StringHelper.urlEncodeISO88591(label));
		
		ZipOutputStream zout = null;
		try {
			zout = new ZipOutputStream(hres.getOutputStream());
			zout.setLevel(9);
			QPoolService qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
			qpoolService.exportItem(item, zout);
		} catch (IOException e) {
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
