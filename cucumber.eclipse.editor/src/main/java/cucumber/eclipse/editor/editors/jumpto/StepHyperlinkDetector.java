package cucumber.eclipse.editor.editors.jumpto;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import cucumber.eclipse.editor.editors.Editor;
import cucumber.eclipse.steps.integration.SerializationHelper;
import cucumber.eclipse.steps.integration.StepDefinition;
import cucumber.eclipse.steps.integration.marker.MarkerFactory;

public class StepHyperlinkDetector implements IHyperlinkDetector {

	private Editor editor;
	
	public StepHyperlinkDetector(Editor editor) {
		this.editor = editor;
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}

		IDocument document = textViewer.getDocument();
		if (document == null) {
			return null;
		}
		
		IFile gherkinFile = editor.getFile();
		
		int offset = region.getOffset();
		try {
			int selectionLineNumber = document.getLineOfOffset(offset) + 1;
			IMarker stepDefinitionMatchMarker = JumpToStepDefinition.findStepDefinitionMatchMarker(selectionLineNumber, gherkinFile);
				
			IRegion lineInfo = document.getLineInformationOfOffset(offset);
			int lineStartOffset = lineInfo.getOffset();
			String currentLine = document.get(lineStartOffset, lineInfo.getLength());
			
			String serializedStepDefinition = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_STEPDEF_ATTRIBUTE);
			StepDefinition stepDefinition = SerializationHelper.deserialize(serializedStepDefinition);

			// define the hyperlink region
			String textStatement = (String) stepDefinitionMatchMarker.getAttribute(MarkerFactory.STEP_DEFINITION_MATCH_TEXT_ATTRIBUTE);
			int statementStartOffset = lineStartOffset + currentLine.indexOf(textStatement);

			IRegion stepRegion = new Region(statementStartOffset, textStatement.length());
			
			return new IHyperlink[] { new StepHyperlink(stepRegion, stepDefinition) };
			
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
