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

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.catalog.ui.CatalogAjaxAddController;
import org.olat.catalog.ui.CatalogEntryAddController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeClickedEvent;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

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
	
	class PublishStepCatalogForm extends StepFormBasicController {
		
		private FormLink addToCatalog;
		private SingleSelection catalogBox;
		private CloseableModalController cmc;
		private Controller catalogAddController;
		private final List<FormLink> deleteLinks = new ArrayList<FormLink>();;
		
		private final RepositoryEntry repositoryEntry;
		private final CatalogManager catalogManager;
		private final CourseEnvironment courseEnv;
		private final CourseNode rootNode;
		
		public PublishStepCatalogForm(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext,
				CourseEnvironment courseEnv, CourseNode rootNode) {
			super(ureq, control, rootForm, runContext, LAYOUT_CUSTOM, "publish_catalog");
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, courseEnv.getCourseResourceableId());
			repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
			catalogManager = CatalogManager.getInstance();
			this.courseEnv = courseEnv;
			this.rootNode = rootNode;
			
			initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
			
			Property prop = cpm.findCourseNodeProperty(rootNode, null, null, "catalog-choice");
			String value = prop == null ? null : prop.getStringValue();
			
			FormItemContainer fc = FormLayoutContainer.createDefaultFormLayout("catalogSettings", getTranslator());
			fc.setRootForm(mainForm);
			formLayout.add("catalogSettings", fc);
			
			final String[] keys = new String[]{"yes","no"};
			final String[] values = new String[] {
					translate("yes"),
					translate("no")
				};
			catalogBox = uifactory.addDropdownSingleselect("catalogBox", "publish.catalog.box", fc, keys, values, null);
			catalogBox.addActionListener(this, FormEvent.ONCHANGE);
			if(!StringHelper.containsNonWhitespace(value)) {
				value = "yes";
			}
			catalogBox.select(value, true);
			flc.contextPut("choice", value);
			boolean activate = "yes".equals(value);
			addToCatalog = uifactory.addFormLink("publish.catalog.add", flc, Link.BUTTON_SMALL);
			addToCatalog.setVisible(activate);
			if(activate) {
				List<CatalogEntry> catalogEntries = catalogManager.getCatalogCategoriesFor(repositoryEntry);
				for(CatalogEntry entry:catalogEntries) {
					CategoryLabel label = new CategoryLabel(entry, entry.getParent(), getPath(entry));
					FormLink link = uifactory.addFormLink(label.getCategoryUUID(), "delete", null, flc, Link.LINK);
					link.setUserObject(label);
					deleteLinks.add(link);
				}
			}
			flc.contextPut("categories", deleteLinks);
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
			List<CategoryLabel> categories = new ArrayList<CategoryLabel>();
			for(FormLink deleteLink:deleteLinks) {
				CategoryLabel label = (CategoryLabel)deleteLink.getUserObject();
				categories.add(label);
			}
			
			removeAsListenerAndDispose(catalogAddController);
			if (getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled()) {
				catalogAddController = new SpecialCatalogAjaxAddController(ureq, getWindowControl(), repositoryEntry, categories);
			} else {
				catalogAddController = new SpecialCatalogEntryAddController(ureq, getWindowControl(), repositoryEntry, categories);
			}

			listenTo(catalogAddController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", catalogAddController.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}

		@Override
		protected void doDispose() {
			//
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
				}
				cmc.deactivate();
				removeAsListenerAndDispose(catalogAddController);
				removeAsListenerAndDispose(cmc);
				catalogAddController = null;
				cmc = null;
			} else {
				super.event(ureq, source, event);
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			if(catalogBox.isOneSelected()) {
				String val = catalogBox.getSelectedKey();
				addToRunContext("catalogChoice", val);
				
				List<CategoryLabel> categories = new ArrayList<CategoryLabel>();
				for(FormLink deletedLink:deleteLinks) {
					CategoryLabel cat = (CategoryLabel)deletedLink.getUserObject();
					categories.add(cat);
				}
				addToRunContext("categories", categories);
			}
			
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
	
	public class SpecialCatalogAjaxAddController extends CatalogAjaxAddController {
		
		private CategoryLabel undoDelete;
		private List<CategoryLabel> categories;
		
		public SpecialCatalogAjaxAddController(UserRequest ureq, WindowControl wControl, RepositoryEntry toBeAddedEntry,
				List<CategoryLabel> categories) {
			super(ureq, wControl, toBeAddedEntry);
			this.categories = categories;
		}

		@Override
		protected VelocityContainer createVelocityContainer(String page) {
			setTranslator(Util.createPackageTranslator(CatalogAjaxAddController.class, getLocale()));
			velocity_root = Util.getPackageVelocityRoot(CatalogAjaxAddController.class);
			return super.createVelocityContainer(page);
		}
		
		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if (source == treeCtr) {
				if (event instanceof TreeNodeClickedEvent) {
					TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
					// build new entry for this catalog level
					CatalogManager cm = CatalogManager.getInstance();
					String nodeId = clickedEvent.getNodeId();
					Long newParentId = Long.parseLong(nodeId);
					CatalogEntry newParent = cm.loadCatalogEntry(newParentId);
					// check first if this repo entry is already attached to this new parent
					for (CategoryLabel label:categories) {
						CatalogEntry category = label.getCategory();
						if(category.getKey() == null) {
							category = label.getParentCategory();
						}
						
						if(category.equalsByPersistableKey(newParent)) {
							if(label.isDeleted()) {
								undoDelete = label;
							} else {
								showError("catalog.tree.add.already.exists", toBeAddedEntry.getDisplayname());
								return;
							}
						}
					}
					// don't create entry right away, user must select submit button first
					selectedParent = newParent;
					// enable link, set dirty button class and trigger redrawing
					selectLink.setEnabled(true);
					selectLink.setCustomEnabledLinkCSS("b_button b_button_dirty");
					selectLink.setDirty(true); 
				}
			}
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			 if (source == selectLink) {
				if(undoDelete != null) {
					fireEvent(ureq, new UndoCategoryEvent(undoDelete));
				} else if (selectedParent != null) {
					CatalogManager cm = CatalogManager.getInstance();
					CatalogEntry newEntry = cm.createCatalogEntry();
					newEntry.setRepositoryEntry(toBeAddedEntry);
					newEntry.setName(toBeAddedEntry.getDisplayname());
					newEntry.setDescription(toBeAddedEntry.getDescription());
					newEntry.setType(CatalogEntry.TYPE_LEAF);
					newEntry.setParent(selectedParent);
					fireEvent(ureq, new AddToCategoryEvent(newEntry, selectedParent));								
				}
			} else {
				super.event(ureq, source, event);
			}
		}
	}
	
	public class SpecialCatalogEntryAddController extends CatalogEntryAddController {
		
		private final RepositoryEntry toBeAddedEntry;
		private final List<CategoryLabel> categories;
		
		public SpecialCatalogEntryAddController(UserRequest ureq, WindowControl wControl, RepositoryEntry toBeAddedEntry,
				List<CategoryLabel> categories) {
			super(ureq, wControl, toBeAddedEntry);

			this.toBeAddedEntry = toBeAddedEntry;
			this.categories = categories;
		}
		
		@Override
		protected VelocityContainer createVelocityContainer(String page) {
			setTranslator(Util.createPackageTranslator(CatalogAjaxAddController.class, getLocale()));
			velocity_root = Util.getPackageVelocityRoot(CatalogAjaxAddController.class);
			return super.createVelocityContainer(page);
		}
		
		@Override
		public void event(UserRequest ureq, Component source, Event event) {
			if (source instanceof SelectionTree) {
				TreeEvent te = (TreeEvent) event;
				if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
					CatalogManager cm = CatalogManager.getInstance();
					Long newParentId = Long.parseLong(te.getNodeId());
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

				} else if (te.getCommand().equals(TreeEvent.COMMAND_CANCELLED)) {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			}

		}
	}
	
	public class UndoCategoryEvent extends Event {
		private final CategoryLabel undelete;
		
		public UndoCategoryEvent(CategoryLabel undelete) {
			super("undelete");
			this.undelete = undelete;
		}

		public CategoryLabel getUndelete() {
			return undelete;
		}
	}
	
	public class AddToCategoryEvent extends Event {
		
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
	
	public class CategoryLabel {
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