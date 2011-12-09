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

package org.olat.group;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.ui.DefaultContextTranslationHelper;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * @author Christian Guretzki
 */

public class BusinessGroupArchiver {

	private static final String DELIMITER = "\t";
	private static final String EOL = "\n";
	private final static String ALL_IN_ONE_FILE_NAME_PREFIX = "members";
	private final static String ZIP_WITH_FILE_PER_GROUP_NAME_PREFIX = "members";


	private static final String FILE_PER_GROUP_OR_AREA_INCL_GROUP_MEMBERSHIP = "memberlistwizard.archive.type.filePerGroupOrAreaInclGroupMembership"; //used as well as translation key
	private static final String FILE_PER_GROUP_OR_AREA = "memberlistwizard.archive.type.filePerGroupOrArea"; //used as well as translation key
	private static final String ALL_IN_ONE = "memberlistwizard.archive.type.allInOne";
	private static String OWNER = "owner";
	private static String PARTICIPANT = "participant";
	private static String WAITING = "waiting";
	
	private static BusinessGroupArchiver instance = new BusinessGroupArchiver();
	private BaseSecurity securityManager;
	private Translator translator;
	private Map<Locale,PackageTranslator> translatorMap;
	private List<UserPropertyHandler> userPropertyHandlers;

	/**
	 * constructs an unitialised BusinessGroup, use setXXX for setting attributes
	 */
	public BusinessGroupArchiver() {
		securityManager = BaseSecurityManager.getInstance();
		String groupCtrlPackage = Util.getPackageName(GroupController.class);
		PackageTranslator fallBacktranslator = new PackageTranslator(groupCtrlPackage, I18nModule.getDefaultLocale());
		String myPackage = Util.getPackageName(this.getClass());
		translator = new PackageTranslator(myPackage, I18nModule.getDefaultLocale(), fallBacktranslator);
		// fallback to translate user properties
		translator = UserManager.getInstance().getPropertyHandlerTranslator(translator);
		// get user property handlers used in this group archiver
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(BusinessGroupArchiver.class.getCanonicalName(), true);
	}

	public static BusinessGroupArchiver getInstance() {
		return instance;
	}

	/**
	 * Retrives a PackageTranslator for the input locale.
	 * 
	 * @param locale
	 * @return
	 */
	protected Translator getPackageTranslator(Locale locale) {
		if(I18nModule.getDefaultLocale().equals(locale)) {
			return translator;			
		} else {
			if(translatorMap==null) {
				translatorMap = new HashMap<Locale,PackageTranslator>();
			}				
			if(translatorMap.containsKey(locale)) {
				return translatorMap.get(locale);
			} else {
				String groupCtrlPackage = Util.getPackageName(GroupController.class);
				PackageTranslator fallBacktranslator = new PackageTranslator(groupCtrlPackage, locale);
				String myPackage = Util.getPackageName(this.getClass());
				PackageTranslator trans = new PackageTranslator(myPackage, locale, fallBacktranslator);
				translatorMap.put(locale, trans);
				return trans;
			}
		}
	}

	public void archiveGroup(BusinessGroup businessGroup, File archiveFile) {
		FileUtils.save(archiveFile, toXls(businessGroup), "utf-8");
	}

	private String toXls(BusinessGroup businessGroup) {
		StringBuffer buf = new StringBuffer();
		// Export Header
		buf.append(translator.translate("archive.group.name"));
		buf.append(DELIMITER);
		buf.append(businessGroup.getName());
		buf.append(DELIMITER);
		buf.append(translator.translate("archive.group.type"));
		buf.append(DELIMITER);
		buf.append(businessGroup.getType());
		buf.append(DELIMITER);
		buf.append(translator.translate("archive.group.description"));
		buf.append(DELIMITER);
		buf.append(FilterFactory.getHtmlTagsFilter().filter(businessGroup.getDescription()));
		buf.append(EOL);
		
			appendIdentityTable(buf, businessGroup.getOwnerGroup(), translator.translate("archive.header.owners"));
			appendIdentityTable(buf, businessGroup.getPartipiciantGroup(), translator.translate("archive.header.partipiciant"));
		
		if (businessGroup.getWaitingListEnabled() ) {
			appendIdentityTable(buf, businessGroup.getWaitingGroup(), translator.translate("archive.header.waitinggroup"));
		}
		return buf.toString();
	}

	private void appendIdentityTable(StringBuffer buf, SecurityGroup group, String title ) {
		if (group != null) {
		appendTitle(buf, title );
		appendIdentityTableHeader(buf);
		for (Iterator iter = securityManager.getIdentitiesAndDateOfSecurityGroup(group).iterator(); iter.hasNext();) {
			Object[] element = (Object[]) iter.next();
			Identity identity = (Identity) element[0];
			Date addedTo = (Date) element[1];
			appendIdentity(buf, identity, addedTo );
			}
		}
	}

	private void appendTitle(StringBuffer buf, String title) {
		buf.append(EOL);
		buf.append(title);
		buf.append(EOL);
	}

	private void appendIdentity(StringBuffer buf, Identity owner, Date addedTo) {
		Locale loc = translator.getLocale();
		// add the identities user name
		buf.append(owner.getName());
		buf.append(DELIMITER);
		// add all user properties
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			String value = propertyHandler.getUserProperty(owner.getUser(), loc);
			if (StringHelper.containsNonWhitespace(value)) {
				buf.append(value);
			}
			buf.append(DELIMITER);
		}
		// add the added-to date
		buf.append(addedTo.toString());
		buf.append(EOL);
	}

	private void appendIdentityTableHeader(StringBuffer buf) {
		// first the identites name
		buf.append( translator.translate("table.user.login") );
		buf.append(DELIMITER);
		// second the users properties
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			String label = translator.translate(propertyHandler.i18nColumnDescriptorLabelKey());
			buf.append(label);
			buf.append(DELIMITER);
		}
		// third the users added-to date
		buf.append( translator.translate("table.subject.addeddate") );
		buf.append(EOL);
	}

	public void archiveBGContext(BGContext context, File archiveFile) {
		FileUtils.save(archiveFile, toXls(context), "utf-8");		
	}

	private String toXls(BGContext context) {
		StringBuffer buf = new StringBuffer();
		// Export Context Header
		buf.append(translator.translate("archive.group.context.name"));
		buf.append(DELIMITER);
		buf.append(context.getName());
		buf.append(DELIMITER);
		buf.append(translator.translate("archive.group.context.type"));
		buf.append(DELIMITER);
		buf.append(context.getGroupType());
		buf.append(DELIMITER);
		buf.append(translator.translate("archive.group.context.description"));
		buf.append(DELIMITER);
		buf.append(FilterFactory.getHtmlTagsFilter().filter(context.getDescription()));
		buf.append(EOL);
		List groups = BGContextManagerImpl.getInstance().getGroupsOfBGContext(context);
		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			BusinessGroup group = (BusinessGroup) iter.next();
			buf.append( toXls(group) );
			buf.append(EOL);
			buf.append(EOL);
		}
		return buf.toString();
	}

	/**
	 * Creates an temp CSV (comma separated) file containing the members info
	 * (namely with the columns specified in "columnList"), the area info (for
	 * the filtered "areaList"), and separated in role sections: owners,
	 * participants and waiting.
	 * @param context
	 * @param columnList
	 * @param areaList
	 * @param archiveType
	 * @param userLocale
	 * @return the output file which could be an CSV or a zip file depending on the input archiveType.
	 * @see BGArea
	 */
	public File archiveAreaMembers(BGContext context, List<String> columnList, List<BGArea> areaList, String archiveType, UserRequest ureq) {

		List<Member> owners = new ArrayList<Member>();
		List<Member> participants = new ArrayList<Member>();
		List<Member> waitings = new ArrayList<Member>();

		List areas = BGAreaManagerImpl.getInstance().findBGAreasOfBGContext(context);
		for (Iterator areaIterator = areas.iterator(); areaIterator.hasNext();) {
			BGArea area = (BGArea) areaIterator.next();
			if (areaList.contains(area)) { //rely on the equals() method of the BGArea impl
 				List areaBusinessGroupList = BGAreaManagerImpl.getInstance().findBusinessGroupsOfArea(area);
				for (Iterator groupIterator = areaBusinessGroupList.iterator(); groupIterator.hasNext();) {
					BusinessGroup group = (BusinessGroup) groupIterator.next();
					if(group.getOwnerGroup()!=null) {
					  Iterator ownerIterator = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getOwnerGroup()).iterator();
					  addMembers(area.getKey(), ownerIterator, owners, OWNER);
					}
					if(group.getPartipiciantGroup()!=null) {
					  Iterator participantsIterator = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getPartipiciantGroup()).iterator();
					  addMembers(area.getKey(), participantsIterator, participants, PARTICIPANT);
					}
					if(group.getWaitingGroup()!=null) {
					  Iterator waitingIterator = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getWaitingGroup()).iterator();
					  addMembers(area.getKey(), waitingIterator, waitings, WAITING);
					}
				}
			}
		}
    Locale userLocale = ureq.getLocale();
    String charset = UserManager.getInstance().getUserCharset(ureq.getIdentity());
		Translator trans = getPackageTranslator(userLocale);
		List<OrganisationalEntity> organisationalEntityList = getOrganisationalEntityList(areaList);
		return generateArchiveFile(context, owners, participants, waitings, columnList, organisationalEntityList, 
				trans.translate("archive.areas"), archiveType, userLocale, charset);
	}
	
	
	/**
	 * Creates an temp CSV (comma separated) file containing the members info
	 * (namely with the columns specified in "columnList"), the groups info (for
	 * the filtered "groupList"), and separated in role sections: owners,
	 * participants and waiting.
	 * @param context
	 * @param columnList
	 * @param groupList
	 * @param archiveType
	 * @param userLocale
	 * @return the output file which could be an CSV or a zip file depending on the input archiveType.
	 * @see BGContext
	 * @see BusinessGroup
	 */
	public File archiveGroupMembers(BGContext context, List<String> columnList, List<BusinessGroup> groupList, String archiveType, UserRequest ureq) {
    
		List<Member> owners = new ArrayList<Member>();
		List<Member> participants = new ArrayList<Member>();
		List<Member> waitings = new ArrayList<Member>();
				
		List groups = BGContextManagerImpl.getInstance().getGroupsOfBGContext(context);
		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			BusinessGroup group = (BusinessGroup) iter.next();
			if (groupList.contains(group)) { //rely on the equals() method of the BusinessGroup impl			
				if(group.getOwnerGroup()!=null) {
				Iterator ownerIterator = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getOwnerGroup()).iterator();
				  addMembers(group.getKey(), ownerIterator, owners, OWNER);
				}
				if(group.getPartipiciantGroup()!=null) {
				  Iterator participantsIterator = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getPartipiciantGroup()).iterator();
				  addMembers(group.getKey(), participantsIterator, participants, PARTICIPANT);
				}
				if(group.getWaitingGroup()!=null) {
				  Iterator waitingIterator = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getWaitingGroup()).iterator();
				  addMembers(group.getKey(), waitingIterator, waitings, WAITING);
				}
			}
		}

		Locale userLocale = ureq.getLocale();
    String charset = UserManager.getInstance().getUserCharset(ureq.getIdentity());
		Translator trans = getPackageTranslator(userLocale);
		List<OrganisationalEntity> organisationalEntityList = getOrganisationalEntityList(groupList);
		return generateArchiveFile(context, owners, participants, waitings, columnList, organisationalEntityList, 
				trans.translate("archive.groups"), archiveType, userLocale, charset);
	}

	/**
	 * 
	 * @param context
	 * @return a List with the course titles associated with the input BGContext.
	 */
	private List<String> getCourseTitles(BGContext context) {
		List<String> courseTitles = new ArrayList<String>();
		List resources = BGContextManagerImpl.getInstance().findOLATResourcesForBGContext(context);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			OLATResource resource = (OLATResource) iter.next();
			if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
				ICourse course = CourseFactory.loadCourse(resource);
				courseTitles.add(course.getCourseTitle());
			}
			}
		return courseTitles;
		}

	private File generateArchiveFile(BGContext context, List<Member> owners, List<Member> participants, List<Member> waitings,
			List<String> columnList, List<OrganisationalEntity> organisationalEntityList, String orgEntityTitle, String archiveType,
			Locale userLocale, String charset) {
		//TODO: sort member lists
		File outFile = null;
		Translator trans = getPackageTranslator(userLocale);		
		String archiveTitle = trans.translate("archive.title") + ": " + DefaultContextTranslationHelper.translateIfDefaultContextName(context, trans);
		try {
		if (ALL_IN_ONE.equals(archiveType)) {
			//File tempDir = getTempDir();
			outFile = archiveAllInOne(context, owners, participants, waitings, archiveTitle, columnList, organisationalEntityList,
					orgEntityTitle, userLocale, ALL_IN_ONE_FILE_NAME_PREFIX, null, charset);
		} else if (FILE_PER_GROUP_OR_AREA_INCL_GROUP_MEMBERSHIP.equals(archiveType)) {
			outFile = archiveFilePerGroupInclGroupmembership(context, owners, participants, waitings, archiveTitle, columnList,
					organisationalEntityList, orgEntityTitle, userLocale, charset);
		} else if (FILE_PER_GROUP_OR_AREA.equals(archiveType)) {
			outFile = archiveFilePerGroup(context, owners, participants, waitings, columnList, organisationalEntityList,
					orgEntityTitle, userLocale, charset);
		}
		} catch (IOException e) {
			throw new OLATRuntimeException(BusinessGroupArchiver.class, "could not create temp file", e);
		}				
		return outFile;
	}
	
	/**
	 * Generates a single file for all groups. <br>
	 * It is the responsability of the caller to delete the returned file after
	 * download.
	 * 
	 * @param owners
	 * @param participants
	 * @param waitings
	 * @param columnList
	 * @param groupList
	 * @param userLocale
	 * @return the generated file located into the temp dir.
	 */
	private File archiveAllInOne(BGContext context,List<Member> owners, List<Member> participants, List<Member> waitings, String contextName,
			List<String> columnList, List<OrganisationalEntity> organisationalEntityList, String orgEntityTitle, Locale userLocale,
			String fileNamePrefix, File tempDir, String charset) throws IOException {
		File outFile = null;
		StringBuffer stringBuffer = new StringBuffer();

		Translator trans = getPackageTranslator(userLocale);		
		Translator propertyHandlerTranslator = UserManager.getInstance().getPropertyHandlerTranslator(trans);
		appendContextInfo(stringBuffer, context, userLocale);
		if (owners.size() > 0) {
			appendSection(stringBuffer, trans.translate("archive.header.owners"), owners, columnList, organisationalEntityList, orgEntityTitle,
					propertyHandlerTranslator, OWNER);
		}
		if (participants.size() > 0) {
			appendSection(stringBuffer, trans.translate("archive.header.partipiciant"), participants, columnList, organisationalEntityList,
					orgEntityTitle, propertyHandlerTranslator, PARTICIPANT);
		}
		if (waitings.size() > 0) {
			appendSection(stringBuffer, trans.translate("archive.header.waitinggroup"), waitings, columnList, organisationalEntityList,
					orgEntityTitle, propertyHandlerTranslator, WAITING);
		}		
		appendInternInfo(stringBuffer, contextName, userLocale);	
		//prefix must be at least 3 chars
		//add two of _ more if this is not the case
		fileNamePrefix = fileNamePrefix + "_";
		fileNamePrefix = fileNamePrefix.length() >= 3 ? fileNamePrefix : fileNamePrefix +"__"; 
		outFile = File.createTempFile(fileNamePrefix, ".xls", tempDir);
		FileUtils.save(outFile, stringBuffer.toString(), charset);
		//FileUtils.saveString(outFile, stringBuffer.toString());
		String outFileName = outFile.getName();
		outFileName = outFileName.substring(0, outFileName.lastIndexOf("_"));
		outFileName += ".xls";
		File renamedFile = new File(outFile.getParentFile(), outFileName);
		boolean succesfullyRenamed = outFile.renameTo(renamedFile);
		if (succesfullyRenamed) {
			outFile = renamedFile;
		}

		return outFile;
	}
	
	private void appendInternInfo(StringBuffer buf, String title, Locale userLocale) {
		Translator trans = getPackageTranslator(userLocale);	
		buf.append(EOL);
		buf.append(trans.translate("archive.interninfo"));
		buf.append(EOL);
		buf.append(title);
		buf.append(EOL);
	}
	
	/**
	 * 
	 * @return a temporary dir in the default temporary-file directory.
	 * @throws IOException
	 */
	private File getTempDir() throws IOException{
		//prefix must be at least 3 chars
		File tempDir = File.createTempFile("temp","archive");
		if(tempDir.delete()) {
			tempDir.mkdir();
		}
		return tempDir;
	}

	/**
	 * Generates a CSV file per group and then creates a zip with them.
	 * 
	 * @param owners
	 * @param participants
	 * @param waitings
	 * @param contextName
	 * @param columnList
	 * @param groupList
	 * @param userLocale
	 * @return the output zip file located into the temp dir.
	 */
	private File archiveFilePerGroupInclGroupmembership(BGContext context, List<Member> owners, List<Member> participants,
			List<Member> waitings, String contextName, List<String> columnList, List<OrganisationalEntity> groupList, String orgEntityTitle,
			Locale userLocale, String charset) {
		Set<String> outFiles = new HashSet<String>();
		File root = null;
		File tempDir = null;
		try {
			tempDir = getTempDir();
			Iterator<OrganisationalEntity> groupIterator = groupList.iterator();
			while (groupIterator.hasNext()) {
				OrganisationalEntity group = groupIterator.next();
				List<Member> groupOwners = getFilteredList(owners, group, this.OWNER);
				List<Member> groupParticipants = getFilteredList(participants, group, this.PARTICIPANT);
				List<Member> groupWaiting = getFilteredList(waitings, group, this.WAITING);

				File filePerGroup = archiveAllInOne(context, groupOwners, groupParticipants, groupWaiting, contextName, columnList, groupList,
						orgEntityTitle, userLocale, group.getName(), tempDir, charset);
				if (root == null && filePerGroup != null) {
					root = filePerGroup.getParentFile();
				}
				outFiles.add(filePerGroup.getName());
			}
			//prefix must be at least 3 chars
			File zipFile = File.createTempFile(ZIP_WITH_FILE_PER_GROUP_NAME_PREFIX, ".zip");
			zipFile.delete();
			boolean successfully = ZipUtil.zip(outFiles, root, zipFile, true);
			if (successfully) { return zipFile; }
		} catch (IOException e) {
			throw new OLATRuntimeException(BusinessGroupArchiver.class, "could not create temp file", e);
		} finally {
			if (tempDir != null) {
				FileUtils.deleteDirsAndFiles(tempDir, true, true);
			}
		}
		return null;
	}

	/**
	 * Generates a CSV file per group and then creates a zip with them.
	 *
	 * @param owners
	 * @param participants
	 * @param waitings
	 * @param contextName
	 * @param columnList
	 * @param groupList
	 * @param userLocale
	 * @return the output zip file located into the temp dir.
	 */
	private File archiveFilePerGroup(BGContext context, List<Member> owners, List<Member> participants,
			List<Member> waitings, List<String> columnList, List<OrganisationalEntity> groupList, String orgEntityTitle, Locale userLocale,
			String charset) {
		Set<String> outFiles = new HashSet<String>();
		File root = null;
		File tempDir = null;
		try {
			tempDir = getTempDir();
			Iterator<OrganisationalEntity> groupIterator = groupList.iterator();
			while (groupIterator.hasNext()) {
				OrganisationalEntity group = groupIterator.next();
				List<Member> groupOwners = getFilteredList(owners, group, this.OWNER);
				List<Member> groupParticipants = getFilteredList(participants, group, this.PARTICIPANT);
				List<Member> groupWaiting = getFilteredList(waitings, group, this.WAITING);

				File filePerGroup = archiveFileSingleGroup(context, groupOwners, groupParticipants, groupWaiting, columnList, groupList, orgEntityTitle,
						userLocale, group.getName(), tempDir, charset);
				if (root == null && filePerGroup != null) {
					root = filePerGroup.getParentFile();
				}
				outFiles.add(filePerGroup.getName());
			}
			//prefix must be at least 3 chars
			File zipFile = File.createTempFile(ZIP_WITH_FILE_PER_GROUP_NAME_PREFIX, ".zip");
			zipFile.delete();
			boolean successfully = ZipUtil.zip(outFiles, root, zipFile, true);
			if (successfully) { return zipFile; }
		} catch (IOException e) {
			throw new OLATRuntimeException(BusinessGroupArchiver.class, "could not create temp file", e);
		} finally {
			if (tempDir != null) {
				FileUtils.deleteDirsAndFiles(tempDir, true, true);
			}
		}
		return null;
	}

	/**
	 * Save one group to xls file.
	 * @param context
	 * @param groupOwners
	 * @param groupParticipants
	 * @param groupWaiting
	 * @param columnList
	 * @param organisationalEntityList
	 * @param orgEntityTitle
	 * @param userLocale
	 * @param fileNamePrefix
	 * @param tempDir
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	private File archiveFileSingleGroup(BGContext context, List<Member> groupOwners, List<Member> groupParticipants, List<Member> groupWaiting,
			List<String> columnList, List<OrganisationalEntity> organisationalEntityList, String orgEntityTitle, Locale userLocale, String fileNamePrefix,
			File tempDir, String charset) throws IOException {
		File outFile = null;
		StringBuffer stringBuffer = new StringBuffer();

		Translator trans = getPackageTranslator(userLocale);
		Translator propertyHandlerTranslator = UserManager.getInstance().getPropertyHandlerTranslator(trans);
		// choice element has only one selected entry
		List<String> titles = getCourseTitles (context);
		Iterator<String> titleIterator = titles.iterator();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, userLocale);
		String formattedDate = dateFormat.format(new Date());

		// coursename
		stringBuffer.append(EOL);
		stringBuffer.append(trans.translate("archive.coursename"));
		stringBuffer.append(EOL);
		while(titleIterator.hasNext()) {
			stringBuffer.append(titleIterator.next());
		}
		stringBuffer.append(EOL);
		stringBuffer.append(EOL);

		// groupname
		stringBuffer.append(trans.translate("group.name"));
		stringBuffer.append(EOL);
		stringBuffer.append(fileNamePrefix);
		stringBuffer.append(EOL);
		stringBuffer.append(EOL);

		// date
		stringBuffer.append(trans.translate("archive.date"));
		stringBuffer.append(EOL);
		stringBuffer.append(formattedDate);
		stringBuffer.append(EOL);

		// members
		if (groupOwners.size() > 0) {
			appendSection(stringBuffer, trans.translate("archive.header.owners"), groupOwners, columnList, new ArrayList<OrganisationalEntity>(), "",
					propertyHandlerTranslator, OWNER);
		}
		if (groupParticipants.size() > 0) {
			appendSection(stringBuffer, trans.translate("archive.header.partipiciant"), groupParticipants, columnList, new ArrayList<OrganisationalEntity>(),
					"", propertyHandlerTranslator, PARTICIPANT);
		}
		if (groupWaiting.size() > 0) {
			appendSection(stringBuffer, trans.translate("archive.header.waitinggroup"), groupWaiting, columnList, new ArrayList<OrganisationalEntity>(),
					"", propertyHandlerTranslator, WAITING);
		}
		//appendInternInfo(stringBuffer, contextName, userLocale);
		//prefix must be at least 3 chars
		//add two of _ more if this is not the case
		fileNamePrefix = fileNamePrefix + "_";
		fileNamePrefix = fileNamePrefix.length() >= 3 ? fileNamePrefix : fileNamePrefix +"__";
		fileNamePrefix = fileNamePrefix.replaceAll("[*?\"<>/\\\\:]","_"); // nicht erlaubte Zeichen in Dateinamen
		String[] search = new String[] { "ß", "ä", "ö", "ü","Ä","Ö","Ü"," " };
		String[] replace = new String[] { "ss", "ae", "oe", "ue","Ae","Oe","Ue","_" };
		for (int i = 0; i < search.length; i++) {
			fileNamePrefix = fileNamePrefix.replaceAll(search[i], replace[i]);
		}
		outFile = File.createTempFile(fileNamePrefix, ".xls", tempDir);
		FileUtils.save(outFile, stringBuffer.toString(), charset);
		//FileUtils.saveString(outFile, stringBuffer.toString());
		String outFileName = outFile.getName();
		outFileName = outFileName.substring(0, outFileName.lastIndexOf("_"));
		outFileName += ".xls";
		File renamedFile = new File(outFile.getParentFile(), outFileName);
		boolean succesfullyRenamed = outFile.renameTo(renamedFile);
		if (succesfullyRenamed) {
			outFile = renamedFile;
		}

		return outFile;
	}

	/**
	 * Filters the input "member" list, and returns only a sublist with the
	 * members of the input "group".
	 * 
	 * @param members
	 * @param group
	 * @param role
	 * @return the list with only the members of the input group.
	 */
	private List<Member> getFilteredList(List<Member> members, OrganisationalEntity group, String role) {
		List<Member> filteredList = new ArrayList<Member>();
		Iterator<Member> memberListIterator = members.iterator();
		while (memberListIterator.hasNext()) {
			Member currMember = memberListIterator.next();
			if (currMember.getOrganisationalEntityRoleList().contains(new OrganisationalEntityRole(group.getKey(), role))) {
				filteredList.add(currMember);
			}
		}
		return filteredList;
	}
	
	/**
	 * Wraps the identities from "identityIterator" into Members, and adds the
	 * members to the "members" list.
	 * 
	 * @param group
	 * @param memberIterator
	 * @param members
	 * @param roleName
	 */
	private void addMembers(Long entityKey, Iterator identityIterator, List<Member> members, String roleName) {
		while (identityIterator.hasNext()) {
			Object[] element = (Object[]) identityIterator.next();
			Identity identity = (Identity) element[0];
			OrganisationalEntityRole role = new OrganisationalEntityRole(entityKey, roleName);
			Member member = new Member(identity,new ArrayList());
			member.getOrganisationalEntityRoleList().add(role);
			if(!members.contains(member)) {
				members.add(member);
			} else {
				Iterator<Member> memberSetIterator = members.iterator();
				while(memberSetIterator.hasNext()) {
					Member currMember = memberSetIterator.next();
					if(currMember.equals(member)) {
						currMember.getOrganisationalEntityRoleList().add(role);
					}
				}
			}
					}
				}

	/**
	 * Appends course names and archive date.
	 * @param buf
	 * @param context
	 * @param userLocale
	 */
	private void appendContextInfo(StringBuffer buf, BGContext context, Locale userLocale) {
		List<String> titles = getCourseTitles (context);
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, userLocale);
		String formattedDate = dateFormat.format(new Date());
		Translator trans = getPackageTranslator(userLocale);
		buf.append(EOL);
		buf.append(trans.translate("archive.coursename"));
		buf.append(DELIMITER);
		buf.append(trans.translate("archive.date"));
		buf.append(EOL);
		
		Iterator<String> titleIterator = titles.iterator();
		int i=0;
		while(titleIterator.hasNext()) {
			buf.append(titleIterator.next());
			buf.append(DELIMITER);
			if(i<1) {
				buf.append(formattedDate);
			}
			buf.append(EOL);
			i++;
		}
	}
	
	/**
	 * Appends header labels to the input stringBuffer as follows: first the
	 * columnList items in that order and next the group list items.
	 * 
	 * @param buf
	 * @param title
	 * @param columList
	 * @param groupList
	 */
	private void appendHeader(StringBuffer buf, String title, List<String> columnList, List<OrganisationalEntity> organisationalEntityList,
			String orgEntityTitle, Translator trans) {
		buf.append(EOL);
		buf.append(title);
		int colSize = columnList.size();
		for (int i = 0; i < colSize; i++) {
			buf.append(DELIMITER);
		}
		buf.append(orgEntityTitle);
		buf.append(EOL);
		Iterator<String> columnIterator = columnList.iterator();
		while(columnIterator.hasNext()) {
			String columnKey = columnIterator.next();
			buf.append( trans.translate(columnKey));
			buf.append(DELIMITER);
		}
		
		Iterator<OrganisationalEntity> groupIterator = organisationalEntityList.iterator();
		while(groupIterator.hasNext()) {
			OrganisationalEntity group = groupIterator.next();
			buf.append(group.getName());
			buf.append(DELIMITER);
		}
		buf.append(EOL);		
	}
	
	/**
	 * Appends member info to the input stringBuffer.
	 * 
	 * @param buf
	 * @param member
	 * @param columnList
	 * @param groupList
	 * @param role
	 */
	private void appendMember(StringBuffer buf, Member member, List<String> columnList, List<OrganisationalEntity> groupList, String role) {
		if(columnList.contains("username")) {
		  buf.append(member.getIdentity().getName());	
		  buf.append(DELIMITER);
		}		

		// get selected user properties and append
		User user = member.getIdentity().getUser();
		for (String column : columnList) {
			String key = column.substring(column.lastIndexOf(".")+1);		
			if(!key.contains("username")) {
			  String value = user.getProperty(key, null); // use default locale			
		    buf.append((value == null ? "" : value));	
		    buf.append(DELIMITER);	
			}
		}
		
		List<OrganisationalEntityRole> groupRoleList = member.getOrganisationalEntityRoleList();
		Iterator<OrganisationalEntity> groupIterator = groupList.iterator();
		while(groupIterator.hasNext()) {
			OrganisationalEntity group = groupIterator.next();
			OrganisationalEntityRole groupRole = new OrganisationalEntityRole(group.getKey(), role);
			if(groupRoleList.contains(groupRole)) {
				buf.append("X"); 
			}
			buf.append(DELIMITER);
		}
		buf.append(EOL);
	}
	
	/**
	 * Appends the section header and next the members.
	 * 
	 * @param stringBuffer
	 * @param sectionTitle
	 * @param members
	 * @param columnList
	 * @param groupList
	 * @param trans
	 * @param role
	 */
	private void appendSection(StringBuffer stringBuffer, String sectionTitle, List<Member> members, List<String> columnList,
			List<OrganisationalEntity> organisationalEntityList, String orgEntityTitle, Translator trans, String role) {
		
		appendHeader(stringBuffer, sectionTitle, columnList, organisationalEntityList, orgEntityTitle, trans);
		Iterator<Member> memberIterator = members.iterator();
		while (memberIterator.hasNext()) {
			Member member = memberIterator.next();
			appendMember(stringBuffer, member, columnList, organisationalEntityList, role);
		}
	}
	
	/**
	 * Converts a list of items of a certain type (BusinessGroup,BGArea) in a list
	 * of OrganisationalEntitys.
	 * 
	 * @param itemList
	 * @return
	 */
	private List<OrganisationalEntity> getOrganisationalEntityList(List itemList) {
		List<OrganisationalEntity> entryList = new ArrayList<OrganisationalEntity>();
		Iterator itemIterator = itemList.iterator();
		while (itemIterator.hasNext()) {
			Object item = itemIterator.next();
			if (item instanceof BusinessGroup) {
				BusinessGroup group = (BusinessGroup) item;
				entryList.add(new OrganisationalEntity(group.getKey(), group.getName()));
			} else if (item instanceof BGArea) {
				BGArea area = (BGArea) item;
				entryList.add(new OrganisationalEntity(area.getKey(), area.getName()));
			}
		}
		return entryList;
	}

	/**
	 * Description:<br>
	 * An organisational entity is a Group or an Area.
	 * Encapsulates the entityKey and the role in the group.
	 * <P>
	 * Initial Date:  26.07.2007 <br>
	 * 
	 * @author Lavinia Dumitrescu
	 */
	private class OrganisationalEntityRole {
		private Long entityKey;
		private String roleInGroup;
		
		public OrganisationalEntityRole(Long entityKey, String roleInGroup) {
			super();
			this.entityKey = entityKey;
			this.roleInGroup = roleInGroup;
		}

		public String getRoleInGroup() {
			return roleInGroup;
		}

		public void setRoleInGroup(String roleInGroup) {
			this.roleInGroup = roleInGroup;
		}

		public Long getEntityKey() {
			return entityKey;
		}

		public void setEntityKey(Long groupKey) {
			this.entityKey = groupKey;
		}
		
		public boolean equals(Object obj) {
			OrganisationalEntityRole that = (OrganisationalEntityRole) obj;
			return this.entityKey.equals(that.getEntityKey()) && this.getRoleInGroup().equals(that.getRoleInGroup());
		}
		
		public int hashCode() {
			return getEntityKey().intValue() + getRoleInGroup().hashCode();
		}
		
	}
	
	/**
	 * Description:<br>
	 * Encapsulates an <code>Identity</code> and a list of <code>OrganisationalEntityRole</code> of the <code>Identity</code>.
	 * <P>
	 * Initial Date:  26.07.2007 <br>
	 * 
	 * @author Lavinia Dumitrescu
	 */
	private class Member {
		private Identity identity;
		private List<OrganisationalEntityRole> organisationalEntityRoleList;
		
		public Member(Identity identity, List<OrganisationalEntityRole> groupRoleList) {
			super();
			this.identity = identity;
			this.organisationalEntityRoleList = groupRoleList;
		}
		
		public List<OrganisationalEntityRole> getOrganisationalEntityRoleList() {
			return organisationalEntityRoleList;
		}

		public void setOrganisationalEntityRoleList(List<OrganisationalEntityRole> groupRoleList) {
			this.organisationalEntityRoleList = groupRoleList;
		}
		public Identity getIdentity() {
			return identity;
		}
		public void setIdentity(Identity identity) {
			this.identity = identity;
		}
		
		/**
		 * Compares the identity of the members.
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			try {
				Member groupMember = (Member)obj;
				return this.identity.equals(groupMember.identity);
			} catch(Exception ex) {	
				//nothing to do
			}
			return false;
		}
		
		public int hashCode() {
			return this.identity.hashCode();
		}
		}

	private class OrganisationalEntity {
		private Long key;
		private String name;

		public OrganisationalEntity(Long key, String name) {
			super();
			this.key = key;
			this.name = name;
	}


		public Long getKey() {
			return key;
		}

		public void setKey(Long key) {
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}