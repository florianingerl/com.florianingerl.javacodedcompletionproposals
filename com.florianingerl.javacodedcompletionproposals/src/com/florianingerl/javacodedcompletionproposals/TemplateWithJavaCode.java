package com.florianingerl.javacodedcompletionproposals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TemplateWithJavaCode {

	private String name = "Default name";
	private String description = "Default description";

	private File xmlFile;

	public TemplateWithJavaCode() {

	}

	public void loadFromFile(File xmlFile) throws SAXException, IOException, ParserConfigurationException {
		this.xmlFile = xmlFile;

		loadFromStream(new FileInputStream(xmlFile));

	}

	public void loadFromStream(InputStream stream) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(stream);
		Element documentElement = doc.getDocumentElement();

		this.name = documentElement.getElementsByTagName("name").item(0).getTextContent();
		this.description = documentElement.getElementsByTagName("description").item(0).getTextContent();

		stream.close();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

}
