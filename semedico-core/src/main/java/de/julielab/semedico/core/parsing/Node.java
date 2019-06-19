package de.julielab.semedico.core.parsing;

import de.julielab.semedico.core.parsing.ParseTree.Serialization;
import de.julielab.semedico.core.search.query.QueryToken;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents any kind of node in a LR td parse tree. It contains
 * methods to query and modify the properties of the node, e.g. its children.
 * 
 * @author hellrich
 * 
 */
public abstract class Node {
	protected long id = -1;
	protected QueryToken.Category tokenType;
	protected BranchNode parent = null;
	protected String text = null;
	protected int originalBeginOffset = -1;
	protected int originalEndOffset = -1;
	protected int height = -1;
	protected QueryToken queryToken;

	/**
	 * The type of a parse tree node distinguishes the semantic categories that
	 * can be found within the parse tree. It is comprised of boolean operators
	 * (AND, OR NOT) and lexical items of different complexity.
	 * 
	 * @author faessler
	 * 
	 */
	public enum NodeType {
		/**
		 * A concept node, can also be ambiguous. There is currently no explicit
		 * enum constant for ambiguous concepts because they would not be used
		 * in the current state of the system.
		 */
		CONCEPT,
		/**
		 * Phrases are searched verbatim, i.e. without concept recognition.
		 * Phrases in Semedico may be input by the user through quotes "..." or
		 * through dash-compounds x-y-z.
		 */
		PHRASE, KEYWORD, @Deprecated EVENT, AND, OR, NOT
	}

	/**
	 * 
	 * @param text
	 *            The textual representation of this node.
	 */
	public Node(String text) {
		this(text, QueryToken.Category.ALPHA);
	}

	/**
	 * 
	 * @param text
	 *            The textual representation of this node.
	 */
	public Node(String text,  QueryToken.Category tokenType) {
		this.tokenType = tokenType;
		if (StringUtils.isBlank(text))
			throw new IllegalArgumentException("Node text must not be blank.");
		this.text = text;
	}

	/**
	 * Set the text of this node.
	 * 
	 * @param text
	 *            The text of this node, can be actual text (e.g. "mouse") or
	 *            information about syntactic role (e.g. "AND").
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Set the id of this node. Used for retrieval via map.
	 * 
	 * @param l
	 *            The id of this node.
	 */
	public void setId(long l) {
		if (l != -1 && this.id != -1)
			throw new IllegalAccessError("The node with ID " + this.id + " cannot be set another ID.");
		this.id = l;
	}

	/**
	 * Set the parent of this node. Used for navigation in the tree.
	 * 
	 * @param parent
	 *            The parent oft this node.
	 */
	public void setParent(BranchNode parent) {
		this.parent = parent;
	}

	/**
	 * Get the parent of this node.
	 * 
	 * @return The parent of this node.
	 */
	public BranchNode getParent() {
		return parent;
	}

	/**
	 * Get the id of this node.
	 * 
	 * @return The id of this node.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Determine whether this node is a leaf.
	 * 
	 * @return True if this node is a leaf.
	 */
	public abstract boolean isLeaf();

	/**
	 * Create a string representation of this node and its subtree (mostly for
	 * debugging and test purposes).
	 * 
	 * @param serializationType
	 * @return A string representation of this node and its subtree.
	 */
	public abstract String toString(Serialization serializationType);

	/**
	 * Determine whether a child can be added.
	 * 
	 * @return True if a child can be added.
	 */
	public abstract boolean subtreeCanTakeNode();

	/**
	 * Get the text of this node.
	 * 
	 * @return The text of this node.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set the original begin offset of this node regarding the query.
	 * 
	 * @param begin
	 *            The original begin offset.
	 */
	public void setBeginOffset(int begin) {
		originalBeginOffset = begin;
	}

	/**
	 * Set the original end offset of this node regarding the query.
	 * 
	 * @param end
	 *            The original end offset.
	 */
	public void setEndOffset(int end) {
		originalEndOffset = end;
	}

	/**
	 * Get the original begin offset of this node regarding the query.
	 * 
	 * @return The original begin offset.
	 */
	public int getBeginOffset() {
		return originalBeginOffset;
	}

	/**
	 * Get the original end offset of this node regarding the query.
	 * 
	 * @return The original end offset.
	 */
	public int getEndOffset() {
		return originalEndOffset;
	}

	/**
	 * Returns this node as a <tt>TextNode</tt> instance, if it actually is a
	 * <tt>TextNode</tt> and thus a leaf. Otherwise, <tt>null</tt> is returned.
	 * 
	 * @return
	 */
	public TextNode asTextNode() {
		if (this.getClass().equals(TextNode.class))
			return (TextNode) this;
		return null;
	}

	/**
	 * 
	 * @return Whether this not is a <tt>TextNode</tt> or an <tt>EventNode</tt>.
	 */
	public boolean isConceptNode() {
		return this.getClass().equals(TextNode.class);
	}

	public abstract NodeType getNodeType();

	public  QueryToken.Category getTokenType() {
		return tokenType;
	}

	public void setTokenType(QueryToken.Category tokenType) {
		this.tokenType = tokenType;
	}

	public abstract boolean isAtomic();

	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height of the subtree represented by this node beginning with
	 * <tt>currentHeight</tt>.
	 * 
	 * @param currentHeight
	 */
	void setHeightRecursively(int currentHeight) {
		height = currentHeight;
		if (this instanceof BranchNode) {
			BranchNode branchNode = (BranchNode) this;
			for (Node child : branchNode.getChildren())
				child.setHeightRecursively(currentHeight + 1);
		}
	}

	/**
	 * Sets the <tt>height</tt> attribute of each node in the tree this node is
	 * part of, relative to the tree root.
	 */
	public void computeTreeHeight() {
		if (null == parent)
			setHeightRecursively(0);
		else
			parent.computeTreeHeight();
	}

	/**
	 * Creates an isolated copy of this node. Isolated means that it does have
	 * no association to to a ParseTree or other nodes (this could cause
	 * undefined behaviour of a parse tree, node traversals etc.)
	 * 
	 * @return
	 */
	public Node copy() {
		throw new NotImplementedException();
	}
	
	/**
	 * Returns the original query token from which this parse node is derived.
	 * 
	 * @return
	 */
	public QueryToken getQueryToken() {
		if (null == queryToken)
			return QueryToken.UNSPECIFIED_QUERY_TOKEN;
		return queryToken;
	}

	public void setQueryToken(QueryToken queryToken) {
		this.queryToken = queryToken;
	}
}
