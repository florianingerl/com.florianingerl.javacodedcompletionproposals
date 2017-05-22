package com.florianingerl.javacodedcompletionproposals;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaDocContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;

public class JavaCodedTemplateCompletionProposalComputer extends AbstractJavaCodedTemplateCompletionProposalComputer {

	private final JavaCodedTemplateEngine fJavaTemplateEngine;
	private final JavaCodedTemplateEngine fJavaStatementsTemplateEngine;
	private final JavaCodedTemplateEngine fJavaMembersTemplateEngine;

	private final JavaCodedTemplateEngine fJavadocTemplateEngine;

	public JavaCodedTemplateCompletionProposalComputer() {
		ContextTypeRegistry templateContextRegistry = JavaPlugin.getDefault().getTemplateContextRegistry();
		fJavaTemplateEngine = createTemplateEngine(templateContextRegistry, JavaContextType.ID_ALL);
		fJavaMembersTemplateEngine = createTemplateEngine(templateContextRegistry, JavaContextType.ID_MEMBERS);
		fJavaStatementsTemplateEngine = createTemplateEngine(templateContextRegistry, JavaContextType.ID_STATEMENTS);
		fJavadocTemplateEngine = createTemplateEngine(templateContextRegistry, JavaDocContextType.ID);
	}

	private static JavaCodedTemplateEngine createTemplateEngine(ContextTypeRegistry templateContextRegistry,
			String contextTypeId) {
		TemplateContextType contextType = templateContextRegistry.getContextType(contextTypeId);
		Assert.isNotNull(contextType);
		return new JavaCodedTemplateEngine(contextType);
	}

	@Override
	protected JavaCodedTemplateEngine computeCompletionEngine(JavaContentAssistInvocationContext context) {
		try {
			String partition = TextUtilities.getContentType(context.getDocument(), IJavaPartitions.JAVA_PARTITIONING,
					context.getInvocationOffset(), true);
			if (partition.equals(IJavaPartitions.JAVA_DOC))
				return fJavadocTemplateEngine;
			else {
				CompletionContext coreContext = context.getCoreContext();
				if (coreContext != null) {
					int tokenLocation = coreContext.getTokenLocation();
					if ((tokenLocation & CompletionContext.TL_MEMBER_START) != 0) {
						return fJavaMembersTemplateEngine;
					}
					if ((tokenLocation & CompletionContext.TL_STATEMENT_START) != 0) {
						return fJavaStatementsTemplateEngine;
					}
				}
				return fJavaTemplateEngine;
			}
		} catch (BadLocationException x) {
			return null;
		}
	}

}
