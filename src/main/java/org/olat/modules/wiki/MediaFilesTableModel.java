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
package org.olat.modules.wiki;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.user.UserManager;

/**
 * Description:<br>
 * Generates an table containing media files in the wiki. Elements are @see org.olat.modules.wiki.MediaFileElement
 * <P>
 * Initial Date: Nov 6, 2006 <br>
 * 
 * @author guido
 */
public class MediaFilesTableModel extends DefaultTableDataModel<MediaFileElement> {
	private static final int COLUMN_COUNT = 5;
	private Formatter formatter;
	private UserManager userManager;

	public MediaFilesTableModel(List<MediaFileElement> objects, Translator trans) {
		super(objects);
		setLocale(trans.getLocale());
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		formatter = Formatter.getInstance(trans.getLocale());
	}

	public void addColumnDescriptors(TableController tableCtr) {
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.filename", 0, WikiMainController.ACTION_SHOW_MEDIA,
				getLocale(), 1, new StrikeThroughCellRenderer()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.created.by", 1, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.creation.date", 2, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.deleted.by", 3, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.deleted.at", 4, null, getLocale()));
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		MediaFileElement entry = getEntryAt(row);
		switch (col) {
			case 0:
				String filename = entry.getFilename();
				if (filename.length() > 40) {
					if(filename.endsWith(WikiMainController.METADATA_SUFFIX)) return entry.getFilename().substring(0, 40) + WikiMainController.METADATA_SUFFIX;
					return entry.getFilename().substring(0, 40) + "...";
				}
				return entry.getFilename();
			case 1:
				long identKey = entry.getCreatedBy();
				if (identKey == 0) return "---";
				return userManager.getUserDisplayName(Long.valueOf(identKey));
			case 2:
				return formatter.formatDateAndTime(new Date(entry.getCreationDate()));
			case 3:
				long key = entry.getDeletedBy();
				if (key == 0) return "---";
				return userManager.getUserDisplayName(Long.valueOf(key));
			case 4:
				long delDate = entry.getDeletionDate();
				if (delDate == 0) return "---";
				return formatter.formatDateAndTime(new Date(delDate));
			default:
				return "ERROR";
		}
	}

	private MediaFileElement getEntryAt(int row) {
		return this.objects.get(row);
	}

	private static class StrikeThroughCellRenderer implements CustomCellRenderer {
		@Override
		public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
			String filename = (String) val;
			if (renderer == null) {
				sb.append(filename);
			} else {
				sb.append("<span class='");
				if (filename.endsWith(WikiMainController.METADATA_SUFFIX)) {
					sb.append("o_wiki-file-deleted");
				}
				int extension = filename.lastIndexOf(".");
				if(extension > 0) {
					filename = filename.substring(0, extension);
				}
				sb.append("'>").append(filename).append("</span>");
			}
		}
	}
}