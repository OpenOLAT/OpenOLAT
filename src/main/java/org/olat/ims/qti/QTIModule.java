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
*/

package org.olat.ims.qti;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.ims.qti.repository.handlers.QTISurveyHandler;
import org.olat.ims.qti.repository.handlers.QTITestHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  13.11.2002
 *
 * @author Mike Stock
 */
@Service
public class QTIModule extends AbstractSpringModule {

	@Value("${qti12.create.resources.enabled:false}")
	private boolean createResourcesEnabled;
	@Value("${qti12.import.resources.enabled:false}")
	private boolean importResourcesEnabled;
	@Value("${qti12.run.enabled:false}")
	private boolean runEnabled;
	@Value("${qti12.survey.create.resources.enabled:false}")
	private boolean createSurveyResourcesEnabled;
	@Value("${qti12.survey.create.course.nodes.enabled:false}")
	private boolean createSurveyCourseNodesEnabled;
	@Value("${qti12.survey.import.resources.enabled:false}")
	private boolean importSurveyResourcesEnabled;
	@Value("${qti12.survey.run.enabled:false}")
	private boolean runSurveyEnabled;
	@Value("${qti12.edit.resources.enabled:false}")
	private boolean createEditResourcesEnabled;

	@Autowired
	public QTIModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		RepositoryHandlerFactory.registerHandler(new QTISurveyHandler(), 10);
		RepositoryHandlerFactory.registerHandler(new QTITestHandler(), 10);
		initFromChangedProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	public boolean isCreateResourcesEnabled() {
		return createResourcesEnabled;
	}

	public boolean isImportResourcesEnabled() {
		return importResourcesEnabled;
	}

	public boolean isRunEnabled() {
		return runEnabled;
	}

	public boolean isImportSurveyResourcesEnabled() {
		return importSurveyResourcesEnabled;
	}
	
	public boolean isCreateSurveyResourcesEnabled() {
		return createSurveyResourcesEnabled;
	}
	
	public boolean isCreateSurveyCourseNodesEnabled() {
		return createSurveyCourseNodesEnabled;
	}
	
	public boolean isRunSurveyEnabled() {
		return runSurveyEnabled;
	}

	public boolean isEditResourcesEnabled() {
		return createEditResourcesEnabled;
	}
}
