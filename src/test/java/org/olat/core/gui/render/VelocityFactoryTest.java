package org.olat.core.gui.render;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.gui.render.velocity.ReadOnlyContext;
import org.olat.core.gui.render.velocity.VelocityFactory;
import org.olat.test.OlatTestCase;


/**
 * 
 * Initial date: 26 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class VelocityFactoryTest extends OlatTestCase {
	
	/**
	 * No introspection
	 * 
	 * @throws IOException
	 */
	@Test
	public void noIntrospectEngine() throws IOException {
		VelocityEngine velocityEngine = VelocityFactory.createNoIntrospectEngine();
		
		Context context = new VelocityContext();
		context.put("atomic", new AtomicInteger(2));
		try(StringWriter writer = new StringWriter()) {
			velocityEngine.evaluate(context, writer, "Test1", "${atomic.intValue()}");
			Assert.assertEquals("${atomic.intValue()}", writer.toString());
		} catch(IOException e) {
			throw e;
		}
	}
	
	/**
	 * No introspection but adhoc variables
	 * @throws IOException
	 */
	@Test
	public void noIntrospectEngineSet() throws IOException {
		VelocityEngine velocityEngine = VelocityFactory.createNoIntrospectEngine();

		Context context = new VelocityContext();
		context.put("atomic", new AtomicInteger(2));
		try(StringWriter writer = new StringWriter()) {
			velocityEngine.evaluate(context, writer, "Test2", "#set($jrn=42+23)$jrn");
			Assert.assertEquals("65", writer.toString());
		} catch(IOException e) {
			throw e;
		}
	}
	
	/**
	 * No introspection, no adhoc variables
	 * @throws IOException
	 */
	@Test
	public void noIntrospectEngineReadOnlyContext() throws IOException {
		VelocityEngine velocityEngine = VelocityFactory.createNoIntrospectEngine();
		
		Context context = new VelocityContext();
		try(StringWriter writer = new StringWriter()) {
			velocityEngine.evaluate(new ReadOnlyContext(context), writer, "Test3", "#set($jrn=42+23)$jrn");
			Assert.assertEquals("jrn", writer.toString());
		} catch(IOException e) {
			throw e;
		}
	}
	
	@Test
	public void secureEngine() throws IOException {
		VelocityEngine velocityEngine = VelocityFactory.createEngine(false);
		
		Context context = new VelocityContext();
		context.put("hello", "Worlds");
		try(StringWriter writer = new StringWriter()) {
			velocityEngine.evaluate(context, writer, "Test4", "#set($jrn=42+23)$jrn $hello");
			Assert.assertEquals("65 Worlds", writer.toString());
		} catch(IOException e) {
			throw e;
		}
	}
}
