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

public class XMLStreamBufferException extends Exception {
    
    public XMLStreamBufferException(String message) {
        super(message);
    }
    
    public XMLStreamBufferException(String message, Exception e) {
        super(message, e);
    }

    public XMLStreamBufferException(Exception e) {
        super(e);
    }
}
