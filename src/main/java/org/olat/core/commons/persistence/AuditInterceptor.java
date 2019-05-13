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

package org.olat.core.commons.persistence;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.olat.core.id.CreateInfo;
import org.olat.core.logging.Tracing;

/**
 * @author Andreas Ch. Kapp
 *  
 */
public class AuditInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 7210083323938075881L;
	private final Logger log = Tracing.createLoggerFor(AuditInterceptor.class);

	private int updates;
	private int creates;

	/**
	 * @see org.hibernate.Interceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		if (log.isDebugEnabled())
			log.debug("\nflush:" + entity + "\npre:"
					+ (previousState == null ? "-" : Arrays.asList(previousState).toString()) + "\ncur:"
					+ (currentState == null ? "-" : Arrays.asList(currentState).toString()));
		return false;
	}

	/**
	 * @see org.hibernate.Interceptor#onLoad(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		return false;
	}

	/**
	 * @see org.hibernate.Interceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		// automatically set the creationdate property on Auditable (all OLAT entities implement Auditable)
		if (entity instanceof CreateInfo) {
			creates++;
			for (int i = 0; i < propertyNames.length; i++) {
				if ("creationDate".equals(propertyNames[i])) {
					if(state[i] == null) {
						state[i] = new Date();
						return true;
					}
				}
			}
		}
		return false;
	}


	/**
	 * @see org.hibernate.Interceptor#postFlush(java.util.Iterator)
	 */
	@SuppressWarnings("rawtypes")
	public void postFlush(Iterator entities) {
		if (log.isDebugEnabled()){
			log.debug("AuditInterceptor - Creations: " + creates + ", Updates: " + updates);
		}
	}

	/**
	 * @see org.hibernate.Interceptor#preFlush(java.util.Iterator)
	 */
	@SuppressWarnings("rawtypes")
	public void preFlush(Iterator entities) {
		updates = 0;
		creates = 0;
	}
}