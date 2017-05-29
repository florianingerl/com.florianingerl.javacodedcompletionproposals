package com.florianingerl.javacodedtemplates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariableType;

import com.florianingerl.util.regex.Capture;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

public class JavaCoded2EclipseTemplateConverter {

	private Template fTemplate;
	private StringBuilder sbJavaSrcFile;
	private boolean fCompile;
	private File javaSourceFile;

	/**
	 * Regex pattern for identifier. Note: For historic reasons, this pattern
	 * <em>allows</em> numbers at the beginning of an identifier.
	 * 
	 * @since 3.7
	 */
	private static final String IDENTIFIER = "(?:[\\p{javaJavaIdentifierPart}&&[^\\$]]++)"; //$NON-NLS-1$

	static final String PATTERN_LAMBDA = "\\s*+(?<arguments>\\((?<rep>\\s*+String\\s++(?<argument>" + IDENTIFIER
			+ ")\\s*+)(,(?rep))*+\\))\\s*+-\\s*+\\>\\s*+(?<body>\\{(//.*+(\r)?\n|/\\*[\\s\\S]*?\\*/|\"(?:\\\\.|[^\"\\\\]++)*+\"|[^\"{}/]++|(?body))*+\\})\\s*+";
	/**
	 * Regex pattern for qualifiedname
	 * 
	 * @since 3.4
	 */
	private static final String QUALIFIED_NAME = "(?:" + IDENTIFIER + "\\.)*+" + IDENTIFIER; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Regex pattern for argumenttext
	 * 
	 * @since 3.4
	 */
	private static final String ARGUMENT_TEXT = "'(?:(?:'')|(?:[^']))*+'"; //$NON-NLS-1$

	/**
	 * Regex pattern for argument
	 * 
	 * @since 3.4
	 */
	private static final String ARGUMENT = "(?:" + QUALIFIED_NAME + ")|(?:" + ARGUMENT_TEXT + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Regex pattern for whitespace
	 * 
	 * @since 3.5
	 */
	private static final String SPACES = "\\s*+"; //$NON-NLS-1$

	/**
	 * Precompiled regex pattern for qualified names.
	 * 
	 * @since 3.3
	 */
	private static final Pattern PARAM_PATTERN = Pattern.compile(ARGUMENT);
	/**
	 * Precompiled regex pattern for valid dollar escapes (dollar literals and
	 * variables) and (invalid) single dollars.
	 * 
	 * @since 3.3
	 */
	private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\$\\$|\\$\\{" + // $$|${ //$NON-NLS-1$
			SPACES + "(" + IDENTIFIER + "?+)" + // variable //$NON-NLS-1$ //$NON-NLS-2$
												// id group (1)
			SPACES + "(?:" + //$NON-NLS-1$
			":" + //$NON-NLS-1$
			SPACES + "(?:(" + QUALIFIED_NAME + ")" + // variable //$NON-NLS-1$ //$NON-NLS-2$
														// type group (2)
			SPACES + "(?:" + //$NON-NLS-1$
			"\\(" + // ( //$NON-NLS-1$
			SPACES + "((?:(?:" + ARGUMENT + ")" + SPACES + "," + SPACES + ")*+(?:" + ARGUMENT + "))" + // arguments //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
																										// group
																										// (3)
			SPACES + "\\)" + // ) //$NON-NLS-1$
			")?" + //$NON-NLS-1$
			"|" + PATTERN_LAMBDA + ")" + SPACES + ")?" + //$NON-NLS-1$
			"\\}|\\$"); // }|$ //$NON-NLS-1$

	public Template convert(Template template, boolean compile) throws TemplateException {
		fTemplate = template;
		fCompile = compile;

		if (!fCompile) {
			File dir = getTemplateStoreDir();
			fCompile = !(new File(dir, fTemplate.getName() + ".class").exists());
		}

		sbJavaSrcFile = new StringBuilder();
		if (fCompile)
			sbJavaSrcFile.append("public class " + fTemplate.getName() + "{ ");
		return convert();
	}

	private Template convert() throws TemplateException {

		String string = fTemplate.getPattern();
		StringBuilder buffer = new StringBuilder();
		Matcher matcher = ESCAPE_PATTERN.matcher(string);

		int complete = 0;
		while (matcher.find()) {
			// append any verbatim text
			buffer.append(string.substring(complete, matcher.start()));

			// check the escaped sequence
			if ("$".equals(matcher.group())) { //$NON-NLS-1$
				// fail(TextTemplateMessages.getString("TemplateTranslator.error.incomplete.variable"));
				// //$NON-NLS-1$
				fail("TemplateTranslator.error.incomplete.variable");
			} else if (matcher.group("body") != null) { //$NON-NLS-1$
				String name = matcher.group(1);
				String body = matcher.group("body");

				if (fCompile) {
					sbJavaSrcFile.append(
							"public static String " + name + matcher.group("arguments") + matcher.group("body"));
				}

				String arguments = matcher.captures("argument").stream().map(Capture::getValue)
						.collect(Collectors.joining(","));

				buffer.append("${" + name + ":javaCoded(" + arguments + ")}");

			} else {
				buffer.append(matcher.group());
			}
			complete = matcher.end();
		}
		buffer.append(string.substring(complete));

		if (fCompile) {
			sbJavaSrcFile.append("}");
			writeJavaSourceFile();
			compileClassFile();
		}

		return new Template(fTemplate.getName(), fTemplate.getDescription(), fTemplate.getContextTypeId(),
				buffer.toString(), fTemplate.isAutoInsertable());
	}

	private void compileClassFile() throws TemplateException {
		try {
			ServiceLocator.getInjector().getInstance(IJavaCompiler.class).compile(javaSourceFile);
		} catch (CompilationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void writeJavaSourceFile() throws TemplateException {
		try {
			File dir = getTemplateStoreDir();
			javaSourceFile = new File(dir, fTemplate.getName() + ".java");
			PrintWriter writer = new PrintWriter(javaSourceFile);
			writer.println(sbJavaSrcFile);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	private void fail(String message) throws TemplateException {
		throw new TemplateException(message);
	}

	private File getTemplateStoreDir() {
		return ServiceLocator.getInjector().getInstance(ITemplateStoreDirProvider.class).getTemplateStoreDir();
	}

}
