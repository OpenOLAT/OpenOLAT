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
 * @author guido
 * 
 */
public class ConditionExpression {
	private String expressionString;
	private String id;
	private Stack<Exception> errorStack;
	private Map<String, Set<Reference>> softReferences;

	public ConditionExpression(String idName, String expression) {
		this(idName);
		this.expressionString = expression;
	}

	public ConditionExpression(String idName) {
		this.id = idName;
		errorStack = new Stack<>();
		softReferences = new HashMap<>();
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

	public void addSoftReference(String category, String softReference, boolean cycleDetector) {
		Set<Reference> catSoftRefs;
		if (softReferences.containsKey(category)) {
			catSoftRefs = softReferences.get(category);
		} else {
			catSoftRefs = new HashSet<>();
			softReferences.put(category, catSoftRefs);
		}
		
		boolean found = false;
		if(catSoftRefs.size() > 0) {
			for(Reference catSoftRef:catSoftRefs) {
				if(softReference.equals(catSoftRef.getSoftReference())) {
					if(!catSoftRef.isCycleDetector() && cycleDetector) {
						catSoftRef.setCycleDetector(true);
					}
					found = true;
				}
			}
		}
		if(!found) {
			catSoftRefs.add(new Reference(softReference, cycleDetector));
		}
	}
	
	public Set<String> getSoftReferencesOf(String category) {
		Set<String> softRefs = new HashSet<>();
		if (softReferences.containsKey(category)) {
			Set<Reference> catSoftRefs = softReferences.get(category);
			softRefs = new HashSet<>();
			for(Reference catSoftRef:catSoftRefs) {
				softRefs.add(catSoftRef.getSoftReference());
			}
		}
		return softRefs;
	}
	
	public Set<String> getSoftReferencesForCycleDetectorOf(String category) {
		Set<String> softRefs = new HashSet<>();
		if (softReferences.containsKey(category)) {
			Set<Reference> catSoftRefs = softReferences.get(category);
			
			for(Reference catSoftRef:catSoftRefs) {
				if(catSoftRef.isCycleDetector()) {
					softRefs.add(catSoftRef.getSoftReference());
				}
			}
		}
		return softRefs;
	}

	public Exception[] getExceptions() {
		return errorStack.toArray(new Exception[errorStack.size()]);
	}

	@Override
	public String toString() {
		String retVal = "";
		String softRefStr ="";
		Set<String> keys = softReferences.keySet();
		for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
			String category = iter.next();
			softRefStr += "["+category+"::";
			Set<Reference> catSoftRefs = softReferences.get(category);
			for (Iterator<Reference> iterator = catSoftRefs.iterator(); iterator.hasNext();) {
				String srs = iterator.next().getSoftReference();
				softRefStr +=srs+",";
			}
			softRefStr +="]";
		}
		retVal += "<ConditionExpression id='" + this.id + "' errorCnt ='" + errorStack.size() + "'>"+softRefStr+"</ConditionExpression>";

		return retVal;
	}
	
	private static class Reference {
		
		private boolean cycleDetector;
		private final String softReference;
		
		public Reference(String softReference, boolean cycleDetector) {
			this.softReference = softReference;
			this.cycleDetector = cycleDetector;
		}
		
		public String getSoftReference() {
			return softReference;
		}
		
		public boolean isCycleDetector() {
			return cycleDetector;
		}

		public void setCycleDetector(boolean cycleDetector) {
			this.cycleDetector = cycleDetector;
		}
	}
}
