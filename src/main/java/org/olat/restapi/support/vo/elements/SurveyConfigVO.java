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
*/
package org.olat.restapi.support.vo.elements;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * survey course node configuration
 * 
 * <P>
 * Initial Date:  27.07.2010 <br>
 * @author skoeber
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "surveyConfigVO")
public class SurveyConfigVO {
	
	private Boolean showNavigation;
	private Boolean allowNavigation;
	private Boolean showSectionsOnly;
	private String sequencePresentation;
	private Boolean allowCancel;
	private Boolean allowSuspend;
	private Boolean showQuestionTitle;

	public SurveyConfigVO() {
		//make JAXB happy
	}

	public Boolean getShowNavigation() {
		return showNavigation;
	}

	public void setShowNavigation(Boolean showNavigation) {
		this.showNavigation = showNavigation;
	}

	public Boolean getAllowNavigation() {
		return allowNavigation;
	}

	public void setAllowNavigation(Boolean allowNavigation) {
		this.allowNavigation = allowNavigation;
	}

	public Boolean getShowSectionsOnly() {
		return showSectionsOnly;
	}

	public void setShowSectionsOnly(Boolean showSectionsOnly) {
		this.showSectionsOnly = showSectionsOnly;
	}

	public String getSequencePresentation() {
		return sequencePresentation;
	}

	public void setSequencePresentation(String sequencePresentation) {
		this.sequencePresentation = sequencePresentation;
	}

	public Boolean getAllowCancel() {
		return allowCancel;
	}

	public void setAllowCancel(Boolean allowCancel) {
		this.allowCancel = allowCancel;
	}

	public Boolean getAllowSuspend() {
		return allowSuspend;
	}

	public void setAllowSuspend(Boolean allowSuspend) {
		this.allowSuspend = allowSuspend;
	}

	public Boolean getShowQuestionTitle() {
		return showQuestionTitle;
	}

	public void setShowQuestionTitle(Boolean showQuestionTitle) {
		this.showQuestionTitle = showQuestionTitle;
	}
	
}
