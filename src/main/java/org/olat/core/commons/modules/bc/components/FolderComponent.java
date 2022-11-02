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
* <p>
*/ 

package org.olat.core.commons.modules.bc.components;

import java.text.Collator;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.modules.bc.FolderLoggingAction;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.commands.FolderCommandFactory;
import org.olat.core.commons.modules.bc.comparators.LockComparator;
import org.olat.core.commons.services.analytics.AnalyticsModule;
import org.olat.core.commons.services.analytics.AnalyticsSPI;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

/**
 * Initial Date:  Feb 11, 2004
 * @author Mike Stock
 */
public class FolderComponent extends AbstractComponent {
	private static final Logger log = Tracing.createLoggerFor(FolderComponent.class);
 	private static final ComponentRenderer RENDERER = new FolderComponentRenderer();
 	
	public static final String SORT_NAME = "name";
	public static final String SORT_SIZE = "size";
	public static final String SORT_DATE = "date";
	public static final String SORT_REV = "revision";
	public static final String SORT_LOCK = "lock";

	protected boolean sortAsc = true;													// asc or desc?
	protected String sortCol = "";  													// column to sort
	protected boolean canMail = false;
	
	private IdentityEnvironment identityEnv;
	private VFSContainer rootContainer;
	private VFSContainer currentContainer;
	private String currentContainerPath;
	private String currentSortOrder;
	// need to know our children in advance in order to be able to identify them later...
	private List<VFSItem> currentContainerChildren;
	private final Collator collator;
	private Comparator<VFSItem> comparator;
	protected Translator translator;
	private VFSItemFilter filter;
	private final DateFormat dateTimeFormat;
	private VFSItemFilter exclFilter;
	private CustomLinkTreeModel customLinkTreeModel;
	private final VFSContainer externContainerForCopy;
	
	private final AnalyticsSPI analyticsSpi;
	private final VFSRepositoryModule vfsRepositoryModule;
	private final VFSRepositoryService vfsRepositoryService;

	/**
	 * Wraps the folder module as a component.
	 * 
	 * @param ureq
	 *            The user request
	 * @param name
	 *            The component name
	 * @param rootContainer
	 *            The base container of this component
	 * @param filter
	 *            A file filter or NULL to not use a filter
	 * @param customLinkTreeModel
	 *            A custom link tree model used in the HTML editor or NULL to
	 *            not use this feature.
	 */
	public FolderComponent(UserRequest ureq, String name,
			VFSContainer rootContainer, VFSItemFilter filter,
			CustomLinkTreeModel customLinkTreeModel) {
		this(ureq, name, rootContainer, filter, customLinkTreeModel, null);
	}
	
	public FolderComponent(UserRequest ureq, String name,
			VFSContainer rootContainer, VFSItemFilter filter,
			CustomLinkTreeModel customLinkTreeModel, VFSContainer externContainerForCopy) {
		super(name);
		analyticsSpi = CoreSpringFactory.getImpl(AnalyticsModule.class).getAnalyticsProvider();
		vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		vfsRepositoryModule = CoreSpringFactory.getImpl(VFSRepositoryModule.class);
		
		this.identityEnv = ureq.getUserSession().getIdentityEnvironment();
		this.filter = filter;
		this.customLinkTreeModel = customLinkTreeModel;
		this.externContainerForCopy = externContainerForCopy;
		exclFilter = new VFSSystemItemFilter();
		Locale locale = ureq.getLocale();
		collator = Collator.getInstance(locale);
		translator = Util.createPackageTranslator(FolderRunController.class, locale);
		sort(SORT_NAME);
		this.rootContainer = rootContainer;
		setCurrentContainerPath("/");
		
		dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		if (ureq.getParameter(ListRenderer.PARAM_EDTID) != null) {
			fireEvent(ureq, new Event(FolderCommandFactory.COMMAND_EDIT));
			return;
		} else if (ureq.getParameter(ListRenderer.PARAM_CONTENT_EDIT_ID) != null) {
			fireEvent(ureq, new Event(FolderCommandFactory.COMMAND_EDIT_CONTENT));
			return;
		} else if (ureq.getParameter(ListRenderer.PARAM_SERV) != null) {
			// this is a link on a file... deliver it
			fireEvent(ureq, new Event(FolderCommandFactory.COMMAND_SERV));
			// don't redraw the file listing when serving a resource -> timestamp not consumed
			setDirty(false);
			return;
			} else if (ureq.getParameter(ListRenderer.PARAM_SORTID) != null) {			// user clicked on table header for sorting column
				setSortAsc(ureq.getParameter(ListRenderer.PARAM_SORTID));
				sort(ureq.getParameter(ListRenderer.PARAM_SORTID));										// just pass selected column
				return;
		} else if (ureq.getParameter("cid") != null) { // user clicked add layer...
			fireEvent(ureq, new Event(ureq.getParameter("cid")));
			return;
		} else if (ureq.getParameter(ListRenderer.PARAM_VERID) != null) {
			fireEvent(ureq, new Event(FolderCommandFactory.COMMAND_VIEW_VERSION));
			return;
		} else if (ureq.getParameter(ListRenderer.PARAM_EPORT) != null) {
				fireEvent(ureq, new Event(FolderCommandFactory.COMMAND_ADD_EPORTFOLIO));
				return;
		} else if (ureq.getParameter(ListRenderer.PARAM_SERV_THUMBNAIL) != null) {
			// this is a link on a file... deliver it
			fireEvent(ureq, new Event(FolderCommandFactory.COMMAND_SERV_THUMBNAIL));
			// don't redraw the file listing when serving a resource -> timestamp not consumed
			setDirty(false);
			return;
		} else if (ureq.getParameter(ListRenderer.PARAM_VIEW_AUDIO_VIDEO) != null) {
			fireEvent(ureq, new Event(FolderCommandFactory.COMMAND_VIEW_AUDIO_VIDEO));
			return;
		}

		// regular browsing, set current container
		setCurrentContainerPath(ureq.getModuleURI());
		// do logging
		ThreadLocalUserActivityLogger.log(FolderLoggingAction.BC_FOLDER_READ, getClass(), 
				CoreLoggingResourceable.wrapBCFile(getCurrentContainerPath()));
		fireEvent(ureq, new Event(FolderCommandFactory.COMMAND_BROWSE));
	}

	private void setSortAsc(String col) {
		if (col == null) col = SORT_NAME;																								// "clicked" column not existent
		if (!sortCol.equals(col)) {																											// if not same col as before, change sort col and sort asc
			sortCol = col;
			sortAsc = true;
		} else {																																				// if same col as before, just change sorting to desc
			sortAsc = !sortAsc;
		}		
	}
	
	public boolean isCanMail() {
		return canMail;
	}
	
	public void setCanMail(boolean canMail) {
		this.canMail = canMail;
	}
	
	public AnalyticsSPI getAnalyticsSPI() {
		return analyticsSpi;
	}

	/**
	 * Sorts the bc folder components table
	 * 
	 * @param col The column to sort
	 */
	private void sort(String col) {
		currentSortOrder = col;
		if (col.equals(SORT_NAME)) {																										// sort after file name?
			comparator = new Comparator<VFSItem>() {
				@Override
				public int compare(VFSItem o1, VFSItem o2) {
					if (sortAsc) {
						if ((o1 instanceof VFSLeaf && o2 instanceof VFSLeaf) || (!(o1 instanceof VFSLeaf) && !(o2 instanceof VFSLeaf))) {
							return collator.compare(o1.getName(), o2.getName());
						} else {
							if (!(o1 instanceof VFSLeaf)) {

								return -1;
							} else {
								return 1;
							}
						}
					} else {
						if ((o1 instanceof VFSLeaf && o2 instanceof VFSLeaf) || (!(o1 instanceof VFSLeaf) && !(o2 instanceof VFSLeaf))) {
							return collator.compare(o2.getName(), o1.getName());
						} else {
							if (!(o1 instanceof VFSLeaf)) {

								return -1;
							} else {
								return 1;
							}
						}
					}
				}
			};
		} else if (col.equals(SORT_DATE)) {																							// sort after modification date (if same, then name)
			comparator = new Comparator<VFSItem>() {
				@Override
				public int compare(VFSItem o1, VFSItem o2) {
					if      (o1.getLastModified() < o2.getLastModified()) return ((sortAsc) ? -1 :  1);			
					else if (o1.getLastModified() > o2.getLastModified()) return ((sortAsc) ?  1 : -1);
					else {
						if (sortAsc) return collator.compare(o1.getName(), o2.getName());
						else         return collator.compare(o2.getName(), o1.getName());
					}
				}
			};
		} else	if (col.equals(SORT_SIZE)) {																						// sort after file size, folders always on top
			comparator = new Comparator<VFSItem>() {
				@Override
				public int compare(VFSItem o1, VFSItem o2) {
					VFSLeaf leaf1 = null;
					if (o1 instanceof VFSLeaf) {
						leaf1 = (VFSLeaf)o1;
					}
					VFSLeaf leaf2 = null;
					if (o2 instanceof VFSLeaf) {
						leaf2 = (VFSLeaf)o2;
					}
					if				(leaf1 == null && leaf2 != null) return -1;											// folders are always smaller
					else if		(leaf1 != null && leaf2 == null) return  1;											// folders are always smaller		
					else if		(leaf1 == null && leaf2 == null)  															// if two folders, sort after name
						if (sortAsc) return collator.compare(o1.getName(), o2.getName());
						else				 return collator.compare(o2.getName(), o1.getName());
					else  																																		// if two leafes, sort after size
						if (sortAsc) return ((leaf1.getSize() < leaf2.getSize()) ? -1 :  1);
						else				 return ((leaf1.getSize() < leaf2.getSize()) ?  1 : -1);
				}
			};
		} else if (col.equals(SORT_REV)) {																							// sort after revision number, folders always on top
			comparator = new Comparator<VFSItem>() {
				@Override
				public int compare(VFSItem o1, VFSItem o2) {
					return o1.getName().compareTo(o2.getName());
					/*
					Versionable v1 = null;
					Versionable v2 = null;
					if (o1 instanceof Versionable) {
						v1 = (Versionable)o1;
					}
					if (o2 instanceof Versionable) {
						v2 = (Versionable)o2;
					}
					if(v1 == null) {
						return -1;
					} else if (v2 == null) {
						return 1;
					}
					
					String r1 = v1.getVersions().getRevisionNr();
					String r2 = v2.getVersions().getRevisionNr();
					if(r1 == null) {
						return -1;
					} else if (r2 == null) {
						return 1;
					}
					return (sortAsc) ? collator.compare(r1, r2) : collator.compare(r2, r1);
					*/
				}
			};
		}  else if (col.equals(SORT_LOCK)) {																							// sort after modification date (if same, then name)
			comparator = new LockComparator(sortAsc, collator);
		} 
		if (currentContainerChildren != null) updateChildren();													// if not empty the update list
	}

	public VFSContainer getRootContainer() {
		return rootContainer;
	}

	public VFSContainer getCurrentContainer() {
		return currentContainer;
	}

	public String getCurrentContainerPath() {
		return currentContainerPath;
	}

	/**
	 * Return the children of the folder of this FolderComponent.
	 * The children are already alphabetically sorted.
	 * @return
	 */
	public List<VFSItem> getCurrentContainerChildren() {
		return currentContainerChildren;
	}
	
	/**
	 * @return the sort order, one of the SORT_* static variables
	 */
	public String getCurrentSortOrder() {
		return currentSortOrder;
	}
	
	/**
	 * @return true: sorted ascending; false: sorted descending
	 */
	public boolean isCurrentSortAsc() {
		return sortAsc;
	}
	
	public void updateChildren() {
		setDirty(true);
		//check if the container is still up-to-date, if not -> return to root
		if(!currentContainer.exists()) {
			currentContainer = rootContainer;
			currentContainerPath = "/";
		}

		// get the children and sort them alphabetically
		List<VFSItem> children;
		if (filter != null) {
			children = currentContainer.getItems(filter);
		} else {
			children = currentContainer.getItems(new VFSSystemItemFilter());			
		}
		// OLAT-5256: filter .nfs files
		for(Iterator<VFSItem> it = children.iterator(); it.hasNext(); ) {
			if (!exclFilter.accept(it.next())) {
				it.remove();
			}
		}
		try {
			Collections.sort(children, comparator);
		} catch (Exception e) {
			log.error("", e);
		}
		
		currentContainerChildren = children;
	}
	
	public boolean setCurrentContainerPath(String relPath) {
		// get the container
		setDirty(true);
		
		if (relPath == null) relPath = "/";
		if (relPath.charAt(0) != '/') relPath = "/" + relPath;
		VFSItem vfsItem = rootContainer.resolve(relPath);
		if (!(vfsItem instanceof VFSContainer)) {
			// unknown path, reset to root contaner...
			currentContainer = rootContainer;
			return false;
		}
		
		currentContainer = (VFSContainer)vfsItem;
		if(currentContainer.canMeta() == VFSConstants.YES && !vfsRepositoryModule.isMigrated()) {
			VFSMetadata currentMetadata = vfsRepositoryService.getMetadataFor(currentContainer);
			if(currentMetadata != null && !"migrated".equals(currentMetadata.getMigrated())) {
				vfsRepositoryService.migrate(currentContainer, currentMetadata);
			}
		}
		
		currentContainerPath = relPath;
		updateChildren();
		return true;
	}

	/**
	 * Set a custom link tree model that is used in the HTML editor to create
	 * links
	 * 
	 * @param customLinkTreeModel
	 *            The link tree model or NULL to not use this feature in the
	 *            editor
	 */
	public void setCustomLinkTreeModel(CustomLinkTreeModel customLinkTreeModel) {
		this.customLinkTreeModel = customLinkTreeModel;
	}

	/**
	 * Get the custom link tree model to build links in the editor
	 * 
	 * @return The custom link tree model or NULL if no such model is used.
	 */
	public CustomLinkTreeModel getCustomLinkTreeModel() {
		return this.customLinkTreeModel; 
	}
	
	public VFSContainer getExternContainerForCopy() {
		return externContainerForCopy;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public IdentityEnvironment getIdentityEnvironnement() {
		return identityEnv;
	}

	public DateFormat getDateTimeFormat() {
		return dateTimeFormat;
	}
}