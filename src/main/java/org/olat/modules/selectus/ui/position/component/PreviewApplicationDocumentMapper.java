/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position.component;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;

/**
 * 
 * Initial date: 19 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PreviewApplicationDocumentMapper implements Mapper {

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		return new PreviewResource();
	}
	
	private static class PreviewResource extends DefaultMediaResource {

		@Override
		public long getCacheControlDuration() {
			return ServletUtil.CACHE_ONE_DAY;
		}

		@Override
		public String getContentType() {
			return "application/pdf";
		}

		@Override
		public InputStream getInputStream() {
			return PreviewApplicationDocumentMapper.class.getResourceAsStream("doc_preview.pdf");
		}
		
		@Override
		public void prepare(HttpServletResponse hres) {
			hres.setHeader("Content-Disposition", "filename*=UTF-8''DocumentPreview.pdf");
		}
	}
}
