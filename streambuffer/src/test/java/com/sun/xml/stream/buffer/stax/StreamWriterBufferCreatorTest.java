/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.BaseBufferTestCase;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Kohsuke Kawaguchi
 */
public class StreamWriterBufferCreatorTest extends BaseBufferTestCase {
    
    public void testSimple() throws Exception {
        MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        XMLStreamWriter writer = buffer.createFromXMLStreamWriter();
        writer.writeStartDocument();
        writer.writeStartElement("foo");
        writer.writeCharacters("body");
        writer.writeEndElement();
        writer.writeEndDocument();

        assertTrue(buffer.isCreated());

        XMLStreamReader reader = buffer.readAsXMLStreamReader();
        assertEquals(XMLStreamConstants.START_DOCUMENT,reader.getEventType());

        assertEquals(XMLStreamConstants.START_ELEMENT,reader.next());
        verifyTag(reader,null,"foo");

        assertEquals(XMLStreamConstants.CHARACTERS,reader.next());
        assertEquals("body",reader.getText());

        assertEquals(XMLStreamConstants.END_ELEMENT,reader.next());
        verifyTag(reader,null,"foo");
    }
    
    public void testNamespaces() throws Exception {
        MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        XMLStreamWriter writer = buffer.createFromXMLStreamWriter();
        
        writer.writeStartDocument();
        
        
        writer.setDefaultNamespace("http://default");
        writer.setPrefix("ns1", "http://ns1");
        writer.setPrefix("ns2", "http://ns2");
        assertEquals("", writer.getPrefix("http://default"));
        assertEquals("ns1", writer.getPrefix("http://ns1"));
        assertEquals("ns2", writer.getPrefix("http://ns2"));
        
        writer.writeStartElement("foo");
        writer.writeDefaultNamespace("http://default");
        writer.writeNamespace("ns1", "http://ns1");
        writer.writeNamespace("ns2", "http://ns2");
        
        
        writer.setDefaultNamespace("http://default-new");
        writer.setPrefix("ns2", "http://ns2-new");
        writer.setPrefix("ns3", "http://ns3");
        writer.setPrefix("ns4", "http://ns4");
        assertEquals("", writer.getPrefix("http://default-new"));
        assertEquals("ns1", writer.getPrefix("http://ns1"));
        assertEquals("ns2", writer.getPrefix("http://ns2-new"));
        assertEquals("ns3", writer.getPrefix("http://ns3"));
        assertEquals("ns4", writer.getPrefix("http://ns4"));
        
        writer.writeStartElement("bar");
        writer.writeDefaultNamespace("http://default-new");
        writer.writeNamespace("ns2", "http://ns2-new");
        writer.writeNamespace("ns3", "http://ns3");
        writer.writeNamespace("ns4", "http://ns4");
                        
        writer.writeEndElement(); // bar
        writer.writeEndElement(); // foo
        
        assertEquals(null, writer.getPrefix("http://ns3"));
        assertEquals(null, writer.getPrefix("http://ns4"));
        assertEquals("", writer.getPrefix("http://default"));
        
        writer.writeEndDocument();

        
        XMLStreamReader reader = buffer.readAsXMLStreamReader();
        assertEquals(XMLStreamConstants.START_DOCUMENT,reader.getEventType());

        assertEquals(XMLStreamConstants.START_ELEMENT,reader.next());
        assertEquals("http://default", reader.getNamespaceURI(""));
        assertEquals("http://ns1", reader.getNamespaceURI("ns1"));
        assertEquals("http://ns2", reader.getNamespaceURI("ns2"));
        assertEquals(3, reader.getNamespaceCount());
        verifyTag(reader,"http://default","foo");
        
        assertEquals(XMLStreamConstants.START_ELEMENT,reader.next());
        assertEquals("http://default-new", reader.getNamespaceURI(""));
        assertEquals("http://ns1", reader.getNamespaceURI("ns1"));
        assertEquals("http://ns2-new", reader.getNamespaceURI("ns2"));
        assertEquals("http://ns3", reader.getNamespaceURI("ns3"));
        assertEquals("http://ns4", reader.getNamespaceURI("ns4"));
        assertEquals(4, reader.getNamespaceCount());
        verifyTag(reader,"http://default-new","bar");
        
        assertEquals(XMLStreamConstants.END_ELEMENT,reader.next());
        assertEquals("http://default-new", reader.getNamespaceURI(""));
        assertEquals("http://ns1", reader.getNamespaceURI("ns1"));
        assertEquals("http://ns2-new", reader.getNamespaceURI("ns2"));
        assertEquals("http://ns3", reader.getNamespaceURI("ns3"));
        assertEquals("http://ns4", reader.getNamespaceURI("ns4"));
        assertEquals(4, reader.getNamespaceCount());
        verifyTag(reader,"http://default-new","bar");
        
        assertEquals(XMLStreamConstants.END_ELEMENT,reader.next());
        assertEquals("http://default", reader.getNamespaceURI(""));
        assertEquals("http://ns1", reader.getNamespaceURI("ns1"));
        assertEquals("http://ns2", reader.getNamespaceURI("ns2"));
        assertEquals(null, reader.getNamespaceURI("ns3"));
        assertEquals(null, reader.getNamespaceURI("ns4"));
        assertEquals(3, reader.getNamespaceCount());
        verifyTag(reader,"http://default","foo");
        
        assertEquals(XMLStreamConstants.END_DOCUMENT,reader.next());
        assertEquals(null, reader.getNamespaceURI(""));
        assertEquals(null, reader.getNamespaceURI("ns1"));
        assertEquals(null, reader.getNamespaceURI("ns2"));
    }
}
