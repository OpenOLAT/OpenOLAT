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

package org.olat.core.logging.activity;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;

/**
 * Core implementation for LoggingResourceable - i.e. contains those
 * LoggingResourceables that are known in the olatcore.
 * <p>
 * A LoggingResourceable is the least common denominator between an OlatResourceable,
 * an OlatResource, a RepositoryEntry and simple Strings - all of which want to be
 * used as (greatGrandParent,grandParent,parent,target) resources in the logging table.
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
public class CoreLoggingResourceable implements ILoggingResourceable {

	/** the logging object used in this class **/
	private static final Logger log_ = Tracing.createLoggerFor(CoreLoggingResourceable.class);

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
	
	private final boolean ignorable_;
	
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
	private CoreLoggingResourceable(OLATResourceable resourceable, ILoggingResourceableType resourceableType, String type, String id, String name, boolean ignorable) {
		if (type!=null && type.length()>32) {
			log_.error("<init> type too long. Allowed 32, actual: "+type.length()+", type="+type);
			type = type.substring(0, 32);
		}
		if (id!=null && id.length()>64) {
			log_.error("<init> id too long. Allowed 64, actual: "+id.length()+", id="+id);
			id = id.substring(0, 64);
		}
		if (name!=null && name.length()>230) {
			log_.error("<init> name too long. Allowed 230 (to have some margin to 256), actual: "+name.length()+", name="+name);
			name = name.substring(0, 230);
		}
		resourceable_ = resourceable;
		resourceableType_ = resourceableType;
		type_ = type;
		id_ = id;
		name_ = name;
		ignorable_ = ignorable;
	}
	
//
// Following is a set of wrap*() methods which take specific 'olat resourceable' objects
// and selects the type/id/name information to be taken out of it
//
	
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
	public static CoreLoggingResourceable wrapNonOlatResource(StringResourceableType type, String idForDB, String nameForDB) {
		return new CoreLoggingResourceable(null, type, 
				type.name(), idForDB, nameForDB, false);
	}
	
	/**
	 * Wraps a filename as type StringResourceableType.uploadFile into a LoggingResourceable
	 * @param uploadFileName the filename - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given filename as type StringResourceableType.uploadFile
	 */
	public static CoreLoggingResourceable wrapUploadFile(String uploadFileName) {
		return wrapNonOlatResource(StringResourceableType.uploadFile, createUniqueId(StringResourceableType.uploadFile.toString(), uploadFileName), uploadFileName);
	}
	
	/**
	 * Wraps a filename as type StringResourceableType.bcFile into a LoggingResourceable
	 * @param bcFileName the filename - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given filename as type StringResourceableType.bcFile
	 */
	public static CoreLoggingResourceable wrapBCFile(String bcFileName) {
		return wrapNonOlatResource(StringResourceableType.bcFile, createUniqueId(StringResourceableType.bcFile.toString(), bcFileName), bcFileName);
	}
	
	/**
	 * Wraps a cpNodeName as type StringResourceableType.cpNode into a LoggingResourceable
	 * @param cpNodeName the node name - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given node name as type StringResourceableType.cpNode
	 */
	public static CoreLoggingResourceable wrapCpNode(String cpNodeName) {
		return wrapNonOlatResource(StringResourceableType.cpNode, createUniqueId(StringResourceableType.cpNode.toString(), cpNodeName), cpNodeName);
	}
	
	/**
	 * Wraps a single page uri as type StringResourceableType.spUri into a LoggingResourceable
	 * @param spUri the single page uri - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given uri as type StringResourceableType.spUri
	 */
	public static CoreLoggingResourceable wrapSpUri(String spUri) {
		return wrapNonOlatResource(StringResourceableType.spUri, createUniqueId(StringResourceableType.spUri.toString(), spUri), spUri);
	}
	
	/**
	 * Wraps a businessgroup right as type StringResourceableType.bgRight into a LoggingResourceable
	 * @param right the name of the businessgroup right - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given right name as type StringResourceableType.bgRight
	 */
	public static CoreLoggingResourceable wrapBGRight(String right) {
		return wrapNonOlatResource(StringResourceableType.bgRight, createUniqueId(StringResourceableType.bgRight.toString(), right), right);
	}
	
	/**
	 * Wraps an Identity as type StringResourceableType.targetIdentity into a LoggingResourceable
	 * @param identity the identity - to be stored to the database in the name field
	 * @return a LoggingResourceable wrapping the given identity as type StringResourceableType.targetIdentity
	 */
	public static CoreLoggingResourceable wrap(Identity identity) {
		return wrapNonOlatResource(StringResourceableType.targetIdentity, String.valueOf(identity.getKey()), identity.getName());
	}
	
	/**
	 * General wrapper for an OlatResourceable - as it's not obvious of what type that 
	 * OlatResourceable is (in terms of being able to later compare it against the businessPath etc)
	 * an ILoggingResourceableType needs to be passed to this method as well.
	 * @param olatResourceable a general OlatResourceable
	 * @param type the type of the olatResourceable
	 * @return a LoggingResourceable wrapping the given olatResourceable type pair
	 */
	public static CoreLoggingResourceable wrap(OLATResourceable olatResourceable, ILoggingResourceableType type) {
		return new CoreLoggingResourceable(olatResourceable, type, olatResourceable.getResourceableTypeName(),
				String.valueOf(olatResourceable.getResourceableId()), "", false);			
	}
	
	/**
	 * General wrapper for an OlatResourceable - as it's not obvious of what type that 
	 * OlatResourceable is (in terms of being able to later compare it against the businessPath etc)
	 * an ILoggingResourceableType needs to be passed to this method as well.
	 * 
	 * @param olatResourceable A generic OLATResourceable
	 * @param type The type of the resource
	 * @param name The display name of the resource
	 * @return
	 */
	public static CoreLoggingResourceable wrap(OLATResourceable olatResourceable, ILoggingResourceableType type, String name) {
		return new CoreLoggingResourceable(olatResourceable, type, olatResourceable.getResourceableTypeName(),
				String.valueOf(olatResourceable.getResourceableId()), name, false);			
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
		return ignorable_;
	}

	@Override
	public int hashCode() {
		return type_.hashCode()+id_.hashCode()+(resourceable_!=null ? resourceable_.getResourceableTypeName().hashCode()+(int)resourceable_.getResourceableId().longValue() : 0) + (resourceableType_!=null ? resourceableType_.hashCode() : 0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CoreLoggingResourceable)) {
			return false;
		} else if (super.equals(obj)) {
			return true;
		} else if (hashCode()!=obj.hashCode()) {
			return false;
		}
		
		CoreLoggingResourceable lri = (CoreLoggingResourceable)obj;
		if (!type_.equals(lri.type_)) {
			return false;
		}
		if (!id_.equals(lri.id_)) {
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
			if (ceResourceable.equals(resourceable_)) {
				return true;
			}
			try{
				// last chance to get a 'true' as the result
				
				//@TODO
				//@TODO-OLAT-4924
				//PREFACE: Notice that this code actually corresponds nicely to the olat3 version of this class!
				//
				// This 'hack' is necessary due to the fact that the GlossaryMainController is in the olatcore and has no
				// access to olat3 code. Hence it has no access to RepositoryEntry or OLATResource etc which makes
				// clean comparison here impossible.
				// What happens here is: the GlossaryMainController is created with an OLATResourceImpl (from olat3) 
				// then passes this to the ThreadLocalUserActivityLogger via CoreLoggingResourceable.wrap().
				// Then, when logging the 'LEARNING_RESOURCE_OPEN' action, the UserActivityLoggerImpl fetches the
				// businesspath to go through that list and compares it with what it got in its resourceable list.
				// Now, the businesspath contains a RepositoryEntry which *contains* an OLATResourceImpl representing the
				// glossary.
				// The resourceable list though, contains what was just added before, namely the OLATResourceImpl directly.
				// The RepositoryEntry's OLATResourceImpl is in fact the same (==) as the one in the resourceable list.
				// BUT: We can't find this out easily, i.e. we want to return true in this case but we can't do this 
				//      since we are in the core here. So the 'hack' here is to get the 'getOlatResource' method
				//      via reflection (autsch!) - knowing that the ceResourceable here is actually the RepositoryEntry.
				//      that OlatResourceable (can't cast it to OLATResource since that is in the olat3 again..... :( )
				//      and then go compare the two and voila, find out that they are the same and return true.
				java.lang.reflect.Method getOlatResource = ceResourceable.getClass().getDeclaredMethod("getOlatResource");
				if (getOlatResource!=null) {
					Object ceOlatResourceObj = getOlatResource.invoke(ceResourceable);
					if (ceOlatResourceObj!=null && ceOlatResourceObj instanceof OLATResourceable) {
						OLATResourceable ceOlatResource = (OLATResourceable)ceOlatResourceObj;
						if (ceOlatResource.getResourceableTypeName().equals(resourceable_.getResourceableTypeName()) &&
								ceOlatResource.getResourceableId().equals(resourceable_.getResourceableId())) {
							return true;
						}
						if (ceOlatResource.equals(resourceable_)) {
							return true;
						}
					}
				}
			} catch(Exception e) {
				// ignore any of those
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
