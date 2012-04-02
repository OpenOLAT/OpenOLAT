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

package org.olat.group.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.area.BGAreaManagerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * Description: <BR>
 * Implementation of the business group context manager.
 * <P>
 * Initial Date: Aug 19, 2004 <br>
 * 
 * @author gnaegi
 */
public class BGContextManagerImpl extends BasicManager implements BGContextManager {

	private static BGContextManager INSTANCE;
	static {
		INSTANCE = new BGContextManagerImpl();
	}

	/**
	 * @return singleton instance
	 */
	public static BGContextManager getInstance() {
		return INSTANCE;
	}

	private BGContextManagerImpl() {
	// no public constructor
	}

	/**
	 * @see org.olat.group.context.BGContextManager#createAndPersistBGContext(java.lang.String,
	 *      java.lang.String, java.lang.String, org.olat.core.id.Identity,
	 *      boolean)
	 */
	public BGContext createAndPersistBGContext(String name, String description, String groupType, Identity owner, boolean defaultContext) {
		if (name == null) throw new AssertException("Business group context name must not be null");
		if (groupType == null) throw new AssertException("Business group groupType name must not be null");
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		// 1) create administrative owner security group, add owner if available
		SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
		if (owner != null) {
			securityManager.addIdentityToSecurityGroup(owner, ownerGroup);
		}
		// 2) create new group context with this security group and save it
		BGContext bgContext = new BGContextImpl(name, description, ownerGroup, groupType, defaultContext);
		DBFactory.getInstance().saveObject(bgContext);
		// 3) save context owner policy to this context and the owner group
		securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, bgContext);
		// 4) save groupmanager policy on this group - all members are automatically
		// group managers
		securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GROUPMANAGER);
		Tracing.logAudit("Created Business Group Context", bgContext.toString(), this.getClass());
		return bgContext;
	}

	/**
	 * @see org.olat.group.context.BGContextManager#copyAndAddBGContextToResource(java.lang.String,
	 *      org.olat.resource.OLATResource, org.olat.group.context.BGContext)
	 */
	public BGContext copyAndAddBGContextToResource(String contextName, OLATResource resource, BGContext originalBgContext) {
		BGAreaManager areaManager = BGAreaManagerImpl.getInstance();
		BusinessGroupManager groupManager = BusinessGroupManagerImpl.getInstance();
		if (!originalBgContext.isDefaultContext()) { throw new AssertException("Can only copy default contexts"); }

		// 1. Copy context as default context. Owner group of original context will
		// not be
		// copied since this is a default context
		BGContext targetContext = createAndAddBGContextToResource(contextName, resource, originalBgContext.getGroupType(), null, true);
		// 2. Copy areas
		Map areas = areaManager.copyBGAreasOfBGContext(originalBgContext, targetContext);
		// 3. Copy Groups
		// only group configuration will be copied, no group members are copied
		List origGroups = getGroupsOfBGContext(originalBgContext);
		Iterator iter = origGroups.iterator();
		while (iter.hasNext()) {
			BusinessGroup origGroup = (BusinessGroup) iter.next();
			groupManager.copyBusinessGroup(origGroup, origGroup.getName(), origGroup.getDescription(), origGroup.getMinParticipants(), origGroup
					.getMaxParticipants(), targetContext, areas, true, true, true, false, false, true, false); 
		}
		return targetContext;
	}

	/**
	 * @see org.olat.group.context.BGContextManager#updateBGContext(org.olat.group.context.BGContext)
	 */
	public void updateBGContext(BGContext bgContext) {
		// 1) update context
		DBFactory.getInstance().updateObject(bgContext);
		// 2) reload course contexts for all courses wher this context is used
		List resources = findOLATResourcesForBGContext(bgContext);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			OLATResource resource = (OLATResource) iter.next();
			if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
				ICourse course = CourseFactory.loadCourse(resource);
				course.getCourseEnvironment().getCourseGroupManager().initGroupContextsList();
			} else if (resource.getResourceableTypeName().equals("junitcourse")) {
				// do nothing when in junit test mode
			} else {
				throw new AssertException("Currently only course resources allowed in resource to context relations.");
			}
		}
	}

	/**
	 * @see org.olat.group.context.BGContextManager#deleteBGContext(org.olat.group.context.BGContext)
	 */
	public void deleteBGContext(BGContext bgContext) {
		bgContext = (BGContext) DBFactory.getInstance().loadObject(bgContext);
		BusinessGroupManager bgManager = BusinessGroupManagerImpl.getInstance();
		BGAreaManager areaManager = BGAreaManagerImpl.getInstance();
		// 1) Delete all groups from group context
		List groups = getGroupsOfBGContext(bgContext);
		bgManager.deleteBusinessGroups(groups);
		// 2) Delete all group areas
		List areas = areaManager.findBGAreasOfBGContext(bgContext);
		for (Iterator iter = areas.iterator(); iter.hasNext();) {
			BGArea area = (BGArea) iter.next();
			areaManager.deleteBGArea(area);
		}
		// 3) Delete group to resource relations
		List referencingResources = findOLATResourcesForBGContext(bgContext);
		for (Iterator iter = referencingResources.iterator(); iter.hasNext();) {
			OLATResource refRes = (OLATResource) iter.next();
			removeBGContextFromResource(bgContext, refRes);
		}
		// 4) Delete group context
		DBFactory.getInstance().deleteObject(bgContext);
		// 5) Delete security group
		SecurityGroup owners = bgContext.getOwnerGroup();
		if (owners != null) {
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			secMgr.deleteSecurityGroup(owners);
		}
		Tracing.logAudit("Deleted Business Group Context", bgContext.toString(), this.getClass());
	}

	/**
	 * @see org.olat.group.context.BGContextManager#getGroupsOfBGContext(org.olat.group.context.BGContext)
	 */
	@Override
	public List<BusinessGroup> getGroupsOfBGContext(BGContext bgContext) {
		DB db = DBFactory.getInstance();
		DBQuery query;
		if (bgContext == null) {
			String q = "select bg from org.olat.group.BusinessGroupImpl bg where bg.groupContext is null";
			query = db.createQuery(q);
		} else {
			String q = "select bg from org.olat.group.BusinessGroupImpl bg where bg.groupContext = :context";
			query = db.createQuery(q);
			query.setEntity("context", bgContext);
		}
		return (List<BusinessGroup>) query.list();
	}
	
	@Override
	public List<BusinessGroup> getGroupsOfBGContext(Collection<BGContext> bgContexts, int firstResult, int maxResults) {
		if(bgContexts == null || bgContexts.isEmpty()) {
			return Collections.emptyList();
		}
		
		DB db = DBFactory.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("select bg from ").append(BusinessGroupImpl.class.getName()).append(" bg")
		  .append("  where bg.groupContext.key in (:contextKeys)");
			
		DBQuery query = db.createQuery(sb.toString());
		List<Long> contextKeys = new ArrayList<Long>(bgContexts.size());
		for(BGContext bgContext:bgContexts) {
			contextKeys.add(bgContext.getKey());
		}
		query.setParameterList("contextKeys", contextKeys);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.list();
	}

	/**
	 * @see org.olat.group.context.BGContextManager#countGroupsOfBGContext(org.olat.group.context.BGContext)
	 */
	public int countGroupsOfBGContext(BGContext bgContext) {
		DB db = DBFactory.getInstance();
		String q = "select count(bg) from org.olat.group.BusinessGroupImpl bg where bg.groupContext = :context";
		DBQuery query = db.createQuery(q);
		query.setEntity("context", bgContext);
		return ((Long) query.list().get(0)).intValue();
	}

	/**
	 * @see org.olat.group.context.BGContextManager#countGroupsOfType(java.lang.String)
	 */
	public int countGroupsOfType(String groupType) {
		DB db = DBFactory.getInstance();
		String q = "select count(bg) from org.olat.group.BusinessGroupImpl bg where bg.type = :type";
		DBQuery query = db.createQuery(q);
		query.setString("type", groupType);
		return ((Long) query.list().get(0)).intValue();
	}

	/**
	 * @see org.olat.group.context.BGContextManager#findGroupOfBGContext(java.lang.String,
	 *      org.olat.group.context.BGContext)
	 */
	public BusinessGroup findGroupOfBGContext(String groupName, BGContext bgContext) {
		DB db = DBFactory.getInstance();
		String q = "select bg from org.olat.group.BusinessGroupImpl bg where bg.groupContext = :context and bg.name = :name";
		DBQuery query = db.createQuery(q);
		query.setEntity("context", bgContext);
		query.setString("name", groupName);
		List results = query.list();
		if (results.size() == 0) return null;
		return (BusinessGroup) results.get(0);
	}

	/**
	 * @see org.olat.group.context.BGContextManager#findGroupAttendedBy(org.olat.core.id.Identity,
	 *      java.lang.String, org.olat.group.context.BGContext)
	 */
	public BusinessGroup findGroupAttendedBy(Identity identity, String groupName, BGContext bgContext) {
		String query = "select bgi from " + "  org.olat.group.BusinessGroupImpl as bgi "
				+ ", org.olat.basesecurity.SecurityGroupMembershipImpl as sgmi" + " where bgi.name = :name "
				+ " and bgi.partipiciantGroup =  sgmi.securityGroup" + " and sgmi.identity = :identId" + " and bgi.groupContext = :context";
		DB db = DBFactory.getInstance();
		DBQuery dbq = db.createQuery(query);
		dbq.setEntity("identId", identity);
		dbq.setString("name", groupName);
		dbq.setEntity("context", bgContext);
		List res = dbq.list();
		if (res.size() == 0) return null;
		else if (res.size() > 1) throw new AssertException("more than one result row found for (identity, groupname, context) ("
				+ identity.getName() + ", " + groupName + ", " + bgContext.getName());
		return (BusinessGroup) res.get(0);
	}

	/**
	 * @see org.olat.group.context.BGContextManager#getBGOwnersOfBGContext(org.olat.group.context.BGContext)
	 */
	public List getBGOwnersOfBGContext(BGContext bgContext) {
		DB db = DBFactory.getInstance();
		String q = "select distinct id from org.olat.basesecurity.IdentityImpl as id inner join fetch id.user as iuser"
				+ ", org.olat.basesecurity.SecurityGroupMembershipImpl sgm" + ", org.olat.group.BusinessGroupImpl bg"
				+ " where bg.groupContext = :context" + " and bg.ownerGroup = sgm.securityGroup" + " and sgm.identity = id";
		DBQuery query = db.createQuery(q);
		query.setEntity("context", bgContext);
		return query.list();
	}

	@Override
	//fxdiff VCRP-2: access control
	public List<BusinessGroup> getBusinessGroupAsOwnerOfBGContext(Identity owner, BGContext bgContext) {
		DB db = DBFactory.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("select bg from org.olat.group.BusinessGroupImpl bg")
			.append(" ,org.olat.basesecurity.SecurityGroupMembershipImpl sgm")
			.append(" where bg.groupContext=:context and bg.ownerGroup=sgm.securityGroup and sgm.identity=:id");

		DBQuery query = db.createQuery(sb.toString());
		query.setEntity("context", bgContext);
		query.setEntity("id", owner);
		return query.list();
	}

	/**
	 * @see org.olat.group.context.BGContextManager#countBGOwnersOfBGContext(org.olat.group.context.BGContext)
	 */
	public int countBGOwnersOfBGContext(BGContext bgContext) {
		DB db = DBFactory.getInstance();
		String q = "select count(distinct id) from org.olat.basesecurity.IdentityImpl id"
				+ ", org.olat.basesecurity.SecurityGroupMembershipImpl sgm" + ", org.olat.group.BusinessGroupImpl bg"
				+ " where bg.groupContext = :context" + " and bg.ownerGroup = sgm.securityGroup" + " and sgm.identity = id";
		DBQuery query = db.createQuery(q);
		query.setEntity("context", bgContext);
		List resultList = query.list();

		int result = 0;
		// if no join/group by matches, result list size is 0 and count undefined ->
		// result is 0
		if (resultList.size() > 0) {
			Object obj = resultList.get(0);
			if (obj == null) return 0;
			result = ((Long) obj).intValue();
		}
		return result;
	}

	/**
	 * @see org.olat.group.context.BGContextManager#getBGParticipantsOfBGContext(org.olat.group.context.BGContext)
	 */
	public List getBGParticipantsOfBGContext(BGContext bgContext) {
		DB db = DBFactory.getInstance();
		String q = "select distinct id from org.olat.basesecurity.IdentityImpl as id inner join fetch id.user as iuser"
				+ ", org.olat.basesecurity.SecurityGroupMembershipImpl sgm" + ", org.olat.group.BusinessGroupImpl bg"
				+ " where bg.groupContext = :context" + " and bg.partipiciantGroup = sgm.securityGroup" + " and sgm.identity = id";
		DBQuery query = db.createQuery(q);
		query.setEntity("context", bgContext);
		return query.list();
	}
	
	@Override
	//fxdiff VCRP-2: access control
	public List<BusinessGroup> getBusinessGroupAsParticipantOfBGContext(Identity participant, BGContext bgContext) {
		DB db = DBFactory.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("select bg from org.olat.group.BusinessGroupImpl bg")
			.append(" ,org.olat.basesecurity.SecurityGroupMembershipImpl sgm")
			.append(" where bg.groupContext=:context and bg.partipiciantGroup=sgm.securityGroup and sgm.identity=:id");

		DBQuery query = db.createQuery(sb.toString());
		query.setEntity("context", bgContext);
		query.setEntity("id", participant);
		return query.list();
	}

	/**
	 * @see org.olat.group.context.BGContextManager#countBGParticipantsOfBGContext(org.olat.group.context.BGContext)
	 */
	public int countBGParticipantsOfBGContext(BGContext bgContext) {
		DB db = DBFactory.getInstance();
		String q = "select count(distinct id) from org.olat.basesecurity.IdentityImpl id"
				+ ", org.olat.basesecurity.SecurityGroupMembershipImpl sgm" + ", org.olat.group.BusinessGroupImpl bg"
				+ " where bg.groupContext = :context" + " and bg.partipiciantGroup = sgm.securityGroup" + " and sgm.identity = id";
		DBQuery query = db.createQuery(q);
		query.setEntity("context", bgContext);
		List resultList = query.list();
		int result = 0;
		// if no join/group by matches, result list size is 0 and count undefined ->
		// result is 0
		if (resultList.size() > 0) {
			Object obj = resultList.get(0);
			if (obj == null) return 0;
			result = ((Long) obj).intValue();
		}
		return result;
	}

	/**
	 * @see org.olat.group.context.BGContextManager#isIdentityInBGContext(org.olat.core.id.Identity,
	 *      org.olat.group.context.BGContext, boolean, boolean)
	 */
	public boolean isIdentityInBGContext(Identity identity, BGContext bgContext, boolean asOwner, boolean asParticipant) {
		DB db = DBFactory.getInstance();
		StringBuilder q = new StringBuilder();

		q.append(" select count(grp) from" + " org.olat.group.BusinessGroupImpl as grp,"
				+ " org.olat.basesecurity.SecurityGroupMembershipImpl as secgmemb where grp.groupContext = :context" + " and ");
		// restricting where clause for participants
		String partRestr = "(grp.partipiciantGroup = secgmemb.securityGroup and secgmemb.identity = :id) ";
		// restricting where clause for owners
		String ownRestr = "(grp.ownerGroup = secgmemb.securityGroup and secgmemb.identity = :id)";

		if (asParticipant && asOwner) {
			q.append("(").append(partRestr).append(" or ").append(ownRestr).append(")");
		} else if (asParticipant && !asOwner) {
			q.append(partRestr);
		} else if (!asParticipant && asOwner) {
			q.append(ownRestr);
		} else {
			throw new AssertException("illegal arguments: at leas one of asOwner or asParticipant must be true");
		}

		DBQuery query = db.createQuery(q.toString());
		query.setEntity("id", identity);
		query.setEntity("context", bgContext);
		query.setCacheable(true);
		List result = query.list();

		if (result.size() == 0) return false;
		return (((Long) result.get(0)).intValue() > 0);
	}

	/**
	 * @see org.olat.group.context.BGContextManager#createAndAddBGContextToResource(java.lang.String,
	 *      org.olat.resource.OLATResource, java.lang.String,
	 *      org.olat.core.id.Identity, boolean)
	 */
	public BGContext createAndAddBGContextToResource(String contextName, OLATResource resource, String groupType, Identity initialOwner,
			boolean defaultContext) {
		BGContextManager cm = BGContextManagerImpl.getInstance();
		BGContext context = cm.createAndPersistBGContext(contextName, null, groupType, initialOwner, defaultContext);
		addBGContextToResource(context, resource);
		return context;
	}

	/**
	 * @see org.olat.group.context.BGContextManager#addBGContextToResource(org.olat.group.context.BGContext,
	 *      org.olat.resource.OLATResource)
	 */
	public void addBGContextToResource(BGContext bgContext, OLATResource resource) {
		BGContext2Resource courseBgContext = new BGContext2Resource(resource, bgContext);
		DBFactory.getInstance().saveObject(courseBgContext);
		// update course context list in this course resource
		if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
			ICourse course = CourseFactory.loadCourse(resource);
			course.getCourseEnvironment().getCourseGroupManager().initGroupContextsList();
		} else if (resource.getResourceableTypeName().equals("junitcourse")) {
			// do nothing when in junit test mode
		} else {
			throw new AssertException("Currently only course resources allowed in resource to context relations.");
		}
		Tracing.logAudit("Added Business Group Context to OLATResource " + resource.toString(), bgContext.toString(), this.getClass());
	}

	/**
	 * @see org.olat.group.context.BGContextManager#findBGContextsForResource(org.olat.resource.OLATResource,
	 *      boolean, boolean)
	 */
	public List findBGContextsForResource(OLATResource resource, boolean defaultContexts, boolean nonDefaultContexts) {
		return findBGContextsForResource(resource, null, defaultContexts, nonDefaultContexts);
	}

	/**
	 * @see org.olat.group.context.BGContextManager#findBGContextsForResource(org.olat.resource.OLATResource,
	 *      java.lang.String, boolean, boolean)
	 */
	public List findBGContextsForResource(OLATResource resource, String groupType, boolean defaultContexts, boolean nonDefaultContexts) {
		DB db = DBFactory.getInstance();
		StringBuilder q = new StringBuilder();
		q.append(" select context from org.olat.group.context.BGContextImpl as context,");
		q.append(" org.olat.group.context.BGContext2Resource as bgcr");
		q.append(" where bgcr.resource = :resource");
		q.append(" and bgcr.groupContext = context");
		if (groupType != null) q.append(" and context.groupType = :gtype");

		boolean checkDefault = defaultContexts != nonDefaultContexts;
		if (checkDefault){
			q.append(" and context.defaultContext = :isDefault");
		}
		DBQuery query = db.createQuery(q.toString());
		query.setEntity("resource", resource);
		if (groupType != null) query.setString("gtype", groupType);
		if (checkDefault){
			query.setBoolean("isDefault", defaultContexts ? true : false);
		}
		return query.list();
	}

	/**
	 * @see org.olat.group.context.BGContextManager#findBGContextsForIdentity(org.olat.core.id.Identity,
	 *      boolean, boolean)
	 */
	public List findBGContextsForIdentity(Identity identity, boolean defaultContexts, boolean nonDefaultContexts) {
		DB db = DBFactory.getInstance();
		StringBuilder q = new StringBuilder();
		q.append(" select context from org.olat.group.context.BGContextImpl as context,");
		q.append(" org.olat.basesecurity.SecurityGroupMembershipImpl as secgmemb");
		q.append(" where context.ownerGroup = secgmemb.securityGroup");
		q.append(" and secgmemb.identity = :identity");

		boolean checkDefault = defaultContexts != nonDefaultContexts;
		if (checkDefault){
			q.append(" and context.defaultContext = :isDefault");
		}
		DBQuery query = db.createQuery(q.toString());
		query.setEntity("identity", identity);
		if (checkDefault){
			query.setBoolean("isDefault", defaultContexts ? true : false);
		}
		
		return query.list();
	}

	/**
	 * @see org.olat.group.context.BGContextManager#findOLATResourcesForBGContext(org.olat.group.context.BGContext)
	 */
	public List<OLATResource> findOLATResourcesForBGContext(BGContext bgContext) {
		DB db = DBFactory.getInstance();
		String q = " select bgcr.resource from org.olat.group.context.BGContext2Resource as bgcr where bgcr.groupContext = :context";
		DBQuery query = db.createQuery(q);
		query.setEntity("context", bgContext);
		return query.list();
	}

	/**
	 * @see org.olat.group.context.BGContextManager#findRepositoryEntriesForBGContext(org.olat.group.context.BGContext)
	 */
	public List<RepositoryEntry> findRepositoryEntriesForBGContext(BGContext bgContext) {
		/*List resources = findOLATResourcesForBGContext(bgContext);
		List entries = new ArrayList();
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			OLATResource resource = (OLATResource) iter.next();
			RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(resource, false);
			if (entry == null) {
				throw new AssertException("No repository entry found for olat resource with TYPE::" + resource.getResourceableTypeName() + " ID::"
						+ resource.getResourceableId());
			} else {
				entries.add(entry);
			}
		}*/
		//fxdiff VCRP-1,2: access control of resources
		return findRepositoryEntriesForBGContext(Collections.singletonList(bgContext), 0, -1);
	}
	
	@Override
	//fxdiff VCRP-1,2: access control of resources
	public List<RepositoryEntry> findRepositoryEntriesForBGContext(Collection<BGContext> bgContexts, int firstResult, int maxResults) {
		if(bgContexts == null || bgContexts.isEmpty()) return Collections.emptyList();
		DB db = DBFactory.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
			.append(" inner join fetch v.olatResource as ores ")
			.append(" where ores in (")
			.append("  select bgcr.resource from ").append(BGContext2Resource.class.getName()).append(" as bgcr where bgcr.groupContext in (:contexts)")
			.append(" )");

		DBQuery query = db.createQuery(sb.toString());
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		query.setParameterList("contexts", bgContexts);
		return query.list();
	}
	
	@Override
	//fxdiff VCRP-1,2: access control of resources
	public List<RepositoryEntry> findRepositoryEntriesForBGContext(Collection<BGContext> bgContexts, int access, boolean asOwner, boolean asCoach,
			boolean asParticipant, Identity identity) {
		if(bgContexts == null || bgContexts.isEmpty()) return Collections.emptyList();
		
		DB db = DBFactory.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct v from ").append(RepositoryEntry.class.getName()).append(" as v ")
			.append(" inner join fetch v.olatResource as ores ")
			.append(" where ores in (")
			.append("  select bgcr.resource from ").append(BGContext2Resource.class.getName()).append(" as bgcr where bgcr.groupContext in (:contexts)")
			.append(" ) and ");
		
		boolean setIdentity = appendAccessSubSelects(sb, access, asOwner, asCoach, asParticipant, identity);

		DBQuery query = db.createQuery(sb.toString());
		query.setParameterList("contexts", bgContexts);
		if(setIdentity) {
			query.setEntity("identity", identity);
		}
		
		List<RepositoryEntry> results = query.list();
		return results;
	}
	
	private boolean appendAccessSubSelects(StringBuilder sb, int access, boolean asOwner, boolean asCoach, boolean asParticipant, Identity identity) {
		sb.append("(v.access >= ");
		if (access == RepositoryEntry.ACC_OWNERS) {
			sb.append(access).append(")");
			return false;
		}
		sb.append(access);
		boolean setIdentity = false;
		if(identity != null && (asOwner || asCoach || asParticipant)) {
			setIdentity = true;
			//sub select are very quick
			sb.append(" or (")
				.append("  v.access=1 and v.membersOnly=true and (");
			
			if(asOwner) {
				sb.append(" v.ownerGroup in (select ownerSgmsi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" ownerSgmsi where ownerSgmsi.identity=:identity)");
			}
			if(asCoach) {
				if(asOwner) sb.append(" or ");
				sb.append(" v.tutorGroup in (select tutorSgmsi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" tutorSgmsi where tutorSgmsi.identity=:identity)");
			}
			if(asParticipant) {
				if(asOwner || asCoach) sb.append(" or ");
				sb.append(" v.participantGroup in (select partiSgmsi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" partiSgmsi where partiSgmsi.identity=:identity)");
			}
			sb.append(" ))");
		}
		sb.append(")");
		return setIdentity;
	}

	/**
	 * @see org.olat.group.context.BGContextManager#removeBGContextFromResource(org.olat.group.context.BGContext,
	 *      org.olat.resource.OLATResource)
	 */
	public void removeBGContextFromResource(BGContext bgContext, OLATResource resource) {
		// 1) delete references for this resource
		String q = " from org.olat.group.context.BGContext2Resource as bgcr where bgcr.groupContext = ? and bgcr.resource = ?";
		DBFactory.getInstance().delete(q, new Object[] { bgContext.getKey(), resource.getKey() },
				new Type[] { Hibernate.LONG, Hibernate.LONG });
		// 2) update course context list in this course resource
		if (resource.getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
			ICourse course = CourseFactory.loadCourse(resource);
			course.getCourseEnvironment().getCourseGroupManager().initGroupContextsList();
		} else if (resource.getResourceableTypeName().equals("junitcourse")) {
			// do nothing when in junit test mode
		} else {
			throw new AssertException("Currently only course resources allowed in resource to context relations.");
		}

		Tracing.logAudit("Removed Business Group Context from OLATResource " + resource.toString(), bgContext.toString(), this.getClass());
	}

	/**
	 * @see org.olat.group.context.BGContextManager#loadBGContext(org.olat.group.context.BGContext)
	 */
	public BGContext loadBGContext(BGContext bgContext) {
		return (BGContext) DBFactory.getInstance().loadObject(bgContext);
	}

}