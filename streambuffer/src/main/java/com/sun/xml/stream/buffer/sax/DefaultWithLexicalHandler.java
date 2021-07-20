/*
 * Copyright (c) 2005, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.xml.stream.buffer.sax;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public class DefaultWithLexicalHandler extends DefaultHandler implements LexicalHandler {

    public DefaultWithLexicalHandler() {
        super();
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException { }
    
    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException { }
    
    @Override
    public void endDTD() throws SAXException { }
    
    @Override
    public void startEntity(String name) throws SAXException { }
    
    @Override
    public void endEntity(String name) throws SAXException { }
    
    @Override
    public void startCDATA() throws SAXException { }
    
    @Override
    public void endCDATA() throws SAXException { }
    
}
