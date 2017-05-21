package com.florianingerl.javacodedcompletionproposals;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

public class ReflectionUtilsTest {

	public static String staticMethod(String param) {
		return param.toUpperCase();
	}

	public static String anotherStaticMethod(String a1, String a2, String a3) {
		return a1 + a2 + a3;
	}

	@Test
	public void findAnyMethodTest() {
		Method m = ReflectionUtils.findAnyMethod(ReflectionUtilsTest.class, "staticMethod");
		assertEquals("staticMethod", m.getName());
		assertEquals(1, m.getParameters().length);
	}

}
