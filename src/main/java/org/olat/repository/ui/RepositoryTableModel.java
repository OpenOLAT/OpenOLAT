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

package org.olat.repository.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DateCellRenderer;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.user.UserManager;

/**
 * Initial Date:  Mar 31, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class RepositoryTableModel extends DefaultTableDataModel<RepositoryEntry> {
	
	/**
	 * Identifies a table selection event (outer-left column)
	 */
	public static final String TABLE_ACTION_SELECT_LINK = "rtbSelectLink";
	/**
	 * Identifies a table selection event (outer-left column)
	 */
	public static final String TABLE_ACTION_REMOVE_LINK = "rtbRemoveLink";
	
	/**
	 * Identifies a table launch event (if clicked on an item in the name column).
	 */
	public static final String TABLE_ACTION_INFOS = "rtbInfos";
	/**
	 * Identifies a multi selection
	 */
	public static final String TABLE_ACTION_SELECT_ENTRIES = "rtbSelectEntrIES";

	private static final int COLUMN_COUNT = 7;
	private final Translator translator;
	private final ACService acService;
	private final AccessControlModule acModule;
	private final RepositoryModule repositoryModule;
	private final RepositoryEntryLifecycleDAO lifecycleDao;
	private final UserManager userManager;
	private final AccessRenderer accessRenderer;
	
	private final Map<Long,OLATResourceAccess> repoEntriesWithOffer = new HashMap<>();
	private final Map<String,String> fullNames = new HashMap<>();
	
	/**
	 * Default constructor.
	 * @param translator
	 */
	public RepositoryTableModel(Locale locale) {
		super(new ArrayList<RepositoryEntry>());
		translator = Util.createPackageTranslator(RepositoryService.class, locale);
		acService = CoreSpringFactory.getImpl(ACService.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		acModule = CoreSpringFactory.getImpl(AccessControlModule.class);
		repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		lifecycleDao = CoreSpringFactory.getImpl(RepositoryEntryLifecycleDAO.class);
		accessRenderer = new AccessRenderer(locale);
	}

	/**
	 * @param tableCtr
	 * @param selectButtonLabel Label of action row or null if no action row should be used
	 * @param enableDirectLaunch
	 * @return the position of the display name column
	 */
	public ColumnDescriptor addColumnDescriptors(TableController tableCtr, boolean selectTitle, boolean selectIcon, boolean remove, boolean infos) {
		Locale loc = translator.getLocale();

		CustomCellRenderer acRenderer = new RepositoryEntryACColumnDescriptor();
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.ac", RepoCols.ac.ordinal(), null, 
				loc, ColumnDescriptor.ALIGNMENT_LEFT, acRenderer) {

			@Override
			public int compareTo(int rowa, int rowb) {
				Object o1 = table.getTableDataModel().getObject(rowa);
				Object o2 = table.getTableDataModel().getObject(rowb);
				if(!(o1 instanceof RepositoryEntry)) return -1;
				if(!(o2 instanceof RepositoryEntry)) return 1;
				RepositoryEntry re1 = (RepositoryEntry)o1;
				RepositoryEntry re2 = (RepositoryEntry)o2;
				
				if(!re1.isAllUsers() && !re1.isGuests()) {
					if(re2.isAllUsers() || re2.isGuests()) {
						return 1;
					}
				} else if(!re1.isAllUsers() && !re1.isGuests()) {
					return -1;
				}
				
				OLATResourceAccess ac1 = repoEntriesWithOffer.get(re1.getOlatResource().getKey());
				OLATResourceAccess ac2 = repoEntriesWithOffer.get(re2.getOlatResource().getKey());
				
				if(ac1 == null && ac2 != null) return -1;
				if(ac1 != null && ac2 == null) return 1;
				if(ac1 != null && ac2 != null) return compareAccess(re1, ac1, re2, ac2);	
				return super.compareString(re1.getDisplayname(), re2.getDisplayname());
			}
			
			private int compareAccess(RepositoryEntry re1, OLATResourceAccess ac1,  RepositoryEntry re2, OLATResourceAccess ac2) {
				int s1 = ac1.getMethods().size();
				int s2 = ac2.getMethods().size();
				int compare = s1 - s2;
				if(compare != 0) return compare;
				if(s1 > 0 && s2 > 0) {
					String t1 = ac1.getMethods().get(0).getMethod().getType();
					String t2 = ac2.getMethods().get(0).getMethod().getType();
					int compareType = super.compareString(t1, t2);
					if(compareType != 0) return compareType;
				}
				return super.compareString(re1.getDisplayname(), re2.getDisplayname());
			}
		});
		tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("table.header.typeimg", RepoCols.repoEntry.ordinal(), null, 
				loc, ColumnDescriptor.ALIGNMENT_LEFT));
		
		String selectAction = selectTitle ? TABLE_ACTION_SELECT_LINK : null;
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.externalid", RepoCols.externalId.ordinal(), selectAction, loc));
		tableCtr.addColumnDescriptor(repositoryModule.isManagedRepositoryEntries(), new DefaultColumnDescriptor("table.header.externalref", RepoCols.externalRef.ordinal(), selectAction, loc));

			boolean lfVisible = lifecycleDao.countPublicLifecycle() > 0;
		tableCtr.addColumnDescriptor(lfVisible, new DefaultColumnDescriptor("table.header.lifecycle.label", RepoCols.lifecycleLabel.ordinal(), null, loc));
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lifecycle.softkey", RepoCols.lifecycleSoftKey.ordinal(), null, loc));
		ColumnDescriptor nameColDesc = new DefaultColumnDescriptor("table.header.displayname", RepoCols.displayname.ordinal(), selectAction, loc) {
			@Override
			public int compareTo(int rowa, int rowb) {
				Object o1 = table.getTableDataModel().getValueAt(rowa, RepoCols.repoEntry.ordinal());
				Object o2 = table.getTableDataModel().getValueAt(rowb, RepoCols.repoEntry.ordinal());
				
				if(!(o1 instanceof RepositoryEntry)) return -1;
				if(!(o2 instanceof RepositoryEntry)) return 1;
				RepositoryEntry re1 = (RepositoryEntry)o1;
				RepositoryEntry re2 = (RepositoryEntry)o2;
				boolean c1 = re1.getEntryStatus() == RepositoryEntryStatusEnum.closed;
				boolean c2 = re2.getEntryStatus() == RepositoryEntryStatusEnum.closed;
				int result = Boolean.compare(c1, c2);
				if(result == 0) {
					Object a = table.getTableDataModel().getValueAt(rowa, dataColumn);
					Object b = table.getTableDataModel().getValueAt(rowb, dataColumn);
					if(!(a instanceof String)) {
						if(!(b instanceof String)) {
							result = 0 ;
						} else {
							result = -1;
						}
					} else if(!(b instanceof String)) {
						result = 1;
					} else {
						result = compareString((String)a, (String)b);
					}
				}
				if(result == 0) {
					result = Long.compare(re1.getKey().longValue(), re2.getKey().longValue());
				}
				return result;
			}
		};
		tableCtr.addColumnDescriptor(nameColDesc);
		
		CustomCellRenderer dateRenderer = new DateCellRenderer(loc);
		tableCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor("table.header.lifecycle.start",
				RepoCols.lifecycleStart.ordinal(), null, loc, ColumnDescriptor.ALIGNMENT_LEFT, dateRenderer));
		tableCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor("table.header.lifecycle.end",
				RepoCols.lifecycleEnd.ordinal(), null, loc, ColumnDescriptor.ALIGNMENT_LEFT, dateRenderer));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", RepoCols.author.ordinal(), null, loc));

		ColumnDescriptor accessColDesc = new CustomRenderColumnDescriptor("table.header.access", RepoCols.repoEntry.ordinal(), null, loc, 
				ColumnDescriptor.ALIGNMENT_LEFT, accessRenderer) {
			@Override
			public int compareTo(int rowa, int rowb) {
				Object o1 = table.getTableDataModel().getValueAt(rowa, 1);
				Object o2 = table.getTableDataModel().getValueAt(rowb, 1);
				
				if(!(o1 instanceof RepositoryEntry)) return -1;
				if(!(o2 instanceof RepositoryEntry)) return 1;
				RepositoryEntry re1 = (RepositoryEntry)o1;
				RepositoryEntry re2 = (RepositoryEntry)o2;
				
				int ar1 = re1.getEntryStatus().ordinal();
				if(!re1.isAllUsers() && !re1.isGuests()) {
					ar1 = 99;
				}
				
				int ar2 = re2.getEntryStatus().ordinal();
				if(!re2.isAllUsers() && !re2.isGuests()) {
					ar2 = 99;
				}
				if(ar1 < ar2) return -1;
				if(ar1 > ar2) return 1;
				return super.compareString(re1.getDisplayname(), re2.getDisplayname());
			}
		};
		tableCtr.addColumnDescriptor(accessColDesc);

		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.date", RepoCols.creationDate.ordinal(), null, loc));
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastusage", RepoCols.lastUsage.ordinal(), null, loc));
		
		if(infos) {
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_INFOS, "info.header", translator.translate("info.header")){
				@Override
				public void renderValue(StringOutput so, int row, Renderer renderer) {
					so.append("<i id='o_core").append(row).append("ref' class='o_icon o_icon-lg o_icon_info_resource'> </i>");
				}
			});
		}
		if(selectIcon) {		
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_SELECT_LINK, "select", translator.translate("table.select")));
		}
		if(remove) {	
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_REMOVE_LINK, "remove", translator.translate("remove")));
		}
		return nameColDesc;
	}
	
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		RepositoryEntry re = getObject(row);
		switch (RepoCols.values()[col]) {
			case ac: {
				if (!re.isAllUsers() && !re.isGuests()) {
					// members only always show lock icon
					return Collections.singletonList("o_ac_membersonly");
				}
				OLATResourceAccess access = repoEntriesWithOffer.get(re.getOlatResource().getKey());
				if(access == null) {
					return null;						
				}
				return access;
			}
			case acGuest: {
				return re.isGuests();
			}
			case repoEntry: return re; 
			case displayname: return getDisplayName(re, translator.getLocale());
			case author: return getFullname(re.getInitialAuthor());
			case access: return accessRenderer.renderEntryStatus(re);
			case creationDate: return re.getCreationDate();
			case lastUsage: return re.getStatistics().getLastUsage();
			case externalId: return re.getExternalId();
			case externalRef: return re.getExternalRef();
			case lifecycleLabel: {
				RepositoryEntryLifecycle lf = re.getLifecycle();
				if(lf == null || lf.isPrivateCycle()) {
					return "";
				}
				return lf.getLabel();
			}
			case lifecycleSoftKey: {
				RepositoryEntryLifecycle lf = re.getLifecycle();
				if(lf == null || lf.isPrivateCycle()) {
					return "";
				}
				return lf.getSoftKey();
			}
			case lifecycleStart: return re.getLifecycle() == null ? null : re.getLifecycle().getValidFrom();
			case lifecycleEnd: return re.getLifecycle() == null ? null : re.getLifecycle().getValidTo();
			default: return "ERROR";
		}
	}
	
	public enum RepoCols {
		ac,
		acGuest,
		repoEntry,
		displayname,
		author,
		access,
		creationDate,
		lastUsage,
		externalId,
		externalRef,
		lifecycleLabel,
		lifecycleSoftKey,
		lifecycleStart,
		lifecycleEnd
	}
	
	
	@Override
	public void setObjects(List<RepositoryEntry> objects) {
		super.setObjects(objects);
		repoEntriesWithOffer.clear();
		secondaryInformations(objects);
	}
	
	public void addObject(RepositoryEntry object) {
		getObjects().add(object);
		secondaryInformations(Collections.singletonList(object));
	}
	
	public void addObjects(List<RepositoryEntry> addedObjects) {
		getObjects().addAll(addedObjects);
		secondaryInformations(addedObjects);
	}
	
	private void secondaryInformations(List<RepositoryEntry> repoEntries) {
		if(repoEntries == null || repoEntries.isEmpty()) return;
		
		secondaryInformationsAccessControl(repoEntries);
		secondaryInformationsUsernames(repoEntries);
	}

	private void secondaryInformationsAccessControl(List<RepositoryEntry> repoEntries) {
		if(repoEntries == null || repoEntries.isEmpty() || !acModule.isEnabled()) return;

		List<OLATResourceAccess> withOffers = acService.filterRepositoryEntriesWithAC(repoEntries);
		for(OLATResourceAccess withOffer:withOffers) {
			repoEntriesWithOffer.put(withOffer.getResource().getKey(), withOffer);
		}
	}
	
	private void secondaryInformationsUsernames(List<RepositoryEntry> repoEntries) {
		if(repoEntries == null || repoEntries.isEmpty()) return;

		Set<String> newNames = new HashSet<>();
		for(RepositoryEntry re:repoEntries) {
			final String author = re.getInitialAuthor();
			if(StringHelper.containsNonWhitespace(author) &&
				!fullNames.containsKey(author)) {
				newNames.add(author);
			}
		}
		
		if(!newNames.isEmpty()) {
			Map<String,String> newFullnames = userManager.getUserDisplayNamesByUserName(newNames);
			fullNames.putAll(newFullnames);
		}
	}
	
	public void removeObject(RepositoryEntry object) {
		getObjects().remove(object);
		repoEntriesWithOffer.remove(object.getOlatResource().getKey());
	}
	
	private String getFullname(String author) {
		if(fullNames.containsKey(author)) {
			return fullNames.get(author);
		}
		return author;
	}

	/**
	 * Get displayname of a repository entry. If repository entry a course 
	 * and is this course closed then add a prefix to the title.
	 */
	private String getDisplayName(RepositoryEntry repositoryEntry, Locale locale) {
		String displayName = repositoryEntry.getDisplayname();
		if (repositoryEntry.getEntryStatus().decommissioned()) {
			Translator pT = Util.createPackageTranslator(RepositoryModule.class, locale);
			displayName = "[" + pT.translate("title.prefix.closed") + "] ".concat(displayName);
		}
		return displayName;
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new RepositoryTableModel(translator.getLocale());
	}
}