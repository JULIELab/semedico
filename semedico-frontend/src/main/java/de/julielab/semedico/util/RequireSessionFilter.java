package de.julielab.semedico.util;

import java.io.IOException;

import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Response;

public class RequireSessionFilter implements ComponentRequestFilter {

	private ComponentSource componentSource;

	private ApplicationStateManager applicationStateManager;

	private Response response;

	private PageRenderLinkSource pageRenderLinkSource;

	public RequireSessionFilter(ComponentSource componentSource, Response response,
			ApplicationStateManager applicationStateManager, PageRenderLinkSource pageRenderLinkSource) {
		this.componentSource = componentSource;
		this.response = response;
		this.applicationStateManager = applicationStateManager;
		this.pageRenderLinkSource = pageRenderLinkSource;
	}

	@Override
	public void handleComponentEvent(ComponentEventRequestParameters parameters, ComponentRequestHandler handler)
			throws IOException {
//		if (redirectIfObjectNotInSession(parameters.getActivePageName())) {
//			return;
//		}

		handler.handleComponentEvent(parameters);
	}

	@Override
	public void handlePageRender(PageRenderRequestParameters parameters, ComponentRequestHandler handler)
			throws IOException {
//		if (redirectIfObjectNotInSession(parameters.getLogicalPageName())) {
//			return;
//		}

		handler.handlePageRender(parameters);
	}

	private boolean redirectIfObjectNotInSession(String pageName) {
		Component component = componentSource.getPage(pageName);
		if (component.getClass().isAnnotationPresent(null)) {
			// Persist annotation = null;
			//
			// if(!applicationStateManager.exists(annotation.value()))
			// {
			// redirect(annotation.redirectPage())
			// return true
			// }
		}

		return false;
	}

	private void redirect(String pageName) throws IOException {
		response.sendRedirect(pageRenderLinkSource.createPageRenderLink(pageName));
	}

}