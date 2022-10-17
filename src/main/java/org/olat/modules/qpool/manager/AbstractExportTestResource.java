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
package org.olat.modules.qpool.manager;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemShort;

/**
 * 
 * Initial date: 17.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractExportTestResource implements MediaResource {

	private static final Logger log = Tracing.createLoggerFor(AbstractExportTestResource.class);
	
	private String encoding;
	private final Locale locale;
	private final List<QuestionItemShort> items;
	
	public AbstractExportTestResource(String encoding, Locale locale, List<QuestionItemShort> items) {
		this.encoding = encoding;
		this.locale = locale;
		this.items = items;
	}
	
	public Locale getLocale() {
		return locale;
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

	public List<QuestionItemShort> getItems() {
		return items;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding(encoding);
		} catch (Exception e) {
			log.error("", e);
		}
		
		String label = "Test";
		String file = StringHelper.transformDisplayNameToFileSystemName(label) + ".zip";
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
		hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));

		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			exportTest(items, zout);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	protected abstract void exportTest(List<QuestionItemShort> itesmToExport, ZipOutputStream zout);

	@Override
	public void release() {
		//
	}
}
