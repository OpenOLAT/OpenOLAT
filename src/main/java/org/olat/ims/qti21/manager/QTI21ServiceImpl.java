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
package org.olat.ims.qti21.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.UserTestSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.JqtiExtensionPackage;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.qtiworks.mathassess.MathAssessExtensionPackage;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21ServiceImpl implements QTI21Service {
	
	private static final OLog log = Tracing.createLoggerFor(QTI21ServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TestSessionDAO testSessionDAO;
	@Autowired
	private QTI21Module qtiModule;
	
	private InfinispanXsltStylesheetCache xsltStylesheetCache;
	
	@Autowired
	public QTI21ServiceImpl(InfinispanXsltStylesheetCache xsltStylesheetCache) {
		this.xsltStylesheetCache = xsltStylesheetCache;
	}
	
    @Bean(initMethod="init", destroyMethod="destroy")
    public JqtiExtensionManager jqtiExtensionManager() {
        final List<JqtiExtensionPackage<?>> extensionPackages = new ArrayList<JqtiExtensionPackage<?>>();

        /* Enable MathAssess extensions if requested */
        if (qtiModule.isMathAssessExtensionEnabled()) {
            log.info("Enabling the MathAssess extensions");
            extensionPackages.add(new MathAssessExtensionPackage(xsltStylesheetCache));
        }

        return new JqtiExtensionManager(extensionPackages);
    }
    
    @Bean
    public QtiSerializer qtiSerializer() {
        return new QtiSerializer(jqtiExtensionManager());
    }
	

	@Override
	public UserTestSession createTestSession(RepositoryEntry testEntry, RepositoryEntry courseEntry, Identity identity) {
		return testSessionDAO.createTestSession(testEntry, courseEntry, identity);
	}
	
	
	

}
