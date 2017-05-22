package com.florianingerl.javacodedcompletionproposals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableType;

import com.florianingerl.util.regex.*;

/**
 * The template translator translates a string into a template buffer. Regions
 * marked as variables are translated into <code>TemplateVariable</code>s.
 * <p>
 * The EBNF grammar of a valid string is as follows:
 * </p>
 * 
 * <pre>
 *  template := (text | escape)*.
 * text := character - dollar.
 * escape := dollar ('{' variable '}' | dollar).
 * dollar := '$'.
 * variable := identifier | identifier ':' type.
 * type := qualifiedname | qualifiedname '(' arguments ')'.
 * arguments := (argument ',')* argument.
 * argument := qualifiedname | argumenttext.
 * qualifiedname := (identifier '.')* identifier.
 * argumenttext := "'" (character - "'" | "'" "'")* "'".
 * identifier := javaidentifierpart - "$".
 * </pre>
 * <p>
 * Clients may only replace the <code>createVariable</code> method of this
 * class.
 * </p>
 *
 * @since 3.0
 */
public class JavaCodedTemplateTranslator {

	static final File TEMPLATES_STORE_LOCATION = new File("C:/Users/Hermann/Desktop/TemplateStore");
	private Template fTemplate;
	private StringBuilder sbJavaSrcFile = new StringBuilder();
	private File javaCompiler = new File("C:\\Program Files\\Java\\jdk1.8.0_92\\bin\\javac.exe");
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

	/**
	 * @since 3.3
	 */
	private final class VariableDescription {
		final List fOffsets = new ArrayList(5);
		final String fName;
		TemplateVariableType fType;

		VariableDescription(String name, TemplateVariableType type) {
			fName = name;
			fType = type;
		}

		void mergeType(TemplateVariableType type) throws TemplateException {
			if (type == null)
				return;
			if (fType == null)
				fType = type;
			if (!type.equals(fType))
				// fail(TextTemplateMessages.getFormattedString("TemplateTranslator.error.incompatible.type",
				// fName)); //$NON-NLS-1$
				fail("TemplateTranslator.error.incompatible.type " + fName);
		}
	}

	/** Last translation error. */
	private String fErrorMessage;
	/**
	 * Used to ensure compatibility with subclasses overriding
	 * {@link #createVariable(String, String, int[])}.
	 * 
	 * @since 3.3
	 */
	private TemplateVariableType fCurrentType;

	/**
	 * Returns an error message if an error occurred for the last translation,
	 * <code>null</code> otherwise.
	 *
	 * @return the error message if an error occurred during the most recent
	 *         translation, <code>null</code> otherwise
	 */
	public String getErrorMessage() {
		return fErrorMessage;
	}

	/**
	 * Translates a template to a <code>TemplateBuffer</code>. <code>null</code>
	 * is returned if there was an error. <code>getErrorMessage()</code>
	 * retrieves the associated error message.
	 *
	 * @param template
	 *            the template to translate.
	 * @return returns the template buffer corresponding to the string
	 * @see #getErrorMessage()
	 * @throws TemplateException
	 *             if translation failed
	 */
	public TemplateBuffer translate(Template template) throws TemplateException {
		fTemplate = template;

		sbJavaSrcFile.append("public class " + template.getName() + "{ ");
		return parse(template.getPattern());
	}

	/**
	 * Translates a template string to <code>TemplateBuffer</code>.
	 * <code>null</code> is returned if there was an error.
	 * <code>getErrorMessage()</code> retrieves the associated error message.
	 *
	 * @param string
	 *            the string to translate.
	 * @return returns the template buffer corresponding to the string
	 * @see #getErrorMessage()
	 * @throws TemplateException
	 *             if translation failed
	 */
	public TemplateBuffer translate(String string) throws TemplateException {
		return parse(string);
	}

	/**
	 * Internal parser.
	 *
	 * @param string
	 *            the string to parse
	 * @return the parsed <code>TemplateBuffer</code>
	 * @throws TemplateException
	 *             if the string does not conform to the template format
	 */
	private TemplateBuffer parse(String string) throws TemplateException {

		fErrorMessage = null;
		final StringBuffer buffer = new StringBuffer(string.length());
		final Matcher matcher = ESCAPE_PATTERN.matcher(string);
		final Map variables = new LinkedHashMap();
		Set<String> allParameters = new HashSet<String>();
		Set<String> allVariables = new HashSet<String>();

		int complete = 0;
		while (matcher.find()) {
			// append any verbatim text
			buffer.append(string.substring(complete, matcher.start()));

			// check the escaped sequence
			if ("$".equals(matcher.group())) { //$NON-NLS-1$
				// fail(TextTemplateMessages.getString("TemplateTranslator.error.incomplete.variable"));
				// //$NON-NLS-1$
				fail("TemplateTranslator.error.incomplete.variable");
			} else if ("$$".equals(matcher.group())) { //$NON-NLS-1$
				// escaped $
				buffer.append('$');
			} else {
				// parse variable
				String name = matcher.group(1);
				String typeName = matcher.group(2);
				String params = matcher.group(3);
				String body = matcher.group("body");

				TemplateVariableType type = null;
				if (body != null) {
					sbJavaSrcFile.append(
							"public static String " + name + matcher.group("arguments") + matcher.group("body"));
					final List<String> arguments = new LinkedList<String>();
					matcher.captures("argument").stream().forEach((Capture capture) -> {
						arguments.add(capture.getValue());
					});
					if (arguments.contains(name)) {
						fail("Arguments to a Java-coded template variable mustn't have the name of the template variable itself");
					}
					allParameters.addAll(arguments);

					type = constructTemplateVariableType("javaCoded", arguments.toArray(new String[arguments.size()]));
				} else {
					allVariables.add(name);
					type = createType(typeName, params);
				}
				updateOrCreateVariable(variables, name, type, buffer.length());

				buffer.append(name);
			}
			complete = matcher.end();
		}
		// append remaining verbatim text
		buffer.append(string.substring(complete));

		TemplateVariable[] vars = createVariables(variables);
		sbJavaSrcFile.append("}");
		writeJavaSourceFile();
		compileClassFile();

		if (!allVariables.containsAll(allParameters)) {
			fail("Not all parameters of Java-coded template variables were the names of other template variables");
		}

		return new TemplateBuffer(buffer.toString(), vars);
	}

	private void compileClassFile() throws TemplateException {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(javaCompiler.getAbsolutePath(),
					javaSourceFile.getAbsolutePath());
			Process p = processBuilder.start();
			String processOutput = getProcessOutput(p);
			System.out.println(processOutput);

			int result = p.waitFor();
			if (result != 0) {
				fail(processOutput);

			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
			// assert classFile != null && classFile.exists();
		}
	}

	private String getProcessOutput(Process process) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		return sb.toString();
	}

	private void writeJavaSourceFile() {
		try {
			javaSourceFile = new File(TEMPLATES_STORE_LOCATION, fTemplate.getName() + ".java");
			PrintWriter writer = new PrintWriter(javaSourceFile);
			writer.println(sbJavaSrcFile);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private TemplateVariableType createType(String typeName, String paramString) {
		if (typeName == null)
			return null;

		if (paramString == null)
			return constructTemplateVariableType(typeName);

		final Matcher matcher = PARAM_PATTERN.matcher(paramString);
		List params = new ArrayList(5);
		while (matcher.find()) {
			String argument = matcher.group();
			if (argument.charAt(0) == '\'') {
				// argumentText
				argument = argument.substring(1, argument.length() - 1).replaceAll("''", "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			params.add(argument);
		}

		return constructTemplateVariableType(typeName, (String[]) params.toArray(new String[params.size()]));
	}

	private void fail(String message) throws TemplateException {
		fErrorMessage = message;
		throw new TemplateException(message);
	}

	/**
	 * If there is no variable named <code>name</code>, a new variable with the
	 * given type, name and offset is created. If one exists, the offset is
	 * added to the variable and the type is merged with the existing type.
	 *
	 * @param variables
	 *            the variables by variable name
	 * @param name
	 *            the name of the variable
	 * @param type
	 *            the variable type, <code>null</code> for not defined
	 * @param offset
	 *            the buffer offset of the variable
	 * @throws TemplateException
	 *             if merging the type fails
	 * @since 3.3
	 */
	private void updateOrCreateVariable(Map variables, String name, TemplateVariableType type, int offset)
			throws TemplateException {
		VariableDescription varDesc = (VariableDescription) variables.get(name);
		if (varDesc == null) {
			varDesc = new VariableDescription(name, type);
			variables.put(name, varDesc);
		} else {
			varDesc.mergeType(type);
		}
		varDesc.fOffsets.add(new Integer(offset));
	}

	/**
	 * Creates proper {@link TemplateVariable}s from the variable descriptions.
	 *
	 * @param variables
	 *            the variable descriptions by variable name
	 * @return the corresponding variables
	 * @since 3.3
	 */
	private TemplateVariable[] createVariables(Map variables) {
		TemplateVariable[] result = new TemplateVariable[variables.size()];
		int idx = 0;
		for (Iterator it = variables.values().iterator(); it.hasNext(); idx++) {
			VariableDescription desc = (VariableDescription) it.next();
			TemplateVariableType type = desc.fType == null ? constructTemplateVariableType(desc.fName) : desc.fType;
			int[] offsets = new int[desc.fOffsets.size()];
			int i = 0;
			for (Iterator intIt = desc.fOffsets.iterator(); intIt.hasNext(); i++) {
				Integer offset = (Integer) intIt.next();
				offsets[i] = offset.intValue();
			}
			fCurrentType = type;
			/*
			 * Call the deprecated version of createVariable. When not
			 * overridden, it will delegate to the new version using
			 * fCurrentType.
			 */
			TemplateVariable var = createVariable(type.getName(), desc.fName, offsets);
			result[idx] = var;
		}
		fCurrentType = null; // avoid dangling reference
		return result;
	}

	/**
	 * Hook method to create new variables. Subclasses may override to supply
	 * their custom variable type.
	 * <p>
	 * Clients may replace this method.
	 * </p>
	 *
	 * @param type
	 *            the type of the new variable.
	 * @param name
	 *            the name of the new variable.
	 * @param offsets
	 *            the offsets where the variable occurs in the template
	 * @return a new instance of <code>TemplateVariable</code>
	 * @deprecated as of 3.3 use
	 *             {@link #createVariable(TemplateVariableType, String, int[])}
	 *             instead
	 */
	protected TemplateVariable createVariable(String type, String name, int[] offsets) {
		return createVariable(fCurrentType, name, offsets);
	}

	/**
	 * Hook method to create new variables. Subclasses may override to supply
	 * their custom variable type.
	 * <p>
	 * Clients may replace this method.
	 * </p>
	 *
	 * @param type
	 *            the type of the new variable.
	 * @param name
	 *            the name of the new variable.
	 * @param offsets
	 *            the offsets where the variable occurs in the template
	 * @return a new instance of <code>TemplateVariable</code>
	 * @since 3.3
	 */
	protected TemplateVariable createVariable(TemplateVariableType type, String name, int[] offsets) {
		return new TemplateVariable(type, name, name, offsets);
	}

	private static Constructor<TemplateVariableType> constructor1;
	private static Constructor<TemplateVariableType> constructor2;

	static {
		try {
			constructor1 = TemplateVariableType.class.getDeclaredConstructor(String.class);
			constructor1.setAccessible(true);
			constructor2 = TemplateVariableType.class.getDeclaredConstructor(String.class, String[].class);
			constructor2.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static TemplateVariableType constructTemplateVariableType(String typeName) {
		try {
			return constructor1.newInstance(typeName);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static TemplateVariableType constructTemplateVariableType(String typeName, String[] params) {
		try {
			return constructor2.newInstance(typeName, params);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

}
