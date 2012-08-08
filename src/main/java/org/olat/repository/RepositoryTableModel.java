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
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

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
	//fxdiff VCRP-1,2: access control of resources
	private static final int COLUMN_COUNT = 7;
	Translator translator; // package-local to avoid synthetic accessor method.
	private final ACService acService;
	
	private Map<Long,OLATResourceAccess> repoEntriesWithOffer;
		
	/**
	 * Default constructor.
	 * @param translator
	 */
	public RepositoryTableModel(Translator translator) {
		super(new ArrayList<RepositoryEntry>());
		this.translator = translator;
		repoEntriesWithOffer = new HashMap<Long,OLATResourceAccess>();
		acService = CoreSpringFactory.getImpl(ACService.class);
	}

	/**
	 * @param tableCtr
	 * @param selectButtonLabel Label of action row or null if no action row should be used
	 * @param enableDirectLaunch
	 */
	public void addColumnDescriptors(TableController tableCtr, String selectButtonLabel, boolean enableDirectLaunch) {
		//fxdiff VCRP-1,2: access control of resources
		CustomCellRenderer acRenderer = new RepositoryEntryACColumnDescriptor();
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.ac", 0, null, 
				translator.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, acRenderer));
		tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("table.header.typeimg", 1, null, 
				translator.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.displayname", 2, enableDirectLaunch ? TABLE_ACTION_SELECT_ENTRY : null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 3, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.access", 4, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.date", 5, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastusage", 6, null, translator.getLocale()));
		if (selectButtonLabel != null) {
			StaticColumnDescriptor desc = new StaticColumnDescriptor(TABLE_ACTION_SELECT_LINK, selectButtonLabel, selectButtonLabel);
			desc.setTranslateHeaderKey(false);			
			tableCtr.addColumnDescriptor(desc);
		}
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
		switch (col) {
			//fxdiff VCRP-1,2: access control of resources
			case 0: {
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
			case 1: return re; 
			case 2: return getDisplayName(re, translator.getLocale());
			case 3: return re.getInitialAuthor();
			case 4: {
				//fxdiff VCRP-1,2: access control of resources
				if(re.isMembersOnly()) {
					return translator.translate("table.header.access.membersonly"); 
				}
				switch (re.getAccess()) {
					case RepositoryEntry.ACC_OWNERS: return translator.translate("table.header.access.owner");
					case RepositoryEntry.ACC_OWNERS_AUTHORS: return translator.translate("table.header.access.author");
					case RepositoryEntry.ACC_USERS: return translator.translate("table.header.access.user");
					case RepositoryEntry.ACC_USERS_GUESTS: return translator.translate("table.header.access.guest");
					default:						
						// OLAT-6272 in case of broken repo entries with no access code
						// return error instead of nothing
						return "ERROR";
				}
			}
			case 5: return re.getCreationDate();
			case 6: return re.getLastUsage();
			default: return "ERROR";
		}
	}
	
	
	@Override
	//fxdiff VCRP-1,2: access control of resources
	public void setObjects(List<RepositoryEntry> objects) {
		super.setObjects(objects);
		
		repoEntriesWithOffer = new HashMap<Long,OLATResourceAccess>();
		List<OLATResourceAccess> withOffers = acService.filterRepositoryEntriesWithAC(objects);
		for(OLATResourceAccess withOffer:withOffers) {
			repoEntriesWithOffer.put(withOffer.getResource().getKey(), withOffer);
		}
	}
	
	public void addObject(RepositoryEntry object) {
		getObjects().add(object);
		List<RepositoryEntry> repoList = Collections.singletonList(object);
		List<OLATResourceAccess> withOffers = acService.filterRepositoryEntriesWithAC(repoList);
		for(OLATResourceAccess withOffer:withOffers) {
			repoEntriesWithOffer.put(withOffer.getResource().getKey(), withOffer);
		}
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
