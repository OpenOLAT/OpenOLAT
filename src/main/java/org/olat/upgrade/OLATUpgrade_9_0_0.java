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
package org.olat.upgrade;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.upgrade.model.DBMailAttachmentData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_9_0_0 extends OLATUpgrade {
	
	private static final int BATCH_SIZE = 20;
	private static final String TASK_MAILS = "Upgrade mails";
	private static final String VERSION = "OLAT_9.0.0";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	
	public OLATUpgrade_9_0_0() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}
	
	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) {
				return false;
			}
		}
		
		boolean allOk = upgradeMailAttachments(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_9_0_0 successfully!");
		} else {
			log.audit("OLATUpgrade_9_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeMailAttachments(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MAILS)) {
			int counter = 0;
			List<Long> attachments;
			do {
				attachments = findAttachments(counter, BATCH_SIZE);
				for(Long attachment:attachments) {
					processAttachments(attachment);
				}
				counter += attachments.size();
				log.audit("Mail attachment processed: " + attachments.size());
				dbInstance.commitAndCloseSession();
			} while(attachments.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_MAILS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private List<Long> findAttachments(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();	
		sb.append("select attachment.key from ").append(DBMailAttachmentData.class.getName()).append(" attachment order by key");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private void processAttachments(Long attachment) {
		try {
			DBMailAttachmentData data = dbInstance.getCurrentEntityManager()
					.find(DBMailAttachmentData.class, attachment);
			
			byte[] binaryDatas = data.getDatas();
			if(binaryDatas == null || binaryDatas.length <= 0) {
				return;
			}
			
			long checksum = checksum(binaryDatas);
			String name = data.getName();
			InputStream in = new ByteArrayInputStream(binaryDatas);
			String path = mailManager.saveAttachmentToStorage(name, data.getMimetype(), checksum, data.getSize(), in);
			data.setChecksum(new Long(checksum));
			data.setLastModified(new Date());
			data.setPath(path);
			
			VFSLeaf savedFile = mailManager.getAttachmentDatas(data);
			if(savedFile != null && savedFile.exists() && savedFile.getSize() > 0) {
				data.setDatas(null);
				dbInstance.getCurrentEntityManager().merge(data);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private long checksum(byte[] binaryDatas) throws IOException {
		InputStream in = null;
		Adler32 checksum = new Adler32();
    try {
        in = new CheckedInputStream(new ByteArrayInputStream(binaryDatas), checksum);
        IOUtils.copy(in, new NullOutputStream());
    } finally {
        IOUtils.closeQuietly(in);
    }
    return checksum.getValue();
	}
}
