package org.olat.restapi.support.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * The course configuration
 * 
 * <P>
 * Initial Date:  27 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "courseVO")
public class CourseConfigVO {
	
	private String sharedFolderSoftKey;
	
	public CourseConfigVO() {
		//make JAXB happy
	}

	public String getSharedFolderSoftKey() {
		return sharedFolderSoftKey;
	}

	public void setSharedFolderSoftKey(String sharedFolderSoftKey) {
		this.sharedFolderSoftKey = sharedFolderSoftKey;
	}
}
