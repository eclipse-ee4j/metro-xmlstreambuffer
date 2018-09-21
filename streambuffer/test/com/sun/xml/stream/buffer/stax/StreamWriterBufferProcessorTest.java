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

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import java.net.URL;

/**
 * @author Rama Pulavarthi
 */
public class StreamWriterBufferProcessorTest extends junit.framework.TestCase {
    private static final String SOAP_MESSAGE = "data/test-epr.xml";
    private URL test_doc;

    public StreamWriterBufferProcessorTest(String testName) {
        super(testName);
        test_doc = this.getClass().getClassLoader().getResource(SOAP_MESSAGE);
    }

    /**
     * test where there is a comment at the start.
     * @throws Exception
     */
    public void testBufferWithCommentAtStart() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance().
                createXMLStreamReader(test_doc.openStream());
        XMLStreamBuffer xsb = MutableXMLStreamBuffer.createNewBufferFromXMLStreamReader(reader);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
        StreamWriterBufferProcessor p = new StreamWriterBufferProcessor(xsb, true);
        p.writeFragment(writer);
        writer.close();
    }
}
