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
package org.olat.ldap.manager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.ldap.LDAPConstants;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.LDAPSyncConfiguration;
import org.olat.ldap.model.LDAPGroup;
import org.olat.ldap.model.LDAPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.fortuna.ical4j.util.TimeZones;

/**
 * 
 * Initial date: 24.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LDAPDAO {
	
	private static final Logger log = Tracing.createLoggerFor(LDAPDAO.class);
	
	private static final int DEFAULT_PAGE_SIZE = 50;
	private static final TimeZone UTC_TIME_ZONE;
	private static final String PAGED_RESULT_CONTROL_OID = "1.2.840.113556.1.4.319";
	
	static {
		UTC_TIME_ZONE = TimeZone.getTimeZone(TimeZones.UTC_ID);
	}
	
	private boolean pagingSupportedAlreadyFound = false;
	
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private LDAPSyncConfiguration syncConfiguration;
	
	/**
	 * Search in the list of specified bases with the attributes cn and member.
	 * 
	 * @param ctx The LDAP context
	 * @param groupDNs A list of bases
	 * @param filter An optional filter
	 * @return
	 */
	public List<LDAPGroup> searchGroupsWithMembers(LdapContext ctx, List<String> groupDNs, String filter) {
		List<LDAPGroup> ldapGroups = new ArrayList<>();
		String[] groupAttributes = new String[]{"cn", "member"};
		for(String groupDN:groupDNs) {
			LDAPGroupVisitor visitor = new LDAPGroupVisitor();
			search(visitor, groupDN, filter, groupAttributes, ctx);
			ldapGroups.addAll(visitor.getGroups());
		}
		return ldapGroups;
	}
	
	public List<LDAPGroup> searchGroups(LdapContext ctx, List<String> groupDNs, String filter) {
		final List<LDAPGroup> ldapGroups = new ArrayList<>();
		String[] groupAttributes = new String[]{ "cn" };
		filter = escapeFilter(filter);
		for(String groupDN:groupDNs) {
			LDAPVisitor visitor = new LDAPVisitor() {
				@Override
				public void visit(SearchResult searchResult) throws NamingException {
					Attributes resAttributes = searchResult.getAttributes();
					Attribute cnAttr = resAttributes.get("cn");

					Object cn = cnAttr.get();
					if(cn instanceof String cns) {
						LDAPGroup group = new LDAPGroup();
						group.setCommonName(cns);
						ldapGroups.add(group);
					}
				}
				
			};
			search(visitor, groupDN, filter, groupAttributes, ctx);
		}
		return ldapGroups;
	}
	
	public void search(LDAPVisitor visitor, String ldapBase, String filter, String[] returningAttrs, LdapContext ctx) {
		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(returningAttrs);
		ctls.setCountLimit(0); // set no limits
		
		final int pageSize = pageSize();
		final boolean paging = isPagedResultControlSupported(ctx);
		filter = escapeFilter(filter);
	
		int counter = 0;
		try {
			if(paging) {
				byte[] cookie = null;
				ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.NONCRITICAL) });
				do {
					NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
					while (enm.hasMore()) {
						visitor.visit(enm.next());
					}
					cookie = getCookie(ctx, pageSize);
					counter++;
				} while (cookie != null);
			} else {
				ctx.setRequestControls(null); // reset on failure, see FXOLAT-299
				NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
				while (enm.hasMore()) {
					visitor.visit(enm.next());
				}
				counter++;
			}
		} catch (SizeLimitExceededException e) {
			log.error("SizeLimitExceededException after {} records when getting all users from LDAP, reconfigure your LDAP server, hints: http://www.ldapbrowser.com/forum/viewtopic.php?t=14", counter);
		} catch (NameNotFoundException e) {
			log.warn("Name not found: {} in base: {}", filter, ldapBase);
		} catch (NamingException e) {
			log.error("NamingException when trying to search from LDAP using ldapBase::{} on row::{}", ldapBase,counter, e);
		} catch (Exception e) {
			log.error("Exception when trying to search from LDAP using ldapBase::{} on row::{}", ldapBase, counter, e);
		}
		log.debug("finished search for ldapBase:: {}", ldapBase);
	}
	
	
	public void searchInLdap(LDAPVisitor visitor, String filter, String[] returningAttrs, LdapContext ctx) {
		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(returningAttrs);
		ctls.setCountLimit(0); // set no limits

		filter = escapeFilter(filter);
		final int pageSize = pageSize();
		final boolean paging = isPagedResultControlSupported(ctx);
		for (String ldapBase : syncConfiguration.getLdapBases()) {
			int counter = 0;
			try {
				if(paging) {
					byte[] cookie = null;
					ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.NONCRITICAL) });
					do {
						NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
						while (enm.hasMore()) {
							visitor.visit(enm.next());
							counter++;
						}
						cookie = getCookie(ctx, pageSize);
					} while (cookie != null);
				} else {
					ctx.setRequestControls(null); // reset on failure, see FXOLAT-299
					NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
					while (enm.hasMore()) {
						visitor.visit(enm.next());
						counter++;
					}
				}
			} catch (SizeLimitExceededException e) {
				log.error("SizeLimitExceededException after {} records when getting all users from LDAP, reconfigure your LDAP server, hints: http://www.ldapbrowser.com/forum/viewtopic.php?t=14", counter, e);
			} catch (NamingException e) {
				log.error("NamingException when trying to search users from LDAP using ldapBase::{} on row::{}", ldapBase, counter, e);
			} catch (Exception e) {
				log.error("Exception when trying to search users from LDAP using ldapBase::{} on row::{}", ldapBase, counter, e);
			}
			log.debug("finished search for ldapBase:: {}", ldapBase);
		}
	}
	
	public String searchUserForLogin(String login, DirContext ctx) {
		if(ctx == null) return null;
		
		List<String> ldapUserIDAttributes = syncConfiguration.getLdapUserLoginAttributes();
		String filter = buildSearchUserFilter(ldapUserIDAttributes, login);
		return searchUserDN(login, filter, ctx);
	}
	
	public String searchUserDNByUid(String uid, DirContext ctx) {
		if(ctx == null) return null;
		
		String ldapUserIDAttribute = syncConfiguration.getOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
		String filter = buildSearchUserFilter(ldapUserIDAttribute, uid);
		return searchUserDN(uid, filter, ctx);
	}
	
	private String searchUserDN(String username, String filter, DirContext ctx) {
		
		List<String> ldapBases = syncConfiguration.getLdapBases();
		String[] serachAttr = { "dn" };
		filter = escapeFilter(filter);
		
		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(serachAttr);

		String userDN = null;
		for (String ldapBase : ldapBases) {
			try {
				NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
				while (enm.hasMore()) {
					SearchResult result = enm.next();
					userDN = result.getNameInNamespace();
				}
				if (userDN != null) {
					break;
				}
			} catch (NamingException e) {
				log.error("NamingException when trying to bind user with username::{} on ldapBase::{}", username, ldapBase, e);
			}
		}
		
		return userDN;
	}
	
	public LDAPUser searchLDAPUserByUid(String username, DirContext ctx) {
		if(ctx == null) return null;
		
		String ldapUserIDAttribute = syncConfiguration.getOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
		String filter = buildSearchUserFilter(ldapUserIDAttribute, username);
		filter = escapeFilter(filter);

		List<String> ldapBases = syncConfiguration.getLdapBases();

		List<String> searchAttr = new ArrayList<>();
		searchAttr.add("dn");
		Collections.addAll(searchAttr, getEnhancedUserAttributes());

		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(searchAttr.toArray(new String[ searchAttr.size() ]));

		LDAPUserVisitor visitor = new LDAPUserVisitor(syncConfiguration);
		for (String ldapBase : ldapBases) {
			try {
				NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
				while (enm.hasMore()) {
					SearchResult result = enm.next();
					visitor.visit(result);
				}
				if(!visitor.getLdapUserList().isEmpty()) {
					break;
				}
			} catch (NamingException e) {
				log.error("NamingException when trying to bind user with username::{} on ldapBase::{}", username, ldapBase, e);
			}
		}
		
		List<LDAPUser> users = visitor.getLdapUserList();
		return users.isEmpty() ? null : users.get(0);
	}
	
	protected static List<String> parseGroupList(Attributes resAttribs, String attributeName, String attributeSeparator) {
		List<String> groupList = List.of();
		if(StringHelper.containsNonWhitespace(attributeName)) {
			try {
				Attribute groupAttr = resAttribs.get(attributeName);
				if(groupAttr != null && groupAttr.get() instanceof String) {
					String groupString = (String)groupAttr.get();
					if(!"-".equals(groupString)) {
						String[] groupArr = groupString.split(attributeSeparator);
						groupList = new ArrayList<>(groupArr.length);
						for(String group:groupArr) {
							groupList.add(group);
						}
					}
				}
			} catch (Exception e) {
				log.error("");
			}
		}
		return groupList;
	}
	
	protected static boolean hasAttributeValue(Attributes resAttribs, String attribute, String value) {
		boolean hasAttributeValue = false;
		if(StringHelper.containsNonWhitespace(attribute) && StringHelper.containsNonWhitespace(value)) {
			try {
				Attribute attr = resAttribs.get(attribute);
				if(attr != null) {
					if(attr.size() == 1) {
						if(value.equals(attr.get())) {
							hasAttributeValue = true;
						}
					} else if(attr.size() > 1) {
						for(NamingEnumeration<?> valuEnum=attr.getAll(); valuEnum.hasMoreElements(); ) {
							Object nextValue = valuEnum.next();
							if(value.equals(nextValue)) {
								hasAttributeValue = true;
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("");
			}
		}
		return hasAttributeValue;
	}
	
	protected String buildSearchUserFilter(String attribute, String uid) {
		String ldapUserFilter = syncConfiguration.getLdapUserFilter();
		StringBuilder filter = new StringBuilder();
		if (ldapUserFilter != null) {
			// merge preconfigured filter (e.g. object class, group filters) with username using AND rule
			filter.append("(&").append(ldapUserFilter);	
		}
		filter.append("(").append(attribute).append("=").append(uid).append(")");
		if (ldapUserFilter != null) {
			filter.append(")");	
		}
		return filter.toString();
	}
	
	protected String buildSearchUserFilter(List<String> attributes, String uid) {
		if(attributes == null || attributes.isEmpty()) {
			return null;
		} else if(attributes.size() == 1) {
			return buildSearchUserFilter(attributes.get(0), uid);
		}
		
		String ldapUserFilter = syncConfiguration.getLdapUserFilter();
		StringBuilder filter = new StringBuilder(64);
		if (ldapUserFilter != null) {
			// merge preconfigured filter (e.g. object class, group filters) with username using AND rule
			filter.append("(&").append(ldapUserFilter);	
		}
		
		filter.append("(|");
		for(int i=0; i<attributes.size(); i++) {
			String attribute = attributes.get(i);
			filter.append("(").append(attribute).append("=").append(uid).append(")");
			
		}
		filter.append(")");
		
		if (ldapUserFilter != null) {
			filter.append(")");	
		}
		return filter.toString();
	}
	
	/**
	 * 
	 * Creates list of all LDAP Users or changed Users since syncTime
	 * 
	 * Configuration: userAttr = ldapContext.xml (property=userAttrs) LDAP Base =
	 * ldapContext.xml (property=ldapBase)
	 * 
	 * 
	 * 
	 * @param syncTime The time to search in LDAP for changes since this time.
	 *          SyncTime has to formatted: JJJJMMddHHmm
	 * @param ctx The LDAP system connection, if NULL or closed NamingExecpiton is
	 *          thrown
	 * 
	 * @return Returns list of Arguments of found users or empty list if search
	 *         fails or nothing is changed
	 * 
	 * @throws NamingException
	 */

	public List<LDAPUser> getUserAttributesModifiedSince(Date syncTime, LdapContext ctx) {
		final boolean debug = log.isDebugEnabled();
		String userFilter = syncConfiguration.getLdapUserFilter();
		StringBuilder filter = new StringBuilder();
		if (syncTime == null) {
			if(debug)  log.debug("LDAP get user attribs since never -> full sync!");
			if (userFilter != null) {
				filter.append(userFilter);				
			}
		} else {
			String dateFormat = ldapLoginModule.getLdapDateFormat();
			SimpleDateFormat generalizedTimeFormatter = new SimpleDateFormat(dateFormat);
			generalizedTimeFormatter.setTimeZone(UTC_TIME_ZONE);
			String syncTimeForm = generalizedTimeFormatter.format(syncTime);
			if(debug) log.debug("LDAP get user attribs since {} -> means search with date restriction-filter: {}", syncTime, syncTimeForm);
			if (userFilter != null) {
				// merge user filter with time fileter using and rule
				filter.append("(&").append(userFilter);				
			}
			filter.append("(|(");								
			filter.append(syncConfiguration.getLdapUserLastModifiedTimestampAttribute()).append(">=").append(syncTimeForm);
			filter.append(")(");
			filter.append(syncConfiguration.getLdapUserCreatedTimestampAttribute()).append(">=").append(syncTimeForm);
			filter.append("))");
			if (userFilter != null) {
				filter.append(")");				
			}
		}

		String[] userAttrs = getEnhancedUserAttributes();
		LDAPUserVisitor userVisitor = new LDAPUserVisitor(syncConfiguration);
		long start = System.nanoTime();
		log.info("Start loading users from LDAP server");
		searchInLdap(userVisitor, filter.toString(), userAttrs, ctx);
		List<LDAPUser> ldapUserList = userVisitor.getLdapUserList();
		if(debug) {
			log.debug("attrib search returned {} results", ldapUserList.size());
		}
		log.info("{} LDAP users retrieved in {}ms", ldapUserList.size(), CodeHelper.nanoToMilliTime(start));
		return ldapUserList;
	}
	
	public String[] getEnhancedUserAttributes() {
		String[] userAttrs = syncConfiguration.getUserAttributes();
		
		List<String> userAttrList = new ArrayList<>(userAttrs.length + 7);
		for(String userAttr:userAttrs) {
			userAttrList.add(userAttr);
		}
		if(StringHelper.containsNonWhitespace(syncConfiguration.getCoachRoleAttribute())) {
			userAttrList.add(syncConfiguration.getCoachRoleAttribute());
		}
		if(StringHelper.containsNonWhitespace(syncConfiguration.getAuthorRoleAttribute())) {
			userAttrList.add(syncConfiguration.getAuthorRoleAttribute());
		}
		if(StringHelper.containsNonWhitespace(syncConfiguration.getUserManagerRoleAttribute())) {
			userAttrList.add(syncConfiguration.getUserManagerRoleAttribute());
		}
		if(StringHelper.containsNonWhitespace(syncConfiguration.getGroupManagerRoleAttribute())) {
			userAttrList.add(syncConfiguration.getGroupManagerRoleAttribute());
		}
		if(StringHelper.containsNonWhitespace(syncConfiguration.getQpoolManagerRoleAttribute())) {
			userAttrList.add(syncConfiguration.getQpoolManagerRoleAttribute());
		}
		if(StringHelper.containsNonWhitespace(syncConfiguration.getLearningResourceManagerRoleAttribute())) {
			userAttrList.add(syncConfiguration.getLearningResourceManagerRoleAttribute());
		}
		if(StringHelper.containsNonWhitespace(syncConfiguration.getGroupAttribute())) {
			userAttrList.add(syncConfiguration.getGroupAttribute());
		}
		if(StringHelper.containsNonWhitespace(syncConfiguration.getCoachedGroupAttribute())) {
			userAttrList.add(syncConfiguration.getCoachedGroupAttribute());
		}
		return userAttrList.toArray(new String[userAttrList.size()]);
	}
	
	private byte[] getCookie(LdapContext ctx, int pageSize) throws NamingException, IOException {
		byte[] cookie = null;
		// Examine the paged results control response
		Control[] controls = ctx.getResponseControls();
		if (controls != null) {
		  for (int i = 0; i < controls.length; i++) {
		    if (controls[i] instanceof PagedResultsResponseControl prrc) {
		      cookie = prrc.getCookie();
		    }
		  }
		}
		// Re-activate paged results
		ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.NONCRITICAL) });
		return cookie;
	}
	
	private boolean isPagedResultControlSupported(LdapContext ctx) {
		// FXOLAT-299, might return false on 2nd execution
		if (pagingSupportedAlreadyFound) return true;
		try {
			SearchControls ctl = new SearchControls();
			ctl.setReturningAttributes(new String[]{"supportedControl"});
			ctl.setSearchScope(SearchControls.OBJECT_SCOPE);

			/* search for the rootDSE object */
			NamingEnumeration<SearchResult> results = ctx.search("", "(objectClass=*)", ctl);

			while(results.hasMore()){
				SearchResult entry = results.next();
				NamingEnumeration<? extends Attribute> attrs = entry.getAttributes().getAll();
				while (attrs.hasMore()){
					Attribute attr = attrs.next();
					NamingEnumeration<?> vals = attr.getAll();
					while (vals.hasMore()){
						String value = (String) vals.next();
						if(value.equals(PAGED_RESULT_CONTROL_OID))
							pagingSupportedAlreadyFound = true;
							return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			log.error("Exception when trying to know if the server support paged results.", e);
			return false;
		}
	}
	
	public static String escapeFilter(String filter) {
		if(StringHelper.containsNonWhitespace(filter)) {
			filter = filter.replace("\\,", "\\5c,");
		}
		return filter;
	}

	private int pageSize() {
		Integer  pageSize = ldapLoginModule.getBatchSize();
		return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize.intValue();
	}
}
