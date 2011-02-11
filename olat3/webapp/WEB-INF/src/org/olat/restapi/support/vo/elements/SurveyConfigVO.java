package org.olat.restapi.support.vo.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
