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

import com.florianingerl.util.regex.Capture;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class JavaCoded2EclipseTemplateConverterTest {

	private static final Pattern PATTERN_LAMBDA = Pattern.compile(JavaCoded2EclipseTemplateConverter.PATTERN_LAMBDA);

	private static boolean javaCompilerCalled = false;

	private static void check(String lambda, String argumentsInBrackets, String[] arguments, String functionBody) {
		Matcher matcher = PATTERN_LAMBDA.matcher(lambda);
		assertTrue(matcher.matches());

		assertEquals(argumentsInBrackets, matcher.group("arguments"));
		assertEquals(functionBody, matcher.group("body"));

		assertArrayEquals(arguments, matcher.captures("argument").stream().map((Capture capture) -> {
			return capture.getValue();
		}).toArray());
	}

	@Test
	public void testOneParamter() {
		String lambda = " ( String first ) -> { return first.toUpperCase(); } ";
		check(lambda, "( String first )", new String[] { "first" }, "{ return first.toUpperCase(); }");
	}

	@Test
	public void testTwoParameters() {
		String lambda = " ( String first ,String second) - > { return first + second; } ";
		check(lambda, "( String first ,String second)", new String[] { "first", "second" },
				"{ return first + second; }");
	}

	@Test
	public void testThreeParameters() {
		String lambda = " (String first, String second ,String third  )->{ return first + second + third; } ";
		check(lambda, "(String first, String second ,String third  )", new String[] { "first", "second", "third" },
				"{ return first + second + third; }");
	}

	@Test
	public void testCommentsAndStringsInFunctionBody() {
		String body = "{ /* }{\" */ return s; }"; // String s = \" }{ \\\"\"; //
													// Comment until end of line
													// }{\" \n { String b =
													// \"}\"; /* } */ // }\n }
													// return s; } ";
		String lambda = " (String first) -> " + body;
		check(lambda, "(String first)", new String[] { "first" }, body);
	}

	public static void initialize() {

		IJavaCompiler javaCompiler = new IJavaCompiler() {

			private IJavaCompiler jc = new JavaCompiler();

			@Override
			public void compile(File srcFile) throws CompilationException {
				javaCompilerCalled = true;
				jc.compile(srcFile);
			}

		};
		Injector injector = Guice.createInjector(new TestDependencyResolverModule(javaCompiler));
		ServiceLocator.setInjector(injector);

	}

	@BeforeClass
	public static void setUpBeforeClass() {
		initialize();
	}

	static String readResource(String resource) {
		String s = null;
		try {
			s = IOUtils.toString(JavaCoded2EclipseTemplateConverterTest.class.getClassLoader()
					.getResourceAsStream("resources/" + resource));
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
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
			assertTrue(e.getMessage(), false);
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
				assertTrue(e.getMessage(), false);
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

	@Test
	public void compileOnlyIfNeededTest() throws TemplateException {
		File dir = ServiceLocator.getInjector().getInstance(ITemplateStoreDirProvider.class).getTemplateStoreDir();
		deleteDir(dir);

		String pattern = readResource("oneparameter.txt");
		Template template = new Template("oneparameter", "description", "Java", pattern, false);
		JavaCoded2EclipseTemplateConverter converter = new JavaCoded2EclipseTemplateConverter();

		// class file doesn't exist, parameter false
		javaCompilerCalled = false;
		converter.convert(template, false);
		assertTrue(javaCompilerCalled);

		deleteDir(dir);
		// class file doesn't exist, parameter true
		javaCompilerCalled = false;
		converter.convert(template, true);
		assertTrue(javaCompilerCalled);

		// class file exits, parameter false
		javaCompilerCalled = false;
		assertTrue(new File(dir, "oneparameter.class").exists());
		converter.convert(template, false);
		assertFalse(javaCompilerCalled);

		// class file exits, parameter true
		javaCompilerCalled = false;
		assertTrue(new File(dir, "oneparameter.class").exists());
		converter.convert(template, true);
		assertTrue(javaCompilerCalled);

	}

	static void deleteDir(File dir) {
		if (dir.listFiles() != null) {
			for (File file : dir.listFiles()) {
				assertTrue(file.delete());
			}
		}
		if (dir.exists()) {
			assertTrue(dir.delete());
		}
	}

}
