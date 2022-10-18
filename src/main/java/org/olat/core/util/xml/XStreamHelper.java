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
* <p>
*/
package org.olat.core.util.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;

/**
 * Description:<br>
 * The XStreamHelper provides you with some methods to quickly save and restore
 * objects on disk using XML. This helper does the try/catch and writing and
 * reading from/to the disk.
 * <p>
 * Using xStream without configuration is not recommended. This persists the
 * class using the class name which makes it very difficult to refactor the
 * class at a later stage.
 * <p>
 * It is recommended to have a private XStream instance in the manager of your
 * package. You can create such an instance using the createXStreamInstance()
 * method. Note that the XStream instance is threadsave, it is normally ok to
 * have one stream per manager. Now you can use field and attribute mappers to
 * define how the XML should look like. 
 * <p>
 * <code>
 * private static final XStream mystream;
 * static {
 * 	mystream = XStreamHelper.createXStreamInstance();
 *  mystream.alias("user", UserImpl.class);
 *  mystream.alias("identity", IdentityImpl.class);
 *  mystream.alias("addr", AddressImpl.class);
 * }
 * ... 
 * XStreamHelper.writeObject(mystream, myfile, myIdentity);
 * ...
 * Identity ident = (Identity) XStreamHelper.readObject(mystream, myfile);
 * <code>
 * <p>
 * @see http://xstream.codehaus.org/alias-tutorial.html
 * <p>
 * Initial Date: 01.12.2004 <br>
 * 
 * @author Felix Jost, Florian Gnaegi
 */
public class XStreamHelper {

	private static final String[] DEFAULT_PACKAGES = new String[] {
			"org.olat.**",
			"de.bps.**",
			"at.ac.uibk.**",
			"org.hibernate.collection.**"
		};
	private static final XStream cloneXStream = new XStream();
	static {
		allowDefaultPackage(cloneXStream);
		enhanceXstream(cloneXStream);
	}
	
	public static final void allowDefaultPackage(XStream xstream) {
		xstream.allowTypesByWildcard(DEFAULT_PACKAGES);
	}

	/**
	 * clones an object with the library XStream. The object to be cloned does
	 * not need to be serializable, but must have a default constructor.
	 * 
	 * @param in
	 * @return the clone Object
	 */
	public static Object xstreamClone(Object in) {
		String data = cloneXStream.toXML(in);
		return cloneXStream.fromXML(data);
	}

	/**
	 * Factory to create a fresh XStream instance. Use this when reading and
	 * writing to a configured XML mapping
	 */
	public static XStream createXStreamInstance() {
		XStream xstream = new EnhancedXStream(false);
		enhanceXstream(xstream);
		return xstream;
	}
	
	private static void enhanceXstream(XStream xstream) {
		xstream.omitField(ArrayList.class, "modCount");
		xstream.registerConverter(new CollectionConverter(xstream.getMapper()) {

			@Override
			public boolean canConvert(Class type) {
				if("java.util.Arrays$ArrayList".equals(type.getName())
						|| "java.util.Collections$EmptyList".equals(type.getName())
						|| type.getName().startsWith("java.util.ImmutableCollections$List")) {
					return true;
				}
				return super.canConvert(type);
			}
			
		    protected Object createCollection(Class type) {
		    	if("java.util.Arrays$ArrayList".equals(type.getName())
		    			|| "java.util.Collections$EmptyList".equals(type.getName())
		    			|| type.getName().equals("java.util.ImmutableCollections$List")) {
					return new ArrayList<>();
				}
		        return super.createCollection(type);
		    }
		});
	}
	
	/**
	 * Factory to create a fresh XStream instance. Use this when
	 * writing, it has more aliases to convert hibernate collections
	 * to java collections.
	 * @return
	 */
	public static XStream createXStreamInstanceForDBObjects() {
		XStream xstream = new EnhancedXStream(true);
		enhanceXstream(xstream);
		return xstream;
	}

	/**
	 * Read an object from the given file using the xStream object. It is
	 * usefull to add field and attribute mappers to the stream.
	 * 
	 * @param xStream
	 *            The (configured) xStream.
	 * @param file
	 * @return
	 */
	public static Object readObject(XStream xStream, File file) {
		try(FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis, FileUtils.BSIZE)) {
			return readObject(xStream, bis);
		} catch (IOException e) {
			throw new OLATRuntimeException(XStreamHelper.class, "could not read Object from file: " + file.getAbsolutePath(), e);
		}
	}
	
	/**
	 * 
	 * @param xStream
	 * @param path
	 * @return
	 */
	public static Object readObject(XStream xStream, Path path) {
		try (InputStream in = Files.newInputStream(path);
				InputStream bis = new BufferedInputStream(in)) {
			return readObject(xStream, bis);
		} catch (Exception e) {
			throw new OLATRuntimeException(XStreamHelper.class, "could not read Object from file: " + path, e);
		}
	}
	
	/**
	 * Read an object from the given leaf using the xStream object. It is
	 * usefull to add field and attribute mappers to the stream.
	 * 
	 * @param xStream
	 *            The (configured) xStream.
	 * @param file
	 * @return
	 */
	public static Object readObject(XStream xStream, VFSLeaf file) {
		try(InputStream fis = file.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(fis)) {
			return readObject(xStream, bis);
		} catch (Exception e) {
			throw new OLATRuntimeException(XStreamHelper.class, "could not read Object from file: " + file.getName(), e);
		}
	}

	/**
	 * Read an object from the given input stream using the xStream object. It
	 * is usefull to add field and attribute mappers to the stream.
	 * 
	 * @param xStream
	 *            The (configured) xStream.
	 * @param is
	 * @return
	 */
	public static Object readObject(XStream xStream, InputStream is) {
		try(InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);) {
			return xStream.fromXML(isr);
		} catch (Exception e) {
			throw new OLATRuntimeException(XStreamHelper.class,
					"could not read Object from inputstream: " + is, e);
		}
	}
	
	/**
	 * Read an object from the given xml string using the xStream object.
	 * 
	 * @param xStream The XStream deserializer
	 * @param xml XML in form of a string
	 * @return
	 */
	public static Object readObject(XStream xStream, String xml) {
		try(InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
			return readObject(xStream, is);
		} catch (Exception e) {
			throw new OLATRuntimeException(XStreamHelper.class,
					"could not read Object from string: " + xml, e);
		} 
	}

	/**
	 * Write a an object to an XML file. UTF-8 is used as encoding. It is
	 * useful to set attribute and field mappers to allow later refactoring of
	 * the class!
	 * 
	 * @param xStream
	 *            The (configured) xStream.
	 * @param vfsLeaf
	 * @param obj
	 *            the object to be serialized
	 */
	public static void writeObject(XStream xStream, VFSLeaf vfsLeaf, Object obj) {
		try(OutputStream out=vfsLeaf.getOutputStream(false)) {
			writeObject(xStream, out, obj);
		} catch(Exception e) {
			throw new OLATRuntimeException(XStreamHelper.class, "Could not write object to file: " + vfsLeaf, e);
		}
	}

	/**
	 * Write a an object to an XML file. UTF-8 is used as encoding. It is
	 * useful to set attribute and field mappers to allow later refactoring of
	 * the class!
	 * 
	 * @param xStream
	 *            The (configured) xStream.
	 * @param file
	 * @param obj
	 *            the object to be serialized
	 */
	public static void writeObject(XStream xStream, File file, Object obj) {
		try(OutputStream out=new FileOutputStream(file);
				BufferedOutputStream bout = new BufferedOutputStream(out, FileUtils.BSIZE)) {
			writeObject(xStream, bout, obj);
		} catch (Exception e) {
			throw new OLATRuntimeException(XStreamHelper.class, "Could not write object to file: " + file.getAbsolutePath(), e);
		}
	}

	/**
	 * Write a an object to an output stream. UTF-8 is used as encoding in the
	 * XML declaration. It is useful to set attribute and field mappers to
	 * allow later refactoring of the class!
	 * 
	 * @param xStream
	 *            The (configured) xStream.
	 * @param os
	 * @param obj
	 *            the object to be serialized
	 */
	public static void writeObject(XStream xStream, OutputStream os, Object obj) {
		try(OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
			String data = xStream.toXML(obj);
			data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ data; // give a decent header with the encoding used
			osw.write(data);
			osw.flush();
		} catch (Exception e) {
			throw new OLATRuntimeException(XStreamHelper.class, "Could not write object to stream.", e);
		}
	}
}
