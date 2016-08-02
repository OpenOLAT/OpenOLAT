package org.olat.modules.portfolio.ui.media;

import java.io.File;

/**
 * 
 * Initial date: 02.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UploadMedia {
	
	private final File file;
	private final String filename;
	private final String mimeType;
	
	public UploadMedia(File file, String filename, String mimeType) {
		this.file = file;
		this.filename = filename;
		this.mimeType = mimeType;
	}

	public File getFile() {
		return file;
	}

	public String getFilename() {
		return filename;
	}

	public String getMimeType() {
		return mimeType;
	}
}
