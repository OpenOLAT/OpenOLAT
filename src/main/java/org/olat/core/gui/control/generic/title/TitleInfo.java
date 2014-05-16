/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.gui.control.generic.title;

/**
 * 
 * Description:<br>
 * This encapsulates the info needed by the ForumControllerFactory respective TitledWrapperController.
 * Any of its members could be null.
 * 
 * <P>
 * Initial Date:  25.06.2007 <br>
 * @author Lavinia Dumitrescu, Florian Gnägi
 */
public class TitleInfo {
	public static final int TITLE_SIZE_H1 = 1;
	public static final int TITLE_SIZE_H2 = 2;
	public static final int TITLE_SIZE_H3 = 3;
	public static final int TITLE_SIZE_H4 = 4;

	private String contextTitle;
	private String title;
	private String subTitle;
	private String descriptionTitle;
	private String persistedId;
	private String description;
	private int titleSize = TITLE_SIZE_H2; // default
	private boolean separatorEnabled = false;
	private String cssIconClass = "";
	private String descriptionCssClass = "";

	/**
	 * Constructor for a title object. Use the setter methods to set additional
	 * configuration options
	 * 
	 * @param contextTitle
	 * @param title
	 */
	public TitleInfo(String contextTitle, String title) {
		this(contextTitle, title, null, null);
	}
	
	/**
	 * Constructor for a title object with a description. Use the setter methods to set
	 * additional configuration options
	 * 
	 * @param contextTitle
	 * @param title
	 * @param description
	 * @param id used by the user property to save the state
	 */
	public TitleInfo(String contextTitle, String title, String description, String persistedId) {
		this.contextTitle = contextTitle;
		this.title = title;
		this.persistedId = persistedId;
		this.description = description;
	}

	public String getContextTitle() {
		return contextTitle;
	}
	public void setContextTitle(String contextTitle) {
		this.contextTitle = contextTitle;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescriptionTitle() {
		return descriptionTitle;
	}
	public void setDescriptionTitle(String descriptionTitle) {
		this.descriptionTitle = descriptionTitle;
	}

	public String getPersistedId() {
		return persistedId;
	}
	public void setPersistedId(String persistedId) {
		this.persistedId = persistedId;
	}

	public int getTitleSize() {
		return this.titleSize;
	}
	public void setTitleSize(int titleSize) {
		this.titleSize = titleSize;
	}

	public boolean isSeparatorEnabled() {
		return this.separatorEnabled;
	}
	public void setSeparatorEnabled(boolean separatorEnabled) {
		this.separatorEnabled = separatorEnabled;
	}

	public String getIconCssClass() {
		return this.cssIconClass;
	}
	public void setIconCssClass(String cssClass) {
		this.cssIconClass = cssClass;
	}

	public String getDescriptionCssClass() {
		return descriptionCssClass;
	}
	public void setDescriptionCssClass(String descriptionCssClass) {
		this.descriptionCssClass = descriptionCssClass;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	public String getSubTitle() {
		return this.subTitle;
	}
}
