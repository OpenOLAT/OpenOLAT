package org.olat.core.commons.services.pdf;

/**
 * 
 * Initial date: 15 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfOutputOptions {
	
	private Integer marginLeft;
	private Integer marginRight;
	private Integer marginTop;
	private Integer marginBottom;
	private MediaType emulatedMediaType;
	
	public static PdfOutputOptions defaultOptions() {
		return new PdfOutputOptions();
	}
	
	/**
	 * The options are not available to every provider.
	 * 
	 * @param emulatedMediaType Emulate screen media (support: Gotenberg)
	 * @param margin Set the top, bottom, left and right margin (support: Gotenberg)
	 * @return The options object
	 */
	public static PdfOutputOptions valueOf(MediaType emulatedMediaType, Integer margin) {
		PdfOutputOptions options = new PdfOutputOptions();
		options.setEmulatedMediaType(emulatedMediaType);
		options.setMarginLeft(margin);
		options.setMarginRight(margin);
		options.setMarginTop(margin);
		options.setMarginBottom(margin);
		return options;
	}

	public Integer getMarginLeft() {
		return marginLeft;
	}

	public void setMarginLeft(Integer marginLeft) {
		this.marginLeft = marginLeft;
	}

	public Integer getMarginRight() {
		return marginRight;
	}

	public void setMarginRight(Integer marginRight) {
		this.marginRight = marginRight;
	}

	public Integer getMarginTop() {
		return marginTop;
	}

	public void setMarginTop(Integer marginTop) {
		this.marginTop = marginTop;
	}

	public Integer getMarginBottom() {
		return marginBottom;
	}

	public void setMarginBottom(Integer marginBottom) {
		this.marginBottom = marginBottom;
	}

	public MediaType getEmulatedMediaType() {
		return emulatedMediaType;
	}

	public void setEmulatedMediaType(MediaType emulatedMediaType) {
		this.emulatedMediaType = emulatedMediaType;
	}
	
	
	public enum MediaType {
		screen,
		print
	}

}
