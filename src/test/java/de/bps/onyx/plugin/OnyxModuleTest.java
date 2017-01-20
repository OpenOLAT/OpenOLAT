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
package de.bps.onyx.plugin;

import static org.olat.modules.iq.IQTestHelper.createRepository;
import static org.olat.modules.iq.IQTestHelper.createSet;
import static org.olat.modules.iq.IQTestHelper.modDate;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.QTIResultSet;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OnyxModuleTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Test
	public void existsResultSet() {
		RepositoryEntry re = createRepository();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qti-result-mgr-1");
		dbInstance.commit();
		
		long assessmentId = 838l;
		String resSubPath = "qtiResult34";
		QTIResultSet set = createSet(1.0f, assessmentId, id, re, resSubPath, modDate(3, 8, 5), modDate(3, 8, 20));
		dbInstance.commit();
		Assert.assertNotNull(set);
		
		// fake the course node
		QTICourseNode courseNode = new IQTESTCourseNode();
		courseNode.setIdent(resSubPath);
		IQEditController.setIQReference(re, courseNode.getModuleConfiguration());

		boolean foundIt = OnyxModule.existsResultSet(re.getOlatResource().getResourceableId(), courseNode, id, assessmentId);
		Assert.assertTrue(foundIt);
		
		boolean notFoundIt = OnyxModule.existsResultSet(re.getOlatResource().getResourceableId(), courseNode, id, null);
		Assert.assertFalse(notFoundIt);
		
		boolean notFoundItAlt = OnyxModule.existsResultSet(re.getOlatResource().getResourceableId(), courseNode, id, -123l);
		Assert.assertFalse(notFoundItAlt);
	}
}
