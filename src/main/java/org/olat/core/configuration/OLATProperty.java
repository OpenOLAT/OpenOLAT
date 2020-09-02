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

package org.olat.core.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Initial Date:  27.08.2020 <br>
 * @author aboeckle, mjenny, alexander.boeckle@frentix.com, http://www.frentix.com
 */


public class OLATProperty implements Comparable<OLATProperty> {
	
	String key;
	String value;
	String comment;
	boolean overwritten;
	boolean hasComment;
	List<String> availableValues = new ArrayList<>(3);
	private String overwriteValue;
	

	public OLATProperty(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public boolean isOverwritten() {return overwritten;}
	
	public String getOverwriteValue() {
		return overwriteValue;
	}

	public void setOverwriteValue(String overwriteValue) {
		this.overwriteValue = "Property is overwritten with value: "+overwriteValue;
	}

	public void setOverwritten(boolean overwritten) {this.overwritten = overwritten;}

	public boolean hasComment() {return hasComment;}

	public void setComment(String comment) {
		this.hasComment = true;
		this.comment = comment;
		}
	
	public String getComment() {return comment;}

	public List<String> getAvailableValues() {return availableValues;}

	public void setAvailableValues(String availableValuesDelimited) {
		StringTokenizer tokens = new StringTokenizer(availableValuesDelimited, ",");
		
		while (tokens.hasMoreElements()) {
			availableValues.add(tokens.nextToken());
		}
	}

	public String getKey() {return key;}

	public String getValue() {return value;}
	
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(OLATProperty prop) {
		return this.getKey().compareTo(prop.getKey());
	}
	
}
