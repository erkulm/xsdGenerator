package com.hititcs.xsdgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wiztools.commons.Charsets;
import org.wiztools.commons.StringUtil;
import org.xml.sax.SAXException;

/**
 * @author subWiz
 */
public final class XsdGen {

	private static final String XSD_NS_URI = "http://www.w3.org/2001/XMLSchema";

	private final String xsdPrefix;
	private final boolean enableMaxOccursOnce;

	private Document doc = null; // populated after the parse method is called!

	/**
	 * Constructs new {@code XsdGen} with defaults settings.
	 */
	public XsdGen() {
		this(new XsdConfig());
	}

	/**
	 * Constructs new {@code XsdGen} with parameters :
	 *
	 * @param config
	 *            the XSD configuration
	 */
	public XsdGen(XsdConfig config) {
		xsdPrefix = config.getXsdPrefix();
		this.enableMaxOccursOnce = config.isEnableMaxOccursOnce();
	}

	private void processAttributes(final Element inElement, final Element outElement) {
		NamedNodeMap attributesMap = inElement.getAttributes();
		
		for (int i = 0; i < attributesMap.getLength(); i++) {
			final Node attribute = attributesMap.item(i);
			final String name = attribute.getLocalName();
			final String value = attribute.getNodeValue();
			Element attributeElement = outElement.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + "attribute");
			attributeElement.setAttribute("name", name);
			attributeElement.setAttribute("type", xsdPrefix + TypeInferenceUtil.getTypeOfContent(value));
			attributeElement.setAttribute("use"," required");
			outElement.appendChild(attributeElement);
		}
	}

	private void recurseGen(Element parent, Element parentOutElement) {
		// Adding complexType element:
		Element complexType = null;
		Element sequence = null;
		if (parentOutElement != null && parentOutElement.getChildNodes() != null && parentOutElement.getChildNodes().getLength() == 0) {
			complexType = parentOutElement.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + ":complexType");
			sequence = parentOutElement.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + ":sequence");
			complexType.appendChild(sequence);
			processAttributes(parent, complexType);
		} else {
			try {
				
				complexType = (Element) parentOutElement.getParentNode();
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
			sequence = parentOutElement;

		}
		try {
			parentOutElement.appendChild(complexType);
		} catch (Exception e) {
			//
		}

		NodeList children = parent.getChildNodes();
		final Set<String> elementNamesProcessed = new HashSet<>();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i).getNodeType() == javax.xml.soap.Node.ELEMENT_NODE)) {
				continue;
			}
			Element e = (Element) children.item(i);
			final String localName = e.getLocalName();
			final String nsURI = e.getNamespaceURI();
			final String nsName = e.getTagName();

			if (!elementNamesProcessed.contains(nsName) && !alreadyExists(parentOutElement, nsName)) { // process
																										// an
																										// element
																										// first
																										// time
																										// only
				if (e.getChildNodes().getLength() > 0) { // Is complex type with
														// children!
					Element element = null;
					element = parentOutElement.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + ":element");
					element.setAttribute("name", localName);
					processOccurences(element, parent, localName, nsURI);
					recurseGen(e, element); // recurse into children:
					sequence.appendChild(element);

				} else {
					final String cnt = e.getNodeValue();
					final String eValue = cnt == null ? null : cnt.trim();
					final String type = xsdPrefix + TypeInferenceUtil.getTypeOfContent(eValue);
					Element element = null;
					element = parentOutElement.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + ":element");
					element.setAttribute("name", localName);
					processOccurences(element, parent, localName, nsURI);

					// Attributes
					final int attrCount = e.getAttributes().getLength();
					if (attrCount > 0) {
						// has attributes: complex type without sequence!
						Element complexTypeCurrent = null;
						complexTypeCurrent = parentOutElement.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + ":complexType");
						Element simpleContent = null;
						simpleContent = parentOutElement.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + ":simpleContent");
						Element extension = null;
						extension = parentOutElement.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + ":extension");
						extension.setAttribute("base", type);
						processAttributes(e, extension);
						simpleContent.appendChild(extension);
						complexTypeCurrent.appendChild(simpleContent);
						element.appendChild(complexTypeCurrent);
					} else { // if no attributes, just put the type:
						element.setAttribute("type", type);
					}
					sequence.appendChild(element);
				}
			} else if (elementNamesProcessed.contains(nsName)) {
				Element temp = null;
				if (e.getChildNodes().getLength() > 0) { // complexType
					temp = getParentOutElement(parentOutElement, e.getLocalName());
					if (getDescendantByLocalName(temp, "sequence") != null) {
						temp = getDescendantByLocalName(temp, "sequence");
					}
				}
				if (temp == null) {
					System.err.println("aaa");
				}
				recurseGen(e, temp);
			}
			elementNamesProcessed.add(nsName);
		}
	}

	private Element getParentOutElement(Element base, String name) {
		NodeList children = base.getChildNodes();
		if (base != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getAttributes().getNamedItem("name") != null
						? children.item(i).getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase(name) : false) {
					return (Element) children.item(i);
				}
			}
			for (int j = 0; j < children.getLength(); j++) {
				Element returned = getParentOutElement((Element) children.item(j), name);
				if (returned != null) {
					return returned;
				}
			}

		}
		return null;
	}

	private Boolean alreadyExists(Element element, String name) {
		Boolean exists = Boolean.FALSE;

		NodeList childNodes = element.getChildNodes();
		if (element != null && element.getChildNodes().getLength() > 0) {


			for (int k = 0; k < childNodes.getLength(); k++) {
				if (childNodes.item(k).getAttributes().getNamedItem("name") != null
						? childNodes.item(k).getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase(name) : false) {
					exists = Boolean.TRUE;
				}
			}
		}
		return exists;
	}

	private Element getDescendantByLocalName(Element base, String name) {
		if (base != null && base.getChildNodes().getLength() > 0) {
			NodeList children = base.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getLocalName() != null ? children.item(i).getLocalName().equalsIgnoreCase(name)
						: false) {
					return (Element) children.item(i);
				}
			}
			for (int j = 0; j < children.getLength(); j++) {
				Element returned = getDescendantByLocalName((Element) children.item(j), name);
				if (returned != null) {
					return returned;
				}
			}

		}
		return null;
	}

	private void processOccurences(final Element element, final Element parent, final String localName,
			final String nsURI) {
		if (parent.getChildNodes().getLength() > 1) {
			element.setAttribute("maxOccurs", "unbounded");
		} else {
			element.setAttribute("minOccurs", "0");
			if (enableMaxOccursOnce)
				element.setAttribute("maxOccurs", "1");
		}
	}

	private Document getDocument(InputStream is) throws IOException, ParserConfigurationException, SAXException {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();
			Document d = parser.parse(is);
			final Element rootElement = d.getDocumentElement();
			
			DocumentBuilderFactory outDbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder outParser = outDbf.newDocumentBuilder();
			

			// output Document
			Element outRoot = null;
			Document outDoc = outParser.newDocument();
			
			outRoot = outDoc.createElementNS(XSD_NS_URI, xsdPrefix + ":schema");
			

			// setting targetNamespace
			final String nsPrefix = rootElement.getPrefix();
			final boolean hasXmlns = rootElement.getNamespaceURI() != null;
			if (hasXmlns || StringUtil.isNotEmpty(nsPrefix)) {
				outRoot.setAttribute(rootElement.getNamespaceURI(), "targetNameSpace");
				outRoot.setAttribute("elementFormDefault", "qualified");
			}

			// adding all other namespace attributes
//			for (int i = 0; i < rootElement.getNamespaceDeclarationCount(); i++) {
//				final String nsPrefix2 = rootElement.getNamespacePrefix(i);
//				final String nsURI = rootElement.getNamespaceURI(nsPrefix2);
//				outRoot.addNamespaceDeclaration(nsPrefix, nsURI);
//			}

			// adding the root element
			Element rootElementXsd = outRoot.getOwnerDocument().createElementNS(XSD_NS_URI, xsdPrefix + ":element");
			rootElementXsd.setAttribute("name", rootElement.getLocalName());
			outRoot.appendChild(rootElementXsd);
			recurseGen(rootElement, rootElementXsd);
			return outDoc;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public XsdGen parse(File file) throws IOException, ParserConfigurationException, SAXException {
		return parse(new FileInputStream(file));
	}

	public XsdGen parse(InputStream is) throws IOException, ParserConfigurationException, SAXException {
			doc = getDocument(is);
			return this;
	}

	public void write(final OutputStream os) throws IOException, TransformerException {
		if (doc == null)
			throw new IllegalStateException("Call parse() before calling this method!");
		write(os, Charsets.UTF_8);
	}

	public void write(final OutputStream os, final Charset charset) throws IOException, TransformerException {
		if (doc == null)
			throw new IllegalStateException("Call parse() before calling this method!");
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
		
		System.out.println(output);
//		// Display output:
//		Serializer serializer = new Serializer(os, charset.name());
//		serializer.setIndent(4);
//		serializer.write(doc);
	}

//	@Override
//	public String toString() {
////		return ((Object) doc).toXML();
//	}
}