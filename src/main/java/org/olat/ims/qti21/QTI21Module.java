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
package org.olat.ims.qti21;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.ims.qti21.repository.handlers.QTI21AssessmentTestHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21Module extends AbstractSpringModule {
	
	@Autowired
	private QTI21AssessmentTestHandler assessmentHandler;
	
	@Value("${qti21.math.assessment.extension.enabled:true}")
	private boolean mathAssessExtensionEnabled;
	
	@Autowired
	public QTI21Module(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		RepositoryHandlerFactory.registerHandler(assessmentHandler, 10);
		//Saxon is mandatory, JQTI need XSLT 2.0
		//XsltFactoryUtilities.SAXON_TRANSFORMER_FACTORY_CLASS_NAME;
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	public boolean isMathAssessExtensionEnabled() {
		return mathAssessExtensionEnabled;
	}

	public void setMathAssessExtensionEnabled(boolean mathAssessExtensionEnabled) {
		this.mathAssessExtensionEnabled = mathAssessExtensionEnabled;
	}
	
	
	
	

}
