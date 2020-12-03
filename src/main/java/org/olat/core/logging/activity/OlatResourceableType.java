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
package org.olat.core.logging.activity;

import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Specialization of ILoggingResourceableType which represents a
 * OlatResourceable (or OlatResource/RepositoryEntry in some other cases)
 * <p>
 * See the LoggingResourceable class's corresponding wrap() method.
 * <P>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 * @see LoggingResourceable#wrap
 */
public enum OlatResourceableType implements ILoggingResourceableType {

	/** this represents an ICourse **/
	course,
	/** this represents a CourseNode **/
	node,

	/** this represents a BusinessGroup **/
	businessGroup,
	/** this represents a BGContext **/
	bgContext,

	/** this represents a Forum **/
	forum,
	/** this represents a (Forum) Message **/
	forumMessage,
	/** this represents a (Info) Message **/
	infoMessage,

	/** this represents a I(MS)Q(TI) TEST **/
	iq,

	/** represents a SCORM Resource **/
	scormResource,

	/** represents a Wiki **/
	wiki,

	/** represents a content package **/
	cp,

	/** this represents a QTi test **/
	test,

	/** represents a shared folder **/
	sharedFolder,

	/** represents a feed resource (blog or podcast) **/
	feed,
	/** represents a feed item (blog post or podcast episode) **/
	feedItem,

	/** represents a generic - non further specified - repositoryentry. use this only if you dont know the type beforehand. **/
	genRepoEntry,

	/**   represents an calendar   **/
	calendar,

	/**   represents a portfolio map   **/
	portfolio,
	section,
	assignment,
	media,

	/** represents virtual class room **/
	openmeetings,
	adobeconnect,
	bigbluebutton,
	teams,

	/** business path component **/
	businessPath,
	/** represents a lecture block of a course */
	lectureBlock,

	/** represents an Edubase course node */
	edubase
}