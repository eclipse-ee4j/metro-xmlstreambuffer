/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.xml.stream.buffer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EmptyBufferTest extends BaseBufferTestCase {
    
    public EmptyBufferTest(String testName) {
        super(testName);
    }
    
    public void testEmptyBufferUsingXMLStreamReader() throws Exception {    
        MutableXMLStreamBuffer b = new MutableXMLStreamBuffer();
        XMLStreamReader r = b.readAsXMLStreamReader();
        
        assertEquals(true, r.getEventType() == XMLStreamReader.START_DOCUMENT);
        r.next();
        assertEquals(true, r.getEventType() == XMLStreamReader.END_DOCUMENT);
    }
    
    public void testEmptyBufferUsingContentHandler() throws Exception {    
        MutableXMLStreamBuffer b = new MutableXMLStreamBuffer();
        b.writeTo(new ContentHandler() {
            boolean _startDocument = false;
            
            public void setDocumentLocator(Locator locator) {
            }

            public void startDocument() throws SAXException {
                _startDocument = true;
            }

            public void endDocument() throws SAXException {
                assertEquals(true, _startDocument);
            }

            public void startPrefixMapping(String prefix, String uri) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void endPrefixMapping(String prefix) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void endElement(String uri, String localName, String qName) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void characters(char[] ch, int start, int length) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void processingInstruction(String target, String data) throws SAXException {
                assertEquals(false, _startDocument);
            }

            public void skippedEntity(String name) throws SAXException {
                assertEquals(false, _startDocument);
            }
        },false);
    }
}
