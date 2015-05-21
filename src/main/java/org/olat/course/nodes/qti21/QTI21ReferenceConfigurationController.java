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
package org.olat.course.nodes.qti21;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.QTI21AssessmentCourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.InMemoryOutcomesListener;
import org.olat.ims.qti21.ui.QTI21DisplayController;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.value.NumberValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ReferenceConfigurationController extends BasicController {
	
	private Link previewLink, editLink;
	private final VelocityContainer mainVC;
	private final Link chooseButton, changeButton;
	private final BreadcrumbPanel stackPanel;

	private Controller previewCtr;
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchController;
	
	private QTI21AssessmentCourseNode courseNode;
	private final ModuleConfiguration config;
	
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21ReferenceConfigurationController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			QTI21AssessmentCourseNode courseNode) {
		super(ureq, wControl);
		
		this.courseNode = courseNode;
		this.stackPanel = stackPanel;
		config = courseNode.getModuleConfiguration();
		mainVC = createVelocityContainer("reference");
		
		chooseButton = LinkFactory.createButtonSmall("command.create", mainVC, this);
		chooseButton.setElementCssClass("o_sel_qti21_choose_repofile");
		changeButton = LinkFactory.createButtonSmall("command.change", mainVC, this);
		changeButton.setElementCssClass("o_sel_qti21_change_repofile");
		
		if (config.get(QTI21AssessmentCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of the chosen cp
			RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
			if (re == null) { // we cannot display the entries name, because the
				// repository entry had been deleted between the time when it was chosen here, and now				
				showError("error.entry.missing");
				mainVC.contextPut("showPreviewButton", Boolean.FALSE);
				mainVC.contextPut("chosen", translate("no.test.chosen"));
			} else {
				if (isEditable(ureq.getIdentity(), ureq.getUserSession().getRoles(), re)) {
					editLink = LinkFactory.createButtonSmall("edit", mainVC, this);
				}
				mainVC.contextPut("showPreviewButton", Boolean.TRUE);
				String displayname = StringHelper.escapeHtml(re.getDisplayname());
				previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, mainVC, this);
				previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
				previewLink.setTitle(getTranslator().translate("command.preview"));
				updateModuleConfigFromQTIFile(re.getOlatResource());
			}
		} else {
			// no valid config yet
			mainVC.contextPut("showPreviewButton", Boolean.FALSE);
			mainVC.contextPut("chosen", translate("no.test.chosen"));
		}
		
		putInitialPanel(mainVC);
	}
	
	private boolean isEditable(Identity identity, Roles roles, RepositoryEntry entry) {
		return identity != null && roles != null && entry != null;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == chooseButton || source == changeButton) {
			removeAsListenerAndDispose(searchController);
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					ImsQTI21Resource.TYPE_NAME, translate("choose"));			
			listenTo(searchController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					searchController.getInitialComponent(), true, translate("choose.assessment"));
			listenTo(cmc);
			cmc.activate();
		} else if (source == previewLink){
			// Preview as modal dialogue only if the config is valid
			RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
			if (re == null) { // we cannot preview it, because the repository entry
				// had been deleted between the time when it was chosen here, and now				
				showError("error.entry.missing");
			} else {
				removeAsListenerAndDispose(previewCtr);
				InMemoryOutcomesListener listener = new InMemoryOutcomesListener();
				previewCtr = new QTI21DisplayController(ureq, getWindowControl(), listener, re, null, null);
				stackPanel.pushController(translate("preview"), previewCtr);
			}
		} else if (source == editLink) {
			CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), courseNode);
		}
	}
	
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == searchController) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) { 
				// search controller done
				// -> close closeable modal controller
				cmc.deactivate();
				RepositoryEntry re = searchController.getSelectedEntry();
				if (re != null) {
					config.setStringValue(QTI21AssessmentCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
					updateModuleConfigFromQTIFile(re.getOlatResource());
					mainVC.contextPut("showPreviewButton", Boolean.TRUE);
					
					String displayname = StringHelper.escapeHtml(re.getDisplayname());
					previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED, mainVC, this);
					previewLink.setCustomEnabledLinkCSS("o_preview");
					previewLink.setTitle(getTranslator().translate("command.preview"));
					// remove existing edit link, add new one if user is allowed to edit this CP
					if (editLink != null) {
						mainVC.remove(editLink);
						editLink = null;
					}
					if (isEditable(urequest.getIdentity(), urequest.getUserSession().getRoles(), re)) {
						editLink = LinkFactory.createButtonSmall("edit", mainVC, this);
					}
					// fire event so the updated config is saved by the editormaincontroller
					fireEvent(urequest, Event.CHANGED_EVENT);
				}
			}
		}
	}

	private void updateModuleConfigFromQTIFile(OLATResource res) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(res);
		
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentObject(fUnzippedDirRoot);
		AssessmentTest test = resolvedAssessmentTest.getTestLookup().getRootNodeHolder().getRootNode();

		Float minValue = null, maxValue = null, cutValue = null;
		for (OutcomeDeclaration declaration : test.getOutcomeDeclarations()) {
			if(QTI21Constants.SCORE_IDENTIFIER.equals(declaration.getIdentifier())) {
				minValue = extractValue(declaration);
			} else if(QTI21Constants.MAXSCORE_IDENTIFIER.equals(declaration.getIdentifier())) {
				maxValue = extractValue(declaration);
			}
        }
	
		// Put values to module configuration
		config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, minValue);
		config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, maxValue);
		config.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, cutValue);
	}
	
	private Float extractValue(OutcomeDeclaration declaration) {
		Float floatValue = null;
		Value value = declaration.getDefaultValue().evaluate();
		if(value instanceof NumberValue) {
			floatValue = (float)((NumberValue)value).doubleValue();
		}
		return floatValue;
	}
}
