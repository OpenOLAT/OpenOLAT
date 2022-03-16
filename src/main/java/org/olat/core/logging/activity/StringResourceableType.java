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
 * String rather than an OlatResourceable.
 * <p>
 * Note that the string can be something like an URL, an Identity
 * or a group right - but it can also correspond to an OlatResourceable
 * in cases like scormResource, assessmentID. In the latter case
 * we do not have an OlatResourceable at the time where the LoggingResourceable
 * is added to the IUserActivityLogger - otherwise it would be
 * preferable to add an OlatResourceable always.
 * <p>
 * See the LoggingResourceable class's corresponding wrap() method.
 * <P>
 * Initial Date:  21.10.2009 <br>
 * @author Stefan
 * @see LoggingResourceable#wrap
 */
public enum StringResourceableType implements ILoggingResourceableType {
	/** the calendar id **/
	calendar,
	/** corresponds to the CourseNode ID - but we seem to not have an OlatResourceable at the time **/
	nodeId,

	/** the single page URI **/
	spUri,

	/** the businessgroup area name **/
	bgArea,

	/** the businessgroup right (key) **/
	bgRight,

	/** the content package node name (filename) **/
	cpNode,

	/** a checklist **/
	checklist,

	 /** a checkpoint from a checklist **/
	checkpoint,

	 /** checkbox from a checklist **/
	checkbox,

	/** the briefcase filename **/
	bcFile,

	/** the uploaded filename **/
	uploadFile,

	/** the target identity **/
	targetIdentity,

	/** the softkey of the glossary - we seem to not have an OlatResourceable at the time **/
	glossarySoftKey,

	/** the scorm resource ID - we seem to not have an OlatResourceable at the time **/
	scormResource,

	/** the number of attemts - in QTI **/
	qtiAttempts,

	qtiGrade,

	/** the score - in QTI **/
	qtiScore,

	/** the passed value - in QTI **/
	qtiPassed,

	/** the assessment ID - we seem to not have an Olatresourceable there **/
	assessmentID,

	/** the comment - in QTI **/
	qtiComment,

	/** the user comment - in QTI **/
	qtiUserComment,

	/** the coach comment - in QTI **/
	qtiCoachComment,

	/** the param part of the URI during a QTI test - equivalent to what was passed to IQDisplayController.logAudit before **/
	qtiParams,
	
	/** assessment document **/
	assessmentDocument,

	/** Context sensitive help **/
	csHelp,

	/** Blog Post and Podcast Episodes **/
	feedItem,

	/** the name of the statisticmanager viewed **/
	statisticManager,

	/** the type of statistic viewed **/
	statisticType,

	/** the title of the column of which the total is viewed **/
	statisticColumn,

	/** Special case to allow any type before a certain type e.g. [*][*]...[portfolio] **/
	anyBefore,

	/** Edubase book section */
	bookSection;
	
}