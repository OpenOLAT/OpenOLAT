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

package org.olat.repository;

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
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.login.LoginModule;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
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
	 * Identifies a table launch event (if clicked on an item in the name column).
	 */
	public static final String TABLE_ACTION_SELECT_ENTRY = "rtbSelectEntry";
	/**
	 * Identifies a multi selection
	 */
	public static final String TABLE_ACTION_SELECT_ENTRIES = "rtbSelectEntrIES";
	//fxdiff VCRP-1,2: access control of resources
	private static final int COLUMN_COUNT = 7;
	private final Translator translator; // package-local to avoid synthetic accessor method.
	private final ACService acService;
	private final AccessControlModule acModule;
	private final RepositoryModule repositoryModule;
	private final RepositoryEntryLifecycleDAO lifecycleDao;
	private final UserManager userManager;
	
	private final Map<Long,OLATResourceAccess> repoEntriesWithOffer = new HashMap<Long,OLATResourceAccess>();;
	private final Map<String,String> fullNames = new HashMap<String, String>();
	
	/**
	 * Default constructor.
	 * @param translator
	 */
	public RepositoryTableModel(Translator translator) {
		super(new ArrayList<RepositoryEntry>());
		this.translator = translator;

		acService = CoreSpringFactory.getImpl(ACService.class);
		acModule = CoreSpringFactory.getImpl(AccessControlModule.class);
		repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		lifecycleDao = CoreSpringFactory.getImpl(RepositoryEntryLifecycleDAO.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
	}

	/**
	 * @param tableCtr
	 * @param selectButtonLabel Label of action row or null if no action row should be used
	 * @param enableDirectLaunch
	 * @return the position of the display name column
	 */
	public int addColumnDescriptors(TableController tableCtr, String selectButtonLabel, boolean enableDirectLaunch) {
		
		
		//fxdiff VCRP-1,2: access control of resources
		CustomCellRenderer acRenderer = new RepositoryEntryACColumnDescriptor();
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.ac", RepoCols.ac.ordinal(), null, 
				translator.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, acRenderer));
		tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("table.header.typeimg", RepoCols.repoEntry.ordinal(), null, 
				translator.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		
		int indexDisplaynameCol = 1;//col 0,1 visible see above
		if(repositoryModule.isManagedRepositoryEntries()) {
			tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.externalid", RepoCols.externalId.ordinal(), null, translator.getLocale()));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.externalref", RepoCols.externalRef.ordinal(), null, translator.getLocale()));
			indexDisplaynameCol++;
		}
		boolean lfVisible = lifecycleDao.countPublicLifecycle() > 0;
		if(lfVisible) {
			indexDisplaynameCol++;
		}
		tableCtr.addColumnDescriptor(lfVisible, new DefaultColumnDescriptor("table.header.lifecycle.label", RepoCols.lifecycleLabel.ordinal(), null, translator.getLocale()));
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lifecycle.softkey", RepoCols.lifecycleSoftKey.ordinal(), null, translator.getLocale()));

		indexDisplaynameCol++;//see above
		ColumnDescriptor nameColDesc = new DefaultColumnDescriptor("table.header.displayname", RepoCols.displayname.ordinal(), enableDirectLaunch ? TABLE_ACTION_SELECT_ENTRY : null, translator.getLocale()) {
			@Override
			public int compareTo(int rowa, int rowb) {
				Object o1 =table.getTableDataModel().getValueAt(rowa, 1);
				Object o2 = table.getTableDataModel().getValueAt(rowb, 1);
				
				if(o1 == null || !(o1 instanceof RepositoryEntry)) return -1;
				if(o2 == null || !(o2 instanceof RepositoryEntry)) return 1;
				RepositoryEntry re1 = (RepositoryEntry)o1;
				RepositoryEntry re2 = (RepositoryEntry)o2;
				boolean c1 = RepositoryManager.getInstance().createRepositoryEntryStatus(re1.getStatusCode()).isClosed();
				boolean c2 = RepositoryManager.getInstance().createRepositoryEntryStatus(re2.getStatusCode()).isClosed();
				int result = (c2 == c1 ? 0 : (c1 ? 1 : -1));//same as Boolean compare
				if(result == 0) {
					Object a = table.getTableDataModel().getValueAt(rowa, dataColumn);
					Object b = table.getTableDataModel().getValueAt(rowb, dataColumn);
					if(a == null || !(a instanceof String)) return -1;
					if(b == null || !(b instanceof String)) return 1;
					String s1 = (String)a;
					String s2 = (String)b;
					result = compareString(s1, s2);
				}
				return result;
			}
		};
		tableCtr.addColumnDescriptor(nameColDesc);
		
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lifecycle.start", RepoCols.lifecycleStart.ordinal(), null, translator.getLocale()));
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lifecycle.end", RepoCols.lifecycleEnd.ordinal(), null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", RepoCols.author.ordinal(), null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.access", RepoCols.access.ordinal(), null, translator.getLocale()));
		
		
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.date", RepoCols.creationDate.ordinal(), null, translator.getLocale()));
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastusage", RepoCols.lastUsage.ordinal(), null, translator.getLocale()));
		if (selectButtonLabel != null) {
			StaticColumnDescriptor desc = new StaticColumnDescriptor(TABLE_ACTION_SELECT_LINK, selectButtonLabel, selectButtonLabel);
			desc.setTranslateHeaderKey(false);			
			tableCtr.addColumnDescriptor(desc);
		}
		
		return indexDisplaynameCol;
	}
	
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		RepositoryEntry re = (RepositoryEntry)getObject(row);
		switch (RepoCols.values()[col]) {
			//fxdiff VCRP-1,2: access control of resources
			case ac: {
				if (re.isMembersOnly()) {
					// members only always show lock icon
					List<String> types = new ArrayList<String>(1);
					types.add("b_access_membersonly");
					return types;
				}
				OLATResourceAccess access = repoEntriesWithOffer.get(re.getOlatResource().getKey());
				if(access == null) {
					return null;						
				}
				return access;
			}
			case repoEntry: return re; 
			case displayname: return getDisplayName(re, translator.getLocale());
			case author: return getFullname(re.getInitialAuthor());
			case access: {
				//fxdiff VCRP-1,2: access control of resources
				if(re.isMembersOnly()) {
					return translator.translate("table.header.access.membersonly"); 
				}
				switch (re.getAccess()) {
					case RepositoryEntry.ACC_OWNERS: return translator.translate("table.header.access.owner");
					case RepositoryEntry.ACC_OWNERS_AUTHORS: return translator.translate("table.header.access.author");
					case RepositoryEntry.ACC_USERS: return translator.translate("table.header.access.user");
					case RepositoryEntry.ACC_USERS_GUESTS: {
						if(!LoginModule.isGuestLoginLinksEnabled()) {
							return translator.translate("table.header.access.user");
						}
						return translator.translate("table.header.access.guest");
					}
					default:						
						// OLAT-6272 in case of broken repo entries with no access code
						// return error instead of nothing
						return "ERROR";
				}
			}
			case creationDate: return re.getCreationDate();
			case lastUsage: return re.getLastUsage();
			case externalId: return re.getExternalId();
			case externalRef: return re.getExternalRef();
			case lifecycleLabel: return re.getLifecycle() == null ? null : re.getLifecycle().getLabel();
			case lifecycleSoftKey: return re.getLifecycle() == null ? null : re.getLifecycle().getSoftKey();
			case lifecycleStart: return re.getLifecycle() == null ? null : re.getLifecycle().getValidFrom();
			case lifecycleEnd: return re.getLifecycle() == null ? null : re.getLifecycle().getValidTo();
			default: return "ERROR";
		}
	}
	
	public enum RepoCols {
		ac,
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
	//fxdiff VCRP-1,2: access control of resources
	public void setObjects(List<RepositoryEntry> objects) {
		super.setObjects(objects);
		repoEntriesWithOffer.clear();
		secondaryInformations(objects);
	}
	
	public void addObject(RepositoryEntry object) {
		getObjects().add(object);
		List<RepositoryEntry> objects = Collections.singletonList(object);
		secondaryInformations(objects);
	}
	
	public void addObjects(List<RepositoryEntry> objects) {
		getObjects().addAll(objects);
		secondaryInformations(objects);
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

		Set<String> newNames = new HashSet<String>();
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
		if (repositoryEntry != null && RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
			PackageTranslator pT = new PackageTranslator(RepositoryEntryStatus.class.getPackage().getName(), locale);
			displayName = "[" + pT.translate("title.prefix.closed") + "] ".concat(displayName);
		}
		return displayName;
	}

	public Object createCopyWithEmptyList() {
		RepositoryTableModel copy = new RepositoryTableModel(translator);
		return copy;
	}

}
