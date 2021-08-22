package foxsylv.bwa;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import foxsylv.lexicon.LexiconTrie;

/**
 * An abstract class for storing and handling the basic operations of a
 * generic Bookworm Adventures-esque game board. Anything specific
 * to a certain game's board is relegated to abstract methods (e.g. 
 * {@code dictionary} and {@code attackPower}).
 * <p>
 * Such generic operations include randomizing the board with
 * {@code randomizeBoard}, or only certain letters with
 * {@code replaceTiles} and {@code replaceTilesWithGravity}.
 * The tile distributions are supplied via. the abstract {@code tileData}
 * <P>
 * 
 */
public abstract class BWABoard {	
	private Random randomizer = new Random();
	private String[] board = new String[boardWidth() * boardHeight()];
	
	/**
	 * Returns the board width
	 * 
	 * @return Board height
	 */
	abstract public int boardWidth();
	
	/**
	 * Returns the board height
	 * 
	 * @return Board height
	 */
	abstract public int boardHeight();
	
	
	/**
	 * Returns the dictionary
	 * 
	 * @return Dictionary
	 */
	abstract public LexiconTrie dictionary();
	
	/**
	 * Returns the tile probabilites
	 * 
	 * @return Tile Probabilities
	 */
	abstract public JSONObject tileData();
	
	
	/**
	 * Gives the raw attack power of a word based on some criteria
	 * 
	 * @param word Attacking word
	 * @params params Other params (see individual documentation for details)
	 */
	abstract public AttackDmgs attackPower(String word, int... params);
	
	protected static class AttackDmgs {
		private int baseQh, finalDmg;
		
		/**
		 * Initializer for both raw and boosted damage
		 * 
		 * @param rawDmg Raw Damage
		 * @param boostedDmg Boosted Damage
		 */
		public AttackDmgs(int baseQh, int finalDmg) {
			this.baseQh = baseQh;
			this.finalDmg = finalDmg;
		} //end AttackDmgs()
		
		
		/**
		 * Getter for raw damage
		 * 
		 * @return Raw Damage
		 */
		public int getBaseQh() {
			return baseQh;
		} //end getRawDmg()
		
		/**
		 * Getter for boosted damage
		 * 
		 * @return Boosted Damage
		 */
		public int getFinalDmg() {
			return finalDmg;
		} //end getBoostedDmg()
	} //end AttackDmgs
	
	
	
	/**
	 * Returns the JSON in a file (for tile data)
	 * 
	 * @param file Target file
	 * @return Contained JSON
	 */
	static protected JSONObject extractJSONData(String file) {
		try {
			return (JSONObject) (new JSONParser()).parse(new String(Files.readAllBytes(Paths.get(file))));
		}
		catch (Exception e) {
			return null;
		}
	}
	
	
	
	
	/**
	 * Gets a random tile based on the supplied probabilities
	 * 
	 * @return Random Tile
	 */
	private String randomTile() {
		JSONObject tileProbabilities = tileData();
		Collection<String> tiles = tileProbabilities.keySet();
		
		//Count total odds
		int totalOdds = 0;
		for (String tile : tiles) {
			Object probability = ((JSONObject) tileProbabilities.get(tile)).get("probability");
			totalOdds += ((Long) probability).intValue();
		}
		
		//Find appropriate letter
		int oddsLeft = randomizer.nextInt(totalOdds);
		for (String tile : tiles) {
			Object probability = ((JSONObject) tileProbabilities.get(tile)).get("probability");
			oddsLeft -= ((Long) probability).intValue();
			if (oddsLeft < 0) {
				return tile;
			}
		}
		
		//This should never happen
		return "?";
	} //end randomTile()
	
	
	/**
	 * Supplants the current board with a new one, with
	 * the given tiles being guaranteed in the grid.
	 * 
	 * @param tiles Guaranteed tiles
	 */
	public void randomizeBoard(String tiles) {
		//Get new board
		for (int tile = 0; tile < board.length; ++tile) {
			board[tile] = randomTile();
		}
		
		//Randomly place the guaranteed letters
		boolean[] locationsUsed = new boolean[boardWidth() * boardHeight()];
		for (int i = 0; i < Math.min(boardWidth() * boardHeight(), tiles.length()); ++i) {
			int loc = randomizer.nextInt(16);
			while (locationsUsed[loc]) {
				loc = (loc + 1) % (boardWidth() * boardHeight());
			}
			locationsUsed[loc] = true;
			board[loc] = tiles.substring(i, i + 1);
		}
	} //end randomizeBoard()
	
	
	
	
	/**
	 * Tests whether a given string is contained in the rack.
	 * '?' counts as a wildcard tile and 'Q' counts as Qu.
	 * 
	 * @param word Word to test
	 * @param Whether {@code word} is in the rack
	 */
	public boolean containsWord(String word) {
		String upperWord = word.toUpperCase();
		//Checks for Q's without U's and illegal characters in word
		for (int i = 0; i < upperWord.length() - 1; ++i) {
			if (upperWord.charAt(i) < 'A' || upperWord.charAt(i) > 'Z') {
				return false;
			}
			if (upperWord.charAt(i) == 'Q' && upperWord.charAt(i + 1) != 'U') {
				return false;
			}
		}
		if (upperWord.charAt(upperWord.length() - 1) == 'Q') {
			return false;
		}
		
		
		int wildCount = 0;
		int[] letterCounts = new int[26];
		
		//Count letters in rack
		for (String tile : board) {
			char b = tile.charAt(0);
			if (b == '?') {
				++wildCount;
			}
			if (b == 'Q') {
				++letterCounts['U' - 'A'];
			}
			++letterCounts[b - 'A'];
		}
		
		//Count letters in word and checks for deficits
		int wildsNeeded = 0;
		for (char c : upperWord.toCharArray()) {
			if (--letterCounts[c - 'A'] < 0) {
				++wildsNeeded;
			}
		}
		
		return wildCount >= wildsNeeded;
	} //end containsWord()
	
	
	
	
	/**
	 * A comparator that compares a words' attack power
	 * 
	 * @author FoxSylv
	 */
	private class AttackPowerComparator implements Comparator<String> {
		/**
		 * Returns whether word 2 is more powerful than word 1
		 * 
		 * @param word1 Word 1
		 * @param word2 Word 2
		 * @return Amount word 2 is more powerful by
		 */
		@Override
		public int compare(String word1, String word2) {
			return attackPower(word2).getBaseQh() - attackPower(word1).getBaseQh();
		} //end compare()
	} //end AttackPowerComparator
	
	/**
	 * Returns a list of the best words in the rack, in descending order
	 * first by goodness, then by alphabetical placement.
	 * 
	 * @return List of best words
	 */
	public List<String> bestWords() {
		List<String> bestWords = new ArrayList<String>();
		
		for (String word : dictionary()) {
			if (containsWord(word)) {
				bestWords.add(word);
			}
		}
		
		Collections.sort(bestWords, new AttackPowerComparator());
		return bestWords;
	} //end bestWords()
	
	
	
	/**
	 * Returns a string containing the visual representation
	 * of the contained board in a grid
	 * 
	 * @return visual representation of the board
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int h = 0; h < boardHeight(); ++h) {
			for (int w = 0; w < boardWidth(); ++w) {
				sb.append(board[boardWidth() * h + w]);
				sb.append(' ');
			}
			sb.append('\n');
		}
		sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
	} //end toString()
} //end BWABoard
