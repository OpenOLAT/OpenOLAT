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
package org.olat.modules.openbadges.ui;

import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesManager;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge00ImageStep extends BasicStep {

	private final CreateBadgeClassWizardContext createBadgeClassContext;

	public CreateBadge00ImageStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		this.createBadgeClassContext = createBadgeClassContext;
		setI18nTitleAndDescr("form.image", null);
		setNextStep(new CreateBadge02DetailsStep(ureq));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		runContext.put(CreateBadgeClassWizardContext.KEY, createBadgeClassContext);
		return new CreateBadge00ImageForm(ureq, wControl, form, runContext);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	private class CreateBadge00ImageForm extends StepFormBasicController {

		private CreateBadgeClassWizardContext createContext;
		private List<Card> cards;

		@Autowired
		OpenBadgesManager openBadgesManager;

		public CreateBadge00ImageForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "image_step");

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			initForm(ureq);

			flc.getFormItemComponent().addListener(this);
		}

		@Override
		public void event(UserRequest ureq, Component source, Event event) {
			if ("select".equals(event.getCommand())) {
				String templateKeyString = ureq.getParameter("templateKey");
				if (templateKeyString != null) {
					long templateKey = Long.parseLong(templateKeyString);
					doSelectTemplate(templateKey);
				}
			}
			super.event(ureq, source, event);
		}

		private void doSelectTemplate(long templateKey) {
			BadgeTemplate template = openBadgesManager.getTemplate(templateKey);
			BadgeClass badgeClass = createContext.getBadgeClass();
			badgeClass.setName(template.getName());
			badgeClass.setDescription(template.getDescription());
			createContext.setSelectedTemplateKey(template.getKey());
			createContext.setSelectedTemplateImage(template.getImage());
			flc.contextPut("chooseTemplate", false);
			flc.contextPut("card", findCard(templateKey));
		}

		private Card findCard(long templateKey) {
			return cards.stream().filter(c -> c.key == templateKey).findFirst().orElse(null);
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			super.event(ureq, source, event);
		}

		@Override
		protected void formNext(UserRequest ureq) {
			if (createContext.needsCustomization()) {
				setNextStep(new CreateBadge01CustomizationStep(ureq));
			} else {
				setNextStep(new CreateBadge02DetailsStep(ureq));
			}
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			flc.contextPut("chooseTemplate", createContext.getSelectedTemplateKey() == null);

			String mediaUrl = registerMapper(ureq, new BadgeImageMapper());
			cards = openBadgesManager.getTemplatesWithSizes().stream()
					.map(template -> {
						Size targetSize = template.fitIn(120, 66);
						return new Card(
								template.template().getKey(),
								template.template().getName(),
								mediaUrl + "/" + template.template().getImage(),
								targetSize.getWidth(), targetSize.getHeight());
					})
					.toList();
			flc.contextPut("cards", cards);
		}

		public record Card(Long key, String name, String imageSrc, int width, int height) {
		}

		private class BadgeImageMapper implements Mapper {

			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				VFSLeaf templateLeaf = openBadgesManager.getTemplateVfsLeaf(relPath);
				if (templateLeaf != null) {
					return new VFSMediaResource(templateLeaf);
				}
				return new NotFoundMediaResource();
			}
		}
	}
}
