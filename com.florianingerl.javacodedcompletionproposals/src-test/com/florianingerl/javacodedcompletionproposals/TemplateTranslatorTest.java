package com.florianingerl.javacodedcompletionproposals;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariableType;
import org.junit.Test;

import com.florianingerl.util.regex.Capture;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

public class TemplateTranslatorTest {

	private static final Pattern PATTERN_LAMBDA = Pattern.compile(JavaCodedTemplateTranslator.PATTERN_LAMBDA);

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

	@Test
	public void testTranslate() {
		String pattern = null;
		try {
			pattern = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("resources/Template1.txt"));
		} catch (IOException e) {
			e.printStackTrace();
			assertFalse(true);
		}
		Template template = new Template("name", "description", "SWT statements", pattern, false);
		JavaCodedTemplateTranslator translator = new JavaCodedTemplateTranslator();
		try {
			TemplateBuffer buffer = translator.translate(template, true);
		} catch (TemplateException e) {

			e.printStackTrace();
			assertTrue(false);
		}

	}

}
