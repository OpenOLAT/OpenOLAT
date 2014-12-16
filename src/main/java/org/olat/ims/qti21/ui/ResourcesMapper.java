package org.olat.ims.qti21.ui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResourcesMapper implements Mapper {
	
	private static final OLog log = Tracing.createLoggerFor(ResourcesMapper.class);

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		String href = request.getParameter("href");
		MediaResource resource = null;
		try {
			URI uri = new URI(href);
			File file = new File(uri);
			if(file.exists()) {
				resource = new FileMediaResource(file);
			} else {
				resource = new NotFoundMediaResource(href);
			}
		} catch (URISyntaxException e) {
			log.error("", e);
			resource = new NotFoundMediaResource(href);
		}
		return resource;
	}

	
}
