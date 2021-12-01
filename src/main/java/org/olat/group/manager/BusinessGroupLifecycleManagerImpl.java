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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleObjectStateException;
import org.olat.basesecurity.GroupRoles;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.KnownIssueException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.DateUtils;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.manager.AssessmentModeDAO;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupLifecycle;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.DeletableGroupData;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.BusinessGroupDeletedEvent;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.lifecycle.BusinessGroupLifecycleTypeEnum;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryDeletionModule;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BusinessGroupLifecycleManagerImpl implements BusinessGroupLifecycleManager {

	private static final Logger log = Tracing.createLoggerFor(BusinessGroupLifecycleManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private AssessmentModeDAO assessmentModeDao;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupModule businessGroupModule;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private InfoMessageFrontendManager infoMessageManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryDeletionModule repositoryDeletionModule;
	
	private Date getDate(int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);
		Date date = cal.getTime();
		return CalendarUtils.startOfDay(date);
	}
	
	@Override
	public Date getInactivationDate(BusinessGroupLifecycle businessGroup) {
		BusinessGroupStatusEnum status = businessGroup.getGroupStatus();
		if(status == BusinessGroupStatusEnum.inactive || status == BusinessGroupStatusEnum.deleted || status == BusinessGroupStatusEnum.trash) {
			return businessGroup.getInactivationDate();
		}

		int numOfDaysBeforeDeactivation = businessGroupModule.getNumberOfInactiveDayBeforeDeactivation();
		Date lastUsage = businessGroup.getLastUsage();
		Date deactivation = DateUtils.addDays(lastUsage, numOfDaysBeforeDeactivation);
		
		Date inactivationEmailDate = businessGroup.getInactivationEmailDate();
		if(inactivationEmailDate != null) {
			deactivation = DateUtils.addDays(inactivationEmailDate, businessGroupModule.getNumberOfDayBeforeDeactivationMail());
		} else if(businessGroup.getReactivationDate() != null) {
			Date reactivation = businessGroup.getReactivationDate();
			deactivation = DateUtils.addDays(reactivation, businessGroupModule.getNumberOfDayReactivationPeriod());
		}
		
		return deactivation;
	}
	
	@Override
	public Date getInactivationEmailDate(BusinessGroupLifecycle businessGroup) {
		return businessGroup.getInactivationEmailDate();
	}

	@Override
	public Date getSoftDeleteEmailDate(BusinessGroup businessGroup) {
		return ((BusinessGroupImpl)businessGroup).getSoftDeleteEmailDate();
	}

	@Override
	public long getInactivationResponseDelayUsed(BusinessGroup businessGroup) {
		BusinessGroupStatusEnum status = businessGroup.getGroupStatus();
		if(status == BusinessGroupStatusEnum.inactive || status == BusinessGroupStatusEnum.deleted || status == BusinessGroupStatusEnum.trash) {
			return -1l;
		}
		
		long numOfDays = businessGroupModule.getNumberOfDayBeforeDeactivationMail();
		Date mailDate = ((BusinessGroupImpl)businessGroup).getInactivationEmailDate();
		if(numOfDays <= 0 || mailDate == null) {
			return -1l;
		}
		return DateUtils.countDays(mailDate, new Date());
	}

	@Override
	public Date getSoftDeleteDate(BusinessGroupLifecycle businessGroup) {
		BusinessGroupStatusEnum status = businessGroup.getGroupStatus();
		if(status == BusinessGroupStatusEnum.active) {
			return null;
		}
		if(status == BusinessGroupStatusEnum.deleted || status == BusinessGroupStatusEnum.trash) {
			return businessGroup.getSoftDeleteDate();
		}

		Date deletionDate = null;
		Date softDeleteEmailDate = businessGroup.getSoftDeleteEmailDate();
		if(softDeleteEmailDate != null) {
			deletionDate = DateUtils.addDays(softDeleteEmailDate, businessGroupModule.getNumberOfDayBeforeSoftDeleteMail());
		} else {
			long numOfDaysBeforeSoftDelete = businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete();
			Date inactivationDate = businessGroup.getInactivationDate();
			if(inactivationDate != null) {
				Date now = new Date();
				long inactiveDays = DateUtils.countDays(inactivationDate, now);
				
				long days = numOfDaysBeforeSoftDelete - inactiveDays;
				deletionDate = DateUtils.addDays(now, (int)days);
			}
		}
		return deletionDate;
	}

	@Override
	public long getSoftDeleteResponseDelayUsed(BusinessGroup businessGroup) {
		BusinessGroupStatusEnum status = businessGroup.getGroupStatus();
		if(status == BusinessGroupStatusEnum.active || status == BusinessGroupStatusEnum.deleted || status == BusinessGroupStatusEnum.trash) {
			return -1l;
		}
		
		long numOfDays = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
		Date mailDate = ((BusinessGroupImpl)businessGroup).getSoftDeleteEmailDate();
		if(numOfDays <= 0 || mailDate == null) {
			return -1l;
		}
		return DateUtils.countDays(mailDate, new Date());
	}

	@Override
	public Date getDefinitiveDeleteDate(BusinessGroupLifecycle businessGroup) {
		BusinessGroupStatusEnum status = businessGroup.getGroupStatus();
		if(status != BusinessGroupStatusEnum.trash) {
			return null;
		}
		
		long numOfDaysBeforeDelete = businessGroupModule.getNumberOfSoftDeleteDayBeforeDefinitivelyDelete();
		Date softDeleteDate = businessGroup.getSoftDeleteDate();
		if(softDeleteDate != null) {
			Date now = new Date();
			long softDeleteDays = DateUtils.countDays(now, softDeleteDate);
			
			long days = numOfDaysBeforeDelete - softDeleteDays;
			if(days < 0) {
				days = 0;
			}
			return DateUtils.addDays(now, (int)days);
		}
		
		return null;
	}

	@Override
	public void inactivateAutomaticallyBusinessGroups(Set<BusinessGroup> vetoed) {
		int numOfDaysBeforeDeactivation = businessGroupModule.getNumberOfInactiveDayBeforeDeactivation();
		int numOfDaysBeforeEmail = businessGroupModule.getNumberOfDayBeforeDeactivationMail();
		int numOfDaysReactivation = businessGroupModule.getNumberOfDayReactivationPeriod();
		Date reactivationDatebefore = getDate(numOfDaysReactivation);
		boolean sendMailBeforeDeactivation = businessGroupModule.isMailBeforeDeactivation() && numOfDaysBeforeEmail > 0;
		if(sendMailBeforeDeactivation) {
			int days = numOfDaysBeforeDeactivation - numOfDaysBeforeEmail;
			Date lastLoginDate = getDate(days);
			List<BusinessGroup> businessGroups = getReadyToInactivateBusinessGroups(lastLoginDate, reactivationDatebefore);
			if(!businessGroups.isEmpty()) {
				for(BusinessGroup businessGroup:businessGroups) {
					if(businessGroup.getLastUsage() != null && (vetoed.isEmpty() || !vetoed.contains(businessGroup))) {
						sendEmail(businessGroup, "notification.mail.before.deactivation.subject", "notification.mail.before.deactivation.body", "before deactivation",
								businessGroupModule.getMailCopyBeforeDeactivation());
						businessGroup = setBusinessGroupInactivationMail(businessGroup);
						vetoed.add(businessGroup);
					}
				}
			}
		}

		Date lastLoginDate = getDate(numOfDaysBeforeDeactivation);
		List<BusinessGroup> businessGroups;
		if(sendMailBeforeDeactivation) {
			Date emailBeforeDate = getDate(numOfDaysBeforeEmail);
			businessGroups = getBusinessGroupsToInactivate(lastLoginDate, emailBeforeDate, reactivationDatebefore);
		} else {
			businessGroups = getBusinessGroupsToInactivate(lastLoginDate, null, reactivationDatebefore);
		}
		
		inactivateBusinessGroups(businessGroups, vetoed);
		dbInstance.commitAndCloseSession();
	}

	@Override
	public void inactivateBusinessGroupsAfterResponseTime(Set<BusinessGroup> vetoed) {
		int numOfDaysBeforeEmail = businessGroupModule.getNumberOfDayBeforeDeactivationMail();
		if(numOfDaysBeforeEmail <= 0) {
			return;
		}
		
		int numOfDaysBeforeDeactivation = businessGroupModule.getNumberOfInactiveDayBeforeDeactivation();
		Date lastLoginDate = getDate(numOfDaysBeforeDeactivation);
		int numOfDaysReactivation = businessGroupModule.getNumberOfDayReactivationPeriod();
		Date reactivationDatebefore = getDate(numOfDaysReactivation);

		Date emailBeforeDate = getDate(numOfDaysBeforeEmail);
		List<BusinessGroup> businessGroups = getBusinessGroupsToInactivate(lastLoginDate, emailBeforeDate, reactivationDatebefore);
		
		inactivateBusinessGroups(businessGroups, vetoed);
		dbInstance.commitAndCloseSession();
	}
	
	private void inactivateBusinessGroups(List<BusinessGroup> businessGroupsToInactivate, Set<BusinessGroup> vetoed) {
		for(BusinessGroup businessGroup:businessGroupsToInactivate) {
			if(vetoed.isEmpty() || !vetoed.contains(businessGroup)) {
				businessGroup = setBusinessGroupAsInactive(businessGroup, null);
				if(businessGroup.getLastUsage() != null && businessGroupModule.isMailAfterDeactivation()) {
					sendEmail(businessGroup, "notification.mail.after.deactivation.subject", "notification.mail.after.deactivation.body",
							"after deactivation", businessGroupModule.getMailCopyAfterDeactivation());
				}
				vetoed.add(businessGroup);
			}
		}
	}

	private BusinessGroup setBusinessGroupInactivationMail(BusinessGroup businessGroup) {
		BusinessGroupImpl reloadedGroup = (BusinessGroupImpl)businessGroupDao.loadForUpdate(businessGroup);
		reloadedGroup.setInactivationEmailDate(new Date());
		businessGroup = businessGroupDao.merge(reloadedGroup);
		dbInstance.commit();
		return businessGroup;
	}
	
	private BusinessGroup setBusinessGroupAsInactive(BusinessGroup businessGroup, Identity doer) {
		businessGroup = changeBusinessGroupStatus(businessGroup, BusinessGroupStatusEnum.inactive, doer, false);
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_INACTIVATED, getClass(), LoggingResourceable.wrap(businessGroup));
		return businessGroup;
	}
	
	@Override
	public BusinessGroup changeBusinessGroupStatus(BusinessGroup businessGroup, BusinessGroupStatusEnum status, Identity doer, boolean asOwner) {
		BusinessGroupImpl reloadedBusinessGroup = (BusinessGroupImpl)businessGroupDao.loadForUpdate(businessGroup);
		BusinessGroup mergedGroup = null;
		if(reloadedBusinessGroup != null) {
			BusinessGroupStatusEnum previousStatus = reloadedBusinessGroup.getGroupStatus();
			reloadedBusinessGroup.setGroupStatus(status);
			if(status == BusinessGroupStatusEnum.active) {
				reloadedBusinessGroup.setInactivationDate(null);
				reloadedBusinessGroup.setInactivationEmailDate(null);
				reloadedBusinessGroup.setInactivatedBy(null);
				reloadedBusinessGroup.setSoftDeleteDate(null);
				reloadedBusinessGroup.setSoftDeleteEmailDate(null);
				reloadedBusinessGroup.setSoftDeletedBy(null);
				if(BusinessGroupStatusEnum.inactive == previousStatus || BusinessGroupStatusEnum.trash == previousStatus) {
					if(asOwner) {
						reloadedBusinessGroup.setLastUsage(new Date());
					} else {
						reloadedBusinessGroup.setReactivationDate(new Date());
					}
				}
			} else if(status == BusinessGroupStatusEnum.inactive) {
				reloadedBusinessGroup.setInactivationDate(new Date());
				reloadedBusinessGroup.setInactivatedBy(doer);
				reloadedBusinessGroup.setReactivationDate(null);
			} else if(status == BusinessGroupStatusEnum.trash) {
				reloadedBusinessGroup.setSoftDeleteDate(new Date());
				reloadedBusinessGroup.setSoftDeletedBy(doer);
			}
			mergedGroup = businessGroupDao.merge(reloadedBusinessGroup);
			//prevent lazy loading issues
			mergedGroup.getBaseGroup().getKey();
		}
		dbInstance.commit();
		return mergedGroup;
	}
	
	@Override
	public Identity getInactivatedBy(BusinessGroupRef businessGroup) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select ident from businessgroup as bgi")
		  .append(" inner join bgi.inactivatedBy as ident")
		  .append(" inner join ident.user as user")
		  .append(" where bgi.key=:key");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("key", businessGroup.getKey())
				.getResultList();
		return identities == null || identities.isEmpty() ? null : identities.get(0);
	}

	public List<BusinessGroup> getReadyToInactivateBusinessGroups(Date usageDate, Date reactivationDateLimit) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select bgi from businessgroup as bgi")
		  .where().append("bgi.status=:status and ((bgi.lastUsage = null and bgi.creationDate < :lastUsage) or bgi.lastUsage < :lastUsage)")
		  .and().append("bgi.inactivationEmailDate is null and (bgi.reactivationDate is null or bgi.reactivationDate<:reactivationDateLimit)");
		appendBusinessGroupTypesRestrictions(sb);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("status", BusinessGroupStatusEnum.active.name())
				.setParameter("lastUsage", usageDate, TemporalType.TIMESTAMP)
				.setParameter("reactivationDateLimit", reactivationDateLimit)
				.getResultList();
	}
	
	/**
	 * If the email date is present, the main criteria is the inactivation email
	 * date, it overrides the last usage. If not present, the last usage is the most
	 * important criteria.
	 * 
	 * @param usageDate The limit on usage date
	 * @param emailBeforeDate The limit on email date (optional)
	 * @param reactivationDateLimit
	 * @return A list of groups to inactivate
	 */
	public List<BusinessGroup> getBusinessGroupsToInactivate(Date usageDate, Date emailBeforeDate, Date reactivationDateLimit) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select bgi from businessgroup as bgi")
		  .where()
		  .append(" bgi.status=:status")
		  .and(); 
		if(emailBeforeDate != null) {
			sb.append("(bgi.inactivationEmailDate<:emailDate)");	
		} else {
			sb.append("((bgi.lastUsage is null and bgi.creationDate<:lastUsage) or bgi.lastUsage<:lastUsage)");
		}
		sb.and().append(" (bgi.reactivationDate is null or bgi.reactivationDate<:reactivationDateLimit)");
		appendBusinessGroupTypesRestrictions(sb);

		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("status", BusinessGroupStatusEnum.active.name())
				.setParameter("reactivationDateLimit", reactivationDateLimit);
		if(emailBeforeDate != null) {
			query.setParameter("emailDate", emailBeforeDate);	
		} else {
			query.setParameter("lastUsage", usageDate, TemporalType.TIMESTAMP);	
			
		}
		return query.getResultList();
	}
	
	private void appendBusinessGroupTypesRestrictions(QueryBuilder sb) {
		List<BusinessGroupLifecycleTypeEnum> types = businessGroupModule.getGroupLifecycleTypeEnumsList();
		sb.and().append(" (");
		
		// business, lti, managed, course
		// business, lti, managed
		// business, lti, course
		// business, managed, course
		// lti, managed, course
		// business, lti
		// business, managed
		// business, course

		if(types.contains(BusinessGroupLifecycleTypeEnum.business) || types.contains(BusinessGroupLifecycleTypeEnum.lti)) {
			List<BusinessGroupLifecycleTypeEnum> technicalTypes = new ArrayList<>();
			if(types.contains(BusinessGroupLifecycleTypeEnum.business)) {
				technicalTypes.add(BusinessGroupLifecycleTypeEnum.business);
			}
			if(types.contains(BusinessGroupLifecycleTypeEnum.lti)) {
				technicalTypes.add(BusinessGroupLifecycleTypeEnum.lti);
			}
			sb.append(" bgi.technicalType ").in(technicalTypes.toArray());
			
			if(!types.contains(BusinessGroupLifecycleTypeEnum.managed)) {
				sb.append(" and (bgi.managedFlagsString is null or bgi.managedFlagsString='')");
			}
			
			if(!types.contains(BusinessGroupLifecycleTypeEnum.course)) {
				sb.append(" and not exists (select reToGroup.key from repoentrytogroup as reToGroup")
				  .append(" where bgi.baseGroup.key=reToGroup.group.key)");
			}
		} else if(types.size() == 2 && types.contains(BusinessGroupLifecycleTypeEnum.managed) && types.contains(BusinessGroupLifecycleTypeEnum.course)) {
			sb.append(" (bgi.managedFlagsString is not null and bgi.managedFlagsString <> '')")
			  .append("  or exists (select reToGroup.key from repoentrytogroup as reToGroup")
			  .append(" where bgi.baseGroup.key=reToGroup.group.key)")
			  .append(" and bgi.technicalType <> '").append(BusinessGroupLifecycleTypeEnum.lti.name()).append("'");	
		} else if(types.contains(BusinessGroupLifecycleTypeEnum.managed)) {
			sb.append(" (bgi.managedFlagsString is not null and bgi.managedFlagsString <> '')");
		} else if(types.contains(BusinessGroupLifecycleTypeEnum.course)) {
			sb.append(" exists (select reToGroup.key from repoentrytogroup as reToGroup")
			  .append(" where bgi.baseGroup.key=reToGroup.group.key)")
			  .append(" and bgi.technicalType <> '").append(BusinessGroupLifecycleTypeEnum.lti.name()).append("'");
		} else {
			log.error("Unseen settings: {}", types);
		}
		
		sb.append(")");
	}
	
	@Override
	public BusinessGroup sendInactivationEmail(BusinessGroup businessGroup) {
		businessGroup = setBusinessGroupInactivationMail(businessGroup);
		sendEmail(businessGroup, "notification.mail.before.deactivation.subject", "notification.mail.before.deactivation.body",
				"before deactivation", businessGroupModule.getMailCopyBeforeDeactivation());
		return businessGroup;
	}
	
	@Override
	public BusinessGroup inactivateBusinessGroup(BusinessGroup businessGroup, Identity doer, boolean withMail) {
		businessGroup = setBusinessGroupAsInactive(businessGroup, doer);
		if(withMail) {
			sendEmail(businessGroup, "notification.mail.after.deactivation.subject", "notification.mail.after.deactivation.body", "after deactiviation", null);
		}
		return businessGroup;
	}

	@Override
	public BusinessGroup reactivateBusinessGroup(BusinessGroup businessGroup, Identity doer, boolean asGroupOwner) {
		businessGroup = changeBusinessGroupStatus(businessGroup, BusinessGroupStatusEnum.active, doer, asGroupOwner);
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_REACTIVATED, getClass(), LoggingResourceable.wrap(businessGroup));
		return businessGroup;
	}
	
	@Override
	public void softDeleteAutomaticallyBusinessGroups(Set<BusinessGroup> vetoed) {
		int numOfDaysBeforeSoftDelete = businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete();
		int numOfDaysBeforeEmail = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();

		boolean sendMailBeforeDeactivation = businessGroupModule.isMailBeforeSoftDelete() && numOfDaysBeforeEmail > 0;
		if(sendMailBeforeDeactivation) {
			int days = numOfDaysBeforeSoftDelete - numOfDaysBeforeEmail;
			Date lastUsageDate = getDate(days);
			List<BusinessGroup> businessGroups = getReadyToSoftDeleteBusinessGroups(lastUsageDate);
			if(!businessGroups.isEmpty()) {
				for(BusinessGroup businessGroup:businessGroups) {
					if(((BusinessGroupImpl)businessGroup).getInactivationDate() != null && (vetoed.isEmpty() || !vetoed.contains(businessGroup))) {
						sendEmail(businessGroup, "notification.mail.before.soft.delete.subject", "notification.mail.before.soft.delete.body",
								"before soft delete",  businessGroupModule.getMailCopyBeforeSoftDelete());
						businessGroup = setBusinessGroupSoftDeleteMail(businessGroup);
						vetoed.add(businessGroup);
					}
				}
			}
		}

		Date lastUsageDate = getDate(numOfDaysBeforeSoftDelete);
		List<BusinessGroup> businessGroups;
		if(sendMailBeforeDeactivation) {
			Date emailBeforeDate = getDate(numOfDaysBeforeEmail);
			businessGroups = getBusinessGroupsToSoftDelete(lastUsageDate, emailBeforeDate);
		} else {
			businessGroups = getBusinessGroupsToSoftDelete(lastUsageDate, null);
		}
		softDeleteBusinessGroups(businessGroups, vetoed);
		dbInstance.commitAndCloseSession();
	}
	
	@Override
	public void softDeleteBusinessGroupsAfterResponseTime(Set<BusinessGroup> vetoed) {
		int numOfDaysBeforeSoftDelete = businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete();
		int numOfDaysBeforeEmail = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
		
		Date lastUsageDate = getDate(numOfDaysBeforeSoftDelete);
		Date emailBeforeDate = getDate(numOfDaysBeforeEmail);
		List<BusinessGroup> businessGroups = getBusinessGroupsToSoftDelete(lastUsageDate, emailBeforeDate);
	
		softDeleteBusinessGroups(businessGroups, vetoed);
		dbInstance.commitAndCloseSession();
	}
	
	private void softDeleteBusinessGroups(List<BusinessGroup> businessGroupsToSoftDelete, Set<BusinessGroup> vetoed) {
		for(BusinessGroup businessGroup:businessGroupsToSoftDelete) {
			if(vetoed.isEmpty() || !vetoed.contains(businessGroup)) {
				List<Identity> recipients = getEmailRecipients(businessGroup);
				businessGroup = deleteBusinessGroupSoftly(businessGroup, null);
				if(businessGroup.getLastUsage() != null && businessGroupModule.isMailAfterSoftDelete()) {
					sendEmail(businessGroup, recipients, null, "notification.mail.after.soft.delete.subject", "notification.mail.after.soft.delete.body",
							"after soft delete", businessGroupModule.getMailCopyAfterSoftDelete());
				}
				vetoed.add(businessGroup);
			}
		}
	}

	public BusinessGroup setBusinessGroupSoftDeleteMail(BusinessGroup businessGroup) {
		BusinessGroupImpl reloadedGroup = (BusinessGroupImpl)businessGroupDao.loadForUpdate(businessGroup);
		reloadedGroup.setSoftDeleteEmailDate(new Date());
		businessGroup = businessGroupDao.merge(reloadedGroup);
		dbInstance.commit();
		return businessGroup;
	}
	
	protected BusinessGroup deleteBusinessGroupSoftly(BusinessGroup businessGroup, Identity deletedBy) {
		businessGroup = changeBusinessGroupStatus(businessGroup, BusinessGroupStatusEnum.trash, deletedBy, false);
		
		List<Long> memberKeys = businessGroupRelationDao
				.getMemberKeys(Collections.singletonList(businessGroup), GroupRoles.coach.name(), GroupRoles.participant.name());
		List<Long> entryKeys = businessGroupRelationDao.getRepositoryEntryKeys(businessGroup);
		
		// remove members
		businessGroupDao.removeMemberships(businessGroup);
		// 2) Delete the group areas
		areaManager.deleteBGtoAreaRelations(businessGroup);
		// 3) Delete the relations
		businessGroupRelationDao.deleteRelationsToRepositoryEntry(businessGroup);
		assessmentModeDao.deleteAssessmentModesToGroup(businessGroup);
		
		// log
		log.info(Tracing.M_AUDIT, "Soft deleted Business Group {}", businessGroup);
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_TRASHED, getClass(), LoggingResourceable.wrap(businessGroup));
		
		//notify
		BusinessGroupDeletedEvent event = new BusinessGroupDeletedEvent(BusinessGroupDeletedEvent.RESOURCE_DELETED_EVENT, memberKeys, entryKeys);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(event, OresHelper.lookupType(BusinessGroup.class));

		return businessGroup;
	}
	
	@Override
	public BusinessGroup deleteBusinessGroupSoftly(BusinessGroup businessGroup, Identity deletedBy, boolean withMail) {
		List<Identity> recipients = getEmailRecipients(businessGroup);
		businessGroup = deleteBusinessGroupSoftly(businessGroup, deletedBy);
		if(withMail) {
			sendEmail(businessGroup, recipients, null, "notification.mail.after.soft.delete.subject", "notification.mail.after.soft.delete.body",
					"after soft delete", null);
		}
		return businessGroup;
	}
	
	@Override
	public Identity getSoftDeletedBy(BusinessGroupRef businessGroup) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select ident from businessgroup as bgi")
		  .append(" inner join bgi.softDeletedBy as ident")
		  .append(" inner join ident.user as user")
		  .append(" where bgi.key=:key");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("key", businessGroup.getKey())
				.getResultList();
		return identities == null || identities.isEmpty() ? null : identities.get(0);
	}
	
	public List<BusinessGroup> getReadyToSoftDeleteBusinessGroups(Date usageDate) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select bgi from businessgroup as bgi")
		  .where().append("bgi.status=:status and bgi.inactivationDate<:lastUsage and bgi.softDeleteEmailDate is null");
		appendBusinessGroupTypesRestrictions(sb);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("status", BusinessGroupStatusEnum.inactive.name())
				.setParameter("lastUsage", usageDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<BusinessGroup> getBusinessGroupsToSoftDelete(Date usageDate, Date emailBeforeDate) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select bgi from businessgroup as bgi")
		  .where().append(" bgi.status=:status");
		if(emailBeforeDate != null) {
			sb.and().append(" bgi.softDeleteEmailDate<:emailDate");	
		} else {
			sb.and().append(" bgi.inactivationDate<:lastUsage");
		}
		appendBusinessGroupTypesRestrictions(sb);

		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("status", BusinessGroupStatusEnum.inactive.name());
		if(emailBeforeDate != null) {
			query.setParameter("emailDate", emailBeforeDate, TemporalType.TIMESTAMP);	
		} else {
			query.setParameter("lastUsage", usageDate, TemporalType.TIMESTAMP);
		}
		return query.getResultList();
	}
	
	public List<BusinessGroup> getBusinessGroupsToDefinitivelyDelete(Date usageDate) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select bgi from businessgroup as bgi")
		  .where().append(" bgi.status=:status and bgi.softDeleteDate<:lastUsage");

		appendBusinessGroupTypesRestrictions(sb);

		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("status", BusinessGroupStatusEnum.trash.name())
				.setParameter("lastUsage", usageDate, TemporalType.TIMESTAMP);

		return query.getResultList();
	}

	@Override
	public BusinessGroup sendDeleteSoftlyEmail(BusinessGroup businessGroup) {
		businessGroup = setBusinessGroupSoftDeleteMail(businessGroup);
		sendEmail(businessGroup, "notification.mail.before.soft.delete.subject", "notification.mail.before.soft.delete.body",
				"before soft delete", businessGroupModule.getMailCopyBeforeSoftDelete());
		return businessGroup;
	}

	@Override
	public void definitivelyDeleteBusinessGroups(Set<BusinessGroup> vetoed) {
		int numOfDaysBeforeDefintivelyDelete = businessGroupModule.getNumberOfSoftDeleteDayBeforeDefinitivelyDelete();

		Date lastUsageDate = getDate(numOfDaysBeforeDefintivelyDelete);
		List<BusinessGroup> businessGroups = getBusinessGroupsToDefinitivelyDelete(lastUsageDate);
		
		for(BusinessGroup businessGroup:businessGroups) {
			if(vetoed.isEmpty() || !vetoed.contains(businessGroup)) {
				try {
					deleteBusinessGroup(businessGroup, null);
				} catch (Exception e) {
					log.error("", e);
					dbInstance.commitAndCloseSession();
				}
			}
		}
		
		dbInstance.commitAndCloseSession();
	}
	
	@Override
	public void deleteBusinessGroup(BusinessGroup businessGroupToDelete, Identity deletedBy, boolean withMail) {
		List<Identity> users = businessGroupRelationDao.getMembers(businessGroupToDelete,
				GroupRoles.coach.name(), GroupRoles.participant.name(), GroupRoles.waiting.name());
		// now delete the group first
		deleteBusinessGroup(businessGroupToDelete, deletedBy);
		dbInstance.commit();
		if(!users.isEmpty() && withMail) {
			// finally send email
			String metaId = UUID.randomUUID().toString();
			sendEmail(businessGroupToDelete, users, metaId, "notification.mail.deleted.subject", "notification.mail.deleted.body",
					"after definitively deleted group", null);
		}
	}
	
	protected void deleteBusinessGroup(BusinessGroup group, Identity doer) {
		try{
			log.info(Tracing.M_AUDIT, "Start deleting Business Group {}", group);
			
			Long doerKey = doer == null ? null : doer.getKey();
			OLATResourceableJustBeforeDeletedEvent delEv = new OLATResourceableJustBeforeDeletedEvent(group, doerKey);
			// notify all (currently running) BusinessGroupXXXcontrollers
			// about the deletion which will occur.
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(delEv, group);
	
			// refresh object to avoid stale object exceptions
			group = businessGroupDao.load(group.getKey());
			
			List<Long> memberKeys = businessGroupRelationDao
					.getMemberKeys(Collections.singletonList(group), GroupRoles.coach.name(), GroupRoles.participant.name());
			List<Long> entryKeys = businessGroupRelationDao.getRepositoryEntryKeys(group);
			// 0) Loop over all deletableGroupData
			Map<String,DeletableGroupData> deleteListeners = CoreSpringFactory.getBeansOfType(DeletableGroupData.class);
			for (DeletableGroupData deleteListener : deleteListeners.values()) {
				if(log.isDebugEnabled()) {
					log.debug("deleteBusinessGroup: call deleteListener={}", deleteListener);
				}
				deleteListener.deleteGroupDataFor(group);
			} 
			
			// 1) Delete all group properties
			CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
			ct.deleteTools(group);// deletes everything concerning properties&collabTools
			// 2) Delete the group areas
			areaManager.deleteBGtoAreaRelations(group);
			// 3) Delete the relations
			businessGroupRelationDao.deleteRelationsToRepositoryEntry(group);
			assessmentModeDao.deleteAssessmentModesToGroup(group);
			// 4) delete properties
			propertyManager.deleteProperties(null, group, null, null, null);
			propertyManager.deleteProperties(null, null, group, null, null);
			// 5) delete the publisher attached to this group (e.g. the forum and folder
			// publisher)
			notificationsManager.deletePublishersOf(group);
			// 6) delete info messages and subscription context associated with this group
			infoMessageManager.removeInfoMessagesAndSubscriptionContext(group);
			// 7) the group
			LoggingResourceable wrappedResource = LoggingResourceable.wrap(group);
			businessGroupDao.delete(group);
			
			dbInstance.commit();
			
			// log
			log.info(Tracing.M_AUDIT, "Deleted Business Group {}", group);
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_DELETED, getClass(), wrappedResource);
			// notify
			BusinessGroupDeletedEvent event = new BusinessGroupDeletedEvent(BusinessGroupDeletedEvent.RESOURCE_DELETED_EVENT, memberKeys, entryKeys);
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(event, OresHelper.lookupType(BusinessGroup.class));
		} catch(DBRuntimeException dbre) {
			Throwable th = dbre.getCause();
			if ((th instanceof ObjectNotFoundException) && th.getMessage().contains("org.olat.group.BusinessGroupImpl")) {
				//group already deleted
				return;
			}
			if ((th instanceof StaleObjectStateException) &&
					(th.getMessage().startsWith("Row was updated or deleted by another transaction"))) {
				// known issue OLAT-3654
				log.info("Group was deleted by another user in the meantime. Known issue OLAT-3654");
				throw new KnownIssueException("Group was deleted by another user in the meantime", 3654);
			} else {
				throw dbre;
			}
		}
	}
	
	private List<Identity> getEmailRecipients(BusinessGroup businessGroup) {
		List<Identity> recipients = businessGroupRelationDao.getMembers(businessGroup, GroupRoles.coach.name());
		if(recipients == null || recipients.isEmpty()) {
			recipients = businessGroupRelationDao.getMembers(businessGroup, GroupRoles.participant.name());
		}
		return recipients;
	}
	
	private void sendEmail(BusinessGroup businessGroup, String subjectI18nKey, String bodyI18nKey, String type, List<String> bcc) {
		List<Identity> recipients = getEmailRecipients(businessGroup);
		sendEmail(businessGroup, recipients, null, subjectI18nKey, bodyI18nKey, type, bcc);
	}
	
	private MailerResult sendEmail(BusinessGroup businessGroup, List<Identity> recipients, String metaId,
			String subjectI18nKey, String bodyI18nKey, String type, List<String> bcc) {

		MailerResult result = new MailerResult();
		for(Identity identity:recipients) {
			MailTemplate template = BGMailHelper.createMailTemplate(businessGroup, identity, subjectI18nKey, bodyI18nKey);
			sendUserEmailTo(businessGroup, identity, metaId, template, type, result);
			if(bcc != null && !bcc.isEmpty()) {
				sendEmailCopy(businessGroup, identity, bcc, subjectI18nKey, bodyI18nKey, type);
			}
		}
		return result;
	}
	
	private void sendEmailCopy(BusinessGroup businessGroup, Identity identity, List<String> recepients, String subjectI18nKey, String bodyI18nKey, String type) {
		if (recepients == null || recepients.isEmpty()) {
			return;
		}
		
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(null);
		for (String recepient : recepients) {
			if (MailHelper.isValidEmailAddress(recepient)) {
				MailTemplate template = BGMailHelper.createCopyMailTemplate(businessGroup, identity, subjectI18nKey, bodyI18nKey, locale);
				sendUserEmailCopyTo(businessGroup, recepient, template, type);
			}
		}
	}
	
	private void sendUserEmailTo(BusinessGroup businessGroup, Identity identity, String metaId, MailTemplate template, String type, MailerResult result) {
		// for backwards compatibility
		template.addToContext("responseTo", repositoryDeletionModule.getEmailResponseTo());

		MailContext context = new MailContextImpl("[BusinessGroup:" + businessGroup.getKey() + "]");
		MailBundle bundle = mailManager.makeMailBundle(context, identity, template, null, metaId, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
		}
		log.info(Tracing.M_AUDIT, "Businesss group lifecycle {} send to identity={} with email={}", type, identity.getKey(), identity.getUser().getProperty(UserConstants.EMAIL, null));
	}
	
	private void sendUserEmailCopyTo(BusinessGroup businessGroup, String receiver, MailTemplate template, String type) {
		// for backwards compatibility
		template.addToContext("responseTo", repositoryDeletionModule.getEmailResponseTo());

		MailerResult result = new MailerResult();
		MailContext context = new MailContextImpl("[BusinessGroup:" + businessGroup.getKey() + "]");
		MailBundle bundle = mailManager.makeMailBundle(context, null, template, null, null, result);
		if(bundle != null) {
			bundle.setTo(receiver);
			bundle.setToId(null);
			mailManager.sendExternMessage(bundle, result, true);
		}
		log.info(Tracing.M_AUDIT, "Business group lifecycle {} send copy regarding business group={} ({}) to email={}", type, businessGroup.getKey(), businessGroup.getName(), receiver);
	}
}
