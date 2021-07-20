/*
 * Copyright (c) 2005, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.xml.stream.buffer.stax;

import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamWriterEx;

import jakarta.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

/**
 * {@link XMLStreamWriter} that fills {@link MutableXMLStreamBuffer}.
 * <p>
 * TODO: need to retain all attributes/namespaces and then store all namespaces
 * before the attributes. Currently it is necessary for the caller to ensure
 * all namespaces are written before attributes and the caller must not intermix
 * calls to the writeNamespace and writeAttribute methods.
 *
 */
public class StreamWriterBufferCreator extends StreamBufferCreator implements XMLStreamWriterEx {
    private final NamespaceContexHelper namespaceContext = new NamespaceContexHelper();

    /**
     * Nesting depth of the element.
     * This field is ultimately used to keep track of the # of trees we created in
     * the buffer.
     */
    private int depth=0;

    public StreamWriterBufferCreator() {
        setXMLStreamBuffer(new MutableXMLStreamBuffer());
    }

    public StreamWriterBufferCreator(MutableXMLStreamBuffer buffer) {
        setXMLStreamBuffer(buffer);
    }

    // XMLStreamWriter

    @Override
    public Object getProperty(String str) throws IllegalArgumentException {
        return null; //return  null for all the property names instead of
                    //throwing unsupported operation exception.
    }

    @Override
    public void close() throws XMLStreamException {
    }

    @Override
    public void flush() throws XMLStreamException {
    }

    @Override
    public NamespaceContextEx getNamespaceContext() {
        return namespaceContext;
    }

    @Override
    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        /*
         * It is really unclear from the JavaDoc how to implement this method.
         */
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDefaultNamespace(String namespaceURI) throws XMLStreamException {
        setPrefix("", namespaceURI);
    }

    @Override
    public void setPrefix(String prefix, String namespaceURI) throws XMLStreamException {
        namespaceContext.declareNamespace(prefix, namespaceURI);
    }

    @Override
    public String getPrefix(String namespaceURI) throws XMLStreamException {
        return namespaceContext.getPrefix(namespaceURI);
    }


    @Override
    public void writeStartDocument() throws XMLStreamException {
        writeStartDocument("", "");
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        writeStartDocument("", "");
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        namespaceContext.resetContexts();
        
        storeStructure(T_DOCUMENT);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        storeStructure(T_END);
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        namespaceContext.pushContext();
        depth++;
        
        final String defaultNamespaceURI = namespaceContext.getNamespaceURI("");
        
        if (defaultNamespaceURI == null)
            storeQualifiedName(T_ELEMENT_LN, null, null, localName);
        else 
            storeQualifiedName(T_ELEMENT_LN, null, defaultNamespaceURI, localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        namespaceContext.pushContext();
        depth++;

        final String prefix = namespaceContext.getPrefix(namespaceURI);
        if (prefix == null) {
            throw new XMLStreamException();
        }
        
        namespaceContext.pushContext();
        storeQualifiedName(T_ELEMENT_LN, prefix, namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        namespaceContext.pushContext();
        depth++;

        storeQualifiedName(T_ELEMENT_LN, prefix, namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeStartElement(localName);
        writeEndElement();
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElement(namespaceURI, localName);
        writeEndElement();
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeStartElement(prefix, localName, namespaceURI);
        writeEndElement();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        namespaceContext.popContext();
        
        storeStructure(T_END);
        if(--depth==0)
            increaseTreeCount();
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        storeNamespaceAttribute(null, namespaceURI);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if ("xmlns".equals(prefix))
            prefix = null;
        storeNamespaceAttribute(prefix, namespaceURI);
    }


    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        storeAttribute(null, null, localName, "CDATA", value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        final String prefix = namespaceContext.getPrefix(namespaceURI);
        if (prefix == null) {
            // TODO
            throw new XMLStreamException();
        }
        
        writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        storeAttribute(prefix, namespaceURI, localName, "CDATA", value);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        storeStructure(T_TEXT_AS_STRING);
        storeContentString(data);
    }

    @Override
    public void writeCharacters(String charData) throws XMLStreamException {
        storeStructure(T_TEXT_AS_STRING);
        storeContentString(charData);
    }

    @Override
    public void writeCharacters(char[] buf, int start, int len) throws XMLStreamException {
        storeContentCharacters(T_TEXT_AS_CHAR_ARRAY, buf, start, len);
    }

    @Override
    public void writeComment(String str) throws XMLStreamException {
        storeStructure(T_COMMENT_AS_STRING);
        storeContentString(str);
    }

    @Override
    public void writeDTD(String str) throws XMLStreamException {
        // not support. just ignore.
    }

    @Override
    public void writeEntityRef(String str) throws XMLStreamException {
        storeStructure(T_UNEXPANDED_ENTITY_REFERENCE);
        storeContentString(str);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        writeProcessingInstruction(target, "");
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        storeProcessingInstruction(target, data);
    }

    // XMLStreamWriterEx
    
    @Override
    public void writePCDATA(CharSequence charSequence) throws XMLStreamException {
        if (charSequence instanceof Base64Data) {
            storeStructure(T_TEXT_AS_OBJECT);
            storeContentObject(((Base64Data)charSequence).clone());
        } else {
            writeCharacters(charSequence.toString());
        }
    }

    @Override
    public void writeBinary(byte[] bytes, int offset, int length, String endpointURL) throws XMLStreamException {
        Base64Data d = new Base64Data();
        byte b[] = new byte[length];
        System.arraycopy(bytes, offset, b, 0, length);
        d.set(b, length, null, true);
        storeStructure(T_TEXT_AS_OBJECT);
        storeContentObject(d);
    }

    @Override
    public void writeBinary(DataHandler dataHandler) throws XMLStreamException {
        Base64Data d = new Base64Data();
        d.set(dataHandler);
        storeStructure(T_TEXT_AS_OBJECT);
        storeContentObject(d);
    }

    @Override
    public OutputStream writeBinary(String endpointURL) throws XMLStreamException {
        // TODO
        throw new UnsupportedOperationException();
    }
}
