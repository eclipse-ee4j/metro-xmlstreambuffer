/*
 * Copyright (c) 2005, 2022 Oracle and/or its affiliates. All rights reserved.
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
import org.jvnet.staxex.XMLStreamReaderEx;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Create a buffer using an {@link XMLStreamReader}.
 * <p>
 * TODO: Implement the marking the stream on the element when an ID
 * attribute on the element is defined
 */
public class StreamReaderBufferCreator extends StreamBufferCreator {
    private int _eventType;
    private boolean _storeInScopeNamespacesOnElementFragment;
    private Map<String, Integer> _inScopePrefixes;

    /**
     * Create a stream reader buffer creator.
     * <p>
     * A stream buffer will be created for storing the infoset
     * from a stream reader.
     */
    public StreamReaderBufferCreator() {
    }

    /**
     * Create a stream reader buffer creator using a mutable stream buffer.
     *
     * @param buffer the mutable stream buffer.
     */
    public StreamReaderBufferCreator(MutableXMLStreamBuffer buffer) {
        setBuffer(buffer);
    }

    /**
     * Create the buffer from a stream reader.
     * <p>
     * The stream reader must be positioned at the start of the document
     * or the start of an element.
     * <p>
     * If the stream is positioned at the start of the document then the
     * whole document is stored and after storing the stream will be positioned
     * at the end of the document.
     * <p>
     * If the stream is positioned at the start of an element then the
     * element and all its children will be stored and after storing the stream
     * will be positioned at the next event after the end of the element.
     *
     * @return the mutable stream buffer.
     * @throws XMLStreamException if the stream reader is not positioned at
     *         the start of the document or at an element.
     */
    public MutableXMLStreamBuffer create(XMLStreamReader reader) throws XMLStreamException {
        if (_buffer == null) {
            createBuffer();
        }
        store(reader);

        return getXMLStreamBuffer();
    }

    /**
     * Creates the buffer from a stream reader that is an element fragment.
     * <p>
     * The stream reader will be moved to the position of the next start of
     * an element if the stream reader is not already positioned at the start
     * of an element.
     * <p>
     * The element and all its children will be stored and after storing the stream
     * will be positioned at the next event after the end of the element.
     *
     * @param storeInScopeNamespaces true if in-scope namespaces of the element
     *        fragment should be stored.
     * @return the mutable stream buffer.
     * @throws XMLStreamException if the stream reader cannot be positioned at
     *         the start of an element.
     */
    public MutableXMLStreamBuffer createElementFragment(XMLStreamReader reader,
            boolean storeInScopeNamespaces) throws XMLStreamException {
        if (_buffer == null) {
            createBuffer();
        }

        if (!reader.hasNext()) {
            return _buffer;
        }

        _storeInScopeNamespacesOnElementFragment = storeInScopeNamespaces;

        _eventType = reader.getEventType();
        if (_eventType != XMLStreamReader.START_ELEMENT) {
            do {
                _eventType = reader.next();
            } while(_eventType != XMLStreamReader.START_ELEMENT && _eventType != XMLStreamReader.END_DOCUMENT);
        }

        if (storeInScopeNamespaces) {
            _inScopePrefixes = new HashMap<>();
        }

        storeElementAndChildren(reader);

        return getXMLStreamBuffer();
    }

    private void store(XMLStreamReader reader) throws XMLStreamException {
        if (!reader.hasNext()) {
            return;
        }

        _eventType = reader.getEventType();
        switch (_eventType) {
            case XMLStreamReader.START_DOCUMENT:
                storeDocumentAndChildren(reader);
                break;
            case XMLStreamReader.START_ELEMENT:
                storeElementAndChildren(reader);
                break;
            default:
                throw new XMLStreamException("XMLStreamReader not positioned at a document or element");
        }

        increaseTreeCount();
    }

    private void storeDocumentAndChildren(XMLStreamReader reader) throws XMLStreamException {
        storeStructure(T_DOCUMENT);

        _eventType = reader.next();
        while (_eventType != XMLStreamReader.END_DOCUMENT) {
            switch (_eventType) {
                case XMLStreamReader.START_ELEMENT:
                    storeElementAndChildren(reader);
                    continue;
                case XMLStreamReader.COMMENT:
                    storeComment(reader);
                    break;
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    storeProcessingInstruction(reader);
                    break;
            }
            _eventType = reader.next();
        }

        storeStructure(T_END);
    }

    private void storeElementAndChildren(XMLStreamReader reader) throws XMLStreamException {
        if (reader instanceof XMLStreamReaderEx) {
            storeElementAndChildrenEx((XMLStreamReaderEx)reader);
        } else {
            storeElementAndChildrenNoEx(reader);
        }
    }

    private void storeElementAndChildrenEx(XMLStreamReaderEx reader) throws XMLStreamException {
        int depth = 1;
        if (_storeInScopeNamespacesOnElementFragment) {
            storeElementWithInScopeNamespaces(reader);
        } else {
            storeElement(reader);
        }

        while(depth > 0) {
            _eventType = reader.next();
            switch (_eventType) {
                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    storeElement(reader);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    depth--;
                    storeStructure(T_END);
                    break;
                case XMLStreamReader.NAMESPACE:
                    storeNamespaceAttributes(reader);
                    break;
                case XMLStreamReader.ATTRIBUTE:
                    storeAttributes(reader);
                    break;
                case XMLStreamReader.SPACE:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA: {
                    CharSequence c = reader.getPCDATA();
                    if (c instanceof Base64Data) {
                        storeStructure(T_TEXT_AS_OBJECT);
                        //Instead of clone the Base64Data, the original Base64Data instance is used here to preserve the DataHandler
                        storeContentObject(c);
                    } else {
                        storeContentCharacters(T_TEXT_AS_CHAR_ARRAY,
                                reader.getTextCharacters(), reader.getTextStart(),
                                reader.getTextLength());
                    }
                    break;
                }
                case XMLStreamReader.COMMENT:
                    storeComment(reader);
                    break;
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    storeProcessingInstruction(reader);
                    break;
            }
        }

        /*
         * Move to next item after the end of the element
         * that has been stored
         */
        _eventType = reader.next();
    }

    private void storeElementAndChildrenNoEx(XMLStreamReader reader) throws XMLStreamException {
        int depth = 1;
        if (_storeInScopeNamespacesOnElementFragment) {
            storeElementWithInScopeNamespaces(reader);
        } else {
            storeElement(reader);
        }

        while(depth > 0) {
            _eventType = reader.next();
            switch (_eventType) {
                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    storeElement(reader);
                    break;
                case XMLStreamReader.END_ELEMENT:
                    depth--;
                    storeStructure(T_END);
                    break;
                case XMLStreamReader.NAMESPACE:
                    storeNamespaceAttributes(reader);
                    break;
                case XMLStreamReader.ATTRIBUTE:
                    storeAttributes(reader);
                    break;
                case XMLStreamReader.SPACE:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA: {
                    storeContentCharacters(T_TEXT_AS_CHAR_ARRAY,
                            reader.getTextCharacters(), reader.getTextStart(),
                            reader.getTextLength());
                    break;
                }
                case XMLStreamReader.COMMENT:
                    storeComment(reader);
                    break;
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                    storeProcessingInstruction(reader);
                    break;
            }
        }

        /*
         * Move to next item after the end of the element
         * that has been stored
         */
        _eventType = reader.next();
    }

    private void storeElementWithInScopeNamespaces(XMLStreamReader reader) {
        storeQualifiedName(T_ELEMENT_LN,
                reader.getPrefix(), reader.getNamespaceURI(), reader.getLocalName());

        if (reader.getNamespaceCount() > 0) {
            storeNamespaceAttributes(reader);
        }

        if (reader.getAttributeCount() > 0) {
            storeAttributes(reader);
        }
    }

    private void storeElement(XMLStreamReader reader) {
        storeQualifiedName(T_ELEMENT_LN,
                reader.getPrefix(), reader.getNamespaceURI(), reader.getLocalName());

        if (reader.getNamespaceCount() > 0) {
            storeNamespaceAttributes(reader);
        }

        if (reader.getAttributeCount() > 0) {
            storeAttributes(reader);
        }
    }

    /**
     * A low level method a create a structure element explicitly. This is useful when xsb is
     * created from a fragment's XMLStreamReader and inscope namespaces can be passed using
     * this method. Note that there is no way to enumerate namespaces from XMLStreamReader.
     *
     * For e.g: Say the SOAP message is as follows
     * <pre>{@code
     *  <S:Envelope xmlns:n1=".."><S:Body><ns2:A> ...}
     * </pre>
     * when xsb is to be created using a reader that is at {@code <ns2:A>} tag, the inscope
     * namespace like 'n1' can be passed using this method.
     *
     * WARNING: Instead of using this, try other methods(if you don't know what you are
     * doing).
     *
     * @param ns an array of the even length of the form { prefix0, uri0, prefix1, uri1, ... }.
     */
    public void storeElement(String nsURI, String localName, String prefix, String[] ns) {
        storeQualifiedName(T_ELEMENT_LN, prefix, nsURI, localName);
        storeNamespaceAttributes(ns);
    }

    /**
     * A low level method a create a structure element explicitly. This is
     * required to support {@link #storeElement} method.
     *
     * WARNING: Instead of using this, try other methods(if you don't know what
     * you are doing).
     */
    public void storeEndElement() {
        storeStructure(T_END);
    }

    private void storeNamespaceAttributes(XMLStreamReader reader) {
        int count = reader.getNamespaceCount();
        for (int i = 0; i < count; i++) {
            storeNamespaceAttribute(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }
    }

    /**
     * @param ns an array of the even length of the form { prefix0, uri0, prefix1, uri1, ... }.
     */
    private void storeNamespaceAttributes(String[] ns) {
        for (int i = 0; i < ns.length; i=i+2) {
            storeNamespaceAttribute(ns[i], ns[i+1]);
        }
    }

    private void storeAttributes(XMLStreamReader reader) {
        int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            storeAttribute(reader.getAttributePrefix(i), reader.getAttributeNamespace(i), reader.getAttributeLocalName(i),
                    reader.getAttributeType(i), reader.getAttributeValue(i));
        }
    }

    private void storeComment(XMLStreamReader reader) {
        storeContentCharacters(T_COMMENT_AS_CHAR_ARRAY,
                reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
    }

    private void storeProcessingInstruction(XMLStreamReader reader) {
        storeProcessingInstruction(reader.getPITarget(), reader.getPIData());
    }
}
