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
package org.olat.course.nodes.cl.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.AssessmentData;
import org.olat.course.nodes.cl.model.AssessmentDataView;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.course.nodes.cl.model.DBCheckbox;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CheckboxManagerImpl implements CheckboxManager {
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public DBCheckbox createDBCheckbox(String checkboxId, OLATResourceable ores, String resSubPath) {
		DBCheckbox checkbox = new DBCheckbox();
		checkbox.setCreationDate(new Date());
		checkbox.setLastModified(new Date());
		checkbox.setCheckboxId(checkboxId);
		checkbox.setResName(ores.getResourceableTypeName());
		checkbox.setResId(ores.getResourceableId());
		checkbox.setResSubPath(resSubPath);
		dbInstance.getCurrentEntityManager().persist(checkbox);
		return checkbox;
	}

	@Override
	public List<DBCheckbox> loadCheckbox(OLATResourceable ores, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select box from clcheckbox box where box.resName=:resName and box.resId=:resId");
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			sb.append(" and box.resSubPath=:resSubPath");
		}
		
		TypedQuery<DBCheckbox> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), DBCheckbox.class)
			.setParameter("resName", ores.getResourceableTypeName())
			.setParameter("resId", ores.getResourceableId());
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			query.setParameter("resSubPath", resSubPath);
		}
		return query.getResultList();
	}
	
	@Override
	public DBCheckbox loadCheckbox(OLATResourceable ores, String resSubPath, String checkboxId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select box from clcheckbox box")
		  .append(" where box.checkboxId=:checkboxId and box.resName=:resName and box.resId=:resId")
		  .append(" and box.resSubPath=:resSubPath");

		
		List<DBCheckbox> box = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), DBCheckbox.class)
			.setParameter("resName", ores.getResourceableTypeName())
			.setParameter("resId", ores.getResourceableId())
			.setParameter("resSubPath", resSubPath)
			.setParameter("checkboxId", checkboxId)
			.getResultList();
		if(box.isEmpty()) {
			return null;
		}
		return box.get(0);
	}
	
	private DBCheckbox loadForUpdate(DBCheckbox checkbox) {
		dbInstance.getCurrentEntityManager().detach(checkbox);

		StringBuilder sb = new StringBuilder();
		sb.append("select box from clcheckbox box where box.key=:key");

		
		List<DBCheckbox> box = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), DBCheckbox.class)
			.setParameter("key", checkbox.getKey())
			.getResultList();
		if(box.isEmpty()) {
			return null;
		}
		return box.get(0);
	}

	@Override
	public void removeCheckbox(DBCheckbox checkbox) {
		DBCheckbox ref = dbInstance.getCurrentEntityManager()
				.getReference(DBCheckbox.class, checkbox.getKey());
		dbInstance.getCurrentEntityManager().remove(ref);
	}

	@Override
	public List<DBCheck> loadCheck(Identity identity, OLATResourceable ores, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select check from clcheck check")
		  .append(" inner join fetch check.checkbox box")
		  .append(" where check.identity.key=:identityKey")
		  .append(" and box.resName=:resName and box.resId=:resId");
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			sb.append(" and box.resSubPath=:resSubPath");
		}
		
		TypedQuery<DBCheck> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DBCheck.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setParameter("identityKey", identity.getKey());
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			query.setParameter("resSubPath", resSubPath);
		}
		return query.getResultList();
	}

	@Override
	public void syncCheckbox(CheckboxList checkboxList, OLATResourceable ores, String resSubPath) {
		List<DBCheckbox> dbCheckboxList = loadCheckbox(ores, resSubPath);
		Map<String,DBCheckbox> uuids = new HashMap<String,DBCheckbox>();
		for(DBCheckbox dbCheckbox:dbCheckboxList) {
			uuids.put(dbCheckbox.getCheckboxId(), dbCheckbox);
		}

		List<Checkbox> resCheckboxList =  checkboxList.getList();
		for(Checkbox resCheckbox:resCheckboxList) {
			String resUuid = resCheckbox.getCheckboxId();
			if(uuids.containsKey(resUuid)) {
				uuids.remove(resUuid);//already synched
			} else {
				createDBCheckbox(resUuid, ores, resSubPath);
			}
		}
		
		for(DBCheckbox dbCheckbox:uuids.values()) {
			System.out.println("Remove them??? " + dbCheckbox.getCheckboxId());
		}
	}
	
	@Override
	public void deleteCheckbox(OLATResourceable ores, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from clcheck check")
		  .append(" where check.checkbox.key in (")
		  .append("   select box.key from clcheckbox box where box.resName=:resName and box.resId=:resId and box.resSubPath=:resSubPath")
		  .append(" )");
		dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("resName", ores.getResourceableTypeName())
			.setParameter("resId", ores.getResourceableId())
			.setParameter("resSubPath", resSubPath)
			.executeUpdate();
		
		StringBuilder sb2 = new StringBuilder();
		sb2.append("delete from clcheckbox box")
		   .append(" where box.resName=:resName and box.resId=:resId and box.resSubPath=:resSubPath");
		dbInstance.getCurrentEntityManager()
			.createQuery(sb2.toString())
			.setParameter("resName", ores.getResourceableTypeName())
			.setParameter("resId", ores.getResourceableId())
			.setParameter("resSubPath", resSubPath)
			.executeUpdate();
	}

	@Override
	public void check(DBCheckbox checkbox, Identity owner, Float score, Boolean checked) {
		DBCheck currentCheck = loadCheck(checkbox, owner);
		if(currentCheck == null) {
			DBCheckbox lockedCheckbox = loadForUpdate(checkbox);
			if(lockedCheckbox != null) {
				//locked -> reload to make sure nobody create it
				DBCheck reloaedCheck = loadCheck(checkbox, owner);
				if(reloaedCheck == null) {
					createCheck(lockedCheckbox, owner, score, checked);
				} else {
					currentCheck = reloaedCheck;
				}
			}
		}
		if(currentCheck != null) {
			currentCheck.setScore(score);
			currentCheck.setChecked(checked);
		}
		dbInstance.commit();	
	}
	
	protected DBCheck loadCheck(DBCheckbox checkbox, Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select check from clcheck as check")
		  .append(" where check.identity.key=:identityKey and check.checkbox.key=:checkboxKey");
		
		List<DBCheck> checks = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DBCheck.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("checkboxKey", checkbox.getKey())
				.getResultList();
		
		if(checks.isEmpty()) {
			return null;
		}
		return checks.get(0);
	}

	protected DBCheck createCheck(DBCheckbox checkbox, Identity owner, Float score, Boolean checked) {
		DBCheck check = new DBCheck();
		check.setCreationDate(new Date());
		check.setLastModified(new Date());
		check.setIdentity(owner);
		check.setCheckbox(checkbox);
		check.setChecked(checked);
		check.setScore(score);
		dbInstance.getCurrentEntityManager().persist(check);
		return check;
	}
	
	@Override
	public int countChecked(Identity identity, OLATResourceable ores, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(check) from clcheck check")
		  .append(" inner join check.checkbox box")
		  .append(" inner join check.identity ident")
		  .append(" where check.identity.key=:identityKey and box.resName=:resName and box.resId=:resId");
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			sb.append(" and box.resSubPath=:resSubPath");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setParameter("identityKey", identity.getKey());
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			query.setParameter("resSubPath", resSubPath);
		}
		
		Number numOfChecks = query.getSingleResult();
		return numOfChecks.intValue();
	}

	@Override
	public float calculateScore(Identity identity, OLATResourceable ores, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select sum(check.score) from clcheck check")
		  .append(" inner join check.checkbox box")
		  .append(" inner join check.identity ident")
		  .append(" where check.identity.key=:identityKey and check.checked=true and box.resName=:resName and box.resId=:resId");
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			sb.append(" and box.resSubPath=:resSubPath");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setParameter("identityKey", identity.getKey());
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			query.setParameter("resSubPath", resSubPath);
		}
		
		Number numOfChecks = query.getSingleResult();
		return numOfChecks == null ? 0.0f : numOfChecks.floatValue();
	}

	@Override
	public List<AssessmentData> getAssessmentDatas(OLATResourceable ores, String resSubPath, List<SecurityGroup> secGroups) {
		StringBuilder sb = new StringBuilder();
		sb.append("select check from clcheck check")
		  .append(" inner join fetch check.checkbox box")
		  .append(" inner join fetch check.identity ident")
		  .append(" where box.resName=:resName and box.resId=:resId");
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			sb.append(" and box.resSubPath=:resSubPath");
		}
		if(secGroups != null && secGroups.size() > 0) {
			sb.append(" and check.identity.key in ( select secMembership.identity.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" secMembership ")
			  .append("   where secMembership.securityGroup in (:secGroups)")
			  .append(" )");
		}
		
		TypedQuery<DBCheck> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), DBCheck.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId());
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			query.setParameter("resSubPath", resSubPath);
		}
		if(secGroups != null && secGroups.size() > 0) {
			query.setParameter("secGroups", secGroups);
		}
		
		List<DBCheck> checks = query.getResultList();
		Map<Long, AssessmentData> identToBox = new HashMap<Long,AssessmentData>();
		for(DBCheck check:checks) {
			AssessmentData data = identToBox.get(check.getIdentity().getKey());
			if(data == null) {
				data = new AssessmentData(check.getIdentity());
				identToBox.put(check.getIdentity().getKey(), data);
			}
			data.getChecks().add(check);
		}
		return new ArrayList<AssessmentData>(identToBox.values());
	}

	@Override
	public List<AssessmentDataView> getAssessmentDataViews(OLATResourceable ores, String resSubPath, List<Checkbox> checkbox,
			List<SecurityGroup> secGroups, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		
		List<AssessmentData> datas = getAssessmentDatas(ores, resSubPath, secGroups);
		List<AssessmentDataView> dataViews = new ArrayList<>();
		
		int numOfcheckbox = checkbox.size();
		Map<String,Integer> indexed = new HashMap<String,Integer>();
		for(int i=numOfcheckbox; i-->0; ) {
			indexed.put(checkbox.get(i).getCheckboxId(), new Integer(i));
		}
		
		for(AssessmentData data:datas) {
			Boolean[] checkBool = new Boolean[numOfcheckbox];
			float totalPoints = 0.0f;
			for(DBCheck check:data.getChecks()) {
				Float score = check.getScore();
				if(score != null) {
					totalPoints += score.floatValue();
				}
				
				if(check.getChecked() == null) continue;
				
				Integer index = indexed.get(check.getCheckbox().getCheckboxId());
				if(index != null) {
					int i = index.intValue();
					if(i >= 0 && i<numOfcheckbox) {
						checkBool[i] = check.getChecked();
					}
				}
			}
			dataViews.add(new AssessmentDataView(data.getIdentity(), checkBool, totalPoints, userPropertyHandlers, locale));
		}
		return dataViews;
	}

	@Override
	public VFSContainer getFileContainer(CourseEnvironment courseEnv, CheckListCourseNode cNode, Checkbox checkbox) {
		String path = courseEnv.getCourseBaseContainer().getRelPath() + "/" + CheckListCourseNode.FOLDER_NAME + "/" + cNode.getIdent();
		OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(path, null);
		return rootFolder; 
	}

	@Override
	public File getFileDirectory(CourseEnvironment courseEnv, CheckListCourseNode cNode, Checkbox checkbox) {
		String path = courseEnv.getCourseBaseContainer().getRelPath() + "/" + CheckListCourseNode.FOLDER_NAME + "/" + cNode.getIdent();
		File rootFolder = new File(FolderConfig.getCanonicalRoot(), path);
		return rootFolder; 
	}
}
