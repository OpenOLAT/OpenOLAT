package org.olat.core.commons.services.export.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportManagerTest extends OlatTestCase {
	
	@Autowired
	private ExportManager exportManager;
	
	@Test
	public void getContainer() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("export-area-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode courseNode = course.getRunStructure().getRootNode();
		
		VFSContainer container = exportManager.getExportContainer(entry, courseNode.getIdent());
		Assert.assertNotNull(container);
		Assert.assertTrue(container.exists());
	}
	
	/**
	 * Mostly a dummy test to check the query syntax and such things.
	 */
	@Test
	public void getResultsExport() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("export-area-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode courseNode = course.getRunStructure().getRootNode();
		
		List<ExportInfos> infos = exportManager.getResultsExport(entry, courseNode.getIdent());
		Assert.assertNotNull(infos);
		Assert.assertTrue(infos.isEmpty());
	}

}
