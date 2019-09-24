/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.olat.core.commons.services.webdav.servlets;

import java.io.PrintWriter;
import java.io.Writer;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A sample DOM writer. This sample program illustrates how to traverse a DOM
 * tree in order to print a document that is parsed.
 */
public class DOMWriter {

    /** Default Encoding */
    private static final String PRINTWRITER_ENCODING = "UTF8";

    /** Print writer. */
    protected final PrintWriter out;

    /** Canonical output. */
    protected final boolean canonical;


    public DOMWriter(Writer writer, boolean canonical) {
        out = new PrintWriter(writer);
        this.canonical = canonical;
    }


    public static String getWriterEncoding() {
        return (PRINTWRITER_ENCODING);
    }


    /** Prints the specified node, recursively. */
    public void print(Node node) {

        // is there anything to do?
        if (node == null) {
            return;
        }

        int type = node.getNodeType();
        switch (type) {
            // print document
            case Node.DOCUMENT_NODE:
                if (!canonical) {
                    String encoding = getWriterEncoding();
                    if (encoding.equalsIgnoreCase("DEFAULT"))
                        encoding = "UTF-8";
                    else if (encoding.equalsIgnoreCase("Unicode"))
                        encoding = "UTF-16";
                    else
                        encoding = null;

                    out.println("<?xml version=\"1.0\" encoding=\"" + encoding +
                            "\"?>");
                }
                print(((Document) node).getDocumentElement());
                out.flush();
                break;

            // print element with attributes
            case Node.ELEMENT_NODE:
                out.print('<');
                out.print(node.getLocalName());
                Attr attrs[] = sortAttributes(node.getAttributes());
                for (int i = 0; i < attrs.length; i++) {
                    Attr attr = attrs[i];
                    out.print(' ');
                    out.print(attr.getLocalName());

                    out.print("=\"");
                    out.print(normalize(attr.getNodeValue()));
                    out.print('"');
                }
                out.print('>');
                printChildren(node);
                break;

            // handle entity reference nodes
            case Node.ENTITY_REFERENCE_NODE:
                if (canonical) {
                    printChildren(node);
                } else {
                    out.print('&');
                    out.print(node.getLocalName());
                    out.print(';');
                }
                break;

            // print cdata sections
            case Node.CDATA_SECTION_NODE:
                if (canonical) {
                    out.print(normalize(node.getNodeValue()));
                } else {
                    out.print("<![CDATA[");
                    out.print(node.getNodeValue());
                    out.print("]]>");
                }
                break;

            // print text
            case Node.TEXT_NODE:
                out.print(normalize(node.getNodeValue()));
                break;

            // print processing instruction
            case Node.PROCESSING_INSTRUCTION_NODE:
                out.print("<?");
                out.print(node.getLocalName());

                String data = node.getNodeValue();
                if (data != null && data.length() > 0) {
                    out.print(' ');
                    out.print(data);
                }
                out.print("?>");
                break;
            }

        if (type == Node.ELEMENT_NODE) {
            out.print("</");
            out.print(node.getLocalName());
            out.print('>');
        }

        out.flush();

    } // print(Node)


    private void printChildren(Node node) {
        NodeList children = node.getChildNodes();
        if (children != null) {
            int len = children.getLength();
            for (int i = 0; i < len; i++) {
                print(children.item(i));
            }
        }
    }


    /** Returns a sorted list of attributes. */
    protected Attr[] sortAttributes(NamedNodeMap attrs) {
        if (attrs == null) {
            return new Attr[0];
        }

        int len = attrs.getLength();
        Attr array[] = new Attr[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Attr) attrs.item(i);
        }
        for (int i = 0; i < len - 1; i++) {
            String name = null;
            name = array[i].getLocalName();
            int index = i;
            for (int j = i + 1; j < len; j++) {
                String curName = null;
                curName = array[j].getLocalName();
                if (curName.compareTo(name) < 0) {
                    name = curName;
                    index = j;
                }
            }
            if (index != i) {
                Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }

        return (array);

    }

    /** Normalizes the given string. */
    protected String normalize(String s) {
        if (s == null) {
            return "";
        }

        StringBuilder str = new StringBuilder();

        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<':
                    str.append("&lt;");
                    break;
                case '>':
                    str.append("&gt;");
                    break;
                case '&':
                    str.append("&amp;");
                    break;
                case '"':
                    str.append("&quot;");
                    break;
                case '\r':
                case '\n':
                    if (canonical) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    }
                    // else, default append char
                //$FALL-THROUGH$
                default:
                    str.append(ch);
            }
        }

        return (str.toString());
    }
}
