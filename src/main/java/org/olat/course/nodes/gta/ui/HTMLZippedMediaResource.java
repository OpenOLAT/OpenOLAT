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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * 
 * Initial date: 19.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HTMLZippedMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(HTMLZippedMediaResource.class);
	
	private final String filename;
	private final File documentsDir;
	
	public HTMLZippedMediaResource(String filename, File documentsDir) {
		this.filename = filename;
		this.documentsDir = documentsDir;
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
		String encodedFileName = filename;
		int lastIndexOf = encodedFileName.lastIndexOf('.');
		if(lastIndexOf > 0) {
			encodedFileName = encodedFileName.substring(0, lastIndexOf);
		}
		encodedFileName = StringHelper.urlEncodeUTF8(encodedFileName) + ".zip";
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
		hres.setHeader("Content-Description", encodedFileName);

		File mainFile = new File(documentsDir, filename);
		HTMLHandler handler = filter(mainFile);
		Collection<String> medias = new HashSet<>(handler.getMedias());

		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);

			ZipUtil.addFileToZip(filename, mainFile, zout);
			
			for(String media:medias) {
				File mediaFile = new File(documentsDir, media);
				ZipUtil.addFileToZip(media, mediaFile, zout);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
	
	public HTMLHandler filter(File file) {
		try(InputStream in = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE)) {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			HTMLHandler contentHandler = new HTMLHandler();
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(bis));
			return contentHandler;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	private static class HTMLHandler extends DefaultHandler {
		private List<String> medias = new ArrayList<>();
		
		public List<String> getMedias() {
			return medias;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			String elem = localName.toLowerCase();
			if("img".equals(elem)) {
				String media = attributes.getValue("src");
				medias.add(media);
			} else if("a".equals(elem)) {
				String media = attributes.getValue("href");
				medias.add(media);
			}
		}
	}
}
