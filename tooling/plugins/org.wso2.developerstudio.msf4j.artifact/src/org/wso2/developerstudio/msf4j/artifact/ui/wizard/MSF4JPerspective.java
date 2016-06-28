package org.wso2.developerstudio.msf4j.artifact.ui.wizard;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class MSF4JPerspective implements IPerspectiveFactory{

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		// Top left: Project Explorer
		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.15f, editorArea);
		topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);


		
	}

}
