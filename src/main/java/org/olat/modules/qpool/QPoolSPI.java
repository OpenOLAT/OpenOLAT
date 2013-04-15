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
package org.olat.modules.qpool;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QPoolSPI {
	
	public int getPriority();
	
	public String getFormat();
	
	public boolean isCompatible(String filename, File file);
	
	public boolean isCompatible(String filename, VFSLeaf file);
	
	/**
	 * Extract text for indexing
	 * @param item
	 * @return
	 */
	public String extractTextContent(QuestionItemFull item);
	
	public List<QuestionItem> importItems(Identity owner, Locale defaultLocale, String filename, File file);
	
	/**
	 * Export the item to the Zip
	 * @param item
	 * @param out
	 */
	public void exportItem(QuestionItemFull item, ZipOutputStream out);
	
	/**
	 * Copy the item attachment...
	 * @param original
	 * @param copy
	 */
	public void copyItem(QuestionItemFull original, QuestionItemFull copy);
	
	/**
	 * Return the preview controller used in the main list panel of
	 * the question poll.
	 * @param ureq
	 * @param wControl
	 * @param item
	 * @return
	 */
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item);

	/**
	 * Is OpenOLAT able to edit this content?
	 * 
	 * @return If the service provider can deliver a controller to edit the content
	 */
	public boolean isTypeEditable();
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param item
	 * @return
	 */
	public Controller getEditableController(UserRequest ureq, WindowControl wControl, QuestionItem item);
}
