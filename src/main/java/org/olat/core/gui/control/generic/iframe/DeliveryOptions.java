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
package org.olat.core.gui.control.generic.iframe;

import java.io.Serializable;

/**
 * 
 * Initial date: 29.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DeliveryOptions implements Cloneable, Serializable {

	private static final long serialVersionUID = 4863839055413222066L;
	
	public static final String CONFIG_HEIGHT_AUTO = "auto";
	public static final String CONFIG_HEIGHT_IGNORE = "ignore";
	
	private Boolean inherit;
	private Boolean standardMode;
	private Boolean jQueryEnabled;
	private Boolean prototypeEnabled;
	private Boolean glossaryEnabled;
	
	private Boolean openolatCss;
	
	private String height;
	private String contentEncoding;
	private String javascriptEncoding;
	
	public DeliveryOptions() {
		//
	}
	
	public DeliveryOptions(Boolean standardMode) {
		this.standardMode = standardMode;
	}
	
	/**
	 * Return a set of options with glossary
	 * @return
	 */
	public static DeliveryOptions defaultWithGlossary() {
		DeliveryOptions defaultOptions = new DeliveryOptions();
		defaultOptions.setStandardMode(Boolean.FALSE);
		defaultOptions.setGlossaryEnabled(Boolean.TRUE);
		defaultOptions.setHeight(null);
		defaultOptions.setjQueryEnabled(Boolean.TRUE);
		defaultOptions.setOpenolatCss(Boolean.TRUE);
		return defaultOptions;
	}

	public Boolean getInherit() {
		return inherit;
	}

	public void setInherit(Boolean inherit) {
		this.inherit = inherit;
	}

	public Boolean getStandardMode() {
		return standardMode;
	}

	public void setStandardMode(Boolean standardMode) {
		this.standardMode = standardMode;
	}

	public Boolean getjQueryEnabled() {
		return jQueryEnabled;
	}

	public void setjQueryEnabled(Boolean jQueryEnabled) {
		this.jQueryEnabled = jQueryEnabled;
	}

	public Boolean getPrototypeEnabled() {
		return prototypeEnabled;
	}

	public void setPrototypeEnabled(Boolean prototypeEnabled) {
		this.prototypeEnabled = prototypeEnabled;
	}

	public Boolean getGlossaryEnabled() {
		return glossaryEnabled;
	}

	public void setGlossaryEnabled(Boolean glossarEnabled) {
		this.glossaryEnabled = glossarEnabled;
	}

	public Boolean getOpenolatCss() {
		return openolatCss;
	}

	public void setOpenolatCss(Boolean openolatCss) {
		this.openolatCss = openolatCss;
	}
	
	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getJavascriptEncoding() {
		return javascriptEncoding;
	}

	public void setJavascriptEncoding(String javascriptEncoding) {
		this.javascriptEncoding = javascriptEncoding;
	}

	public boolean rawContent() {
		return (jQueryEnabled == null || !jQueryEnabled.booleanValue())
				&& (prototypeEnabled == null || !prototypeEnabled.booleanValue())
				&& (openolatCss == null || !openolatCss.booleanValue());
	}

	@Override
	protected DeliveryOptions clone() {
		DeliveryOptions config = new DeliveryOptions();
		config.jQueryEnabled = jQueryEnabled;
		config.prototypeEnabled = prototypeEnabled;
		config.glossaryEnabled = glossaryEnabled;
		config.height = height;
		config.contentEncoding = contentEncoding;
		config.javascriptEncoding = javascriptEncoding;
		config.openolatCss = openolatCss;
		config.standardMode = standardMode;
		config.inherit = inherit;
		return config;
	}
}