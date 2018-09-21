/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.xml.stream.buffer.sax;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.*;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class NamespaceTest extends TestCase {
    
    public NamespaceTest(String testName) {
        super(testName);
    }

    public void testManyNamespaceDeclarations() throws Exception {
        for (int i = 0; i <= 50; i++) {
            _testManyNamespaceDeclarations(i);
        }
    }
    
    private void _testManyNamespaceDeclarations(int n) throws Exception {
        InputStream d = createDocument(n);
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        XMLReader r = spf.newSAXParser().getXMLReader();

        XMLStreamBuffer b = XMLStreamBuffer.createNewBufferFromXMLReader(r, d);
    }
    
    private InputStream createDocument(int n) {
        StringBuilder b = new StringBuilder();
        
        b.append("<root");
        for (int i = 0; i <= n; i++) {
            b.append(" xmlns:p" + i + "='urn:" + i + "'");
        }
        b.append(">");
        b.append("</root>");
        
        return new ByteArrayInputStream(b.toString().getBytes());
    }
    

    public void testEndPrefixMappings() throws Exception {
    	String xml =
                "<a:x xmlns:a='http://foo.bar/a' xmlns:b='http://foo.bar/b'>" +
                  "<a:y xmlns:c='http://foo.bar/c' xmlns:d='http://foo.bar/d'>" +
                    "<a:z xmlns:e='http://foo.bar/e'>e:ZZZ</a:z>" +
                  "</a:y>" +
                "</a:x>";
        XMLStreamReader r = getReader(xml);
        do r.next(); while(!"y".equals(r.getLocalName()));

        XMLStreamBuffer b = XMLStreamBuffer.createNewBufferFromXMLStreamReader(r);
        SAXBufferProcessor sp = new SAXBufferProcessor(b, true);
        MyContentHandler ch = new MyContentHandler();
        sp.setContentHandler(ch);
        sp.process();
        assertEquals(3, ch.startPrefixCount);
        assertEquals(3, ch.endPrefixCount);
        assertTrue(ch.prefix_e_Ended);
    }


    static XMLStreamReader getReader(String xml) throws Exception {
    	ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
        XMLInputFactory readerFactory = XMLInputFactory.newInstance();
        return readerFactory.createXMLStreamReader(is);
    }
    
    static class MyContentHandler extends DefaultHandler {
    	int startPrefixCount = 0;
    	int endPrefixCount = 0;    	
    	boolean element_z_Ended = false;
    	boolean prefix_e_Ended = false;    	
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			startPrefixCount ++;
		}
		public void endPrefixMapping(String prefix) throws SAXException {
			endPrefixCount ++;
			if (element_z_Ended && "e".equals(prefix)) prefix_e_Ended = true;
			
		}
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("z".equals(localName)) element_z_Ended = true;			
		}
    }
}
