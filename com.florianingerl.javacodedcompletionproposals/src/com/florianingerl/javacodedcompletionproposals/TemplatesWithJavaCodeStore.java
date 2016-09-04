package com.florianingerl.javacodedcompletionproposals;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class TemplatesWithJavaCodeStore {
	public static List<TemplateWithJavaCode> templates = new LinkedList<TemplateWithJavaCode>();

	public static void loadAndCompileAllTemplates(File locationDir) {
		File[] xmlFiles = locationDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().toLowerCase().endsWith(".xml");
			}
		});
		for (File xmlFile : xmlFiles) {

			TemplateWithJavaCode template = new TemplateWithJavaCode();
			try {
				template.loadFromFile(xmlFile);
				templates.add(template);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
