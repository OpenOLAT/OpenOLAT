/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */

package org.olat.commons.info.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageManager;
import org.olat.commons.info.InfoMessageToCurriculumElement;
import org.olat.commons.info.InfoMessageToGroup;
import org.olat.commons.info.model.InfoMessageImpl;
import org.olat.commons.info.model.InfoMessageToCurriculumElementImpl;
import org.olat.commons.info.model.InfoMessageToGroupImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.curriculum.CurriculumElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * The manager for info messages
 * 
 * <P>
 * Initial Date:  26 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Service("infoMessageManager")
public class InfoMessageManagerImpl implements InfoMessageManager {
	
	@Autowired
	private DB dbInstance;

	@Override
	public InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author) {
		if(ores == null) throw new NullPointerException("OLAT Resourceable cannot be null");
		
		InfoMessageImpl info = new InfoMessageImpl();
		info.setCreationDate(new Date());
		info.setResId(ores.getResourceableId());
		info.setResName(ores.getResourceableTypeName());
		info.setResSubPath(subPath);
		info.setBusinessPath(normalizeBusinessPath(businessPath));
		info.setAuthor(author);
		return info;
	}

	@Override
	public void saveInfoMessage(InfoMessage infoMessage) {
		if(infoMessage instanceof InfoMessageImpl impl) {
			if(impl.getKey() == null) {
				dbInstance.saveObject(impl);
			} else {
				dbInstance.updateObject(impl);
			}
		}
	}

	@Override
	public void deleteInfoMessage(InfoMessage infoMessage) {
		if(infoMessage instanceof InfoMessageImpl impl) {
			if(impl.getKey() != null) {
				dbInstance.deleteObject(impl);
			}
		}
	}

	@Override
	public List<InfoMessage> loadUnpublishedInfoMessages(int firstResult, int maxResults) {
		QueryBuilder qb = new QueryBuilder();
		Date currentDate = new Date();

		qb.append("select msg from infomessage as msg")
				.and().append("msg.published=false")
				.and().append("msg.publishDate<=:before");

		TypedQuery<InfoMessage> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), InfoMessage.class);

		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		query.setParameter("before", currentDate);

		return query.getResultList();
	}

	@Override
	public List<InfoMessage> loadInfoMessagesOfIdentity(BusinessGroupRef businessGroup, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select msg from ").append(InfoMessageImpl.class.getName()).append(" msg")
			.append(" left join fetch msg.author author")
			.append(" left join fetch author.user")
			.append(" left join fetch msg.modifier modifier")
			.append(" left join fetch modifier.user")
			.append(" where (author.key=:authorKey")
			.append(" or modifier.key=:authorKey)")
			.append(" and msg.resId=:groupKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), InfoMessage.class)
				.setParameter("authorKey",identity.getKey())
				.setParameter("groupKey", businessGroup.getKey())
				.getResultList();
	}

	@Override
	public InfoMessage loadInfoMessageByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select msg from ").append(InfoMessageImpl.class.getName()).append(" msg")
			.append(" left join fetch msg.author author")
			.append(" left join fetch author.user")
			.append(" left join fetch msg.modifier modifier")
			.append(" left join fetch modifier.user")
			.append(" where msg.key=:key");
		
		List<InfoMessage> infoMessages = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), InfoMessage.class)
				.setParameter("key", key)
				.getResultList();
		if (infoMessages.isEmpty()) {
			return null;
		}
		return infoMessages.get(0);
	}

	@Override
	public List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before, int firstResult, int maxResults) {
		
		TypedQuery<InfoMessage> query = queryInfoMessageByResource(ores, subPath, businessPath, after, before, InfoMessage.class);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	@Override
	public int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before) {
		
		TypedQuery<Number> query = queryInfoMessageByResource(ores, subPath, businessPath, after, before, Number.class);
		Number count = query.getSingleResult();
		return count.intValue();
	}
	
	private <U> TypedQuery<U> queryInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before, Class<U> resultClass) {
		boolean count = resultClass.equals(Number.class);
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ");
		if(count) {
			sb.append("count(msg.key)");
		} else {
			sb.append("msg");
		}
		
		sb.append(" from infomessage msg");
		if (!count) {
			sb.append(" left join fetch msg.author author")
			.append(" left join fetch author.user")
			.append(" left join fetch msg.modifier modifier")
			.append(" left join fetch modifier.user");
		}
		
		if(ores != null) {
			appendAnd(sb, "msg.resId=:resId and msg.resName=:resName ");
		}
		if(StringHelper.containsNonWhitespace(subPath)) {
			appendAnd(sb, "msg.resSubPath=:subPath");
		}
		if(StringHelper.containsNonWhitespace(businessPath)) {
			appendAnd(sb, "msg.businessPath=:businessPath");
		}
		if(after != null) {
			appendAnd(sb, "msg.creationDate>=:after");
		}
		if(before != null) {
			appendAnd(sb, "msg.creationDate<=:before");
		}
		if(!count) {
			sb.append(" order by msg.creationDate desc");
		}
		
		TypedQuery<U> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), resultClass);
		if(ores != null) {
			query.setParameter("resId", ores.getResourceableId());
			query.setParameter("resName", ores.getResourceableTypeName());
		}
		if(StringHelper.containsNonWhitespace(subPath)) {
			query.setParameter("subPath", subPath);
		}
		if(StringHelper.containsNonWhitespace(businessPath)) {
			query.setParameter("businessPath", normalizeBusinessPath(businessPath));
		}
		if(after != null) {
			query.setParameter("after", after, TemporalType.TIMESTAMP);
		}
		if(before != null) {
			query.setParameter("before", before, TemporalType.TIMESTAMP);
		}
		return query;
	}

	@Override
	public InfoMessageToGroup createInfoMessageToGroup(InfoMessage infoMessage, BusinessGroup businessGroup) {
		InfoMessageToGroupImpl infoMessageToGroup = new InfoMessageToGroupImpl();
		infoMessageToGroup.setInfoMessage(infoMessage);
		infoMessageToGroup.setBusinessGroup(businessGroup);
		dbInstance.getCurrentEntityManager().persist(infoMessageToGroup);
		return infoMessageToGroup;
	}

	@Override
	public List<InfoMessageToGroupImpl> loadInfoMessageToGroupByGroup(BusinessGroup group) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select infogroup from infomessagetogroup as infogroup")
				.and().append("infogroup.businessGroup=:group");

		TypedQuery<InfoMessageToGroupImpl> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), InfoMessageToGroupImpl.class);
		query.setParameter("group", group);

		return query.getResultList();
	}

	@Override
	public void deleteInfoMessageToGroup(InfoMessageToGroup infoMessageToGroup) {
		dbInstance.getCurrentEntityManager().remove(infoMessageToGroup);
	}

	@Override
	public InfoMessageToCurriculumElement createInfoMessageToCurriculumElement(InfoMessage infoMessage, CurriculumElement curriculumElement) {
		InfoMessageToCurriculumElementImpl infoMessageToCurriculumElement = new InfoMessageToCurriculumElementImpl();
		infoMessageToCurriculumElement.setInfoMessage(infoMessage);
		infoMessageToCurriculumElement.setCurriculumElement(curriculumElement);
		dbInstance.getCurrentEntityManager().persist(infoMessageToCurriculumElement);
		return infoMessageToCurriculumElement;
	}

	@Override
	public List<InfoMessageToCurriculumElementImpl> loadInfoMessageToCurriculumElementByCurEl(CurriculumElement curriculumElement) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select infocurel from infomessagetocurriculumelement as infocurel")
				.and().append("infocurel.curriculumElement=:curriculumElement");

		TypedQuery<InfoMessageToCurriculumElementImpl> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), InfoMessageToCurriculumElementImpl.class);
		query.setParameter("curriculumElement", curriculumElement);

		return query.getResultList();
	}

	@Override
	public void deleteInfoMessageToCurriculumElement(InfoMessageToCurriculumElement infoMessageToCurriculumElement) {
		dbInstance.getCurrentEntityManager().remove(infoMessageToCurriculumElement);
	}

	private StringBuilder appendAnd(StringBuilder sb, String query) {
		if(sb.indexOf("where") > 0) sb.append(" and ");
		else sb.append(" where ");
		sb.append(query);
		return sb;
	}
	
	private String normalizeBusinessPath(String url) {
		if (url == null) return null;
		if (url.startsWith("ROOT")) {
			url = url.substring(4, url.length());
		}
		List<String> tokens = new ArrayList<>();
		for(StringTokenizer tokenizer = new StringTokenizer(url, "[]"); tokenizer.hasMoreTokens(); ) {
			String token = tokenizer.nextToken();
			if(!tokens.contains(token)) {
				tokens.add(token);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(String token:tokens) {
			sb.append('[').append(token).append(']');
		}
		return sb.toString();
	}
}
