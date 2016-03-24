package org.olat.modules.video.models;

import org.olat.core.commons.services.image.Size;

public class VideoQualityVersion{
	// Properties
	String type;
	String fileSize;
	Size dimension;
	String format;
	boolean isTransforming;
	
	public VideoQualityVersion(String type, String fileSize, Size dimension, String format){
		this.type = type;
		this.fileSize = fileSize;
		this.dimension = dimension;
		this.format = format;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public Size getDimension() {
		return dimension;
	}

	public void setDimension(Size dimension) {
		this.dimension = dimension;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public boolean getIsTransforming() {
		return isTransforming;
	}

	public void setIsTransforming(boolean isTranscoding) {
		this.isTransforming = isTranscoding;
	}
}