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
package org.olat.core.commons.services.pdf;

/**
 * 
 * Initial date: 15 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfOutputOptions {
	
	private Margin margin;
	private MediaType emulatedMediaType;
	
	private PageRange pageRange;
	
	/**
	 * The default options set the media type to print, margin to 1cm and outputs all pages.
	 * 
	 * @return The options object
	 */
	public static PdfOutputOptions defaultOptions() {
		return valueOf(MediaType.print, Margin.ONE_CM, null);
	}
	
	/**
	 * The options are not available to every provider.
	 * 
	 * @param emulatedMediaType Emulate screen media (support: Gotenberg)
	 * @param margin Set the top, bottom, left and right margin (support: Gotenberg)
	 * @return The options object
	 */
	public static PdfOutputOptions valueOf(MediaType emulatedMediaType, Margin margin, PageRange range) {
		PdfOutputOptions options = new PdfOutputOptions();
		options.setEmulatedMediaType(emulatedMediaType);
		options.setMargin(margin);
		options.setPageRange(range);
		return options;
	}

	public Margin getMargin() {
		return margin;
	}

	public void setMargin(Margin margin) {
		this.margin = margin;
	}

	public MediaType getEmulatedMediaType() {
		return emulatedMediaType;
	}

	public void setEmulatedMediaType(MediaType emulatedMediaType) {
		this.emulatedMediaType = emulatedMediaType;
	}

	public PageRange getPageRange() {
		return pageRange;
	}

	public void setPageRange(PageRange pageRange) {
		this.pageRange = pageRange;
	}

	public enum MediaType {
		screen,
		print
	}
	
	public enum Margin {
		NONE(0.0d, "o_pdf_output_none"),
		ONE_CM(0.393701d, "o_pdf_output_one_cm");
		
		private final double margin;
		private final String bodyCssClass;
		
		private Margin(double margin, String bodyCssClass) {
			this.margin = margin;
			this.bodyCssClass = bodyCssClass;
		}

		public double margin() {
			return margin;
		}

		public String bodyCssClass() {
			return bodyCssClass;
		}
	}
	
	public record PageRange(int start, int end) {
		//
	}

}
