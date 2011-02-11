
/**
 *
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 *
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */

package de.bps.onyx.plugin;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.gui.media.MediaResource;

/**
 * @author Ingmar Kroll
 */

public class StreamMediaResource implements MediaResource {

	private InputStream is;
	private String fileName;
	private Long size;
	private Long lastModified;

	/**
	 * file assumed to exist, but if it does not exist or cannot be read,
	 * getInputStream() will return null and the class will behave properly.
	 *
	 * @param file
	 */
	public StreamMediaResource(InputStream is, String fileName, Long size, Long lastModified) {
		this.is = is;
		this.fileName = fileName;
		this.size = size;
		this.lastModified = lastModified;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getContentType()
	 */
	public String getContentType() {
		return "application/octet-stream";
	}

	/**
	 * @return
	 * @see org.olat.core.gui.media.MediaRequest#getSize()
	 */
	public Long getSize() {
		return size;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	public InputStream getInputStream() {
		return is;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getLastModified()
	 */
	public Long getLastModified() {
		return lastModified;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#release()
	 */
	public void release() {
	// void
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	public void prepare(HttpServletResponse hres) {
		hres.setHeader("Content-Disposition", "attachment; filename=" + this.fileName);
	}

}

