package com.florianingerl.javacodedcompletionproposals;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.text.template.contentassist.PositionBasedCompletionProposal;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * A template completion proposal.
 * <p>
 * Clients may subclass.
 * </p>
 *
 * @since 3.0
 */
public class JavaCodedTemplateProposal implements ICompletionProposal, ICompletionProposalExtension,
		ICompletionProposalExtension2, ICompletionProposalExtension3 {

	private final Template fTemplate;
	private final TemplateContext fContext;
	private final Image fImage;
	private final IRegion fRegion;
	private int fRelevance;

	private IRegion fSelectedRegion; // initialized by apply()
	private String fDisplayString;
	private InclusivePositionUpdater fUpdater;
	private IInformationControlCreator fInformationControlCreator;

	/**
	 * Creates a template proposal with a template and its context.
	 *
	 * @param template
	 *            the template
	 * @param context
	 *            the context in which the template was requested.
	 * @param region
	 *            the region this proposal is applied to
	 * @param image
	 *            the icon of the proposal.
	 */
	public JavaCodedTemplateProposal(Template template, TemplateContext context, IRegion region, Image image) {
		this(template, context, region, image, 0);
	}

	/**
	 * Creates a template proposal with a template and its context.
	 *
	 * @param template
	 *            the template
	 * @param context
	 *            the context in which the template was requested.
	 * @param image
	 *            the icon of the proposal.
	 * @param region
	 *            the region this proposal is applied to
	 * @param relevance
	 *            the relevance of the proposal
	 */
	public JavaCodedTemplateProposal(Template template, TemplateContext context, IRegion region, Image image,
			int relevance) {
		Assert.isNotNull(template);
		Assert.isNotNull(context);
		Assert.isNotNull(region);

		fTemplate = template;
		fContext = context;
		fImage = image;
		fRegion = region;

		fDisplayString = null;

		fRelevance = relevance;
	}

	/**
	 * Sets the information control creator for this completion proposal.
	 *
	 * @param informationControlCreator
	 *            the information control creator
	 * @since 3.1
	 */
	public final void setInformationControlCreator(IInformationControlCreator informationControlCreator) {
		fInformationControlCreator = informationControlCreator;
	}

	/**
	 * Returns the template of this proposal.
	 *
	 * @return the template of this proposal
	 * @since 3.1
	 */
	protected final Template getTemplate() {
		return fTemplate;
	}

	/**
	 * Returns the context in which the template was requested.
	 *
	 * @return the context in which the template was requested
	 * @since 3.1
	 */
	protected final TemplateContext getContext() {
		return fContext;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This method is no longer called by the framework and clients
	 *             should overwrite {@link #apply(ITextViewer, char, int, int)}
	 *             instead
	 */
	@Deprecated
	@Override
	public final void apply(IDocument document) {
		// not called anymore
	}

	/**
	 * Inserts the template offered by this proposal into the viewer's document
	 * and sets up a <code>LinkedModeUI</code> on the viewer to edit any of the
	 * template's unresolved variables.
	 *
	 * @param viewer
	 *            {@inheritDoc}
	 * @param trigger
	 *            {@inheritDoc}
	 * @param stateMask
	 *            {@inheritDoc}
	 * @param offset
	 *            {@inheritDoc}
	 */
	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {

		IDocument document = viewer.getDocument();
		Class<?> clazz = null;
		try {

			URL url = TemplateTranslator.TEMPLATES_STORE_LOCATION.toURI().toURL();
			URL[] urls = new URL[] { url };
			URLClassLoader classLoader = new URLClassLoader(urls);
			clazz = classLoader.loadClass(fTemplate.getName());

			TemplateVariableResolver tvr = new JavaCodedTemplateVariableResolver(clazz);
			fContext.getContextType().addResolver(tvr);

			fContext.setReadOnly(false);
			int start;
			TemplateBuffer templateBuffer;
			{
				int oldReplaceOffset = getReplaceOffset();
				try {
					// this may already modify the document (e.g. add imports)
					TemplateTranslator translator = new TemplateTranslator();
					templateBuffer = translator.translate(fTemplate);
					fContext.getContextType().resolve(templateBuffer, fContext);
				} catch (TemplateException e1) {
					fSelectedRegion = fRegion;
					return;
				}

				start = getReplaceOffset();
				int shift = start - oldReplaceOffset;
				int end = Math.max(getReplaceEndOffset(), offset + shift);

				// insert template string
				String templateString = templateBuffer.getString();
				document.replace(start, end - start, templateString);
			}

			// translate positions
			LinkedModeModel model = new LinkedModeModel();
			Map<String, LinkedPositionGroup> map = new HashMap<String, LinkedPositionGroup>();
			TemplateVariable[] variables = templateBuffer.getVariables();
			boolean hasPositions = false;
			for (int i = 0; i != variables.length; i++) {
				TemplateVariable variable = variables[i];

				// TODO: Maybe dependent groups want to depend on resolved
				// variables
				if (variable.isUnambiguous())
					continue;

				LinkedPositionGroup group = new LinkedPositionGroup();
				map.put(variable.getName(), group);

				int[] offsets = variable.getOffsets();
				int length = variable.getLength();

				LinkedPosition first;
				{
					String[] values = variable.getValues();
					ICompletionProposal[] proposals = new ICompletionProposal[values.length];
					for (int j = 0; j < values.length; j++) {
						ensurePositionCategoryInstalled(document, model);
						Position pos = new Position(offsets[0] + start, length);
						document.addPosition(getCategory(), pos);
						proposals[j] = new PositionBasedCompletionProposal(values[j], pos, length);
					}

					if (proposals.length > 1)
						first = new ProposalPosition(document, offsets[0] + start, length, proposals);
					else
						first = new LinkedPosition(document, offsets[0] + start, length);
				}

				for (int j = 0; j != offsets.length; j++)
					if (j == 0)
						group.addPosition(first);
					else
						group.addPosition(new LinkedPosition(document, offsets[j] + start, length));

				model.addGroup(group);
				hasPositions = true;
			}

			// For every Java-coded template variable, find its dependecies
			for (TemplateVariable variable : variables) {
				if (!variable.getType().equals("javaCoded")) {
					continue;
				}
				LinkedPositionGroup group = map.get(variable.getName());

				Method m = ReflectionUtils.findAnyMethod(clazz, variable.getName());

				List<LinkedPositionGroup> dependencyGroups = new LinkedList<LinkedPositionGroup>();
				variable.getVariableType().getParams().stream().forEach((String s) -> {
					LinkedPositionGroup g = map.get(s);
					g.addDependentGroup(group);
					dependencyGroups.add(g);
				});

				group.setDependencyGroups(m, dependencyGroups);
			}

			if (hasPositions) {
				model.forceInstall();
				LinkedModeUI ui = new LinkedModeUI(model, viewer);
				ui.setExitPosition(viewer, getCaretOffset(templateBuffer) + start, 0, Integer.MAX_VALUE);
				ui.enter();

				fSelectedRegion = ui.getSelectedRegion();
			} else {
				ensurePositionCategoryRemoved(document);
				fSelectedRegion = new Region(getCaretOffset(templateBuffer) + start, 0);
			}

		} catch (BadLocationException e) {
			openErrorDialog(viewer.getTextWidget().getShell(), e);
			ensurePositionCategoryRemoved(document);
			fSelectedRegion = fRegion;
		} catch (BadPositionCategoryException | MalformedURLException | ClassNotFoundException | SecurityException e) {
			openErrorDialog(viewer.getTextWidget().getShell(), e);
			fSelectedRegion = fRegion;
		}
	}

	private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
		if (!document.containsPositionCategory(getCategory())) {
			document.addPositionCategory(getCategory());
			fUpdater = new InclusivePositionUpdater(getCategory());
			document.addPositionUpdater(fUpdater);

			model.addLinkingListener(new ILinkedModeListener() {

				@Override
				public void left(LinkedModeModel environment, int flags) {
					ensurePositionCategoryRemoved(document);
				}

				@Override
				public void suspend(LinkedModeModel environment) {
				}

				@Override
				public void resume(LinkedModeModel environment, int flags) {
				}
			});
		}
	}

	private void ensurePositionCategoryRemoved(IDocument document) {
		if (document.containsPositionCategory(getCategory())) {
			try {
				document.removePositionCategory(getCategory());
			} catch (BadPositionCategoryException e) {
				// ignore
			}
			document.removePositionUpdater(fUpdater);
		}
	}

	private String getCategory() {
		return "TemplateProposalCategory_" + toString(); //$NON-NLS-1$
	}

	private int getCaretOffset(TemplateBuffer buffer) {

		TemplateVariable[] variables = buffer.getVariables();
		for (int i = 0; i != variables.length; i++) {
			TemplateVariable variable = variables[i];
			if (variable.getType().equals(GlobalTemplateVariables.Cursor.NAME))
				return variable.getOffsets()[0];
		}

		return buffer.getString().length();
	}

	/**
	 * Returns the offset of the range in the document that will be replaced by
	 * applying this template.
	 *
	 * @return the offset of the range in the document that will be replaced by
	 *         applying this template
	 * @since 3.1
	 */
	protected final int getReplaceOffset() {
		int start;
		if (fContext instanceof DocumentTemplateContext) {
			DocumentTemplateContext docContext = (DocumentTemplateContext) fContext;
			start = docContext.getStart();
		} else {
			start = fRegion.getOffset();
		}
		return start;
	}

	/**
	 * Returns the end offset of the range in the document that will be replaced
	 * by applying this template.
	 *
	 * @return the end offset of the range in the document that will be replaced
	 *         by applying this template
	 * @since 3.1
	 */
	protected final int getReplaceEndOffset() {
		int end;
		if (fContext instanceof DocumentTemplateContext) {
			DocumentTemplateContext docContext = (DocumentTemplateContext) fContext;
			end = docContext.getEnd();
		} else {
			end = fRegion.getOffset() + fRegion.getLength();
		}
		return end;
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
	}

	@Override
	public String getAdditionalProposalInfo() {
		try {
			fContext.setReadOnly(true);
			TemplateBuffer templateBuffer;
			try {
				templateBuffer = fContext.evaluate(fTemplate);
			} catch (TemplateException e) {
				return null;
			}

			return templateBuffer.getString();

		} catch (BadLocationException e) {
			return null;
		}
	}

	@Override
	public String getDisplayString() {
		if (fDisplayString == null) {
			// String[] arguments = new String[] { fTemplate.getName(),
			// fTemplate.getDescription() };

			// JFaceTextTemplateMessages.getFormattedString("TemplateProposal.displayString",
			// arguments); // $NON-NLS-1$
			fDisplayString = fTemplate.getName() + " - " + fTemplate.getDescription();
		}
		return fDisplayString;
	}

	@Override
	public Image getImage() {
		return fImage;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	private void openErrorDialog(Shell shell, Exception e) {
		/*
		 * MessageDialog.openError(shell, JFaceTextTemplateMessages.getString(
		 * "TemplateProposal.errorDialog.title"), //$NON-NLS-1$ e.getMessage());
		 */
		MessageDialog.openError(shell, "Error!", //$NON-NLS-1$
				e.getMessage());
	}

	/**
	 * Returns the relevance.
	 *
	 * @return the relevance
	 */
	public int getRelevance() {
		return fRelevance;
	}

	public void setRelevance(int relevance) {
		fRelevance = relevance;
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {
		return fInformationControlCreator;
	}

	@Override
	public void selected(ITextViewer viewer, boolean smartToggle) {
	}

	@Override
	public void unselected(ITextViewer viewer) {
	}

	@Override
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		try {
			int replaceOffset = getReplaceOffset();
			if (offset >= replaceOffset) {
				String content = document.get(replaceOffset, offset - replaceOffset);
				return fTemplate.getName().toLowerCase().startsWith(content.toLowerCase());
			}
		} catch (BadLocationException e) {
			// concurrent modification - ignore
		}
		return false;
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		return fTemplate.getName();
	}

	@Override
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return getReplaceOffset();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This method is no longer called by the framework and clients
	 *             should overwrite {@link #apply(ITextViewer, char, int, int)}
	 *             instead
	 */
	@Deprecated
	@Override
	public void apply(IDocument document, char trigger, int offset) {
		// not called any longer
	}

	@Override
	public boolean isValidFor(IDocument document, int offset) {
		// not called any longer
		return false;
	}

	@Override
	public char[] getTriggerCharacters() {
		// no triggers
		return new char[0];
	}

	@Override
	public int getContextInformationPosition() {
		return fRegion.getOffset();
	}
}
