/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/**
 * Stream based representation for XML infoset
 */
module com.sun.xml.streambuffer {
    requires java.xml;
    requires org.jvnet.staxex;

    exports com.sun.xml.stream.buffer;
    exports com.sun.xml.stream.buffer.sax;
    exports com.sun.xml.stream.buffer.stax;
}
