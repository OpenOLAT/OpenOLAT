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

package org.olat.course.condition.interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
/**
 * 
 * Description:<br>
 * TODO: guido Class Description for ConditionExpression
 * 
 */
public class ConditionExpression {
	private String expressionString;
	private String id;
	private Stack errorStack;
	private Map softReferences;

	public ConditionExpression(String idName, String expression) {
		this(idName);
		this.expressionString = expression;
	}

	public ConditionExpression(String idName) {
		this.id = idName;
		errorStack = new Stack();
		softReferences = new HashMap();
	}

	public String getId() {
		return id;
	}

	public String getExptressionString() {
		return expressionString;
	}

	public void setExpressionString(String expression) {
		expressionString = expression;
	}

	public void pushError(Exception e) {
		errorStack.push(e);
	}

	public void addSoftReference(String category, String softReference) {
		Set catSoftRefs;
		if (softReferences.containsKey(category)) {
			catSoftRefs = (HashSet) softReferences.get(category);
		} else {
			catSoftRefs = new HashSet();
		}
		catSoftRefs.add(softReference);
		softReferences.put(category, catSoftRefs);
	}
	
	public Set<String> getSoftReferencesOf(String category) {
		Set<String> catSoftRefs;
		if (softReferences.containsKey(category)) {
			catSoftRefs = (HashSet) softReferences.get(category);
		} else {
			catSoftRefs = new HashSet<String>();
		}
		return catSoftRefs;
	}

	public Exception[] getExceptions() {
		Exception[] retVal = new Exception[errorStack.size()];
		return (Exception[]) errorStack.toArray(retVal);
	}

	public String toString() {
		String retVal = "";
		String softRefStr ="";
		Set keys = softReferences.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String category = (String) iter.next();
			softRefStr += "["+category+"::";
			Set catSoftRefs = (Set) softReferences.get(category);
			for (Iterator iterator = catSoftRefs.iterator(); iterator.hasNext();) {
				String srs = (String) iterator.next();
				softRefStr +=srs+",";
			}
			softRefStr +="]";
		}
		retVal += "<ConditionExpression id='" + this.id + "' errorCnt ='" + errorStack.size() + "'>"+softRefStr+"</ConditionExpression>";

		return retVal;
	}

}
