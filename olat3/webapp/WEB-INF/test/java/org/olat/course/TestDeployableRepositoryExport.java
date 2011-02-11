package org.olat.course;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * 
 * Description:<br>
 * Testing the url file download
 * 
 * <P>
 * Initial Date:  22.12.2010 <br>
 * @author guido
 */
@ContextConfiguration(locations={"classpath:org/olat/course/_spring/textContextDeployableRepoExport.xml"})
public class TestDeployableRepositoryExport extends AbstractJUnit4SpringContextTests {
	
	@Test
	public void testZipDownloadNormalCase() {
		DeployableCourseExport bean = (DeployableCourseExport) applicationContext.getBean("normalzip");
		assertNotNull(bean);
		assertFalse(bean.isHelpCourse());
		assertEquals(bean.getAccess(),4);
		assertEquals(bean.getVersion(),Float.valueOf(1));
		
		File file = bean.getDeployableCourseZipFile();
		assertEquals(file.getName(),"olatUserAndGroupService.jar");
		assertNotNull(file);
		assertTrue(file.exists());
	}
	
	@Test
	public void testZipDownloadBadUrl() {
		DeployableCourseExport bean = (DeployableCourseExport) applicationContext.getBean("badurl");
		assertNotNull(bean);
		assertNull(bean.getDeployableCourseZipFile());
	}
	
	@Test
	public void testZipDownloadTextFile() {
		DeployableCourseExport bean = (DeployableCourseExport) applicationContext.getBean("textfile");
		assertNotNull(bean);
		assertNull(bean.getDeployableCourseZipFile());
	}

}
