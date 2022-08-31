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
package org.olat.modules.forms.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.PageEditorSecurityCallback;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageLayoutHandler;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.ui.FullEditorSecurityCallback;
import org.olat.modules.ceditor.ui.PageEditorV2Controller;
import org.olat.modules.ceditor.ui.event.ContainerRuleLinkEvent;
import org.olat.modules.ceditor.ui.event.OpenRulesEvent;
import org.olat.modules.forms.handler.ContainerHandler;
import org.olat.modules.forms.handler.DisclaimerHandler;
import org.olat.modules.forms.handler.FileUploadHandler;
import org.olat.modules.forms.handler.HTMLParagraphHandler;
import org.olat.modules.forms.handler.HTMLRawHandler;
import org.olat.modules.forms.handler.ImageHandler;
import org.olat.modules.forms.handler.MultipleChoiceHandler;
import org.olat.modules.forms.handler.RubricHandler;
import org.olat.modules.forms.handler.SessionInformationsHandler;
import org.olat.modules.forms.handler.SingleChoiceHandler;
import org.olat.modules.forms.handler.SpacerHandler;
import org.olat.modules.forms.handler.TableHandler;
import org.olat.modules.forms.handler.TextInputHandler;
import org.olat.modules.forms.handler.TitleHandler;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.model.xml.Rule;
import org.olat.modules.forms.model.xml.VisibilityAction;
import org.olat.modules.forms.rules.EvaluationFormRuleHandlerProvider;
import org.olat.modules.forms.rules.RuleHandlerProvider;
import org.olat.modules.forms.rules.ui.EvaluationFormRulesController;
import org.olat.repository.ui.RepositoryEntryRuntimeController.ToolbarAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormEditorController extends BasicController implements ToolbarAware {
	
	private final TooledStackedPanel toolbar;
	private Link rulesLink;
	private final Component helpLink;

	private PageEditorV2Controller pageEditCtrl;
	private CloseableModalController cmc;
	private EvaluationFormRulesController rulesCtrl;

	private final Form form;
	private final File formFile;
	private final DataStorage storage;
	private boolean changes = false;
	private final boolean restrictedEdit;
	private final boolean restrictedEditWeight;
	private final RuleHandlerProvider ruleHandlerProvider;
	
	@Autowired
	private HelpModule helpModule;
	
	public EvaluationFormEditorController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar,
			File formFile, DataStorage storage, boolean restrictedEdit, boolean restrictedEditWeight) {
		super(ureq, wControl);
		this.toolbar = toolbar;
		this.formFile = formFile;
		this.storage = storage;
		this.restrictedEdit = restrictedEdit;
		this.restrictedEditWeight = restrictedEditWeight;
		this.ruleHandlerProvider = new EvaluationFormRuleHandlerProvider();
		if(formFile.exists()) {
			form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		} else {
			form = new Form();
			persistForm();
		}
		
		HelpLinkSPI provider = helpModule.getManualProvider();
		helpLink = provider.getHelpPageLink(ureq, translate("help"), translate("show.help.tooltip"),
				"o_icon o_icon-lg o_icon_help", "o_chelp", "manual_user/forms/Forms_in_the_ePortfolio_template/");
		
		PageEditorSecurityCallback secCallback = restrictedEdit ? new RestrictedEditorSecurityCallback() : new FullEditorSecurityCallback();
		pageEditCtrl = new PageEditorV2Controller(ureq, getWindowControl(), new FormPageEditorProvider(), secCallback, getTranslator());
		listenTo(pageEditCtrl);
		
		fireContainerRuleLinkEvent(ureq);
		
		putInitialPanel(pageEditCtrl.getInitialComponent());
	}
	
	@Override
	public void initToolbar() {
		rulesLink = LinkFactory.createToolLink("rules", translate("rules"), this, "o_icon_branch");
		toolbar.addTool(rulesLink, Align.left);
		
		toolbar.addTool(helpLink, Align.rightEdge, false, "o_chelp_wrapper");
	}
	
	public boolean hasChanges() {
		return changes;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == rulesLink) {
			doOpenRules(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == pageEditCtrl && event instanceof OpenRulesEvent) {
			doOpenRules(ureq);
		} else if (source == rulesCtrl) {
			if (event == FormEvent.DONE_EVENT) {
				persistForm();
				fireContainerRuleLinkEvent(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		} else if (event == Event.CHANGED_EVENT) {
			persistForm();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(rulesCtrl);
		removeAsListenerAndDispose(cmc);
		rulesCtrl = null;
		cmc = null;
	}

	private void persistForm() {
		XStreamHelper.writeObject(FormXStream.getXStream(), formFile, form);
		changes = true;
	}
	
	private void doOpenRules(UserRequest ureq) {
		rulesCtrl = new EvaluationFormRulesController(ureq, getWindowControl(), form, ruleHandlerProvider);
		listenTo(rulesCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), rulesCtrl.getInitialComponent(),
				true, translate("rules"));
		listenTo(cmc);
		cmc.activate();
	}

	private void fireContainerRuleLinkEvent(UserRequest ureq) {
		Set<String> elementIds = form.getRules().stream()
				.map(Rule::getAction)
				.filter(action -> action instanceof VisibilityAction)
				.map(action -> ((VisibilityAction)action).getElementId())
				.collect(Collectors.toSet());
		fireEvent(ureq, new ContainerRuleLinkEvent(elementIds));
	}

	private class FormPageEditorProvider implements PageEditorProvider {
		
		private final List<PageElementHandler> handlers = new ArrayList<>();
		private final List<PageElementHandler> creationHandlers = new ArrayList<>();
		private final List<PageLayoutHandler> creationlayoutHandlers = new ArrayList<>();
		
		public FormPageEditorProvider() {
			// handler for title
			TitleHandler titleRawHandler = new TitleHandler();
			handlers.add(titleRawHandler);
			// handler for HR code
			SpacerHandler hrHandler = new SpacerHandler();
			handlers.add(hrHandler);
			// handler for HTML code
			HTMLParagraphHandler htmlParagraphHandler = new HTMLParagraphHandler();
			handlers.add(htmlParagraphHandler);
			// handler for HTML code
			HTMLRawHandler htmlHandler = new HTMLRawHandler();
			handlers.add(htmlHandler);
			TableHandler tableHandler = new TableHandler();
			handlers.add(tableHandler);
			// handler media
			ImageHandler imageHandler = new ImageHandler(storage);
			handlers.add(imageHandler);
			// handler for rubric
			RubricHandler rubricHandler = new RubricHandler(restrictedEdit, restrictedEditWeight);
			handlers.add(rubricHandler);
			// handler for text input
			TextInputHandler textInputHandler = new TextInputHandler(restrictedEdit);
			handlers.add(textInputHandler);
			// handler for file upload
			FileUploadHandler fileUploadhandler = new FileUploadHandler(restrictedEdit);
			handlers.add(fileUploadhandler);
			// handler for single choice
			SingleChoiceHandler singleChoiceHandler = new SingleChoiceHandler(restrictedEdit);
			handlers.add(singleChoiceHandler);
			// handler for multiple choice
			MultipleChoiceHandler multipleChoiceHandler = new MultipleChoiceHandler(restrictedEdit);
			handlers.add(multipleChoiceHandler);
			DisclaimerHandler disclaimerHandler = new DisclaimerHandler(restrictedEdit);
			handlers.add(disclaimerHandler);
			SessionInformationsHandler sessionInformationsHandler = new SessionInformationsHandler(restrictedEdit);
			handlers.add(sessionInformationsHandler);
			ContainerHandler containerHandler = new ContainerHandler(EvaluationFormEditorController.this);
			handlers.add(containerHandler);

			if(!restrictedEdit) {
				creationHandlers.add(titleRawHandler);
				creationHandlers.add(htmlParagraphHandler);
				creationHandlers.add(tableHandler);
				creationHandlers.add(imageHandler);
				creationHandlers.add(rubricHandler);
				creationHandlers.add(singleChoiceHandler);
				creationHandlers.add(multipleChoiceHandler);
				creationHandlers.add(textInputHandler);
				creationHandlers.add(fileUploadhandler);
				creationHandlers.add(sessionInformationsHandler);
				creationHandlers.add(disclaimerHandler);
				creationHandlers.add(hrHandler);
				creationHandlers.add(htmlHandler); // legacy
				
				for(ContainerLayout layout:ContainerLayout.values()) {
					if(!layout.deprecated()) {
						creationlayoutHandlers.add(new ContainerHandler(EvaluationFormEditorController.this, layout));
					}
				}
			}
		}

		@Override
		public List<? extends PageElement> getElements() {
			return form.getElements();
		}

		@Override
		public List<PageElementHandler> getCreateHandlers() {
			return creationHandlers;
		}

		@Override
		public List<PageLayoutHandler> getCreateLayoutHandlers() {
			return creationlayoutHandlers;
		}

		@Override
		public List<PageElementHandler> getAvailableHandlers() {
			return handlers;
		}

		@Override
		public int indexOf(PageElement element) {
			List<? extends PageElement> elements = form.getElements();
			return elements.indexOf(element);
		}

		@Override
		public PageElement appendPageElement(PageElement element) {
			if(element instanceof AbstractElement) {
				form.addElement((AbstractElement)element);
				persistForm();
			}
			return element;
		}

		@Override
		public PageElement appendPageElementAt(PageElement element, int index) {
			if(element instanceof AbstractElement) {
				form.addElement((AbstractElement)element, index);
				persistForm();
			}
			return element;
		}

		@Override
		public boolean isRemoveConfirmation(PageElement element) {
			if(element instanceof AbstractElement) {
				AbstractElement abstractElement = (AbstractElement)element;
				return ruleHandlerProvider.getRuleHandlers().stream()
						.anyMatch(handler -> handler.isElementHandled(form, abstractElement));
			}
			return false;
		}
		
		@Override
		public String getRemoveConfirmationI18nKey() {
			return "confirm.remove.element.in.rule";
		}

		@Override
		public void removePageElement(PageElement element) {
			if(element instanceof AbstractElement) {
				form.removeElement((AbstractElement)element);
				persistForm();
			}
		}

		@Override
		public void moveUpPageElement(PageElement element) {
			if(element instanceof AbstractElement) {
				form.moveUpElement((AbstractElement)element);
				persistForm();
			}
		}

		@Override
		public void moveDownPageElement(PageElement element) {
			if(element instanceof AbstractElement) {
				form.moveDownElement((AbstractElement)element);
				persistForm();
			}
		}

		@Override
		public void movePageElement(PageElement elementToMove, PageElement sibling, boolean after) {
			if(elementToMove instanceof AbstractElement && (sibling == null || sibling instanceof AbstractElement)) {
				form.moveElement((AbstractElement)elementToMove, (AbstractElement)sibling, after);
				persistForm();
			}
		}
		
		@Override
		public String getImportButtonKey() {
			return null;
		}
	}
}
