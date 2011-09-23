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
* <p>
*/ 

package org.olat.ims.qti;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;

/**
 * Initial Date: 14.05.2004
 *
 * @author gnaegi
 */
public class QTIResultSet extends PersistentObject { 

	private long olatResource;
	private String olatResourceDetail;
	private long repositoryRef;
	
	private Identity identity;
	private int qtiType;
	private long assessmentID;
	private boolean isPassed;
	private float score;
	private Long duration;
	private Date lastModified;


	public QTIResultSet() {
		//
	}
	
	/**
	 * @see org.olat.core.commons.persistence.Auditable#getLastModified()
	 */
	public Date getLastModified() {
		return lastModified;
	}


	/**
	 * @param date
	 */
	public void setLastModified(Date date) {
		lastModified = date;
	}
	
	
	/**
	 * @return
	 */
	public long getAssessmentID() {
		return assessmentID;
	}

	/**
	 * @return
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @return
	 */
	public boolean getIsPassed() {
		return isPassed;
	}

	/**
	 * @return
	 */
	public long getOlatResource() {
		return olatResource;
	}

	/**
	 * @return
	 */
	public String getOlatResourceDetail() {
		return olatResourceDetail;
	}

	/**
	 * @return
	 */
	public int getQtiType() {
		return qtiType;
	}

	/**
	 * @return
	 */
	public long getRepositoryRef() {
		return repositoryRef;
	}

	/**
	 * @return
	 */
	public float getScore() {
		return score;
	}

	/**
	 * @param l
	 */
	public void setAssessmentID(long l) {
		assessmentID = l;
	}

	/**
	 * @param identity
	 */
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	/**
	 * @param b
	 */
	public void setIsPassed(boolean b) {
		isPassed = b;
	}

	/**
	 * @param l
	 */
	public void setOlatResource(long l) {
		olatResource = l;
	}

	/**
	 * @param string
	 */
	public void setOlatResourceDetail(String string) {
		olatResourceDetail = string;
	}

	/**
	 * @param i
	 */
	public void setQtiType(int i) {
		qtiType = i;
	}

	/**
	 * @param l
	 */
	public void setRepositoryRef(long l) {
		repositoryRef = l;
	}

	/**
	 * @param f
	 */
	public void setScore(float f) {
		score = f;
	}
	
    /**
     * @return Returns the duration or null if not available (only the case by old testsets that have been
     * generated befor the introduction of the duration field)
     */
    public Long getDuration() {
        return duration;
    }
    /**
     * @param duration The duration to set.
     */
    public void setDuration(Long duration) {
        this.duration = duration;
    }
}

