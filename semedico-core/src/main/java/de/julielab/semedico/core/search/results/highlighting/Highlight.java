package de.julielab.semedico.core.search.results.highlighting;

public class Highlight implements ISerpHighlight
{
    /**
     * To be used when a highlight was requested for a field or document part for which no highlight exists
     * or that is not (yet) supported by the document module queried for this highlight.
     */
	public static final Highlight EMPTY_HIGHLIGHT = new Highlight("", "<none>", 0);
	public Highlight(String highlight, String field, float score)
	{
		this.highlight = highlight;
		this.field = field;
		docscore = score;
	}

	private float docscore;
	private String highlight;

    public float getDocscore() {
        return docscore;
    }

    public void setDocscore(float docscore) {
        this.docscore = docscore;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    private String field;

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(highlight);
		sb.append(" (field: ");
		sb.append(field);
		sb.append(", ");
		sb.append("docscore: ");
		sb.append(docscore);
		sb.append(")");
		return sb.toString();
	}
}