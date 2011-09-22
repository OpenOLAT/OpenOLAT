package org.olat.core.util.servlets;
/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A sample DOM writer. This sample program illustrates how to
 * traverse a DOM tree in order to print a document that is parsed.
 */
public class DOMWriter {

   //
   // Data
   //

   /** Default Encoding */
   private static  String
   PRINTWRITER_ENCODING = "UTF8";

   private static String MIME2JAVA_ENCODINGS[] =
    { "Default", "UTF-8", "US-ASCII", "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4",
      "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "ISO-8859-9", "ISO-2022-JP",
      "SHIFT_JIS", "EUC-JP","GB2312", "BIG5", "EUC-KR", "ISO-2022-KR", "KOI8-R", "EBCDIC-CP-US",
      "EBCDIC-CP-CA", "EBCDIC-CP-NL", "EBCDIC-CP-DK", "EBCDIC-CP-NO", "EBCDIC-CP-FI", "EBCDIC-CP-SE",
      "EBCDIC-CP-IT", "EBCDIC-CP-ES", "EBCDIC-CP-GB", "EBCDIC-CP-FR", "EBCDIC-CP-AR1",
      "EBCDIC-CP-HE", "EBCDIC-CP-CH", "EBCDIC-CP-ROECE","EBCDIC-CP-YU",
      "EBCDIC-CP-IS", "EBCDIC-CP-AR2", "UTF-16"
    };


   /** Print writer. */
   protected PrintWriter out;

   /** Canonical output. */
   protected boolean canonical;


   public DOMWriter(String encoding, boolean canonical)
   throws UnsupportedEncodingException {
      out = new PrintWriter(new OutputStreamWriter(System.out, encoding));
      this.canonical = canonical;
   } // <init>(String,boolean)

   //
   // Constructors
   //

   /** Default constructor. */
   public DOMWriter(boolean canonical) throws UnsupportedEncodingException {
      this( getWriterEncoding(), canonical);
   }

    public DOMWriter(Writer writer, boolean canonical) {
        out = new PrintWriter(writer);
        this.canonical = canonical;
    }

   public static String getWriterEncoding( ) {
      return (PRINTWRITER_ENCODING);
   }// getWriterEncoding

   public static void  setWriterEncoding( String encoding ) {
      if( encoding.equalsIgnoreCase( "DEFAULT" ) )
         PRINTWRITER_ENCODING  = "UTF8";
      else if( encoding.equalsIgnoreCase( "UTF-16" ) )
         PRINTWRITER_ENCODING  = "Unicode";
      else
         PRINTWRITER_ENCODING = MIME2Java.convert( encoding );
   }// setWriterEncoding


   public static boolean isValidJavaEncoding( String encoding ) {
      for ( int i = 0; i < MIME2JAVA_ENCODINGS.length; i++ )
         if ( encoding.equals( MIME2JAVA_ENCODINGS[i] ) )
            return (true);

      return (false);
   }// isValidJavaEncoding


   /** Prints the specified node, recursively. */
   public void print(Node node) {

      // is there anything to do?
      if ( node == null ) {
         return;
      }

      int type = node.getNodeType();
      switch ( type ) {
         // print document
         case Node.DOCUMENT_NODE: {
               if ( !canonical ) {
                  String  Encoding = getWriterEncoding();
                  if( Encoding.equalsIgnoreCase( "DEFAULT" ) )
                     Encoding = "UTF-8";
                  else if( Encoding.equalsIgnoreCase( "Unicode" ) )
                     Encoding = "UTF-16";
                  else
                     Encoding = MIME2Java.reverse( Encoding );

                  out.println("<?xml version=\"1.0\" encoding=\""+
                           Encoding + "\"?>");
               }
               print(((Document)node).getDocumentElement());
               out.flush();
               break;
            }

            // print element with attributes
         case Node.ELEMENT_NODE: {
               out.print('<');
               out.print(node.getNodeName());
               Attr attrs[] = sortAttributes(node.getAttributes());
               for ( int i = 0; i < attrs.length; i++ ) {
                  Attr attr = attrs[i];
                  out.print(' ');
                  out.print(attr.getNodeName());
                  out.print("=\"");
                  out.print(normalize(attr.getNodeValue()));
                  out.print('"');
               }
               out.print('>');
               NodeList children = node.getChildNodes();
               if ( children != null ) {
                  int len = children.getLength();
                  for ( int i = 0; i < len; i++ ) {
                     print(children.item(i));
                  }
               }
               break;
            }

            // handle entity reference nodes
         case Node.ENTITY_REFERENCE_NODE: {
               if ( canonical ) {
                  NodeList children = node.getChildNodes();
                  if ( children != null ) {
                     int len = children.getLength();
                     for ( int i = 0; i < len; i++ ) {
                        print(children.item(i));
                     }
                  }
               } else {
                  out.print('&');
                  out.print(node.getNodeName());
                  out.print(';');
               }
               break;
            }

            // print cdata sections
         case Node.CDATA_SECTION_NODE: {
               if ( canonical ) {
                  out.print(normalize(node.getNodeValue()));
               } else {
                  out.print("<![CDATA[");
                  out.print(node.getNodeValue());
                  out.print("]]>");
               }
               break;
            }

            // print text
         case Node.TEXT_NODE: {
               out.print(normalize(node.getNodeValue()));
               break;
            }

            // print processing instruction
         case Node.PROCESSING_INSTRUCTION_NODE: {
               out.print("<?");
               out.print(node.getNodeName());
               String data = node.getNodeValue();
               if ( data != null && data.length() > 0 ) {
                  out.print(' ');
                  out.print(data);
               }
               out.print("?>");
               break;
            }
      }

      if ( type == Node.ELEMENT_NODE ) {
         out.print("</");
         out.print(node.getNodeName());
         out.print('>');
      }

      out.flush();

   } // print(Node)

   /** Returns a sorted list of attributes. */
   protected Attr[] sortAttributes(NamedNodeMap attrs) {

      int len = (attrs != null) ? attrs.getLength() : 0;
      Attr array[] = new Attr[len];
      for ( int i = 0; i < len; i++ ) {
         array[i] = (Attr)attrs.item(i);
      }
      for ( int i = 0; i < len - 1; i++ ) {
         String name  = array[i].getNodeName();
         int    index = i;
         for ( int j = i + 1; j < len; j++ ) {
            String curName = array[j].getNodeName();
            if ( curName.compareTo(name) < 0 ) {
               name  = curName;
               index = j;
            }
         }
         if ( index != i ) {
            Attr temp    = array[i];
            array[i]     = array[index];
            array[index] = temp;
         }
      }

      return (array);

   } // sortAttributes(NamedNodeMap):Attr[]


   /** Normalizes the given string. */
   protected String normalize(String s) {
      StringBuilder str = new StringBuilder();

      int len = (s != null) ? s.length() : 0;
      for ( int i = 0; i < len; i++ ) {
         char ch = s.charAt(i);
         switch ( ch ) {
            case '<': {
                  str.append("&lt;");
                  break;
               }
            case '>': {
                  str.append("&gt;");
                  break;
               }
            case '&': {
                  str.append("&amp;");
                  break;
               }
            case '"': {
                  str.append("&quot;");
                  break;
               }
            case '\r':
            case '\n': {
                  if ( canonical ) {
                     str.append("&#");
                     str.append(Integer.toString(ch));
                     str.append(';');
                     break;
                  }
                  // else, default append char
               }
            default: {
                  str.append(ch);
               }
         }
      }

      return (str.toString());

   } // normalize(String):String

   /*private static void printValidJavaEncoding() {
      System.err.println( "    ENCODINGS:" );
      System.err.print( "   " );
      for( int i = 0;
                     i < MIME2JAVA_ENCODINGS.length; i++) {
         System.err.print( MIME2JAVA_ENCODINGS[i] + " " );
      if( (i % 7 ) == 0 ){
         System.err.println();
         System.err.print( "   " );
         }
      }

   } // printJavaEncoding()
   */

}
