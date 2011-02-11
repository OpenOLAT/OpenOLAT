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
	
	calendar,
	chat,
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
	forummessage,
	forumthread,
	glossar,
	gotonode,
	groupmanagement,
	group,
	grouparea,
	groupareaempty,
	help,
	layout,
	login,
	logout,
	mail,
	node,
	owner,
	participant,
	portfolioartefact,
	portfoliomap,
	portfoliotask,
	publisher,
	quota,
	rating,
	resource,
	rights,
	rightsempty,
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
	waitingperson;

}
