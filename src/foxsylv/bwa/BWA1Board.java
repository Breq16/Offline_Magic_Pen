package foxsylv.bwa;


import foxsylv.lexicon.LexiconTrie;
import org.json.simple.JSONObject;

/**
 * Class for storing the information pertaining to a BWA1 board
 * and registering BWA1 attack damage with {@code attdckPower}.
 * <p>
 * See {@code BWABoard} for game logic
 * 
 * @author FoxSylv
 */
public class BWA1Board extends BWABoard {
	private static final int BOARD_WIDTH = 4;
	private static final int BOARD_HEIGHT = 4;
	
	private static final LexiconTrie DICTIONARY = new LexiconTrie("BWA1_Dictionary.txt");
	private static final LexiconTrie METAL_DICTIONARY = new LexiconTrie("BWA1_MetalDictionary.txt");
	
	//The ints are hundredths of a percent
	private static final JSONObject TILE_DATA = extractJSONData("BWA1_TileData.json");
	
	/**
	 * Returns the board width
	 * 
	 * @return Board height
	 */
	@Override
	public int boardWidth() {
		return BOARD_WIDTH;
	}
	
	/**
	 * Returns the board height
	 * 
	 * @return Board height
	 */
	@Override
	public int boardHeight() {
		return BOARD_HEIGHT;
	}
	
	
	/**
	 * Returns the dictionary used
	 * 
	 * @return Dictionary
	 */
	@Override
	public LexiconTrie dictionary() {
		return DICTIONARY;
	} //end dictionary()

	
	/**
	 * Returns the tile probabilities
	 * 
	 * @return Tile Probabilities
	 */
	@Override
	public JSONObject tileData() {
		return TILE_DATA;
	}
	
	
	
	
	private static final int[] LP_TO_BQH = {1, 1, 1, 2, 3, 4, 6, 8, 11, 14, 18, 22, 27, 32, 38, 44, 52};
	
	/**
	 * Returns the attack power of a word
	 * Note: To get powered down, submit -1 in the power parameter
	 * 
	 * @param word Word
	 * @param Bow level
	 * @param Hammer Usage
	 * @param Parrot Usage
	 * @param Gem Boost x100
	 * @param Attack Boost x1000
	 * @param Powered Effect
	 * @return Attack Damage
	 */
	@Override
	public AttackDmgs attackPower(String word, int... params) {
		if (params.length < 7) {
			return attackPower(word);
		}
		
		int bowLvl = params[0];
		boolean hammerUsed = (params[1] == 1);
		boolean parrotUsed = (params[2] == 1);
		double gemBoost = params[3] / 100.0;
		double attackBoost = params[4] / 1000.0;
		boolean poweredUp = (params[5] == 1);
		boolean poweredDown = (params[5] == -1);
		int armourLvl = params[6];
		
		String upperWord = word.toUpperCase();
		
		//Find base Letter Point Values
		int letterPoints = 0;
		for (char letter : upperWord.toCharArray()) {
			switch (letter) {
			case '?':
				letterPoints += 1;
				break;
			case 'R':
				letterPoints += (parrotUsed ? 8 : 4);
				break;
			case 'X':
			case 'Y':
			case 'Z':
				if (bowLvl == 1) {
					letterPoints += 10;
					break;
				}
				if (bowLvl == 2) {
					letterPoints += 12;
					break;
				}
			default:
				Object damage = ((JSONObject) TILE_DATA.get(Character.toString(letter))).get("damage");
				letterPoints += ((Long) damage).intValue();
				break;
			}
		}
		
		//Find Base Quarter-hearts and Final Adjusted Damage
		int baseQh = (letterPoints < 58) ? LP_TO_BQH[letterPoints / 4] : 52;
		double partialDmg = (baseQh / 4.0d) * (1 + (attackBoost / 1000.0d)) + Math.ceil((baseQh / 4.0d) * (gemBoost / 100.0d));
		double boostMult = 1.0d;
		if (poweredUp) {
			boostMult *= 1.25d;
		}
		if (poweredDown) {
			boostMult *= 0.66d;
		}
		if (hammerUsed && METAL_DICTIONARY.containsWord(word)) {
			boostMult *= 1.5d;
			baseQh *= 1.5d; //To signify metal words' importance
		}
		
		int finalDmg = (int) Math.floor(boostMult * partialDmg) - (6 * armourLvl);
		if (hammerUsed) {
			finalDmg += 4;
		}
		return new AttackDmgs(baseQh, finalDmg);
	} //end attackPower()
	
	/**
	 * Submits default parameters for {@code attackPower}.
	 * 
	 * @param word Word
	 * @return Attack Damage
	 */
	public AttackDmgs attackPower(String word) {
		return attackPower(word, 2, 1, 1, 0, 0, 0, 0);
	}
} //end BWA1Board
