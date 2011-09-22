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
* <p>
*/
package org.olat.core.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Description:<br>
 * this testcase starts up a mini framework where some of the coordinator stuff is mocked
 * 
 * <P>
 * Initial Date:  19.05.2010 <br>
 * @author guido
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = {
	"classpath*:/org/olat/core/test/_spring/webapphelperMock.xml",
	"classpath*:/org/olat/core/util/i18n/_spring/i18nCorecontext.xml",
	"classpath*:/org/olat/core/test/_spring/coordinatorMock.xml",
	"classpath*:/org/olat/core/util/i18n/devtools/_spring/devtoolsCorecontext.xml",
	"classpath*:/org/olat/core/util/_spring/utilCorecontext.xml"})

public class OlatcoreTestCaseWithMocking {

}
