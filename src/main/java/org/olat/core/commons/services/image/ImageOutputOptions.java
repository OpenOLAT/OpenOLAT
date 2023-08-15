package org.olat.core.commons.services.image;

/**
 * 
 * Initial date: 15 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageOutputOptions {
	
	private int dpi;
	private boolean highQuality = false;
	
	public static ImageOutputOptions defaultOptions() {
		ImageOutputOptions options = new ImageOutputOptions();
		options.setDpi(72);
		options.setHighQuality(false);
		return options;
	}
	
	public static ImageOutputOptions valueOf(int dpi, boolean highQuality) {
		ImageOutputOptions options = new ImageOutputOptions();
		options.setDpi(dpi);
		options.setHighQuality(highQuality);
		return options;
	}

	public int getDpi() {
		return dpi;
	}

	public void setDpi(int dpi) {
		this.dpi = dpi;
	}

	public boolean isHighQuality() {
		return highQuality;
	}

	public void setHighQuality(boolean highQuality) {
		this.highQuality = highQuality;
	}
}
