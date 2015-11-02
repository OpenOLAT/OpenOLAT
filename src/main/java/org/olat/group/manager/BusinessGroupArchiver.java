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

package org.olat.group.manager;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nModule;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.BGControllerFactory;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * @author Christian Guretzki
 */
public class BusinessGroupArchiver {

	private static final String DELIMITER = "\t";
	private static final String EOL = "\n";

	private final DB dbInstance;
	private final UserManager userManager;
	
	protected BusinessGroupArchiver(DB dbInstance) {
		this.dbInstance = dbInstance;
		this.userManager = UserManager.getInstance();
	}

	/**
	 * Retrives a PackageTranslator for the input locale.
	 * 
	 * @param locale
	 * @return
	 */
	protected Translator getPackageTranslator(Locale locale) {
		Translator fallBacktranslator1 =  Util.createPackageTranslator(GroupController.class, locale);
		Translator fallBacktranslator2 = Util.createPackageTranslator(BGControllerFactory.class, locale, fallBacktranslator1);
		Translator translator = userManager.getPropertyHandlerTranslator(fallBacktranslator2);
		return translator;
	}
	
	//get user property handlers used in this group archiver
	private List<UserPropertyHandler> getUserPropertyHandlers() {
		return userManager.getUserPropertyHandlersFor("org.olat.group.BusinessGroupArchiver", true);
	}
	
	public void archiveGroups(List<BusinessGroup> groups, File archiveFile) {
		FileUtils.save(archiveFile, toXls(groups), "utf-8");		
	}

	private String toXls(BusinessGroup businessGroup, Translator translator) {
		StringBuffer buf = new StringBuffer();
		// Export Header
		buf.append(translator.translate("archive.group.name"));
		buf.append(DELIMITER);
		buf.append(businessGroup.getName());
		buf.append(DELIMITER);
		buf.append(translator.translate("archive.group.type"));
		buf.append(DELIMITER);
		buf.append("LearningGroup");
		buf.append(DELIMITER);
		buf.append(translator.translate("archive.group.description"));
		buf.append(DELIMITER);
		buf.append(FilterFactory.getHtmlTagsFilter().filter(businessGroup.getDescription()));
		buf.append(EOL);
		
		List<GroupMembership> memberships = getMembers(businessGroup);
		
		appendIdentityTable(buf, memberships, GroupRoles.coach.name(), translator.translate("archive.header.owners"), translator);
		appendIdentityTable(buf, memberships, GroupRoles.participant.name(), translator.translate("archive.header.partipiciant"), translator);
		
		if (businessGroup.getWaitingListEnabled() ) {
			appendIdentityTable(buf, memberships, GroupRoles.waiting.name(), translator.translate("archive.header.waitinggroup"), translator);
		}
		return buf.toString();
	}

	private void appendIdentityTable(StringBuffer buf, List<GroupMembership> members, String role, String title, Translator translator) {

		appendTitle(buf, title);
		appendIdentityTableHeader(buf, translator);
		for (GroupMembership membership:members) {
			if(role.equals(membership.getRole())) {
				Identity identity = membership.getIdentity();
				Date addedTo = membership.getCreationDate();
				appendIdentity(buf, identity, addedTo, translator);
			}
		}
	}
	
	public List<GroupMembership> getMembers(BusinessGroup group) {
		StringBuilder sb = new StringBuilder();
		sb.append("select membership from businessgroup as bgroup ")
		  .append(" inner join bgroup.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where bgroup.key=:businessGroupKey");

		List<GroupMembership> members = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), GroupMembership.class)
				.setParameter("businessGroupKey", group.getKey())
				.getResultList();
		return members;
	}

	private void appendTitle(StringBuffer buf, String title) {
		buf.append(EOL);
		buf.append(title);
		buf.append(EOL);
	}

	private void appendIdentity(StringBuffer buf, Identity owner, Date addedTo, Translator translator) {
		Locale loc = translator.getLocale();
		// add the identities user name

		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(owner);
		String uname = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
		buf.append(uname);
		buf.append(DELIMITER);
		// add all user properties
		for (UserPropertyHandler propertyHandler : getUserPropertyHandlers()) {
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

	private void appendIdentityTableHeader(StringBuffer buf, Translator translator) {
		// first the identites name
		buf.append( translator.translate("table.user.url") );
		buf.append(DELIMITER);
		// second the users properties
		for (UserPropertyHandler propertyHandler : getUserPropertyHandlers()) {
			String label = translator.translate(propertyHandler.i18nColumnDescriptorLabelKey());
			buf.append(label);
			buf.append(DELIMITER);
		}
		// third the users added-to date
		buf.append( translator.translate("table.subject.addeddate") );
		buf.append(EOL);
	}

	private String toXls(List<BusinessGroup> groups) {
		Translator translator = getPackageTranslator(I18nModule.getDefaultLocale());
		StringBuffer buf = new StringBuffer();
		// Export Context Header
		buf.append(translator.translate("archive.group.context.name"));
		buf.append(DELIMITER);
		buf.append("");
		buf.append(DELIMITER);
		buf.append(translator.translate("archive.group.context.type"));
		buf.append(DELIMITER);
		buf.append("All");
		buf.append(DELIMITER);
		buf.append(translator.translate("archive.group.context.description"));
		buf.append(DELIMITER);
		buf.append(FilterFactory.getHtmlTagsFilter().filter("Description"));
		buf.append(EOL);

		for (BusinessGroup group : groups) {
			buf.append(toXls(group, translator));
			buf.append(EOL);
			buf.append(EOL);
		}
		return buf.toString();
	}
}