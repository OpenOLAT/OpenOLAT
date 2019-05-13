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
* <p>
*/

package org.olat.util.logging.activity;

import java.io.UnsupportedEncodingException;

import org.olat.commons.calendar.model.Kalendar;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ILoggingResourceable;
import org.olat.core.logging.activity.ILoggingResourceableType;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.modules.edubase.BookSection;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.Section;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;

/**
 * A LoggingResourceable is the least common denominator between an OlatResourceable,
 * an OlatResource, a RepositoryEntry and simple Strings - all of which want to be
 * used as (greatGrandParent,grandParent,parent,target) resourcs in the logging table.
 * <p>
 * The idea of this class is to have one class containing the three fields
 * <ul>
 *  <li>type: what sort of resource is it</li>
 *  <li>id: an id of the olat database - if available</li>
 *  <li>name: some sort of name or title of this resource</li>
 * </ul>
 * combined.
 * <p>
 * Besides the above (container for the triple type/id/name) it serves the purpose
 * of doing checks between the businessPath/contextEntries and the ThreadLocalUserActivityLogger's
 * LoggingResourceables which have been collected all the way from the initial request
 * creating a particular Controller to the actual event handling method calling
 * into IUserActivityLogger.log() - optionally passing additional LoggingResourceables.
 * <p>
 * The above check is done as a testing means to assure the data we're logging
 * matches what we expect it to contain.
 * <p>
 * This way we avoid difficult if not unrealistic testing of the use of this
 * IUserActivityLogging framework.
 * <p>
 * If a comparison with the businessPath fails, a simple (technical) log.WARN is issued.
 * This should then be noticed by the system administrator hence feeding back
 * into a patch or a fix for the next release.
 * <P>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 */
public class LoggingResourceable implements ILoggingResourceable {

	/** the logging object used in this class **/
	private static final Logger log_ = Tracing.createLoggerFor(LoggingResourceable.class);

	/** the maximum number of bytes for the name field **/
	public static final int MAX_NAME_LEN = 240;

	/** the maximum number of bytes for the id field **/
	public static final int MAX_ID_LEN = 60;

	/** the maximum number of bytes for the type field **/
	public static final int MAX_TYPE_LEN = 30;

	/** type of this LoggingResourceable - contains the OlatResourceable's type in the OlatResourceable case,
	 * or the enum name() of the StringResourceableType otherwise
	 */
	private final String type_;

	/** the id of this LoggingResourceable - contains the OlatResource or RepositoryEntry's ID in those cases,
	 * or -1 in the StringResourceableType case.
	 */
	private final String id_;

	/** the name of this LoggingResourceable - this can be the title in case of a course - or
	 * the html name of a page in case of cp
	 */
	private final String name_;

	/** the ILoggingResourceableType corresponding to this LoggingResourceable - this is used for
	 * checks against the businessPath
	 */
	private final ILoggingResourceableType resourceableType_;

	/** the OlatResourceable if we have one - null otherwise. Used for equals() and the businessPath check mainly **/
	private final OLATResourceable resourceable_;


	private final boolean ignorable;

	/**
	 * Restrict the given argument to the given number of bytes using UTF-8 encoding.
	 * <p>
	 * This method does not issue any logging
	 * @param arg the string to be size-restricted
	 * @param maxBytes the maximum number of bytes the string should result to when converting into UTF-8
	 * @return a string matching into the given number of bytes
	 */
	public static String restrictStringLength(String arg, int maxBytes) {
		return restrictStringLength(arg, maxBytes, null, false);
	}

	/**
	 * Utility method to restrict the given String 'arg' to be of byte-length 'maxBytes'.
	 * <p>
	 * Uses String.getBytes() to determine byte length
	 * @param arg the String to be restricted to the maxBytes length
	 * @param maxBytes the max length allowed
	 * @param argNameForLogging the name of the arg value - used in case there's an error to give more accurate logging details
	 * @return
	 */
	private static String restrictStringLength(String arg, int maxBytes, String argNameForLogging, boolean log) {
		if (arg==null) {
			// we don't restrict arg not to be null - in this case we just return null
			return null;
		}
		// otherwise, if arg is not null, then we check its length
		try{
			if (arg.getBytes("UTF-8").length<=maxBytes) {
				// all fine
				return arg;
			}
			if (log) log_.error("restrictStringLength: "+argNameForLogging+" too long. Allowed "+maxBytes+", actual: "+arg.getBytes().length+", value="+arg);
			String result = arg.substring(0, Math.min(arg.length()-1, maxBytes));
			while(result.getBytes("UTF-8").length>maxBytes) {
				result = result.substring(0, result.length()-4);
			}
			return result;
		} catch(UnsupportedEncodingException uee) {
			log_.error("restrictStringLength: unsupported encoding: ", uee);
			if (arg.getBytes().length<=maxBytes) {
				// all fine
				return arg;
			}
			if (log) log_.error("restrictStringLength: "+argNameForLogging+" too long. Allowed "+maxBytes+", actual: "+arg.getBytes().length+", value="+arg);
			String result = arg.substring(0, Math.min(arg.length()-1, maxBytes));
			while(result.getBytes().length>maxBytes) {
				result = result.substring(0, result.length()-4);
			}
			return result;
		}
	}

	/**
	 * Internal constructor to create a LoggingResourceable object with the given mandatory
	 * parameters initialized.
	 * <p>
	 * This method also does length checks to catch oversized parameters as early as possible
	 * (versus later in the hibernate/mysql handling)
	 * <p>
	 * @param resourceable the OlatResourceable if available - can be null
	 * @param resourceableType the type which is used for comparison later during businessPath checks
	 * @param type the type to be stored to the database
	 * @param id the id to be stored to the database
	 * @param name the name to be stored to the database
	 */
	private LoggingResourceable(OLATResourceable resourceable, ILoggingResourceableType resourceableType, String type, String id, String name, boolean ignorable) {
		type_ = restrictStringLength(type, MAX_TYPE_LEN, "type", true);
		id_ = restrictStringLength(id, MAX_ID_LEN, "id", true);
		name_ = restrictStringLength(name, MAX_NAME_LEN, "name", true);
		resourceable_ = resourceable;
		resourceableType_ = resourceableType;
		this.ignorable = ignorable;
	}

	/**
	 * These are ignored from the logging.
	 * @param olatResourceable
	 * @return
	 */
	public static LoggingResourceable wrapBusinessPath(OLATResourceable olatResourceable) {
		return new LoggingResourceable(olatResourceable, OlatResourceableType.businessPath, "businessPath", "0", "", true);
	}

//
// Following is a set of wrap*() methods which take specific 'olat resourceable' objects
// and selects the type/id/name information to be taken out of it
//

	public static LoggingResourceable wrapScormRepositoryEntry(RepositoryEntry scormRepoEntry) {
		if (scormRepoEntry==null) {
			throw new IllegalArgumentException("scormRepoEntry must not be null");
		}
		return wrap(scormRepoEntry, OlatResourceableType.scormResource);
	}

	/**
	 * Wraps a Wiki into a LoggingResourceable
	 * @param olatResourceable the wiki
	 * @return a LoggingResourceable representing the given wiki
	 */
	public static LoggingResourceable wrapWikiOres(OLATResourceable olatResourceable) {
		if (olatResourceable==null) {
			throw new IllegalArgumentException("olatResourceable must not be null");
		}
		if (olatResourceable.equals(BusinessGroupMainRunController.ORES_TOOLWIKI)) {
			return new LoggingResourceable(olatResourceable, OlatResourceableType.wiki, "wiki", "0", "", false);
		} else {
			return wrap(olatResourceable, OlatResourceableType.wiki);
		}
	}

	/**
	 * Wraps a portfolio into a LoggingResourceable
	 * @param olatResourceable the wiki
	 * @return a LoggingResourceable representing the given wiki
	 */
	public static LoggingResourceable wrapPortfolioOres(OLATResourceable olatResourceable) {
		if (olatResourceable==null) {
			throw new IllegalArgumentException("olatResourceable must not be null");
		}
		if (olatResourceable.equals(BusinessGroupMainRunController.ORES_TOOLPORTFOLIO)) {
			return new LoggingResourceable(olatResourceable, OlatResourceableType.portfolio, "portfolio", "0", "", false);
		} else {
			return wrap(olatResourceable, OlatResourceableType.portfolio);
		}
	}

	/**
	 * Wraps a portfolio v 2.0 or binder into a LoggingResourceable
	 * @param binder The binder (persisted)
	 * @return a LoggingResourceable representing the given binder
	 */
	public static LoggingResourceable wrap(Binder binder) {
		if (binder==null) {
			throw new IllegalArgumentException("binder must not be null");
		}
		return new LoggingResourceable(binder, OlatResourceableType.portfolio, binder.getResourceableTypeName(),
				String.valueOf(binder.getResourceableId()), binder.getTitle(), false);
	}

	/**
	 * Wraps the section of a portfolio v 2.0 or binder into a LoggingResourceable
	 * @param section The section (persisted)
	 * @return a LoggingResourceable representing the given section
	 */
	public static LoggingResourceable wrap(Section section) {
		if (section==null) {
			throw new IllegalArgumentException("assignment must not be null");
		}
		OLATResourceable sectionOres = OresHelper.createOLATResourceableInstance(Section.class, section.getKey());
		return new LoggingResourceable(sectionOres, OlatResourceableType.section, sectionOres.getResourceableTypeName(),
				String.valueOf(sectionOres.getResourceableId()), section.getTitle(), false);
	}

	/**
	 * Wraps the assignment in a portfolio v 2.0 or binder into a LoggingResourceable
	 * @param assignment The assignment (persisted)
	 * @return a LoggingResourceable representing the given assignment
	 */
	public static LoggingResourceable wrap(Assignment assignment) {
		if (assignment == null) {
			throw new IllegalArgumentException("assignment must not be null");
		}
		OLATResourceable assignmentOres = OresHelper.createOLATResourceableInstance(Assignment.class, assignment.getKey());
		return new LoggingResourceable(assignmentOres, OlatResourceableType.assignment, assignmentOres.getResourceableTypeName(),
				String.valueOf(assignmentOres.getResourceableId()), assignment.getTitle(), false);
	}

	/**
	 * Wraps the media in portfolio v 2.0 into a LoggingResourceable
	 * @param media The media (persisted)
	 * @return a LoggingResourceable representing the given media
	 */
	public static LoggingResourceable wrap(Media media) {
		if (media == null) {
			throw new IllegalArgumentException("media must not be null");
		}
		OLATResourceable mediaOres = OresHelper.createOLATResourceableInstance(Media.class, media.getKey());
		return new LoggingResourceable(mediaOres, OlatResourceableType.media, mediaOres.getResourceableTypeName(),
				String.valueOf(mediaOres.getResourceableId()), media.getTitle(), false);
	}

	/**
	 * Wraps OpenMeetings into a LoggingResourceable
	 * @param olatResourceable the meeting
	 * @return a LoggingResourceable representing openmeetings
	 */
	public static LoggingResourceable wrapOpenMeetingsOres(OLATResourceable olatResourceable) {
		if (olatResourceable==null) {
			throw new IllegalArgumentException("olatResourceable must not be null");
		}
		if (olatResourceable.equals(BusinessGroupMainRunController.ORES_TOOLOPENMEETINGS)) {
			return new LoggingResourceable(olatResourceable, OlatResourceableType.openmeetings, "openmeetings", "0", "", false);
		} else {
			return wrap(olatResourceable, OlatResourceableType.openmeetings);
		}
	}

	/**
	 * General wrapper for an OlatResourceable - as it's not obvious of what type that
	 * OlatResourceable is (in terms of being able to later compare it against the businessPath etc)
	 * an ILoggingResourceableType needs to be passed to this method as well.
	 * @param olatResourceable a general OlatResourceable
	 * @param type the type of the olatResourceable
	 * @return a LoggingResourceable wrapping the given olatResourceable type pair
	 */
	public static LoggingResourceable wrap(OLATResourceable olatResourceable, ILoggingResourceableType type) {
		RepositoryEntry repoEntry = null;
		if (olatResourceable instanceof RepositoryEntry) {
			repoEntry = (RepositoryEntry) olatResourceable;
		} else {
			repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(olatResourceable, false);
		}
		if (repoEntry!=null) {
			return new LoggingResourceable(repoEntry, type, repoEntry.getOlatResource().getResourceableTypeName(),
					String.valueOf(repoEntry.getOlatResource().getResourceableId()), repoEntry.getDisplayname(), false);
		} else if (olatResourceable instanceof OLATResource) {
			OLATResource olatResource = (OLATResource) olatResourceable;
			return new LoggingResourceable(olatResource, type, olatResource.getResourceableTypeName(),
					String.valueOf(olatResource.getResourceableId()), String.valueOf(olatResource.getKey()), false);
		} else {
			return new LoggingResourceable(olatResourceable, type, olatResourceable.getResourceableTypeName(),
					String.valueOf(olatResourceable.getResourceableId()), "", false);
		}
	}

	/**
	 * General wrapper for non OlatResourceable types - i.e. for simple Strings.
	 * <p>
	 * The LoggingResourceable always needs to have an ILoggingResourceableType - therefore
	 * it needs to be passed to this method.
	 * <p>
	 * Note that the typeForDB (so to speak) is set to ILoggingResourceableType.name().
	 * <p>
	 * Also note that there are a few further specialized wrapXXX(String) methods for
	 * selected StringResourceableTypes.
	 * <p>
	 * @param type the ILoggingResourceableType which corresponds the given id/name information
	 * @param idForDB the id - to be stored to the database
	 * @param nameForDB the name - to be stored to the database
	 * @return a LoggingResourceable wrapping the given type/id/name triple
	 */
	public static LoggingResourceable wrapNonOlatResource(StringResourceableType type, String idForDB, String nameForDB) {
		return new LoggingResourceable(null, type,
				type.name(), idForDB, nameForDB, false);
	}

	/**
	 * Wraps a filename as type StringResourceableType.uploadFile into a LoggingResourceable
	 * @param uploadFileName the filename - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given filename as type StringResourceableType.uploadFile
	 */
	public static LoggingResourceable wrapUploadFile(String uploadFileName) {
		return wrapNonOlatResource(StringResourceableType.uploadFile, createUniqueId(StringResourceableType.uploadFile.toString(), uploadFileName), uploadFileName);
	}

	/**
	 * Wraps a filename as type StringResourceableType.bcFile into a LoggingResourceable
	 * @param bcFileName the filename - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given filename as type StringResourceableType.bcFile
	 */
	public static LoggingResourceable wrapBCFile(String bcFileName) {
		return wrapNonOlatResource(StringResourceableType.bcFile, createUniqueId(StringResourceableType.bcFile.toString(), bcFileName), bcFileName);
	}

	/**
	 * Wraps a cpNodeName as type StringResourceableType.cpNode into a LoggingResourceable
	 * @param cpNodeName the node name - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given node name as type StringResourceableType.cpNode
	 */
	public static LoggingResourceable wrapCpNode(String cpNodeName) {
		return wrapNonOlatResource(StringResourceableType.cpNode, createUniqueId(StringResourceableType.cpNode.toString(), cpNodeName), cpNodeName);
	}

	/**
	 * Wraps a single page uri as type StringResourceableType.spUri into a LoggingResourceable
	 * @param spUri the single page uri - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given uri as type StringResourceableType.spUri
	 */
	public static LoggingResourceable wrapSpUri(String spUri) {
		return wrapNonOlatResource(StringResourceableType.spUri, createUniqueId(StringResourceableType.spUri.toString(), spUri), spUri);
	}

	/**
	 * Wraps a businessgroup right as type StringResourceableType.bgRight into a LoggingResourceable
	 * @param right the name of the businessgroup right - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given right name as type StringResourceableType.bgRight
	 */
	public static LoggingResourceable wrapBGRight(String right) {
		return wrapNonOlatResource(StringResourceableType.bgRight, createUniqueId(StringResourceableType.bgRight.toString(), right), right);
	}

	/**
	 * Wraps a filename of type StringResourceableType.uploadFile into a LoggingResourceable
	 * @param uploadFileName the filename - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given filename as type StringResourceableType.uploadFile
	 */
	public static LoggingResourceable wrap(BGArea bgArea) {
		return wrapNonOlatResource(StringResourceableType.bgArea, createUniqueId(StringResourceableType.bgArea.toString(), bgArea.toString()), bgArea.getName());
	}

	/**
	 * Wraps an Identity as type StringResourceableType.targetIdentity into a LoggingResourceable
	 * @param identity the identity - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given identity as type StringResourceableType.targetIdentity
	 */
	public static LoggingResourceable wrap(Identity identity) {
		return wrapNonOlatResource(StringResourceableType.targetIdentity, String.valueOf(identity.getKey()), identity.getName());
	}

	/**
	 * Wraps a Forum into a LoggingResourceable - setting type/id/name accordingly
	 * @param forum the forum to be wrapped
	 * @return a LoggingResourceable wrapping the given Forum
	 */
	public static LoggingResourceable wrap(Forum forum) {
		final String name = CoreSpringFactory.getImpl(ForumManager.class).getForumNameForLogging(forum);
		return new LoggingResourceable(forum, OlatResourceableType.forum, forum.getResourceableTypeName(),
				String.valueOf(forum.getResourceableId()), name, false);
	}

	/**
	 * Wraps a (Forum) Message into a LoggingResourceable - setting type/id/name accordingly
	 * @param message the message to be wrapped
	 * @return a LoggingResourceable wrapping the given (Forum) Message
	 */
	public static LoggingResourceable wrap(Message forumMessage) {
		return new LoggingResourceable(OresHelper.createOLATResourceableInstance(Message.class, forumMessage.getKey()), OlatResourceableType.forumMessage, OlatResourceableType.forumMessage.name(),
				String.valueOf(forumMessage.getKey()), forumMessage.getTitle(), false);
	}

	/**
	 * Wraps a Feed into a LoggingResourceable - setting type/id/name accordingly
	 * @param feed the feed to be wrapped
	 * @return a LoggingResourceable wrapping the given feed
	 */
	public static LoggingResourceable wrap(Feed feed) {
		String title = feed.getTitle();
		// truncate title after 230 chars
		if (title == null) title = "";
		if (title.length() > 230) title = title.substring(0, 229);
		return new LoggingResourceable(feed, OlatResourceableType.feed, feed.getResourceableTypeName(),
				String.valueOf(feed.getResourceableId()), title, false);
	}

	/**
	 * Wraps a (Feed) Item into a LoggingResourceable - setting type/id/name accordingly
	 * @param item the item to be wrapped
	 * @return a LoggingResourceable wrapping the given (Feed) Item
	 */
	public static LoggingResourceable wrap(Item item) {
		if (item.getExternalLink() != null) {
			// external feeds often use URL's as Guid, but URL's are too long. Thus in this case use the name instead of the ID field.
			String guid = item.getGuid();
			// only use last 230 chars of the URL if too long
			if (guid.length() > 230) guid = guid.substring(guid.length() - 230);
			return wrapNonOlatResource(StringResourceableType.feedItem, null, guid);
		} else {
			String title = item.getTitle();
			// truncate title after 230 chars
			if (title.length() > 230) title = title.substring(0, 229);
			return wrapNonOlatResource(StringResourceableType.feedItem, item.getGuid(), title);
		}
	}

	/**
	 * Wraps a BusinessGroup into a LoggingResourceable - setting type/id/name accordingly
	 * @param group the group to be wrapped
	 * @return a LoggingResourceable wrapping the given BusinessGroup
	 */
	public static LoggingResourceable wrap(BusinessGroup group) {
		return new LoggingResourceable(group, OlatResourceableType.businessGroup, group.getResourceableTypeName(),
				String.valueOf(group.getKey()), group.getName(), false);
	}

	/**
	 * Wraps a ICourse into a LoggingResourceable - setting type/id/name accordingly
	 * @param course the course to be wrapped
	 * @return a LoggingResourceable wrapping the given ICourse
	 */
	public static LoggingResourceable wrap(ICourse course) {
		return new LoggingResourceable(course, OlatResourceableType.course, course.getResourceableTypeName(),
				String.valueOf(course.getResourceableId()), course.getCourseTitle(), false);
	}

	public static LoggingResourceable wrapTest(RepositoryEntry entry) {
		return new LoggingResourceable(entry, OlatResourceableType.test, entry.getOlatResource().getResourceableTypeName(),
				String.valueOf(entry.getOlatResource().getResourceableId()), entry.getDisplayname(), false);
	}

	/**
	 * Wraps a CourseNode into a LoggingResourceable - setting type/id/name accordingly
	 * @param node the node to be wrapped
	 * @return a LoggingResourceable wrapping the given node
	 */
	public static LoggingResourceable wrap(CourseNode node) {
		final String name = node.getShortTitle();
		final String ident = node.getIdent();

		final String typeForLogging = node.getType();
		try{
			Long id = Long.parseLong(ident);

			return new LoggingResourceable(OresHelper.createOLATResourceableInstance("CourseNode", id), OlatResourceableType.node, typeForLogging,
					node.getIdent(), name, false);
		} catch(NumberFormatException nfe) {
			return new LoggingResourceable(null, OlatResourceableType.node, typeForLogging,
					node.getIdent(), name, false);
		}
	}

	/**
	 * Wraps a calendar into a LoggingResourceable - setting type/id/name accordingly
	 * @param calendar the calendar to be wrapped
	 * @return a LoggingResourceable wrapping the given calendar
	 */
	public static LoggingResourceable wrap(Kalendar calendar) {
		return wrapNonOlatResource(StringResourceableType.calendar, calendar.getCalendarID(), calendar.getType());
	}

	public static LoggingResourceable wrap(BookSection bookSection) {
		return wrapNonOlatResource(
				StringResourceableType.bookSection,
				createUniqueId(StringResourceableType.bookSection.toString(), bookSection.getBookId()),
				bookSection.getBookId());
	}

	/**
	 * Create unique id.
	 * @param type
	 * @param uploadFileName
	 * @return
	 */
	private static String createUniqueId(String type, String name) {
		return OresHelper.createStringRepresenting(OresHelper.createOLATResourceableType(type), name);
	}

	@Override
	public String toString() {
		return "LoggingResourceInfo[type="+type_+",rtype="+resourceableType_.name()+",id="+id_+",name="+name_+"]";
	}

	/**
	 * Returns the type of this LoggingResourceable - this is the OlatResourceable's type
	 * (in case this LoggingResource represents a OlatResourceable) - or the StringResourceableType's enum name()
	 * otherwise
	 * @return the type of this LoggingResourceable
	 */
	public String getType() {
		return type_;
	}

	/**
	 * Returns the id of this LoggingResourceable - the id varies depending on the type of this
	 * LoggingResourceable - but usually it is the olatresourceable id or the olatresource id.
	 * @return the id of this LoggingResourceable
	 */
	public String getId() {
		return id_;
	}

	/**
	 * Returns the name of this LoggingResourceable - the name varies depending on the type
	 * of this LoggingResource - e.g. in the course case it is the name of the course, in
	 * the CP case it is the html filename incl path
	 * @return
	 */
	public String getName() {
		return name_;
	}

	/**
	 * Returns the ILoggingResourceableType of this LoggingResourceable - used for businessPath checking
	 * @return the ILoggingResourceableType of this LoggingResourceable
	 */
	public ILoggingResourceableType getResourceableType() {
		return resourceableType_;
	}

	@Override
	public boolean isIgnorable() {
		return ignorable;
	}

	@Override
	public int hashCode() {
		return type_.hashCode()+(id_!=null ? id_.hashCode() : 1)+(resourceable_!=null ? resourceable_.getResourceableTypeName().hashCode()+(int)resourceable_.getResourceableId().longValue() : 0) + (resourceableType_!=null ? resourceableType_.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LoggingResourceable)) {
			return false;
		} else if (super.equals(obj)) {
			return true;
		} else if (hashCode()!=obj.hashCode()) {
			return false;
		}

		LoggingResourceable lri = (LoggingResourceable)obj;
		if (!type_.equals(lri.type_)) {
			return false;
		}
		if (id_==null) {
			if (lri.id_!=null) {
				return false;
			}
		} else if (lri.id_==null) {
			return false;
		} else if (!id_.equals(lri.id_)) {
			return false;
		}
		if (resourceableType_!=lri.resourceableType_) {
			return false;
		}
		if (resourceable_==null && lri.resourceableType_!=null) {
			return false;
		}
		if (resourceable_!=null && lri.resourceableType_==null) {
			return false;
		}
		if (!resourceable_.getResourceableTypeName().equals(lri.resourceable_.getResourceableTypeName())) {
			return false;
		}
		if (!resourceable_.getResourceableId().equals(lri.resourceable_.getResourceableId())) {
			return false;
		}

		// bingo
		return true;
	}

	/**
	 * Checks whether this LoggingResourceable represents the same resource as the
	 * given ContextEntry.
	 * <p>
	 * This is used during the businessPath check.
	 * @param ce
	 * @return
	 */
	public boolean correspondsTo(ContextEntry ce) {
		if (ce==null) {
			return false;
		}
		OLATResourceable ceResourceable = ce.getOLATResourceable();
		if (ceResourceable==null) {
			return false;
		}

		if (resourceable_!=null) {
			if (ceResourceable.getResourceableTypeName().equals(resourceable_.getResourceableTypeName()) &&
					ceResourceable.getResourceableId().equals(resourceable_.getResourceableId())) {
				return true;
			}
			if (ceResourceable instanceof RepositoryEntry) {
				RepositoryEntry re = (RepositoryEntry) ceResourceable;

				OLATResource ores = re.getOlatResource();
				if (ores!=null &&
						ores.getResourceableTypeName().equals(resourceable_.getResourceableTypeName()) &&
						ores.getResourceableId().equals(resourceable_.getResourceableId())) {
					return true;
				}
			} else if (OresHelper.calculateTypeName(RepositoryEntry.class).equals(ceResourceable.getResourceableTypeName())) {
				// @TODO: Performance hit! Speed optimize this!
				// OLAT-4996
				// OLAT-4955
				// that's the jump-in case where the ContextEntry says it has a [RepositoryEntry:123212321] but
				// the actual class of ceResourceable is not a RepositoryEntry but an OresHelper$3 ...

				// in which case all we have is the key of the repositoryentry and we must make a DB lookup to
				// map the repo key to the corresponding olatresource

				OLATResource ores = RepositoryManager.getInstance().lookupRepositoryEntryResource(ceResourceable.getResourceableId());
				if (ores!=null  &&
						ores.getResourceableTypeName().equals(resourceable_.getResourceableTypeName()) &&
						ores.getResourceableId().equals(resourceable_.getResourceableId())) {
					return true;
				}

			}
			return ceResourceable.equals(resourceable_);
		}

		// if resourceable_ is null it's rather difficult to compare us with the contextentry
		// we still try...
		if (type_.equals(StringResourceableType.targetIdentity.name())  &&
				ceResourceable.getResourceableTypeName()=="Identity") {
			return id_.equals(String.valueOf(ceResourceable.getResourceableId()));
		}
  	return false;
	}

}
