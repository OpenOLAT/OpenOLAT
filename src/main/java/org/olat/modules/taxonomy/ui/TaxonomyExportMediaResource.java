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
package org.olat.modules.taxonomy.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyImportExportService;

/**
 * Streams the complete taxonomy as ZIP (workbook, media, readme).
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyExportMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(TaxonomyExportMediaResource.class);
	
	private final Taxonomy taxonomy;
	private final Identity doer;
	private final Locale locale;
	
	public TaxonomyExportMediaResource(Taxonomy taxonomy, Identity doer, Locale locale) {
		this.taxonomy = taxonomy;
		this.doer = doer;
		this.locale = locale;
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
		TaxonomyImportExportService importExportService = CoreSpringFactory.getImpl(TaxonomyImportExportService.class);
		String filename = importExportService.getExportFilename(taxonomy);
		String encodedFilename = StringHelper.urlEncodeUTF8(filename);
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
		hres.setHeader("Content-Description", encodedFilename);
		try {
			importExportService.exportZip(taxonomy, doer, locale, hres.getOutputStream());
		} catch(IOException e) {
			log.warn("Taxonomy export interrupted: {}", taxonomy.getKey(), e);
		} catch(Exception e) {
			log.error("Cannot export taxonomy: {}", taxonomy.getKey(), e);
		}
	}

	@Override
	public void release() {
		//
	}
}
