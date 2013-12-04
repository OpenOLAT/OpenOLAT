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

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class ArgumentParseException extends RuntimeException {

	private static final long serialVersionUID = 7135633046896613748L;
	/**
	 * Errorcode if the function needs more arguments
	 */
	public static final int NEEDS_MORE_ARGUMENTS = 1;
	/**
	 * Errorcode if the function needs fewer arguments
	 */
	public static final int NEEDS_FEWER_ARGUMENTS = 2;
	/**
	 * Errorcode if an argument has the wrong type or is mal formatted.<BR>
	 * I.e. wrong date format, NodeId instead of groupname.
	 */
	public static final int WRONG_ARGUMENT_FORMAT = 4;
	/**
	 * Errorcode if the argument is a reference, which can not be resolved.<BR>
	 * I.e. a groupname, areaname or other course node id which is not existing
	 */
	public static final int REFERENCE_NOT_FOUND = 8;

	private int errNo;
	private String functionName;
	private String wrongArgs;
	private String whatsWrong;
	private String solutionProposal;

	/**
	 * Thrown if an an exception occurs while parsing function arguments.
	 * 
	 * @param msg
	 */
	public ArgumentParseException(String msg) {
		super(msg);
	}

	/**
	 * @param errNo
	 * @param functionName
	 * @param wrongArgs
	 * @param whatsWrong
	 * @param solutionProposal
	 */
	public ArgumentParseException(int errNo, String functionName, String wrongArgs, String whatsWrong, String solutionProposal) {
		this.errNo = errNo;
		this.functionName = functionName;
		this.wrongArgs = wrongArgs;
		this.whatsWrong = whatsWrong;
		this.solutionProposal = solutionProposal;
	}

	/**
	 * @return Returns the errNo.
	 */
	public int getErrNo() {
		return errNo;
	}

	/**
	 * @return Returns the functionName.
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * the returned string is an untranslated translation key
	 * @return Returns the whatsWrong.
	 */
	public String getWhatsWrong() {
		return whatsWrong;
	}

	/**
	 * the returned string is an untranslated translation key
	 * @return Returns the wrongArgs.
	 */
	public String getWrongArgs() {
		return wrongArgs;
	}

	/**
	 * the returned string is an untranslated translation key
	 * @return
	 */
	public String getSolutionProposal() {
		return solutionProposal;
	}
}
