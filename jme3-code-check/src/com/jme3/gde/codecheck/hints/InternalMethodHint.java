package com.jme3.gde.codecheck.hints;

import com.jme3.system.Annotations.Internal;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

public class InternalMethodHint extends AbstractHint {

    //This hint does not enable the IDE to fix the problem:
    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    //This hint applies to method invocations:
    private static final Set<Tree.Kind> TREE_KINDS =
            EnumSet.<Tree.Kind>of(Tree.Kind.METHOD_INVOCATION);

    public InternalMethodHint() {
        super(true, true, AbstractHint.HintSeverity.WARNING);
    }

    //Specify the kind of code that the hint applies to, in this case,
    //the hint applies to method invocations:
    @Override
    public Set<Kind> getTreeKinds() {
        return TREE_KINDS;
    }

    @Override
    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {

        Tree t = treePath.getLeaf();

        Element el = info.getTrees().getElement(treePath);

        if (el.getAnnotation(Internal.class) != null) {
            //prepare selection for removing
            JTextComponent editor = EditorRegistry.lastFocusedComponent();
            Document doc = editor.getDocument();
            SourcePositions sp = info.getTrees().getSourcePositions();
            int start = (int) sp.getStartPosition(info.getCompilationUnit(), t);
            int end = (int) sp.getEndPosition(info.getCompilationUnit(), t);
            String bodyText = info.getText().substring(start, end);
            //prepare fix
            List<Fix> fixes = new ArrayList<Fix>();
            fixes.add(new MessagesFix(doc, start, bodyText));

            return Collections.<ErrorDescription>singletonList(
                    ErrorDescriptionFactory.createErrorDescription(
                    getSeverity().toEditorSeverity(),
                    getDisplayName(),
                    fixes,
                    info.getFileObject(),
                    (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t),
                    (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t)));

        }

        return null;

    }

    //This is called if/when the hint processing is cancelled:
    @Override
    public void cancel() {
    }

    //Message that the user sees in the left sidebar:
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(InternalMethodHint.class, "InternalMethodHint.display-name");
    }

    //Name of the hint in the Options window:
    @Override
    public String getId() {
        return NbBundle.getMessage(InternalMethodHint.class, "InternalMethodHint.id");
    }

    //Description of the hint in the Options window:
    @Override
    public String getDescription() {
        return NbBundle.getMessage(InternalMethodHint.class, "InternalMethodHint.description");
    }

    class MessagesFix implements EnhancedFix {

        Document doc = null;
        int start = 0;
        String bodyText = null;

        public MessagesFix(Document doc, int start, String bodyText) {
            this.doc = doc;
            this.start = start;
            this.bodyText = bodyText;
        }

        @Override
        public CharSequence getSortText() {
            return "charsequence";
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(InternalMethodHint.class, "InternalMethodHint.fix-text");
        }

        @Override
        public ChangeInfo implement() throws Exception {
            //Add 1 character, for the semi-colon:
            doc.remove(start, bodyText.length() + 1);
            //Display message to user in status bar:
            StatusDisplayer.getDefault().setStatusText("Removed: " + bodyText);
            return null;
        }
    }
}
