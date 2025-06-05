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
package org.olat.core.logging.activity.manager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ActionVerb;
import org.olat.core.logging.activity.ActivityLogService;
import org.olat.core.logging.activity.CrudAction;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ILoggingResourceable;
import org.olat.core.logging.activity.LogModule;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.UserActivityLoggerImpl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {
	
	private static final Logger log_ = Tracing.createLoggerFor(UserActivityLoggerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LogModule logModule;
	
	@Override
	public LoggingObject log(ILoggingAction loggingAction, ActionType actionType, String sessionId, Long identityKey, Class<?> callingClass,
			final boolean backgroundJob, final String businessPath, final List<ContextEntry> bcContextEntries_,
			final List<ILoggingResourceable> loggingResourceableList, ILoggingResourceable... lriOrNull) {
		boolean isLogAnonymous = logModule.isLogAnonymous();

		// don't log entries with loggingAction type 'tracking'
		if(isLogAnonymous && actionType.equals(ActionType.tracking)) {
			return null;
		}
		
		// fetch some of the loggingAction fields - used for error logging below
		final CrudAction crudAction = loggingAction.getCrudAction();
		final ActionVerb actionVerb = loggingAction.getActionVerb();
		final String actionObject = loggingAction.getActionObject();
		
		// calculate the combined and ordered list of LoggingResourceables which should go 
		// to the database below right away
		List<ILoggingResourceable> resourceInfos = getCombinedOrderedLoggingResourceables(bcContextEntries_, loggingResourceableList, lriOrNull);
		
		if(identityKey == null) {
			for (ILoggingResourceable lr:resourceInfos) {
				if (lr.getResourceableType() == StringResourceableType.targetIdentity && StringHelper.isLong(lr.getId())) {
					identityKey = Long.valueOf(lr.getId());
				}
			}
		}
		if(identityKey == null && backgroundJob) {
			identityKey = 0l;// add a fake identity key for background jobs
		}
		
		if (identityKey == null) {
			// no identity available - odd
			log_.error("No identity available to UserActivityLogger. Cannot write log entry: {}:{}, {}, {}",
					crudAction, actionVerb, actionObject, convertLoggingResourceableListToString(resourceInfos),
					new Exception());
			return null;
		}
				
		if (actionType!=ActionType.admin) {
			final String identityKeyStr = String.valueOf(identityKey);
			for (Iterator<ILoggingResourceable> it = resourceInfos.iterator(); it.hasNext();) {
				ILoggingResourceable lr = it.next();
				// we want this info as too much actionTypes are non-admin and log-entry will then be without value not containing targetIdent!, see FXOLAT-104
				if (lr.getResourceableType()==StringResourceableType.targetIdentity && lr.getId().equals(identityKeyStr)) {
					if (log_.isDebugEnabled()) {
						// complain
						log_.debug("OLAT-4955: Not storing targetIdentity for non-admin logging actions. A non-admin logging action wanted to store a user other than the one from the session: action={}, fieldId={}",
								loggingAction, loggingAction.getJavaFieldIdForDebug(), new Exception("OLAT-4955 debug stacktrac"));
					}
					// remove targetIdentity (fxdiff: only if same as executing identity!)
					it.remove();
				}
			}
		}
		// end of moved code
		if(resourceInfos != null) {
			//remove all ignorable resources
			for(Iterator<ILoggingResourceable> riIterator=resourceInfos.iterator(); riIterator.hasNext(); ) {
				if(riIterator.next().isIgnorable()) {
					riIterator.remove();
				}
			}
		}
		
		if (loggingAction.getTypeListDefinition()==null) {
			// this is a foul!
			log_.warn("LoggingAction has no ResourceableTypeList defined: action="+loggingAction+", fieldId="+loggingAction.getJavaFieldIdForDebug());
		} else if(log_.isDebugEnabled()) {
			// good boy
			String errorMsg = loggingAction.getTypeListDefinition().executeCheckAndGetErrorMessage(resourceInfos);
			if (errorMsg!=null) {
				// we found an inconsistency
				// lets make this a warn
				log_.warn("LoggingAction reported an inconsistency (" + errorMsg + ") while logging: "+loggingAction.getActionVerb()+" "+loggingAction.getActionObject()+", action="+loggingAction+", fieldId="+loggingAction.getJavaFieldIdForDebug()+
						", expected: "+loggingAction.getTypeListDefinition().toString()+
						", actual: "+ convertLoggingResourceableListToString(resourceInfos), new Exception("OLAT-4653"));
			}
		}
		
		// start creating the LoggingObject 
		final LoggingObject logObj = new LoggingObject(sessionId, identityKey, crudAction.name().substring(0,1), actionVerb.name(), actionObject, isLogAnonymous);

		if (resourceInfos != null && !resourceInfos.isEmpty()) {
			// this should be the normal case - we do have LoggingResourceables which we can log
			// alongside the log message

			if (resourceInfos.size()>4) {
				log_.warn("More than 4 resource infos set on a user activity log. Can only have 4. Having: "+resourceInfos.size());
				int diff = resourceInfos.size()-4;
				for(int i=0; i<diff; i++) {
					resourceInfos.remove(3);
				}
			}
			
			// get the target resourceable
			ILoggingResourceable ri = resourceInfos.get(resourceInfos.size()-1);
			logObj.setTargetResourceInfo(ri);
			
			// now set parent - if applicable
			if (resourceInfos.size()>1) {
				ri = resourceInfos.get(resourceInfos.size()-2);
				logObj.setParentResourceInfo(ri);
			}
			
			// and set the grand parent - if applicable
			if (resourceInfos.size()>2) {
				ri = resourceInfos.get(resourceInfos.size()-3);
				logObj.setGrandParentResourceInfo(ri);
			}
			
			// and set the great grand parent - if applicable
			if (resourceInfos.size()>3) {
				ri = resourceInfos.get(resourceInfos.size()-4);
				logObj.setGreatGrandParentResourceInfo(ri);
			}
		}
		
		// fill the remaining fields
		logObj.setBusinessPath(businessPath);
		logObj.setSourceClass(callingClass.getCanonicalName());
		logObj.setResourceAdminAction(actionType.equals(ActionType.admin));
		
		// and store it
		if (dbInstance != null && dbInstance.isError()) {
			// then we would run into an ERROR when we'd do more with this DB
			// hence we just issue a log.info here with the details
			//@TODO: lower to log_.info once we checked that it doesn't occur very often (best for 6.4)
			log_.warn("log: DB is in Error state therefore the UserActivityLoggerImpl cannot store the following logging action into the loggingtable: {}", logObj);
		} else {
			dbInstance.getCurrentEntityManager().persist(logObj);
		}
		return logObj;
	}
	
	/**
	 * Returns the combined and orderd list of LoggingResourceables which are set on this
	 * UserActivityLoggerImpl and are matching the contextEntries.
	 * <p>
	 * Note that this method fails if there is a contextEntry which doesn't have a corresponding
	 * LoggingResourceable: This would be a situation where the businessPath/contextEntry has a
	 * resource defined which is unknown to this UserActivityLoggerImpl - i.e. which has not been
	 * set by the Controller or not been passed via the log() call.
	 * <p>
	 * The safety check with the LoggingAction's ResourceableTypeList is not done in this method.
	 * @param lriOrNull an 
	 * @return the combined and ordered list of LoggingResourceables which should go right to the database
	 * in the corresponding fields
	 */
	private List<ILoggingResourceable> getCombinedOrderedLoggingResourceables(List<ContextEntry> bcContextEntries_,
			List<ILoggingResourceable> loggingResourceableList, ILoggingResourceable... additionalLoggingResourceables) {
		
		List<ILoggingResourceable> result = new LinkedList<>();
		List<ILoggingResourceable> inputCopy = new LinkedList<>(loggingResourceableList);
		if (additionalLoggingResourceables != null && additionalLoggingResourceables.length > 0 && additionalLoggingResourceables[0] != null) {
			for (int i = 0; i < additionalLoggingResourceables.length; i++) {
				ILoggingResourceable additionalLoggingResourceable = additionalLoggingResourceables[i];

				int existingPos = inputCopy.indexOf(additionalLoggingResourceable);
				if (existingPos!=-1) {
					ILoggingResourceable existingRI = loggingResourceableList.get(existingPos);
					if (existingRI.getName()!=null && additionalLoggingResourceable.getName()!=null &&
							existingRI.getName().equals(additionalLoggingResourceable.getName())) {
						// ignore - already set
						continue;
					} else if (existingRI.getName()==null && additionalLoggingResourceable.getName()==null) {
						// both names are null and we otherwiese assume that they are equal
						// so ignore them
						continue;
					}
					// otherwise we have a matching resourceInfo already registered (same type,id) but with a different name
					// let's update it
					inputCopy.remove(existingPos);
				}
				
				inputCopy.add(additionalLoggingResourceable);
			}
		}
		if (bcContextEntries_!=null) {
			LinkedList<ContextEntry> bcContextEntriesCopy = new LinkedList<>();
			for (Iterator<ContextEntry> it = bcContextEntries_.iterator(); it.hasNext();) {
				ContextEntry ce = it.next();
				if (!bcContextEntriesCopy.contains(ce)) {
					bcContextEntriesCopy.add(ce);
				}
			}
			for (Iterator<ContextEntry> it = bcContextEntriesCopy.iterator(); it.hasNext();) {
				ContextEntry ce = it.next();
				// SR: see below boolean foundIt = false;
				for (Iterator<ILoggingResourceable> it2 = inputCopy.iterator(); it2.hasNext();) {
					ILoggingResourceable resourceInfo = it2.next();
					if (resourceInfo.correspondsTo(ce)) {
						// perfecto
						result.add(resourceInfo);
						it2.remove();
						// SR: see below foundIt = true;
						break;
					}
				}
				/*
				if (!foundIt) {
					String oresourceableOres = "n/a (null)";
					// SR: why generate exception for unuseable information???
					if (log_.isDebug() && ce !=null && ce.getOLATResourceable() !=null) {
							try {
								java.lang.reflect.Method getOlatResource = ce.getOLATResourceable().getClass().getDeclaredMethod("getOlatResource");
								if (getOlatResource!=null) {
									oresourceableOres = String.valueOf(getOlatResource.invoke(ce.getOLATResourceable()));
								}
							} catch (SecurityException e) {
								log_.error("SecurityException while retrieving getOlatResource() Method from "+ce.getOLATResourceable().getClass());
							} catch (NoSuchMethodException e) {
								log_.info("(OK) ContextEntry's OLATResourceable had no further getOlatResource() method: "+ce.getOLATResourceable().getClass());
							} catch (IllegalArgumentException e) {
								log_.error("IllegalArgumentException while calling getOlatResource() Method from "+ce.getOLATResourceable().getClass(), e);
							} catch (IllegalAccessException e) {
								log_.error("IllegalAccessException while calling getOlatResource() Method from "+ce.getOLATResourceable().getClass(), e);
							} catch (InvocationTargetException e) {
								log_.error("IllegalAccessException while calling getOlatResource() Method from "+ce.getOLATResourceable().getClass(), e);
							}
					}
					log_.info("Could not find any LoggingResourceable corresponding to this ContextEntry: "+ce.toString()+", ce.getOLATResourceable()="+ce.getOLATResourceable()+", ce.getOLATResourceable().getOlatResource()="+oresourceableOres+", dump of resource infos:");
					for (Iterator<ILoggingResourceable> it2 = inputCopy.iterator(); it2.hasNext();) {
						ILoggingResourceable resourceInfo = it2.next();
						log_.info("id: "+resourceInfo.getId()+", name="+resourceInfo.getName()+", type="+resourceInfo.getType()+", toString: "+resourceInfo.toString());
					}
					if(log_.isDebug()) {//only generate the stacktrace in debug mode
						log_.warn("Could not find any LoggingResourceable corresponding to this ContextEntry: "+ce.toString(), 
								new Exception("UserActivityLoggerImpl.getCombinedOrderedLoggingResourceables()"));
					} else {
						log_.warn("Could not find any LoggingResourceable corresponding to this ContextEntry: "+ce.toString(), null);
					}
				}
				*/
			}
		}
		
		if (inputCopy.size()!=0) {
			// otherwise we have an inconsistency 
			
			// just add all the remaining from inputCopy to result
			// no idea about the ordering - but the inputCopy has some sort of useful ordering as well, presumably
			result.addAll(inputCopy);
		}
		
		return result;
	}
	
	/** toString for debug **/
	private String convertLoggingResourceableListToString(List<ILoggingResourceable> resourceInfos) {
		StringBuilder loggingResourceableListToString = new StringBuilder("[LoggingResourceables: ");
		loggingResourceableListToString.append(resourceInfos.size());
		for (Iterator<ILoggingResourceable> iterator = resourceInfos.iterator(); iterator.hasNext();) {
			ILoggingResourceable loggingResourceable = iterator.next();
			loggingResourceableListToString.append(", ");
			loggingResourceableListToString.append(loggingResourceable);
		}
		loggingResourceableListToString.append("]");
		return loggingResourceableListToString.toString();
	}

}
