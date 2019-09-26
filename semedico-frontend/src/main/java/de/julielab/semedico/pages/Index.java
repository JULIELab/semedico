package de.julielab.semedico.pages;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.util.ConceptValueEncoder;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

/**
 * Start page of application semedico-frontend.
 */
@Import(
        stylesheet =
                {
                        "META-INF/assets/css/index.css"
                },
        library =
                {
                }
)

public class Index extends Search {
    @Inject
    private Request request;

    @SessionState
    private SemedicoSessionState sessionState;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @Inject
    private ComponentResources resources;


    @Inject
    private Logger log;


    public void setupRender() {
        super.setupRender();

        String tutorialMode = request.getParameter("tutorialMode");

        // avoid creation of the session state if possible
        if (null != tutorialMode) {
            sessionState.setTutorialMode(Boolean.parseBoolean(tutorialMode));
        }
    }

    public ConceptValueEncoder getConceptValueEncoder() {
        return new ConceptValueEncoder(termService);
    }

    @AfterRender
    public Object afterRender() {
        super.afterRender();
        return null;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}