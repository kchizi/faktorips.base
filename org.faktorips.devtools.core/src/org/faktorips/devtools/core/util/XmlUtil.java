/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.IpsPlugin;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A collection of utility methods for XML handling.
 * 
 * @author Jan Ortmann
 */
public class XmlUtil {

    public static final String XML_ATTRIBUTE_SPACE = "xml:space"; //$NON-NLS-1$

    public static final String XML_ATTRIBUTE_SPACE_VALUE = "preserve"; //$NON-NLS-1$

    /**
     * This is a thread local variable because the document builder is not thread safe.
     */
    private static ThreadLocal<DocumentBuilder> docBuilderHolder = new DocBuilderHolder();

    /**
     * This is a thread local variable because the {@link Transformer} is not thread safe.
     */
    private static ThreadLocal<Transformer> transformerHolder = new ThreadLocal<Transformer>() {
        @Override
        protected Transformer initialValue() {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            try {
                return transformerFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                IpsPlugin.log(e);
            }
            return null;
        }
    };

    private XmlUtil() {
        // Utility class not to be instantiated.
    }

    private static Transformer getTransformer() {
        return transformerHolder.get();
    }

    public static final void setAttributeConvertNullToEmptyString(Element el, String attribute, String value) {
        if (value == null) {
            el.setAttribute(attribute, ""); //$NON-NLS-1$
        } else {
            el.setAttribute(attribute, value);
        }
    }

    public static final String getAttributeConvertEmptyStringToNull(Element el, String attribute) {
        String value = el.getAttribute(attribute);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return value;
    }

    public static final String dateToXmlDateString(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return gregorianCalendarToXmlDateString(calendar);
    }

    public static final String gregorianCalendarToXmlDateString(GregorianCalendar calendar) {
        if (calendar == null) {
            return StringUtils.EMPTY;
        }
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        return calendar.get(Calendar.YEAR) + "-" + (month < 10 ? "0" + month : "" + month) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "-" + (date < 10 ? "0" + date : "" + date); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Parses the given XML String to a Date.
     */
    public static final Date parseXmlDateStringToDate(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return parseXmlDateStringToGregorianCalendar(s).getTime();
    }

    /**
     * Parses the given XML String to a Gregorian calendar.
     * 
     * @throws IllegalArgumentException If the given string cannot be parsed to a Gregorian
     *             calendar.
     * 
     * @deprecated Use {@link #parseGregorianCalendar(String)} instead.
     */
    @Deprecated
    // Deprecated since 3.0
    public static final GregorianCalendar parseXmlDateStringToGregorianCalendar(String s) {
        try {
            return parseGregorianCalendar(s);
        } catch (XmlParseException e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static final GregorianCalendar parseGregorianCalendar(String s) throws XmlParseException {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(s, "-"); //$NON-NLS-1$
            int year = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int date = Integer.parseInt(tokenizer.nextToken());
            return new GregorianCalendar(year, month - 1, date);
        } catch (NumberFormatException e) {
            throw new XmlParseException("Can't parse " + s + " to a date!", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Transforms the given node to a String.
     */
    public static final String nodeToString(Node node, String encoding) throws TransformerException {
        boolean preserveSpace = removePreserveSpace(node);

        StringWriter writer = new StringWriter();
        nodeToWriter(node, writer, encoding);
        String xml = writer.toString();

        if (preserveSpace) {
            xml = addPreserveSpace(xml);
        }
        xml = noIndentationAroundCDATA(xml);
        return removeDuplicateWindowsLineBreaks(xml);
    }

    /**
     * Java 9+ respects {@code xml:space="preserve"} even when writing and ignores indentation
     * settings. We remove the attribute before the transformation and add it to the String
     * afterwards to prevent other tools from formatting the XML.
     * 
     * @return whether {@code xml:space="preserve"} was found on the node
     */
    private static boolean removePreserveSpace(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null && attributes.getNamedItem(XML_ATTRIBUTE_SPACE) != null) {
            attributes.removeNamedItem(XML_ATTRIBUTE_SPACE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see XmlUtil#removePreserveSpace(Node)
     */
    private static String addPreserveSpace(String xml) {
        return xml.replaceFirst("(?<=[^?!/])>", //$NON-NLS-1$
                " " + XML_ATTRIBUTE_SPACE + "=\"" + XML_ATTRIBUTE_SPACE_VALUE + "\">"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Java 9+ adds indentation around CDATA. We remove it to avoid changes in XML files created
     * with Java <=8.
     */
    private static String noIndentationAroundCDATA(String xml) {
        return xml.replaceAll(">[\\n\\r\\s]+<!\\[CDATA", "><![CDATA") //$NON-NLS-1$//$NON-NLS-2$
                .replaceAll("]]>[\\n\\r\\s]+</", "]]></"); //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Workaround for Windows bug producing \r\r\n in CDATA sections.
     */
    private static String removeDuplicateWindowsLineBreaks(String xml) {
        return xml.replace("\r\r", "\r"); //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Transforms the given node to a string and writes in to the given writer.
     * <p>
     * The encoding that is used to transforms the string into bytes is defined by the writer. E.g.
     * a <code>OutputStreamWriter</code> is created with a char set / encoding. With a
     * </code>StringWriter</code> no encoding is necessary.
     * <p>
     * However, to get the encoding option set in the XML header e.g. <code>&lt;?xml version="1.0"
     * encoding="Cp1252"?&gt;</code>, it is necessary to pass the encoding to this method. Note that
     * this method does not check, if the writer's encoding and the given encoding are the same (as
     * the encoding is not available from the writer).
     */
    public static final void nodeToWriter(Node node, Writer writer, String encoding) throws TransformerException {
        Transformer transformer = getTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        // workaround to avoid linebreak after xml declaration
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ""); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1"); //$NON-NLS-1$ //$NON-NLS-2$
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
    }

    /**
     * @deprecated Use {@link #parseDocument(InputStream)} instead.
     */
    @Deprecated
    // Deprecated since 3.0
    @SuppressWarnings("unused")
    // Unused exception suppressed because of deprecation.
    // CSOFF: ThrowsCount
    public static final Document getDocument(InputStream is)
            throws SAXException, IOException, ParserConfigurationException {
        return getDefaultDocumentBuilder().parse(is);
    }

    // CSON: ThrowsCount

    public static final Document parseDocument(InputStream is) throws SAXException, IOException {
        return getDefaultDocumentBuilder().parse(is);
    }

    public static final DocumentBuilder getDefaultDocumentBuilder() {
        return docBuilderHolder.get();
    }

    /**
     * Writes a XML document to a file.
     * <p>
     * See also the
     * <a href='http://developers.sun.com/sw/building/codesamples/dom/doc/DOMUtil.java'>DOMUtil.java
     * example</a>.
     */
    public static void writeXMLtoFile(File file, Document doc, String doctype, int indentWidth, String encoding)
            throws TransformerException {

        writeXMLtoResult(new StreamResult(file), doc, doctype, indentWidth, encoding);
    }

    /**
     * Writes a XML document to a file.
     * <p>
     * See also the
     * <a href='http://developers.sun.com/sw/building/codesamples/dom/doc/DOMUtil.java'>DOMUtil.java
     * example</a>.
     */
    public static void writeXMLtoStream(OutputStream os, Document doc, String doctype, int indentWidth, String encoding)
            throws TransformerException {

        writeXMLtoResult(new StreamResult(os), doc, doctype, indentWidth, encoding);
    }

    /**
     * Writes a XML document to a DOM result object.
     * <p>
     * See also the
     * <a href='http://developers.sun.com/sw/building/codesamples/dom/doc/DOMUtil.java'>DOMUtil.java
     * example</a>.
     */
    private static void writeXMLtoResult(Result res, Document doc, String doctype, int indentWidth, String encoding)
            throws TransformerException {
        Source src = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
        // workaround to avoid linebreak after xml declaration
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ""); //$NON-NLS-1$
        if (encoding != null) {
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        }
        if (doctype != null) {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        if (indentWidth > 0) {
            // both settings are necessary, to accommodate versions in Java 1.4 and 1.5
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indentWidth)); //$NON-NLS-1$
        }
        transformer.transform(src, res);
    }

    public static final Element getFirstElement(Node parent, String tagName) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    return (Element)nl.item(i);
                }
            }
        }
        return null;
    }

    /**
     * Returns the first Element node
     */
    public static final Element getFirstElement(Node parent) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                return (Element)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the child element with the given tag name and index. The index is the position of the
     * element considering all child elements with the given tag name. In contrast to
     * Element#getElementsByTagName(String tagName) this method returns only the direct children,
     * not all descendants.
     * 
     * @param parent The parent node.
     * @param tagName the element tag name.
     * @param index The 0 based position of the child.
     * @return The element at the specified index
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     * 
     * @see Element#getElementsByTagName(java.lang.String)
     */
    public static final Element getElement(Node parent, String tagName, int index) {
        NodeList nl = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    if (count == index) {
                        return (Element)nl.item(i);
                    }
                    count++;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the child element at the given index. The index is the position of the element
     * considering all child nodes of type element.
     * 
     * @param parent The parent node.
     * @param index The 0 based position of the child.
     * 
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     */
    public static final Element getElement(Node parent, int index) {
        NodeList nl = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                if (count == index) {
                    return (Element)nl.item(i);
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the node's text child node or <code>null</code> if the node hasn't got a text node.
     */
    public static final Text getTextNode(Node node) {
        node.normalize();
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
                return (Text)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the node's first CDATA section or <code>null</code> if the node hasn't got one.
     */
    public static final CDATASection getFirstCDataSection(Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                return (CDATASection)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the node's first CDATA section if the node has one. If not, this returns the node's
     * text child node or <code>null</code> if the node hasn't got a text node.
     */
    public static final String getCDATAorTextContent(Node node) {
        if (XmlUtil.getFirstCDataSection(node) != null) {
            return XmlUtil.getFirstCDataSection(node).getData();
        } else if (XmlUtil.getTextNode(node) != null) {
            return XmlUtil.getTextNode(node).getData();
        }
        return null;
    }

    /**
     * Adds a child Element by the name given in childName to the parent and returns the child.
     */
    public static final Element addNewChild(Document doc, Node parent, String childName) {
        Element e = doc.createElement(childName);
        parent.appendChild(e);
        return e;
    }

    /**
     * Adds a TextNode containing the text to the parent and returns the TextNode.
     */
    public static final Node addNewTextChild(Document doc, Node parent, String text) {
        Node n = doc.createTextNode(text);
        parent.appendChild(n);
        return n;
    }

    /**
     * Adds a CDATASection containing the text to the parent and returns the CDATASection.
     */
    public static final Node addNewCDATAChild(Document doc, Node parent, String text) {
        Node n = doc.createCDATASection(text);
        parent.appendChild(n);
        return n;
    }

    /**
     * Adds a TextNode or, if text contains chars>127, a CDATASection containing the text to the
     * parent and returns this new child.
     */
    public static final Node addNewCDATAorTextChild(Document doc, Node parent, String text) {
        if (text == null) {
            return null;
        }
        char[] chars = text.toCharArray();
        boolean toCDATA = false;
        for (int i = 0; i < chars.length && !toCDATA; i++) {
            if (chars[i] < 32 || 126 < chars[i]) {
                toCDATA = true;
            }
        }
        return toCDATA ? addNewCDATAChild(doc, parent, text) : addNewTextChild(doc, parent, text);
    }

    /**
     * Returns the value for the given property from the given parent element. The parent XML
     * element must has the following format:
     * 
     * <pre>
     *   <Parent>
     *      <Property isNull="false"</Property>
     *   </Parent>
     * </pre>
     * 
     * @throws NullPointerException if parent or propertyName is <code>null</code> or the parent
     *             element does not contain an element with the given propertyName.
     */
    public String getValueFromElement(Element parent, String propertyName) {
        Element propertyEl = XmlUtil.getFirstElement(parent, propertyName);
        if (propertyEl == null) {
            throw new NullPointerException();
        }
        String isNull = parent.getAttribute("isNull"); //$NON-NLS-1$
        if (Boolean.valueOf(isNull).booleanValue()) {
            return null;
        } else {
            Text textNode = getTextNode(parent);
            if (textNode == null) {
                return ""; //$NON-NLS-1$
            } else {
                return textNode.getNodeValue();
            }
        }
    }

    private static final class DocBuilderHolder extends ThreadLocal<DocumentBuilder> {
        @Override
        protected DocumentBuilder initialValue() {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void warning(SAXParseException e) throws SAXException {
                    throw e;
                }
            });
            return builder;
        }
    }
}
