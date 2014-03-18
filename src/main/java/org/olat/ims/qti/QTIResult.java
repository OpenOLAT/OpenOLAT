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

package org.olat.ims.qti;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * Initial Date:  Aug 7, 2003
 *
 * @author gnaegi
 */
public class QTIResult extends PersistentObject{ 

	private static final long serialVersionUID = 5999697754463201896L;

	private QTIResultSet resultSet;
	
	private String itemIdent;
	private String answer;
	private Long duration;
	private float score;
	private Date tstamp;
	private String ip;
	private Date lastModified;

	public QTIResult() {
		//
	}
	
	
	/**
	 * @return
	 */
	public String getAnswer() {
		// Schema allows null value, but the Java code is oblivious of that.
		// To prevent calling String functions on null value, we return empty
		// string instead.
		if (answer == null){
			answer = new String();
		}
		return answer;
	}

	
    /**
     * @return Returns the duration.
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
    
	/**
	 * @return
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @return
	 */
	public String getItemIdent() {
		return itemIdent;
	}

	/**
	 * @return
	 */
	public float getScore() {
		return score;
	}

	/**
	 * @return
	 */
	public Date getTstamp() {
		return tstamp;
	}

	/**
	 * @param string
	 */
	public void setAnswer(String string) {
		answer = string;
	}

	/**
	 * @param string
	 */
	public void setIp(String string) {
		ip = string;
	}

	/**
	 * @param string
	 */
	public void setItemIdent(String string) {
		itemIdent = string;
	}

	/**
	 * @param f
	 */
	public void setScore(float f) {
		score = f;
	}

	/**
	 * @param date
	 */
	public void setTstamp(Date date) {
		tstamp = date;
	}


	/**
	 * @param set
	 */
	public void setResultSet(QTIResultSet set) {
		resultSet = set;
	}

	/**
	 * @return
	 */
	public QTIResultSet getResultSet() {
		return resultSet;
	}

	/**
	 * @return lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}


	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 97520 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof QTIResult) {
			QTIResult result = (QTIResult)obj;
			return getKey() != null && getKey().equals(result.getKey());
		}
		return false;
	}
}
