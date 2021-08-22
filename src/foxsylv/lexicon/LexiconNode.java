package foxsylv.lexicon;


import java.util.ArrayList;
import java.util.Iterator;

/**
 * Stores a character which can be retrived using {@code getData},
 * a boolean which can be set and retieved using {@code isWord}, and
 * a list of similarly defined children that can be iterated over.
 * 
 * <p> To access children directly, it is required to specify
 * the character whose data they contain using {@code getNode}.
 * {@code isLeaf} gives whether there are any children to access at all.
 * 
 * <p> Can {@code add}, {@code remove}, and test {@code contains} on
 * children by using their contained characters as references.
 * <strong>NOTE! Removing a node will delete it's children as well!</strong>
 * 
 * @author FoxSylv
 */
public class LexiconNode implements Iterable<LexiconNode> {
	private char data;
	private boolean isWord = false;
	private ArrayList<LexiconNode> children;
	
	private static final int DEFAULT_CHILDREN_CAPACITY = 2;
	
	/**
	 * Initializes a LexiconNode with some given character
	 */
	public LexiconNode(char data) {
		this.data = data;
		this.children = new ArrayList<LexiconNode>(DEFAULT_CHILDREN_CAPACITY);
	} //end LexiconNode()


	/**
	 * Provides an iterator to loop over all children
	 * 
	 * @return Iterator over child nodes
	 */
	@Override
	public Iterator<LexiconNode> iterator() {
		return children.iterator();
	} //end iterator()
	
	
	
	/**
	 * Tests whether or not a character is any direct child's data
	 * 
	 * @param c Character
	 * @return True iff {@code c} is the data of any direct child
	 */
	public boolean contains(char c) {
		for (LexiconNode child : children) {
			if (child.getData() == c) {
				return true;
			}
		}
		return false;
	} //end contains()
	
	
	/**
	 * Removes the child with some given data, if it exists
	 * 
	 * @param c Character
	 * @return True iff {@code c} was successfully removed
	 */
	public boolean remove(char c) {
		for (int i = 0; i < children.size(); ++i) {
			if ((children.get(i)).getData() == c) {
				children.remove(i);
				return true;
			}
		}
		return false;
	} //end remove()
	
	
	/**
	 * Adds a child with some given data in alphabetical
	 * position, if it doesn't already exist
	 * 
	 * @param c Character
	 * @return True iff {@code c} was successfully added
	 */
	public boolean add(char c) {
		if (contains(c)) {
			return false;
		}
		
		int properIndex = 0;
		for (int i = 0; i < children.size(); ++i) {
			if (c > (children.get(i)).getData()) {
				properIndex = i + 1;
			}
		}
		children.add(properIndex, new LexiconNode(c));
		return true;
	} //end add()
	
	
	/**
	 * Returns whether or not this node is a leaf node
	 * 
	 * @return True iff this node is a leaf node
	 */
	public boolean isLeaf() {
		return children.size() == 0;
	} //end isLeaf()
	
	
	
	/**
	 * Returns the child that has a given character as data.
	 * If no such node exists, an exception is thrown
	 * 
	 * @param c Character
	 * @return Child node with {@code c} as its data
	 */
	public LexiconNode getNode(char c) throws IllegalArgumentException {
		for (LexiconNode child : children) {
			if (child.getData() == c) {
				return child;
			}
		}
		throw new IllegalArgumentException("LexiconNode does not have a node with data '" + c + "' as a child!");
	} //end getNode()
	
	
	
	/**
	 * Returns the contained character
	 * 
	 * @return Contained character
	 */
	public char getData() {
		return data;
	} //end getData();
	
	
	/**
	 * Returns whether or not this node finalizes a complete word
	 * 
	 * @return True iff this node finalizes a complete word
	 */
	public boolean isWord() {
		return isWord;
	} //end isWord()
	
	/**
	 * Sets whether this node finalizes a complete word
	 * 
	 * @param isWord Updated value of whether this node finalizes a complete word
	 */
	public void isWord(boolean isWord) {
		this.isWord = isWord;
	} //end isWord()
} //end LexiconNode
