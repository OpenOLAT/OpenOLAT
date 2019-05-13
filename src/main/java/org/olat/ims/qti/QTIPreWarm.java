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
package org.olat.ims.qti;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.PreWarm;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.course.nodes.iq.QTIResourceTypeModule;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * 
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTIPreWarm implements PreWarm {
	
	private static final Logger log = Tracing.createLoggerFor(QTIPreWarm.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OLATResourceManager olatResourceManager;

	@Override
	public void run() {
		long start = System.nanoTime();
		log.info("Start scanning for QTI resources");
		
		List<String> types = new ArrayList<>(2);
		types.add(TestFileResource.TYPE_NAME);
		types.add(SurveyFileResource.TYPE_NAME);
		List<OLATResource> qtiResources = olatResourceManager.findResourceByTypes(types);
		dbInstance.commitAndCloseSession();
		for(OLATResource qtiResource:qtiResources) {
			QTIResourceTypeModule.isOnyxTest(qtiResource);
		}
		log.info(qtiResources.size() + " QTI Resources scanned in (ms): " + CodeHelper.nanoToMilliTime(start));
	}
}
