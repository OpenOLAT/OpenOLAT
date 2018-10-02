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
package org.olat.course.nodes.iq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.ims.qti.process.Resolver;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTIResourceTypeModule {
	
	private static final OLog log = Tracing.createLoggerFor(QTIResourceTypeModule.class);
	
	private static final Map<Long,Boolean> onyxMap = new ConcurrentHashMap<>();
	
	public static boolean isQtiWorks(final OLATResourceable res) {
		return ImsQTI21Resource.TYPE_NAME.equals(res.getResourceableTypeName());
	}
	
	public static boolean isOnyxTest(final OLATResourceable res) {
		if (res.getResourceableTypeName().equals(TestFileResource.TYPE_NAME) ||
				res.getResourceableTypeName().equals(SurveyFileResource.TYPE_NAME)) {
			Long resourceId = res.getResourceableId();
			Boolean onyx = onyxMap.get(resourceId);
			if(onyx == null) {
				onyx = Boolean.FALSE;
				try {
					final Resolver resolver = new ImsRepositoryResolver(res);
					// search for qti.xml, it not exists for qti2
					if (resolver.getQTIDocument() == null) {
						onyx = Boolean.TRUE;
					} else {
						onyx = Boolean.FALSE;
					}
				} catch(OLATRuntimeException e) {
					log.error("", e);
				}
				onyxMap.put(resourceId, onyx);
			}
			return onyx.booleanValue();
		} else {
			return false;
		}
	}
}
