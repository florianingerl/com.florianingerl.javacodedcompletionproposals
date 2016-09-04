package com.florianingerl.javacodedcompletionproposals;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TemplateWithJavaCodeTest {

	@Test
	public void loadFromStreamTest() {
		try {
			TemplateWithJavaCode template = new TemplateWithJavaCode();

			template.loadFromStream(IOUtils.toInputStream(
					"<templatewithjavacode><name>full property</name><description>Java property with change notifications</description></templatewithjavacode>"));
			assertEquals("full property", template.getName());
			assertEquals("Java property with change notifications", template.getDescription());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}

	}

}
