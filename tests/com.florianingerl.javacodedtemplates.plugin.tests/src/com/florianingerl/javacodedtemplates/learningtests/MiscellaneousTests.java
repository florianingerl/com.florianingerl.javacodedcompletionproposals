package com.florianingerl.javacodedtemplates.learningtests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class MiscellaneousTests {

	@Test(expected = java.lang.NullPointerException.class)
	public void test() {
		List<String> list = null;
		for (String s : list) {
			System.out.println(s);
		}
	}

	@Test(expected = java.lang.NullPointerException.class)
	public void test2() {
		String[] array = null;
		for (String s : array) {
			System.out.println(s);
		}
	}

}
