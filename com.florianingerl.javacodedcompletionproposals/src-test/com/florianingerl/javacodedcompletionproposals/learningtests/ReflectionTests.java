package com.florianingerl.javacodedcompletionproposals.learningtests;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.junit.Test;

public class ReflectionTests {

	public static String staticMethod(String param) {
		return param.toUpperCase();
	}

	@Test
	public void test() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Class<?> clazz = ReflectionTests.class;
		Optional<Method> o = Stream.of(clazz.getMethods()).filter((Method m) -> {
			return m.getName().equals("staticMethod");
		}).findAny();
		assertTrue(o.isPresent());
		Method m = o.get();

		String s = (String) m.invoke(null, new String[] { "Harald" });
		assertEquals("HARALD", s);
	}

}
