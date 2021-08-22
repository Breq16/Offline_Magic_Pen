package foxsylv.lexicon;


import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import java.util.regex.Pattern;

/**
 * A tree structure designed to hold a list of words,
 * with all words able to be iterated over with {@code iterator}
 * 
 * <p> Adding and removing words is facilitated.
 * {@code addWord} adds words to the trie (or from a file with
 * {@code addWordsFromFile}), and {@code removeWord} removes words.
 * <i> NOTE: Pointless prefixes are eliminated after word removal </i>
 * 
 * <p> Containment of prefixes and words can be tested by
 * {@code containsPrefix} and {@code containsWord}, respectively
 * 
 * <p> In addition, words can be searched for using a regex input to
 * {@code matchRegex} or corrected using {@code suggestCorrections}.
 * <i>NOTE! It is impossible to return exceptionally large sets</i>
 * 
 * @author FoxSylv
 */
public class LexiconTrie implements Iterable<String> {	
	private LexiconNode root = new LexiconNode('^');
	private int numWords = 0;
	private static Random randomizer = new Random();
	
	/**
	 * Gets the word representation of a given path in the trie
	 * 
	 * @param path Path of nodes
	 * @return Current word
	 */
	private String getString(ArrayList<LexiconNode> path) {
		StringBuilder sb = new StringBuilder(path.size());
		for (LexiconNode node : path) {
			sb.append(node.getData());
		}
		return sb.substring(1);
	} //end currentString()
	
	
	
	
	/**
	 * Node-wise recursive helper method to add words to the trie
	 * 
	 * @param currentNode Current location
	 * @param word Word to add
	 * @param Location in {@code word}
	 */
	private void addWordChar(LexiconNode currentNode, String word, int index) {
		if (index == word.length()) {
			currentNode.isWord(true);
			return;
		}
		
		char currentChar = word.charAt(index);
		if (!currentNode.contains(currentChar)) {
			currentNode.add(currentChar);
		}
		addWordChar(currentNode.getNode(currentChar), word, index + 1);
	} //end AddWordChar()
	
	
	/**
	 * Adds a word to the trie, creating prefix nodes as necessary.
	 * 
	 * 
	 * @param word Word to add
	 * @return True iff the adding was successful
	 */
	public boolean addWord(String word) {
		if (containsWord(word)) {
			return false;
		}
		
		try {
			addWordChar(root, word, 0);
			++numWords;
			return true;
		}
		catch (Exception e) {
			return false;
		}
	} //end addWord()
	
	
	/**
	 * Adds all words listed in a file, so long as
	 * each word is on their own line in that file.
	 * 
	 * @param filename File name
	 * @return Number of words added, or -1 if reading failed
	 */
	public int addWordsFromFile(String filename) {
		try {
			String[] fileWords = (new String(Files.readAllBytes(Paths.get(filename)))).split("\r\n|\r|\n");
			for (String word : fileWords) {
				addWord(word.toLowerCase());
			}
			return fileWords.length;
		}
		catch(Exception e) {
			return -1;
		}
	} //end addWordsFromFile()

	
	
	
	/**
	 * Node-wise recursive helper method to remove words from the trie
	 * 
	 * @param currentNode Current location
	 * @param word Word to remove
	 * @param index Location in {@code word}
	 * @return True iff {@code currentNode} should be deleted
	 */
	private boolean removeWordChar(LexiconNode currentNode, String word, int index) {
		if (index == word.length()) {
			return true;
		}
		
		char currentChar = word.charAt(index);
		if (removeWordChar(currentNode.getNode(currentChar), word, index + 1)) {
			currentNode.remove(currentChar);
			return currentNode.isLeaf();
		}
		return false;
	} //end removeWordChar()
	
	
	/**
	 * Deletes a given word from the trie, if it exists
	 * 
	 * @param Word to delete
	 * @return True iff {@code word} was successfully deleted
	 */
	public boolean removeWord(String word) {
		try {
			removeWordChar(root, word, 0);
			--numWords;
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}//end removeWord()
	
	

	
	/**
	 * Returns the node corresponding to the last letter of a word.
	 * If the word is not in the trie, an exception is thrown.
	 * 
	 * @param word Word to search for
	 * @return Node corresponding to the last letter of {@code word}
	 */
	private LexiconNode getNodeTo(String word) {
		LexiconNode finder = root;
		for (char c : word.toCharArray()) {
			finder = finder.getNode(c);
		}
		return finder;
	}
	
	
	/**
	 * Returns whether or not a word is contained in the trie
	 * 
	 * @param word Word
	 * @return True iff {@code word} is contained in the trie
	 */
	public boolean containsWord(String word) {
		try {
			return getNodeTo(word).isWord();
		}
		catch (Exception e) {
			return false;
		}
	} //end containsWord()

	/**
	 * Returns whether or not a prefix is contained in the trie
	 * 
	 * @param prefix Prefix
	 * @return True iff {@code prefix} is contained in the trie
	 */
	public boolean containsPrefix(String prefix) {
		try {
			getNodeTo(prefix);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	} //end containsPrefix()


	
	
	/**
	 * Returns the number of contained words
	 * 
	 * @return Number of contained words
	 */
	public int numWords() {
		return numWords;
	} //end numWords()

	
	
	
	
	/**
	 * An iterator that loops through all words in
	 * the trie in alphabetical order
	 * 
	 * @author Aurora Theriault
	 */
	private class LexiconTrieIterator implements Iterator<String> {
		private ArrayList<LexiconNode> currentPath = new ArrayList<LexiconNode>();
		String lastWord;
		
		
		/**
		 * Tests whether or not there are more elements in the trie
		 * 
		 * @return True iff more words exist to be iterated over
		 */
		@Override
		public boolean hasNext() {
			return getString(currentPath).compareTo(lastWord) < 0;
		} //end hasNext()
		
		
		/**
		 * Recursive helper method for {@code next} that updates
		 * the current path to the next word
		 * 
		 * @param previousNode Denotes the previous node when ascending
		 * 					   and the current node while descending
		 */
		private void nextPath(LexiconNode previousNode) {
			LexiconNode currentNode = currentPath.get(currentPath.size() - 1);
			
			//Descending left-most node downwards, if possible
			if (previousNode == currentNode) {
				for (LexiconNode nextNode : currentNode) {
					currentPath.add(nextNode);
					if (!nextNode.isWord()) {
						nextPath(nextNode);
					}
					return;
				}
			}
			
			//Test whether the ascending can switch to descending on another branch
			else {
				for (LexiconNode nextNode : currentNode) {
					if (nextNode.getData() > previousNode.getData()) {
						currentPath.add(nextNode);
						if (!nextNode.isWord()) {
							nextPath(nextNode);
						}
						return;
					}
				}
			}
			
			//Ascend if necessary
			currentPath.remove(currentNode);
			nextPath(currentNode);
		} //end nextPath()
		
		
		/**
		 * Searches for the next word based on the current path
		 * and returns that word as a String
		 * 
		 * @return Next word
		 */
		@Override
		public String next() {
			nextPath(currentPath.get(currentPath.size() - 1));
			return getString(currentPath);
		} //end next()
		
		
		/**
		 * Initializes the starting path and last word for iteration
		 */
		private LexiconTrieIterator() {
			currentPath.add(root);
			
			StringBuilder sb = new StringBuilder();
			LexiconNode currentNode = root;
			while (!currentNode.isLeaf()) {
				LexiconNode lastNode = null;
				for (LexiconNode next : currentNode) {
					lastNode = next;
				}
				currentNode = lastNode;
				sb.append(currentNode.getData());
			}
			lastWord = sb.toString();
		} //end LexiconIterator()
	} //end LexiconTrieIterator
	
	
	/**
	 * Returns an iterator that loops over all
	 * contained words in alphabetical order
	 * 
	 * @return Iterator of contained words, in alphabetical order
	 */
	public Iterator<String> iterator() {
		return root.isLeaf() ? Collections.emptyIterator() : new LexiconTrieIterator();
	} //end iterator()
	
	
	
		
	/**
	 * Returns a random word from the trie
	 * 
	 * @return Random word
	 */
	public String randomWord() {
		int wordNum = randomizer.nextInt(numWords);
		Iterator<String> counter = iterator();
		for (int i = 0; i < wordNum - 1; ++i) {
			counter.next();
		}
		return counter.next();
	}

	
	
	
	/**
	 * Recursive helper method for {@code suggestCorrections} that
	 * adds all suggestions from a given path onwards
	 * 
	 * @param currentPath Current path in trie
	 * @param target Word to be corrected
	 * @param maxDistance Maximum number of character transpositions from now onwards
	 * @param corrections List of corrections
	 */
	private void addCorrectionsOnwards(ArrayList<LexiconNode> currentPath, String target, int maxDistance, HashSet<String> corrections) {
		if (maxDistance < 0) {
			return;
		}
		
		int currentLength = currentPath.size() - 1;
		if (currentLength == target.length()) {
			if (currentPath.get(currentLength).isWord()) {
				corrections.add(getString(currentPath));
			}
			return;
		}
		
		for (LexiconNode nextNode : currentPath.get(currentLength)) {
			int distanceDecliner = (nextNode.getData() == target.charAt(currentLength)) ? 0 : 1;
			currentPath.add(nextNode);
			addCorrectionsOnwards(currentPath, target, maxDistance - distanceDecliner, corrections);
			currentPath.remove(nextNode);
		}
	} //end addCorrectionsOnwards()
	
	/**
	 * Gives a list of words that have are at most a given number
	 * of character transpositions away from the given {@code target}
	 * 
	 * @param target Word to be corrected
	 * @param maxDistance Maximum number of character transpositions
	 * @return List of potential corections
	 */
	public Set<String> suggestCorrections(String target, int maxDistance) {
		HashSet<String> corrections = new HashSet<String>();
		ArrayList<LexiconNode> path = new ArrayList<LexiconNode>();
		path.add(root);
		
		addCorrectionsOnwards(path, target, maxDistance, corrections);
		return corrections;
	} //end suggestCorrections()
	
	
	/**
	 * Matches a regex pattern to find words in the trie
	 * 
	 * @param pattern A regex pattern
	 * @return List of words that satisfy {@code pattern}
	 */
	public Set<String> matchRegex(String regex) {
		HashSet<String> matches = new HashSet<String>();
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		
		for (String word : this) {
			if (pattern.matcher(word).find()) {
				matches.add(word);
			}
		}
		
		return matches;
	} //end matchRegex()
	
	
	
	
	
	
	
	/**
	 * Constructs a trie with words from a given file
	 * 
	 * @param wordFile File of words
	 */
	public LexiconTrie(String wordFile) {
		addWordsFromFile(wordFile);
	}
} //end LexiconTrie
