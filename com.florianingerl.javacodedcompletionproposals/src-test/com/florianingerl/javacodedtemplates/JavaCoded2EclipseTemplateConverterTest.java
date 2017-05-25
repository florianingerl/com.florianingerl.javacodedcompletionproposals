package com.florianingerl.javacodedtemplates;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class JavaCoded2EclipseTemplateConverterTest {

	@BeforeClass
	public static void initialize() {
		if (ServiceLocator.getInjector() == null) {
			Injector injector = Guice.createInjector(new TestDependencyResolverModule());
			ServiceLocator.setInjector(injector);
		}
	}

	private String readResource(String resource) {
		String s = null;
		try {
			s = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("resources/" + resource));
		} catch (IOException e) {
			e.printStackTrace();
			assertFalse(true);
		}
		return s;
	}

	private void testConvert(String templateName) {
		String pattern = readResource(templateName + ".txt");

		Template template = new Template(templateName, "description", "Java", pattern, false);
		JavaCoded2EclipseTemplateConverter jc2etc = new JavaCoded2EclipseTemplateConverter();
		Template template2 = null;
		try {
			template2 = jc2etc.convert(template, false);
		} catch (TemplateException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		String pattern2 = readResource(templateName + "eclipse.txt");

		assertEquals(pattern2, template2.getPattern());
	}

	private static class TestData {
		String method;
		String expected;
		String[] parameters;

		public TestData(String method, String expected, String[] parameters) {
			this.method = method;
			this.expected = expected;
			this.parameters = parameters;
		}

	}

	private void testCompile(String templateName, List<TestData> tests) {
		File dir = ServiceLocator.getInjector().getInstance(ITemplateStoreDirProvider.class).getTemplateStoreDir();
		Class<?> clazz = null;
		try {
			clazz = ReflectionUtils.loadClass(dir, templateName);
		} catch (ClassNotFoundException | MalformedURLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		for (TestData testData : tests) {
			Method m = ReflectionUtils.findAnyMethod(clazz, testData.method);
			try {
				assertEquals(testData.expected, (String) m.invoke(null, testData.parameters));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				assertTrue(false);
			}
		}

	}

	@Test
	public void convertTest() {
		testConvert("oneparameter");
		testConvert("twoparameters");
		testConvert("threeparameters");
	}

	@Test
	public void compileTest() {
		File dir = ServiceLocator.getInjector().getInstance(ITemplateStoreDirProvider.class).getTemplateStoreDir();

		deleteDir(dir);

		testConvert("oneparameter");
		testCompile("oneparameter", Arrays.asList(new TestData("var2", "SMALL", new String[] { "small" })));

		testConvert("twoparameters");
		testCompile("twoparameters",
				Arrays.asList(new TestData("var3", "Hello World", new String[] { "Hello ", "World" })));

		testConvert("threeparameters");
		testCompile("threeparameters",
				Arrays.asList(new TestData("var4", "Hello World!", new String[] { "Hello ", "World", "!" })));
		deleteDir(dir);

	}

	private void deleteDir(File dir) {
		for (File file : dir.listFiles()) {
			assertTrue(file.delete());
		}
		assertTrue(dir.delete());
	}

}
