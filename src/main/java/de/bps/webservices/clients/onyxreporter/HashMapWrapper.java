package de.bps.webservices.clients.onyxreporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.codec.binary.Base64;

/**
 * HashMap wrapper class. Uses serialization and base64 encoding to wrap a <code>HashMap<String, String></code>
 * 
 * @author Lars Eberle (laeb@bps-system.de)
 * <ONYX-705>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hashmapwrapper")
public class HashMapWrapper implements Serializable {
	private static final long serialVersionUID = 5831008170384520354L;

	@XmlElement(name = "content")
	private String serialized;

	/**
	 * @return The wrapped map.
	 * @throws OnyxReporterException
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, String> getMap() throws OnyxReporterException {
		if (this.serialized == null) {
			return null;
		}
		HashMap<String, String> results = null;
		final Object o = deserialize(this.serialized);
		if (o == null) {
			final String logMsg = "Got invalid null response from Onyx Reporter in getResults!";
			throw new OnyxReporterException(logMsg);
		} else if (o instanceof HashMap) {
			results = (HashMap<String, String>) o;
		} else {
			String name = null;
			try {
				name = o.getClass().getName();
			} catch (final Exception e) {
				name = "unknown";
			}
			final String logMsg = "Got response from unexpected type from Onyx Reporter in getResults: " + name;
			throw new OnyxReporterException(logMsg);
		}
		return results;
	}

	/**
	 * Wraps the given map.
	 * 
	 * @param map
	 * @throws OnyxReporterException
	 */
	public void setMap(final HashMap<String, String> map) throws OnyxReporterException {
		if (map == null) {
			final String s = serialize(new HashMap<String, String>());
			this.serialized = s;
		} else {
			String s = serialize(map);
			if (s == null) {
				s = serialize(new HashMap<String, String>());
			}
			this.serialized = s;
		}
	}

	/**
	 * Serializes the given object o and encodes the result as non-chunked base64.
	 * 
	 * @param objectToSerializeAndEncode
	 * @return
	 * @throws OnyxReporterException
	 */
	private static final String serialize(final Object objectToSerializeAndEncode) throws OnyxReporterException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(objectToSerializeAndEncode);
			oos.close();
			oos = null;
			return Base64.encodeBase64String(baos.toByteArray());
		} catch (final IOException e) {
			throw new OnyxReporterException("Could not serialize object!", e);
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (final IOException e) {
			}
		}
	}

	/**
	 * Deserializes an object from the given Base65 encoded string.
	 * 
	 * @param base64EncodedSerializedObject
	 * @return
	 * @throws OnyxReporterException
	 */
	private static final Object deserialize(final String base64EncodedSerializedObject) throws OnyxReporterException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decodeBase64(base64EncodedSerializedObject));
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bais);
			final Object o = ois.readObject();
			return o;
		} catch (final IOException e) {
			throw new OnyxReporterException("Could not deserialize object!", e);
		} catch (final ClassNotFoundException e) {
			throw new OnyxReporterException("Could not deserialize object!", e);
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (final IOException e) {
			}
		}
	}
}
// </ONYX-705>