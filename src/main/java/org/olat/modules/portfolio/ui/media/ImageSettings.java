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
package org.olat.modules.portfolio.ui.media;

/**
 * 
 * Initial date: 5 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageSettings {
	
	private ImageHorizontalAlignment alignment;
	private ImageSize size;
	private String style;
	private boolean showSource;
	private boolean showDescription;
	
	private String title;
	private String titleStyle;
	private ImageTitlePosition titlePosition;
	
	private String caption;
	private String description;
	
	public ImageSettings() {
		//
	}
	
	public ImageHorizontalAlignment getAlignment() {
		return alignment;
	}
	
	public void setAlignment(ImageHorizontalAlignment alignment) {
		this.alignment = alignment;
	}

	public ImageSize getSize() {
		return size;
	}

	public void setSize(ImageSize size) {
		this.size = size;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public boolean isShowSource() {
		return showSource;
	}

	public void setShowSource(boolean showSource) {
		this.showSource = showSource;
	}

	public boolean isShowDescription() {
		return showDescription;
	}

	public void setShowDescription(boolean showDescription) {
		this.showDescription = showDescription;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleStyle() {
		return titleStyle;
	}

	public void setTitleStyle(String titleStyle) {
		this.titleStyle = titleStyle;
	}

	public ImageTitlePosition getTitlePosition() {
		return titlePosition;
	}

	public void setTitlePosition(ImageTitlePosition titlePosition) {
		this.titlePosition = titlePosition;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
