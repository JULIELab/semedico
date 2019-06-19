package de.julielab.semedico.util;

import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;

import java.io.IOException;

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