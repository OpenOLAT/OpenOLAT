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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * A ResourceableTypeList is a structured collection of 
 * ILoggingResourceableTypes specifying which of them are mandatory with a 
 * LoggingAction and which are optional.
 * <p>
 * The idea of ResouceableTypeList is to be able to make checks between
 * the businessPath and
 * all the LoggingResourceables collected during the Controller lifetime,
 * the event handling and information passed with the log() call.
 * <p>
 * Check with the LoggingAction class to see most common use cases
 * of ResourceableTypeList - here's an excerpt though:<br>
 * <pre>
 * new ResourceableTypeList().
 *         addMandatory(OlatResourceableType.wiki).
 *    or().addMandatory(OlatResourceableType.businessGroup).addOptional(OlatResourceableType.wiki);
 * </pre>
 * So the idea is to chain addMandatory and addOptional calls for as long
 * as types need to be added to the same list - then OR that by calling or()
 * and add a second/third variant which also is allowed etc etc.
 * As soon as one of the lists validates to true it will return true for the whole list.
 * <P>
 * Initial Date:  21.10.2009 <br>
 * @author Stefan
 */
public class ResourceableTypeList {
	private static final Logger logger = Tracing.createLoggerFor(ResourceableTypeList.class);
	
	/** list of mandatory ILoggingResourceableTypes **/
	private List<ILoggingResourceableType> mandatory_;
	
	/** list of optional ILoggingResourceableTypes **/
	private List<ILoggingResourceableType> optional_;
	
	/** a general magic 'allow anything' flag **/
	private boolean allowAnything_ = false;
	
	/** list of ORed sub-ResourceableTypeLists - this is only set on the parent (the top most list) **/
	private List<ResourceableTypeList> orList_;

	/** ORed lists are stored in the parent_'s orList_ - also, if this list has a parent 
	 * it will always go to the parent to do a job through the whole list/OR structure
	 */
	private ResourceableTypeList parent_ = null;
	
	/**
	 * Create an empty ResourceableTypeList.
	 * <p>
	 * Call this to later add ILoggingResourceableTypes using addMandatory() and addOptional().
	 * <p>
	 * When a new ORed sublist needs to be created, this can be done via the convenient or() method.
	 */
	public ResourceableTypeList() {
		// ok
	}
	
	/**
	 * Starts a new ResourceableTypeList which is ORed with the callee list
	 * @return a new ReosurceableTypeList which is ORed with the callee list
	 */
	public ResourceableTypeList or() {
		if (parent_!=null) {
			return parent_.or();
		}
		ResourceableTypeList result = new ResourceableTypeList();
		result.parent_ = this;
		getOrs().add(result);
		return result;
	}
	
	/**
	 * Add any number of ILoggingResourceableTypes as <b>mandatory</b> to this list.
	 * <p>
	 * Note that addMandatory() and addOptional() methods can be chained/repeated for as often as required
	 * @param resourceableTypes the list of ILoggingResourceableTypes to be added as mandatory to this list
	 * @return this
	 */
	public ResourceableTypeList addMandatory(ILoggingResourceableType... resourceableTypes) {
		for (int i = 0; i < resourceableTypes.length; i++) {
			ILoggingResourceableType loggingResourceableType = resourceableTypes[i];
			getMandatory().add(loggingResourceableType);
		}
		return this;
	}

	/**
	 * Add any number of ILoggingResourceableTypes as <b>optional</b> to this list.
	 * <p>
	 * Note that addMandatory() and addOptional() methods can be chained/repeated for as often as required
	 * @param resourceableTypes the list of ILoggingResourceableTypes to be added as optional to this list
	 * @return this
	 */
	public ResourceableTypeList addOptional(ILoggingResourceableType... resourceableTypes) {
		for (int i = 0; i < resourceableTypes.length; i++) {
			ILoggingResourceableType loggingResourceableType = resourceableTypes[i];
			getOptional().add(loggingResourceableType);
		}
		return this;
	}

	/**
	 * A magic-stick kind of 'allow everything' used to create a ResourceableTypeList which 
	 * doesn't do any checks such as mandatory/optional/or etc at all but simply allows anything
	 * and everything.
	 */
	public void allowAnything() {
		allowAnything_ = true;
		// note that this method doesn't return the ResourceableTypeList as the other 'addOptional/addMandatory' methods
		// do - this is because after allowAny you don't need to add anything further - since allowAny will cause
		// the check to return true in all cases
	}

	@Override
	public String toString() {
		if (parent_!=null) {
			return parent_.toString();
		}
		
		StringBuffer sb = new StringBuffer(thisAsString());
		if (orList_!=null && orList_.size()>0) {
			for (Iterator<ResourceableTypeList> it = orList_.iterator(); it.hasNext();) {
				ResourceableTypeList list = it.next();
				sb.append(" OR ");
				sb.append(list.thisAsString());
			}
		}
		return sb.toString();
	}
	
	/**
	 * Part of toString() used to output orList children
	 * @return
	 */
	private String thisAsString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		if (getMandatory().size()>0) {
			sb.append("Mandatory are ");
			for (Iterator<ILoggingResourceableType> it = getMandatory().iterator(); it.hasNext();) {
				ILoggingResourceableType mandatoryType = it.next();
				sb.append(mandatoryType.name());
				if (it.hasNext()) {
					sb.append(",");
				}
			}
			sb.append(". ");
		}
		if (getOptional().size()>0) {
			sb.append("Optional are ");
			for (Iterator<ILoggingResourceableType> it = getOptional().iterator(); it.hasNext();) {
				ILoggingResourceableType optionalType = it.next();
				sb.append(optionalType.name());
				if (it.hasNext()) {
					sb.append(",");
				}
			}
			sb.append(".");
		}
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * internal getter for the OR list - initializes the orList_ if it's null
	 * @return the orList_ - never null
	 */
	private List<ResourceableTypeList> getOrs() {
		if (orList_==null) {
			orList_ = new LinkedList<ResourceableTypeList>();
		}
		return orList_;
	}
	
	/**
	 * internal getter for the mandatory list - initializes mandatory_ if it's null
	 * @return the mandatory_ List - never null
	 */
	private List<ILoggingResourceableType> getMandatory() {
		if (mandatory_==null) {
			mandatory_ = new LinkedList<ILoggingResourceableType>();
		}
		return mandatory_;
	}
	
	/**
	 * internal getter for the optional list - initializes optional_ if it's null
	 * @return the optional_ List - never null
	 */
	private List<ILoggingResourceableType> getOptional() {
		if (optional_==null) {
			optional_ = new LinkedList<ILoggingResourceableType>();
		}
		return optional_;
	}
	
	/**
	 * Goes through the resourceInfos list and returns the LoggingResourceable which has the same type 
	 * as the passed ILoggingResourceableType.
	 * @param resourceInfos the list which should be searched through
	 * @param type the type which we are looking for
	 * @return the LoggingResourceable matching the given type - or null
	 */
	private ILoggingResourceable findLoggingResourceInfo(List<ILoggingResourceable> resourceInfos, ILoggingResourceableType type) {
		if (resourceInfos==null) {
			throw new IllegalArgumentException("resourceInfos must not be null");
		}
		if (type==null) {
			throw new IllegalArgumentException("type must not be null");
		}
		for (Iterator<ILoggingResourceable> it = resourceInfos.iterator(); it.hasNext();) {
			ILoggingResourceable loggingResourceInfo = it.next();
			if (loggingResourceInfo!=null && loggingResourceInfo.getResourceableType()==type) {
				return loggingResourceInfo;
			}
		}
		return null;
	}
	
	/**
	 * Executes the businessPath-check on <b>this</b> List - i.e. not on any parent
	 * or orList_ child but exactly on this list.
	 * <p>
	 * This method is called by executeCheckAndGetErrorMessage() which deals with 
	 * parent/orList tweaks.
	 * @param resourceInfos the list which should be checked against
	 * @return null if we have a match, the error message otherwise
	 */
	private String doExecuteCheckAndGetErrorMessage(List<ILoggingResourceable> resourceInfos) {
		if (allowAnything_) {
			// that's the jumbo "everything ok" kind of resource type list
			return null;
		}
		
		List<ILoggingResourceable> resourceInfosCopy = new LinkedList<ILoggingResourceable>(resourceInfos);

		for (Iterator<ILoggingResourceable> it = resourceInfosCopy.iterator(); it.hasNext();) {
			if(it.next().isIgnorable()) {
				it.remove();
			}
		}
		
		
		List<ILoggingResourceableType> mandatory = getMandatory();
		for (Iterator<ILoggingResourceableType> it = mandatory.iterator(); it.hasNext();) {
			ILoggingResourceableType type = it.next();
			if ( isAllCondition(type) ) {
				return checkAllCondition(it, resourceInfos);
			}
			
			ILoggingResourceable lri = findLoggingResourceInfo(resourceInfos, type);
			
			if (lri==null) {
				// error - return the error msg to reflect this
				return "Mandatory resource not available: "+type.name();
			} else {
				resourceInfosCopy.remove(lri);
			}
		}
		
		List<ILoggingResourceableType> optional = getOptional();
		for (Iterator<ILoggingResourceable> it = resourceInfosCopy.iterator(); it.hasNext();) {
			ILoggingResourceable lri = it.next();
			final ILoggingResourceableType type = lri.getResourceableType();
			if (type!=null && !optional.contains(type)) {
				// error - return the error msg to reflect this
				return "Resource set which is neither mandatory nor optional: "+type+", name="+lri.getName();
			}
		}
		
		// everything ok - return null to reflect this
		return null;
	}
	
	/**
	 * Check if type is a 'all' (*) condition.
	 * @param type
	 * @return
	 */
	private boolean isAllCondition(ILoggingResourceableType type) {
		return type == StringResourceableType.anyBefore;
	}
	/**
	 * Special case for condition [all (*)] [type]
	 * @param currentIterator            
	 * @param resourceInfos
	 * @return
	 */
	private String checkAllCondition(Iterator<ILoggingResourceableType> currentIterator, List<ILoggingResourceable> resourceInfos) {
		logger.debug("special case *.type"); 
		if (currentIterator.hasNext()) {
			ILoggingResourceableType subtype = currentIterator.next();
			logger.debug("subtype=" + subtype); 
			ILoggingResourceable lri = findLoggingResourceInfo(resourceInfos, subtype);
			if (lri==null) {
				return "Mandatory resource not available: "+subtype.name();
			} else {
				logger.debug("ok an finish" ); 
				return null;
			}
		} else {
			return "'all' without a type not allowed";
		}

	}
	
	/**
	 * Executes the businessPath check on this list - this includes taking into account
	 * any or()-ed sublists.
	 * <p>
	 * @param resourceInfos the list to be checked against
	 * @return null if we have a match, the error message otherwise
	 */
	public String executeCheckAndGetErrorMessage(List<ILoggingResourceable> resourceInfos) {
		if (parent_!=null) {
			return parent_.executeCheckAndGetErrorMessage(resourceInfos);
		}
		// we are at the parent
		String result = doExecuteCheckAndGetErrorMessage(resourceInfos);
		if (result==null) {
			// fine
			return null;
		}
		
		for (Iterator<ResourceableTypeList> it = getOrs().iterator(); it.hasNext();) {
			ResourceableTypeList list = it.next();
			String orResult = list.doExecuteCheckAndGetErrorMessage(resourceInfos);
			if (orResult==null) {
				// fine
				return null;
			}
		}
		return result;
	}
}
