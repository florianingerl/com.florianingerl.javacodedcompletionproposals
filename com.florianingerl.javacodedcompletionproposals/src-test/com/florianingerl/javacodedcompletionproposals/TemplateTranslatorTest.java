package com.florianingerl.javacodedcompletionproposals;

import static org.junit.Assert.*;

import org.junit.Test;

import com.florianingerl.util.regex.Capture;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

public class TemplateTranslatorTest {

	private static final Pattern PATTERN_LAMBDA = Pattern.compile(TemplateTranslator.PATTERN_LAMBDA);
	
	private static void check(String lambda, String argumentsInBrackets, String[] arguments, String functionBody)
	{
		Matcher matcher = PATTERN_LAMBDA.matcher(lambda);
		assertTrue(matcher.matches() );
		
		assertEquals(argumentsInBrackets, matcher.group("arguments") );
		assertEquals(functionBody, matcher.group("body"));
	
		assertArrayEquals(arguments, matcher.captures("argument").stream().map( (Capture capture) -> { return capture.getValue(); } ).toArray() );
	}
	
	@Test
	public void testOneParamter() {
		String lambda = " ( String first ) -> { return first.toUpperCase(); } ";
		check(lambda, "( String first )", new String[] {"first"}, "{ return first.toUpperCase(); }" );
	}
	
	@Test
	public void testTwoParameters() {
		String lambda = " ( String first ,String second) - > { return first + second; } ";
		check(lambda, "( String first ,String second)", new String[] {"first", "second"}, "{ return first + second; }" );
	}
	
	@Test
	public void testThreeParameters() {
		String lambda = " (String first, String second ,String third  )->{ return first + second + third; } ";
		check(lambda, "(String first, String second ,String third  )", new String[] {"first", "second", "third"}, "{ return first + second + third; }");
	}
	
	@Test
	public void testCommentsAndStringsInFunctionBody() {
		String body = "{ /* }{\" */ return s; }"; // String s = \" }{ \\\"\"; // Comment until end of line }{\" \n { String b = \"}\"; /* } */ // }\n  } return s; } ";
		String lambda = " (String first) -> " + body;
		check(lambda, "(String first)", new String[] {"first"}, body );
	}

	
	
}
