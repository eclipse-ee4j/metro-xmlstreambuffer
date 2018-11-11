/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.xml.stream.buffer;

import java.io.InputStream;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

public class RegressionTestBug1 extends junit.framework.TestCase {

    public void test() throws Exception {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("data/wsdlnamespace.wsdl");
        
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        XMLStreamBufferResult xsbr = new XMLStreamBufferResult();
        transformer.transform(new StreamSource(is), xsbr);

        XMLStreamBuffer xrb = xsbr.getXMLStreamBuffer();
        XMLStreamReader xr = xrb.readAsXMLStreamReader();

        while (xr.hasNext()) {
            int t = xr.next();

            if (t == XMLStreamConstants.START_ELEMENT) {
                if (xr.getName().getLocalPart().contains("elementFormDefault")) {
                    fail("Attribute names printed for element name calls.");
                }
            }
        }
    }
}
