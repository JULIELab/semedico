package de.julielab.semedico.components;

import java.io.IOException;
import java.util.Collection;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.search.IFacetedSearchService;
import de.julielab.semedico.util.LazyDisplayGroup;

public class Hitlist {

	@Inject
	private IFacetedSearchService searchService;		
	
	@Property
	@Parameter
	private LazyDisplayGroup<DocumentHit> displayGroup;	
	
	@Property
	private DocumentHit hitItem;

	@Property
	private int hitIndex;

	@Property
	private int authorIndex;

	@Property
	private int pagerItem;
	
	@Property
	private String kwicItem;	

	@Property
	private Author authorItem;
	
	public boolean isCurrentPage() {
		return pagerItem == displayGroup.getCurrentBatchIndex();
	}

	public String getCurrentHitClass() {
		return hitIndex % 2 == 0 ? "evenHit" : "oddHit";
	}

	public String getCurrentArticleTypeClass() {
		if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_ABSTRACT)
			return "hitIconAbstract";
		else if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_TITLE)
			return "hitIconTitle";
		else if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_FULL_TEXT)
			return "hitIconFull";
		else
			return null;
	}

	public int getIndexOfFirstArticle() {
		return displayGroup.getIndexOfFirstDisplayedObject() + 1;
	}

	public boolean isNotLastAuthor() {
		return authorIndex < hitItem.getDocument().getAuthors().size() - 1;
	}
	
	public void onActionFromPagerLink(int page) throws IOException {
		displayGroup.setCurrentBatchIndex(page);
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(startPosition);
		displayGroup.setDisplayedObjects(documentHits);
	}

	public void onActionFromPreviousBatchLink() throws IOException {
		displayGroup.displayPreviousBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(startPosition);
		displayGroup.setDisplayedObjects(documentHits);
	}

	public void onActionFromNextBatchLink() throws IOException {
		displayGroup.displayNextBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(startPosition);
		displayGroup.setDisplayedObjects(documentHits);
	}
}
