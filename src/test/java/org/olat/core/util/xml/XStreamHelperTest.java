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
package org.olat.core.util.xml;

import java.io.File;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.net.URL;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.Assert;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ForbiddenClassException;

/**
 * 
 * Initial date: 18 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class XStreamHelperTest {
	
	@Test
	public void readXmlMapAllowed() throws URISyntaxException {
		URL url = XStreamHelperTest.class.getResource("xstream_map_strings.xml");
		File file = new File(url.toURI());
		Object obj = XStreamHelper.createXStreamInstance().fromXML(file);
		Assert.assertNotNull(obj);
	}
	
	@Test
	public void readXmlMapDbObjectsAllowed() throws URISyntaxException {
		URL url = XStreamHelperTest.class.getResource("xstream_map_strings.xml");
		File file = new File(url.toURI());
		Object obj = XStreamHelper.createXStreamInstanceForDBObjects().fromXML(file);
		Assert.assertNotNull(obj);
	}

	@Test(expected = ForbiddenClassException.class)
	public void readXmlMapNotAllowed() throws URISyntaxException {
		URL url = XStreamHelperTest.class.getResource("xstream_map_alien.xml");
		File file = new File(url.toURI());
		XStreamHelper.createXStreamInstance().fromXML(file);
	}
	
	@Test(expected = ForbiddenClassException.class)
	public void readXmlMapDbObjectsNotAllowed() throws URISyntaxException {
		URL url = XStreamHelperTest.class.getResource("xstream_map_alien.xml");
		File file = new File(url.toURI());
		XStreamHelper.createXStreamInstanceForDBObjects().fromXML(file);
	}

	@Test
	public void readXmlHibernateProxyRemappedToBaseClass() {
		String xml = "<org.olat.core.util.xml.XStreamHelperTest_-TestBean_-HibernateProxy_-a1b2c3d4><name>hello</name></org.olat.core.util.xml.XStreamHelperTest_-TestBean_-HibernateProxy_-a1b2c3d4>";
		XStream xstream = XStreamHelper.createXStreamInstance();
		XStreamHelper.allowDefaultPackage(xstream);
		Object obj = xstream.fromXML(xml);
		Assert.assertTrue(obj instanceof TestBean);
		Assert.assertEquals("hello", ((TestBean)obj).getName());
	}

	@Test(expected = ForbiddenClassException.class)
	public void readXmlHibernateProxyNotAllowed() {
		String xml = "<java.lang.ProcessBuilder_-HibernateProxy_-a1b2c3d4></java.lang.ProcessBuilder_-HibernateProxy_-a1b2c3d4>";
		XStream xstream = XStreamHelper.createXStreamInstance();
		XStreamHelper.allowDefaultPackage(xstream);
		xstream.fromXML(xml);
	}

	@Test
	public void writeXmlHibernateProxyUnwrapped() {
		TestBean bean = new TestBean();
		bean.setName("hello");
		LazyInitializer lazyInitializer = (LazyInitializer)Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { LazyInitializer.class },
				(p, method, args) -> "getImplementation".equals(method.getName()) ? bean : null);
		TestBeanProxy proxy = new TestBeanProxy(lazyInitializer);

		String xml = XStreamHelper.createXStreamInstance().toXML(proxy);
		Assert.assertTrue(xml.contains("TestBean"));
		Assert.assertTrue(xml.contains("hello"));
		Assert.assertFalse(xml.contains("HibernateProxy"));
		Assert.assertFalse(xml.contains("TestBeanProxy"));
	}

	public static class TestBean {

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class TestBeanProxy extends TestBean implements HibernateProxy {

		private static final long serialVersionUID = -634817780480177319L;
		
		private final transient LazyInitializer lazyInitializer;

		public TestBeanProxy(LazyInitializer lazyInitializer) {
			this.lazyInitializer = lazyInitializer;
		}

		@Override
		public Object writeReplace() {
			return this;
		}

		@Override
		public LazyInitializer getHibernateLazyInitializer() {
			return lazyInitializer;
		}
	}
}
