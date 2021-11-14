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
package org.olat.course.wizard.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.QTI21EditForm;
import org.olat.course.wizard.CourseWizardService;
import org.olat.course.wizard.IQTESTCourseNodeContext;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 16 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IQTESTNodeController extends StepFormBasicController {

	private final QTI21EditForm qti21EditForm;
	
	private final IQTESTCourseNodeContext context;

	public IQTESTNodeController(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext,
			String nodeContextKey, RepositoryEntry entry) {
		super(ureq, control, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		
		ICourse course = CourseFactory.loadCourse(entry);
		NodeAccessType nodeAccessType = NodeAccessType.of(course);

		context = (IQTESTCourseNodeContext)getOrCreateFromRunContext(nodeContextKey, IQTESTCourseNodeContext::new);
		ModuleConfiguration moduleConfig = context.getModuleConfig();
		if (moduleConfig == null) {
			CourseNode courseNode = CourseNodeFactory.getInstance().getCourseNodeConfiguration(IQTESTCourseNode.TYPE).getInstance();
			courseNode.updateModuleConfigDefaults(true, course.getRunStructure().getRootNode(), nodeAccessType);
			context.setCourseNode(courseNode);
			moduleConfig = courseNode.getModuleConfiguration();
			moduleConfig.setBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, true);
			context.setModuleConfig(moduleConfig);
		}
		
		boolean needManualCorrection = false;
		RepositoryEntry testEntry = context.getReferencedEntry();
		if (testEntry != null) {
			moduleConfig.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
			IQEditController.setIQReference(testEntry, moduleConfig);
			needManualCorrection = CoreSpringFactory.getImpl(QTI21Service.class).needManualCorrection(testEntry);
		}
		qti21EditForm = new QTI21EditForm(ureq, control, rootForm, entry, context, nodeAccessType, needManualCorrection, false);
		listenTo(qti21EditForm);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {	
		formLayout.add(qti21EditForm.getInitialFormItem());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		qti21EditForm.updateModuleConfig();
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
