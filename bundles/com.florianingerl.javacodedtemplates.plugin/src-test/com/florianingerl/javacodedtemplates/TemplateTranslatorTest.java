package com.florianingerl.javacodedtemplates;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariableType;
import org.junit.BeforeClass;
import org.junit.Test;

import com.florianingerl.javacodedtemplates.JavaCodedTemplateTranslator;
import com.florianingerl.util.regex.Capture;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import static com.florianingerl.javacodedtemplates.JavaCoded2EclipseTemplateConverterTest.readResource;
import static com.florianingerl.javacodedtemplates.JavaCoded2EclipseTemplateConverterTest.initialize;
import static com.florianingerl.javacodedtemplates.JavaCoded2EclipseTemplateConverterTest.deleteDir;

public class TemplateTranslatorTest {

	@BeforeClass
	public static void setUpBeforeClass() {
		initialize();

		File dir = ServiceLocator.getInjector().getInstance(ITemplateStoreDirProvider.class).getTemplateStoreDir();
		deleteDir(dir);
	}

	private void testTemplate(String templateName, boolean good) {
		String pattern = readResource(templateName + ".txt");

		Template template = new Template(templateName, "description", "SWT statements", pattern, false);
		JavaCodedTemplateTranslator translator = new JavaCodedTemplateTranslator();
		try {
			translator.translate(template);
		} catch (TemplateException e) {

			e.printStackTrace();
			if (good)
				assertTrue(false);
			else
				return;
		}
		if (!good)
			assertTrue(false);
	}

	@Test
	public void translateBadTemplatesTest() {
		testTemplate("badtemplate", false);
		testTemplate("badtemplate2", false);
	}

	@Test
	public void translateGoodTemplatesTest() {
		testTemplate("threeparameters", true);
	}

}
