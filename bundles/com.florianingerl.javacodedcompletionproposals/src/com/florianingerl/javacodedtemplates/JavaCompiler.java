package com.florianingerl.javacodedtemplates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

public class JavaCompiler implements IJavaCompiler {

	@Override
	public void compile(File srcFile) throws CompilationException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter errWriter = new PrintWriter(baos);

		boolean success = BatchCompiler.compile(
				srcFile.getAbsolutePath() + " -d " + srcFile.getParentFile().getAbsolutePath(),
				new PrintWriter(new ByteArrayOutputStream()), errWriter, null);

		if (!success)
			throw new CompilationException(baos.toString());

	}

}
