/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  3 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationDocument {
	private final String label;
	private final String cssClass;
	private final String relativePath;
	private final FormLink delLink;
	private boolean image;
	
	public ApplicationDocument(String label, String relativePath) {
		this(label, "o_filetype_pdf", relativePath);
	}
	
	public ApplicationDocument(String label, String cssClass, String relativePath) {
		this(label, cssClass, relativePath, null);
	}
	
	public ApplicationDocument(String label, String cssClass, String relativePath, FormLink delLink) {
		this.label = label;
		this.cssClass = cssClass;
		this.relativePath = relativePath;
		this.delLink = delLink;
	}

	public String getLabel() {
		return label;
	}

	public String getRelativePath() {
		return relativePath;
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public FormLink getDelLink() {
		return delLink;
	}

	public boolean isImage() {
		return image;
	}

	public void setImage(boolean image) {
		this.image = image;
	}
}
