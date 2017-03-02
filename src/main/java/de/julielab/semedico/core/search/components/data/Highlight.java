package de.julielab.semedico.core.search.components.data;

public class Highlight
{
	public Highlight(String highlight, String field, float score)
	{
		this.highlight = highlight;
		this.field = field;
		docscore = score;
	}

	public float docscore;
	public String highlight;
	public String field;

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