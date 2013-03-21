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
package org.olat.core.logging;

/**
 * Use this exception when you want to identify a situation where an exception
 * occurs that we want to treat as a known issues case.
 * <p>
 * Known issues shall then be given a JIRA number and instances of this class
 * hence carry that number.
 * <p>
 * When this exception ends up in a log file, we can then treat this case
 * as a known issue and not let a test fail because of this - plus we have
 * a jira number which should speed up error analysis.
 * @author Stefan
 */
public class KnownIssueException extends OLATRuntimeException {

	private static final long serialVersionUID = -2238916925857721010L;
	private int jiraNumber;

	/**
	 * Includes throwable.
	 * @param logMsg
	 * @param cause
	 */
	public KnownIssueException(String logMsg, int jiraNumber, Throwable cause) {
		super(KnownIssueException.class, logMsg+" (OLAT-"+jiraNumber+")", cause);
		this.jiraNumber = jiraNumber;
	}
	
	/**
	 * Generic signature.
	 * @param logMsg
	 */
	public KnownIssueException(String logMsg, int jiraNumber) {
		this (logMsg, jiraNumber, new Exception("KnownIssueException<init>"));
	}
	
	/**
	 * JIRA Number without prefix
	 * @return
	 */
	public int getJiraNumber(){
		return jiraNumber;
	}
	
	/**
	 * convenience method to retrieve html fragment with link to jira
	 * e.g. <a href="http://bugs.olat.org/jira/browse/OLAT-XXXX">OLAT-XXXX</a>
	 * @return
	 */
	public String getJiraLink(){
		return "<a href='http://bugs.olat.org/jira/browse/OLAT-"+jiraNumber+"'>OLAT-"+jiraNumber+"</a>";
	}
	
}
