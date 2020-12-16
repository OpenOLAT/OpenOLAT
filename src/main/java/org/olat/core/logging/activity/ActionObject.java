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

/**
 * Global list of actionObjects existing in OLAT.
 * <p>
 * The actionObject will be stored to the logging table
 * and the idea is to keep this list short and clean
 * <P>
 * Note that the result of ActionObject.name() will be
 * used directly as the String stored to the database -
 * hence use a meaningful, short (32 characters at max)
 * String.
 * <p>
 * Initial Date:  13.11.2009 <br>
 * @author bja
 */
public enum ActionObject {

	bookSection,
	blog,
	calendar,
	chat,
	check,
	checkbox,
	checkpoint,
	comment,
	course,
	cpgetfile,
	editor,
	efficency,
	feed,
	feeditem,
	file,
	folder,
	forum,
	forummessage,
	forumthread,
	glossar,
	gotonode,
	groupmanagement,
	group,
	grouparea,
	groupareaempty,
	help,
	infomessage,
	layout,
	login,
	logout,
	mail,
	node,
	owner,
	participant,
	participantList,
	portfolioartefact,
	portfoliomap,
	portfoliomedia,
	portfoliotask,
	portfoliosection,
	publisher,
	quota,
	rating,
	resource,
	rights,
	rightsempty,
	search,
	sharedfolder,
	spgetfile,
	statistic,
	test,
	testattempts,
	testcomment,
	testid,
	testscore,
	testsuccess,
	tools,
	toolsempty,
	waitingperson,
	wiki,
	bulkassessment,
	lectures,
	lecturesRollcall,
	assessmentdocument,
	completionType,
	teams,
	bigbluebutton;

}
