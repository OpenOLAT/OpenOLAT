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
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.properties.Property;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.ui.catalog.CatalogEntryAddController;

/**
 * 
 * Description:<br>
 * Step to set the course in the catalog
 * 
 * <P>
 * Initial Date:  16 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://wwww.frentix.com
 */
class PublishStepCatalog extends BasicStep {
	
	private final PrevNextFinishConfig prevNextConfig;
	private final CourseEnvironment courseEnv;
	private final CourseNode rootNode;
	
	public PublishStepCatalog(UserRequest ureq, ICourse course, boolean hasPublishableChanges) {
		super(ureq);

		this.courseEnv = course.getCourseEnvironment();
		this.rootNode = course.getRunStructure().getRootNode();
		setI18nTitleAndDescr("publish.catalog.header", null);
		
		if(hasPublishableChanges) {
			setNextStep(new PublishStep00a(ureq));
			prevNextConfig = PrevNextFinishConfig.BACK_NEXT_FINISH;
		} else {
			setNextStep(Step.NOSTEP);
			prevNextConfig = PrevNextFinishConfig.BACK_FINISH;
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return prevNextConfig;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		return new PublishStepCatalogForm(ureq, wControl, form, stepsRunContext, courseEnv, rootNode);
	}
	
	static class PublishStepCatalogForm extends StepFormBasicController {

		private static final String[] keys = new String[]{"yes","no"};
		
		private FormLink addToCatalog;
		private SingleSelection catalogBox;
		private CloseableModalController cmc;
		private Controller catalogAddController;
		private final List<FormLink> deleteLinks = new ArrayList<>();
		
		private final RepositoryEntry repositoryEntry;
		private final CatalogManager catalogManager;
		private final CourseEnvironment courseEnv;
		private final CourseNode rootNode;
		
		public PublishStepCatalogForm(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext,
				CourseEnvironment courseEnv, CourseNode rootNode) {
			super(ureq, control, rootForm, runContext, LAYOUT_CUSTOM, "publish_catalog");
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, courseEnv.getCourseResourceableId());
			repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
			catalogManager = CoreSpringFactory.getImpl(CatalogManager.class);
			this.courseEnv = courseEnv;
			this.rootNode = rootNode;
			
			initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
			
			Property prop = cpm.findCourseNodeProperty(rootNode, null, null, "catalog-choice");
			String value = prop == null ? null : prop.getStringValue();
			List<CatalogEntry> catalogEntries = catalogManager.getCatalogCategoriesFor(repositoryEntry);
			if("no".equals(value) && !catalogEntries.isEmpty()) {
				value = "yes";
			}
			
			FormItemContainer fc = FormLayoutContainer.createDefaultFormLayout("catalogSettings", getTranslator());
			fc.setRootForm(mainForm);
			formLayout.add("catalogSettings", fc);
			
			final String[] values = new String[] {
					translate("yes"),
					translate("no")
				};
			catalogBox = uifactory.addDropdownSingleselect("catalogBox", "publish.catalog.box", fc, keys, values, null);
			catalogBox.addActionListener(FormEvent.ONCHANGE);
			if(!StringHelper.containsNonWhitespace(value)) {
				value = "yes";
			}
			catalogBox.select(value, true);
			flc.contextPut("choice", value);
			boolean activate = "yes".equals(value);
			addToCatalog = uifactory.addFormLink("publish.catalog.add", flc, Link.BUTTON_SMALL);
			addToCatalog.setElementCssClass("o_sel_publish_add_to_catalog");
			addToCatalog.setVisible(activate);

			for(CatalogEntry entry:catalogEntries) {
				CategoryLabel label = new CategoryLabel(entry, entry.getParent(), getPath(entry));
				FormLink link = uifactory.addFormLink(label.getCategoryUUID(), "delete", null, flc, Link.LINK);
				link.setUserObject(label);
				deleteLinks.add(link);
			}

			flc.contextPut("categories", deleteLinks);
			flc.contextPut("courseTitle", courseEnv.getCourseGroupManager().getCourseEntry().getDisplayname());
		}
		
		private String getPath(CatalogEntry entry) {
			String path = "";
			CatalogEntry tempEntry = entry;
			while (tempEntry != null) {
				path = "/" + tempEntry.getName() + path;
				tempEntry = tempEntry.getParent();
			}
			return path;
		}
		
		private void deleteCategory(CategoryLabel category) {
			for(FormLink deleteLink:deleteLinks) {
				CategoryLabel label = (CategoryLabel)deleteLink.getUserObject();
				if(category.equals(label)) {
					label.setDeleted(true);
					deleteLink.setVisible(false);
					flc.setDirty(true);
				}
			}
		}
		
		private void doAddCatalog(UserRequest ureq) {
			List<CategoryLabel> categories = new ArrayList<>();
			for(FormLink deleteLink:deleteLinks) {
				CategoryLabel label = (CategoryLabel)deleteLink.getUserObject();
				categories.add(label);
			}
			
			removeAsListenerAndDispose(catalogAddController);
			catalogAddController = new SpecialCatalogEntryAddController(ureq, getWindowControl(), repositoryEntry, categories);
			listenTo(catalogAddController);
			removeAsListenerAndDispose(cmc);
			
			String displayName = courseEnv.getCourseGroupManager().getCourseEntry().getDisplayname();
			cmc = new CloseableModalController(getWindowControl(), "close",
					catalogAddController.getInitialComponent(), true, translate("publish.catalog.add.title", new String[] { displayName }));
			listenTo(cmc);
			cmc.activate();
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = true;
			catalogBox.clearError();
			if(catalogBox.isOneSelected() && catalogBox.isSelected(0) && isDeleteLinksEmpty()) {
				catalogBox.setErrorKey("publish.catalog.error", null);
				allOk &= false;
			}
			return allOk && super.validateFormLogic(ureq);
		}
		
		/**
		 * Show if there is active delete links (which is the same has having active categories)
		 * @return
		 */
		private boolean isDeleteLinksEmpty() {
			if(deleteLinks == null || deleteLinks.isEmpty()) return true;
			
			boolean visible = false;
			for(FormLink deleteLink:deleteLinks) {
				visible |= deleteLink.isVisible();
			}
			return !visible;
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(source == cmc) {
				removeAsListenerAndDispose(catalogAddController);
				removeAsListenerAndDispose(cmc);
				catalogAddController = null;
				cmc = null;
			} else if (catalogAddController == source) {
				if(event instanceof AddToCategoryEvent) {
					AddToCategoryEvent e = (AddToCategoryEvent)event;
					CatalogEntry category = e.getCategory();
					CatalogEntry parentCategory = e.getParentCategory();
					CategoryLabel label = new CategoryLabel(category, parentCategory, getPath(parentCategory));
					FormLink link = uifactory.addFormLink(label.getCategoryUUID(), "delete", null, flc, Link.LINK);
					link.setUserObject(label);
					deleteLinks.add(link);
					catalogBox.clearError();
					closeAddCatalog();
				} else if (event instanceof UndoCategoryEvent) {
					UndoCategoryEvent e = (UndoCategoryEvent)event;
					CategoryLabel undelete = e.getUndelete();
					for(FormLink deleteLink:deleteLinks) {
						CategoryLabel label = (CategoryLabel)deleteLink.getUserObject();
						if(label.equals(undelete)) {
							label.setDeleted(false);
							deleteLink.setVisible(true);
							break;
						}
					}
					closeAddCatalog();
				} else if (event == Event.CANCELLED_EVENT) {
					closeAddCatalog();
				}
			} else {
				super.event(ureq, source, event);
			}
		}
		
		private void closeAddCatalog() {
			if(cmc != null) cmc.deactivate();
			removeAsListenerAndDispose(catalogAddController);
			removeAsListenerAndDispose(cmc);
			catalogAddController = null;
			cmc = null;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			CourseCatalog courseCatalog = new CourseCatalog();
			courseCatalog.setChoiceValue(catalogBox.getSelectedKey());
			boolean removeAll = "no".equals(catalogBox.getSelectedKey());
			for(FormLink deletedLink:deleteLinks) {
				CategoryLabel cat = (CategoryLabel)deletedLink.getUserObject();
				if(removeAll) {
					cat.setDeleted(true);
				}
				courseCatalog.getCategoryLabels().add(cat);
			}
			addToRunContext("categories", courseCatalog);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(catalogBox == source) {
				//updateCategories();
				catalogBox.clearError();
				if(catalogBox.isOneSelected() && catalogBox.isSelected(0)) {
					addToCatalog.setVisible(true);
					flc.contextPut("choice", "yes");
					if(deleteLinks == null || deleteLinks.isEmpty()) {
						doAddCatalog(ureq);
					}
				} else {
					addToCatalog.setVisible(false);
					flc.contextPut("choice", "no");
				}
			} else if (source == addToCatalog) {
				doAddCatalog(ureq);
			} else if (deleteLinks.contains(source)) {
				CategoryLabel label = (CategoryLabel)source.getUserObject();
				deleteCategory(label);
				//do by delete updateCategories();
			} else {
				super.formInnerEvent(ureq, source, event);
			}
		}
	}
	
	public static class SpecialCatalogEntryAddController extends CatalogEntryAddController {
		
		private final RepositoryEntry toBeAddedEntry;
		private final List<CategoryLabel> categories;
		
		public SpecialCatalogEntryAddController(UserRequest ureq, WindowControl wControl, RepositoryEntry toBeAddedEntry,
				List<CategoryLabel> categories) {
			super(ureq, wControl, toBeAddedEntry, true, false);

			this.toBeAddedEntry = toBeAddedEntry;
			this.categories = categories;
		}
		
		@Override
		protected VelocityContainer createVelocityContainer(String page) {
			setTranslator(Util.createPackageTranslator(CatalogEntryAddController.class, getLocale()));
			velocity_root = Util.getPackageVelocityRoot(CatalogEntryAddController.class);
			return super.createVelocityContainer(page);
		}

		@Override
		protected void insertNode(UserRequest ureq, Long newParentId) {
			CatalogManager cm = CoreSpringFactory.getImpl(CatalogManager.class);
			CatalogEntry newParent = cm.loadCatalogEntry(newParentId);
			// check first if this repo entry is already attached to this new parent
			for (CategoryLabel label:categories) {
				CatalogEntry category = label.getCategory();
				if(category.getKey() == null) {
					category = label.getParentCategory();
				}
				
				if(category.equalsByPersistableKey(newParent)) {
					if(label.isDeleted()) {
						fireEvent(ureq, new UndoCategoryEvent(label));
					} else {
						showError("catalog.tree.add.already.exists", toBeAddedEntry.getDisplayname());
					}
					return;
				}
			}
			
			CatalogEntry newEntry = cm.createCatalogEntry();
			newEntry.setRepositoryEntry(toBeAddedEntry);
			newEntry.setName(toBeAddedEntry.getDisplayname());
			newEntry.setDescription(toBeAddedEntry.getDescription());
			newEntry.setType(CatalogEntry.TYPE_LEAF);
			newEntry.setParent(newParent);
			fireEvent(ureq, new AddToCategoryEvent(newEntry, newParent));	
		}
	}
	
	public static class UndoCategoryEvent extends Event {
		private static final long serialVersionUID = 1086895594328098215L;
		private final CategoryLabel undelete;
		
		public UndoCategoryEvent(CategoryLabel undelete) {
			super("undelete");
			this.undelete = undelete;
		}

		public CategoryLabel getUndelete() {
			return undelete;
		}
	}
	
	public static class AddToCategoryEvent extends Event {
		private static final long serialVersionUID = -5188729058364093274L;
		private final CatalogEntry category;
		private final CatalogEntry parentCategory;
		
		public AddToCategoryEvent(CatalogEntry category, CatalogEntry parentCategory) {
			super("add-to-catalog");
			this.category = category;
			this.parentCategory = parentCategory;
		}

		public CatalogEntry getCategory() {
			return category;
		}
		
		public CatalogEntry getParentCategory() {
			return parentCategory;
		}
	}
	
	public static class CategoryLabel {
		private final CatalogEntry category;
		private final CatalogEntry parentCategory;
		private final String path;
		private final String uuid;
		
		private boolean deleted = false;
		
		public CategoryLabel(CatalogEntry category, CatalogEntry parentCategory, String path) {
			this.category = category;
			this.parentCategory = parentCategory;
			this.path = path;
			uuid = UUID.randomUUID().toString().replace("-", "");
		}

		public String getCategoryUUID() {
			return uuid;
		}

		public String getPath() {
			return path;
		}

		public CatalogEntry getCategory() {
			return category;
		}

		public CatalogEntry getParentCategory() {
			return parentCategory;
		}

		public boolean isDeleted() {
			return deleted;
		}

		public void setDeleted(boolean deleted) {
			this.deleted = deleted;
		}

		@Override
		public int hashCode() {
			return uuid.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if (obj instanceof CategoryLabel) {
				CategoryLabel label = (CategoryLabel)obj;
				return uuid.equals(label.uuid);
			}
			return false;
		}
	}
}