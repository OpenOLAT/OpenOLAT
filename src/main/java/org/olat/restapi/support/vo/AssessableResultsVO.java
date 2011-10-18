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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.restapi.support.vo;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Value object representing an individual user's results on an
 * {@link org.olat.course.nodes.AssessableCourseNode}.
 * 
 * @author federico, srosse, stephane.rosse@frentix.com
 * @version 1.0
 * @since Feb 24, 2010
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "assessableResultsVO")
public class AssessableResultsVO {
	
	private Long identityKey;
	private Float score;
	private Boolean passed;
	private Map<Long, String> results;
	private Date lastModifiedDate;

	public AssessableResultsVO() {
	//make jaxb happy
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}
	
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Map<Long, String> getResults() {
		return results;
	}

	public void setResults(Map<Long, String> results) {
		this.results = results;
	}
}
