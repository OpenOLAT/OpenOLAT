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
package org.olat.modules.video.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

/**
 * Row in the table of videoconfiguration to list the different quality-versiosn of a video
 * 
 * Initial date: 07.04.2015<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class QualityTableRow {

	private FormLink resolution;
	private String dimension;
	private Object[] size;
	private String format;
	private FormLink deleteLink;

	/**
	 * 
	 * @param resolution
	 * @param dimension
	 * @param size
	 * @param format
	 * @param viewLink
	 * @param deleteLink FormLink for delete row and corresponding Videodata or null if no deleteLink should be shown
	 */
	public QualityTableRow(FormLink resolution, String dimension, Object[] size, String format, FormLink deleteLink) {
		this.resolution = resolution;
		this.resolution.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		this.dimension = dimension;
		this.size = size;
		this.format = format;
		if(deleteLink != null) this.deleteLink = deleteLink;
	}
	
	public FormLink getResolution() {
		return resolution;
	}

	public void setType(FormLink type) {
		this.resolution = type;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public Object[] getSize() {
		return size;
	}

	public void setSize(Object[] size) {
		this.size = size;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public FormLink getDeleteLink() {
		return deleteLink;
	}
	
	public void setDeleteLink(FormLink deleteLink) {
		this.deleteLink = deleteLink;
	}
}
