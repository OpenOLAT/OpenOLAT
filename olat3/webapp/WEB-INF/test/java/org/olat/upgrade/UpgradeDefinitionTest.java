package org.olat.upgrade;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration(locations = {
"classpath:org/olat/upgrade/upgradeDefinitionTest.xml" })
public class UpgradeDefinitionTest extends AbstractJUnit4SpringContextTests {
	
	
	/**
	 * tests if one of the upgrade files needed for upgrading the database are accessible via
	 * classpath
	 */
	@Test
	public void testFileResourceFromClasspath() {
		UpgradesDefinitions defs = (UpgradesDefinitions) applicationContext.getBean("olatupgrades");
		for(OLATUpgrade upgrade: defs.getUpgrades()) {
			String path = "/resources/database/mysql/"+upgrade.getAlterDbStatements();
			Resource file = new ClassPathResource(path);
			assertTrue("file not found: "+path, file.exists());
		}
	}
	
}
