package com.florianingerl.javacodedcompletionproposals;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

public class JavaCodedCompletionProposalComputer
		implements org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer {

	private Logger logger = Logger.getLogger(JavaCodedCompletionProposalComputer.class);

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		try {

			logger.debug("computeCompletionProposals was called!");
			IDocument document = context.getDocument();
			ITextViewer textViewer = context.getViewer();

			context.getInvocationOffset();

			logger.debug("invocationOffset = " + context.getInvocationOffset());
			logger.debug("Document = " + document.get());

			// We need to get all the code snippets here that fit and construct
			// the
			// JavaCodedCompletionProposal with a JavaCodedCompletionTemplate
			ITemplatesWithJavaCodeLocator templatesLocator = ServiceLocator.getInjector()
					.getInstance(ITemplatesWithJavaCodeLocator.class);
			List<TemplateWithJavaCode> templates;

			templates = templatesLocator.getTemplatesWithJavaCode(context.computeIdentifierPrefix().toString());

			Region region = new Region(context.getInvocationOffset() - context.computeIdentifierPrefix().length(),
					context.getInvocationOffset());
			List<ICompletionProposal> result = new LinkedList<ICompletionProposal>();
			for (TemplateWithJavaCode template : templates) {

				result.add(new JavaCodedCompletionProposal(template, region, null));
			}
			return result;
		} catch (BadLocationException e) {
			e.printStackTrace();
			logger.error(e.getLocalizedMessage());
			return Collections.EMPTY_LIST;
		}

	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		logger.debug("computeContextInformation was called!");
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getErrorMessage() {
		logger.debug("getErrorMessage() was called!");
		return "Couldn't compute any java-coded completion proposals!";
	}

	@Override
	public void sessionEnded() {
		logger.debug("sessionEnded was called!");
	}

	@Override
	public void sessionStarted() {
		logger.debug("sessionStarted was called!");
	}

}