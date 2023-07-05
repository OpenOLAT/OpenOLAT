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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.repository.RepositoryEntry;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesRunController extends FormBasicController implements Activateable2 {

	private static final String CMD_DELETE = "delete";
	private static final String CMD_EDIT = "edit";

	private final RepositoryEntry entry;
	private ClassTableModel tableModel;
	private FormLink addLink;
	private CreateBadgeClassWizardContext createBadgeClassContext;
	private StepsMainRunController addStepsController;
	private FlexiTableElement tableEl;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public OpenBadgesRunController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "run");
		this.entry = entry;
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GlobalBadgesController.Cols.image.getI18n(), GlobalBadgesController.Cols.image.ordinal(),
				(renderer, sb, val, row, source, ubu, translator) -> {
					Size targetSize = tableModel.getObject(row).fitIn(60, 60);
					int width = targetSize.getWidth();
					int height = targetSize.getHeight();
					sb.append("<div style='width: ").append(width).append("px; height: ").append(height).append("px;'>");
					sb.append("<div class='o_image'>");
					if (val instanceof String image) {
						sb.append("<img src=\"");
						sb.append(mediaUrl).append("/").append(image).append("\" ");
						sb.append(" width='").append(width).append("px' height='").append(height).append("px' >");
					}
					sb.append("</div>");
					sb.append("</div>");
				}));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GlobalBadgesController.Cols.name.getI18n(), GlobalBadgesController.Cols.name.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GlobalBadgesController.Cols.status.getI18n(), GlobalBadgesController.Cols.status.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GlobalBadgesController.Cols.awardedCount.getI18n(), GlobalBadgesController.Cols.awardedCount.ordinal()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), CMD_EDIT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), CMD_DELETE));

		tableModel = new ClassTableModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "classes", tableModel, getTranslator(),
				formLayout);

		addLink = uifactory.addFormLink("add", "form.add.new.badge", "form.add.new.badge", formLayout, Link.BUTTON);
	}

	private void updateUI() {
		List<OpenBadgesManager.BadgeClassWithSizeAndCount> classesWithSizesAndCounts = openBadgesManager.getBadgeClassesWithSizesAndCounts(entry);
		tableModel.setObjects(classesWithSizesAndCounts);
		tableEl.reset();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addLink) {
			doLaunchAddWizard(ureq);
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent selectionEvent) {
				String command = selectionEvent.getCommand();
				if (CMD_EDIT.equals(command)) {
					doEdit(ureq, tableModel.getObject(selectionEvent.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doEdit(UserRequest ureq, OpenBadgesManager.BadgeClassWithSizeAndCount row) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(row.badgeClass().getUuid());
		createBadgeClassContext = new CreateBadgeClassWizardContext(badgeClass);
		Step start = new CreateBadge02DetailsStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			openBadgesManager.updateBadgeClass(createBadgeClassContext.getBadgeClass());
			updateUI();
			return StepsMainRunController.DONE_MODIFIED;
		};

		addStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("form.add.new.badge"), "o_sel_add_badge_wizard");
		listenTo(addStepsController);
		getWindowControl().pushAsModalDialog(addStepsController.getInitialComponent());

	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == addStepsController) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(addStepsController);
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(addStepsController);
			}
		}
	}

	private void doLaunchAddWizard(UserRequest ureq) {
		createBadgeClassContext = new CreateBadgeClassWizardContext(entry);
		Step start = new CreateBadge00ImageStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			createBadgeClass(createBadgeClassContext);
			updateUI();
			return StepsMainRunController.DONE_MODIFIED;
		};

		addStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("form.add.new.badge"), "o_sel_add_badge_wizard");
		listenTo(addStepsController);
		getWindowControl().pushAsModalDialog(addStepsController.getInitialComponent());
	}

	private void createBadgeClass(CreateBadgeClassWizardContext createBadgeClassContext) {
		BadgeClass badgeClass = createBadgeClassContext.getBadgeClass();
		String image = openBadgesManager.createBadgeClassImageFromSvgTemplate(
				createBadgeClassContext.getSelectedTemplateKey(), createBadgeClassContext.getBackgroundColorId(),
				createBadgeClassContext.getTitle(), getIdentity());
		badgeClass.setImage(image);
		if (badgeClass instanceof BadgeClassImpl badgeClassImpl) {
			openBadgesManager.createBadgeClass(badgeClassImpl);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {

	}

	private class ClassTableModel extends DefaultFlexiTableDataModel<OpenBadgesManager.BadgeClassWithSizeAndCount> {
		public ClassTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			BadgeClass badgeClass = getObject(row).badgeClass();
			return switch (GlobalBadgesController.Cols.values()[col]) {
				case image -> badgeClass.getImage();
				case name -> badgeClass.getName();
				case status -> translate("class.status." + badgeClass.getStatus().name());
				case awardedCount -> getObject(row).count();
			};
		}
	}

	private class BadgeClassMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
			if (classFileLeaf != null) {
				return new VFSMediaResource(classFileLeaf);
			}
			return new NotFoundMediaResource();
		}
	}
}
