package org.olat.user.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.user.UserDataExport;
import org.olat.user.model.UserDataExportImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserDataExportDAO {
	
	@Autowired
	private DB dbInstance;
	
	public UserDataExport createExport(Identity identity, Collection<String> exportIds, UserDataExport.ExportStatus status) {
		UserDataExportImpl data = new UserDataExportImpl();
		data.setCreationDate(new Date());
		data.setLastModified(data.getCreationDate());
		data.setIdentity(identity);
		data.setStatus(status);
		data.setDirectory(identity.getKey() + "-" + CodeHelper.getForeverUniqueID());
		StringBuilder sb = new StringBuilder(256);
		for(String exportId:exportIds) {
			if(sb.length() > 0) sb.append(",");
			sb.append(exportId);
		}
		data.setExporterIdList(sb.toString());
		dbInstance.getCurrentEntityManager().persist(data);
		return data;
	}
	
	public UserDataExport update(UserDataExport exportData) {
		((UserDataExportImpl)exportData).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(exportData);
	}
	
	public UserDataExport loadByKey(Long key) {
		String query = "select data from userdataexport data where data.key=:dataKey";
		List<UserDataExport> datas = dbInstance.getCurrentEntityManager()
				.createQuery(query, UserDataExport.class)
				.setParameter("dataKey", key)
				.getResultList();
		return datas == null || datas.isEmpty() ? null : datas.get(0);
	}
	
	public List<UserDataExport> getUserDataExport(IdentityRef identity, List<UserDataExport.ExportStatus> status) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select data from userdataexport data")
		  .append(" where data.identity.key=:identityKey and data.statusString in (:status)");
		
		List<String> statusString = status.stream()
				.map(UserDataExport.ExportStatus::name)
				.collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserDataExport.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("status", statusString)
				.getResultList();
	}
	
	public UserDataExport getLastUserDataExport(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select data from userdataexport data")
		  .append(" where data.identity.key=:identityKey and data.statusString in (:status)")
		  .append(" order by data.creationDate desc");
		
		List<String> statusString = new ArrayList<>();
		statusString.add(UserDataExport.ExportStatus.requested.name());
		statusString.add(UserDataExport.ExportStatus.processing.name());
		statusString.add(UserDataExport.ExportStatus.ready.name());
		List<UserDataExport> datas = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserDataExport.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("status", statusString)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return datas == null || datas.isEmpty() ? null : datas.get(0);
	}
	

	
	

}
