package foxsylv.bwa;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import foxsylv.bwa.BWABoard.AttackDmgs;

/**
 * Class that process a given command and keywords,
 * yielding a String response.
 * 
 * Any known command can be called from {@code callCommand}
 * (see COMMANDS array for a list of accepted commands)
 * 
 * @author FoxSylv
 */
public class BWACommandHandler {
	private static final String VERSION_NUM = "1.1.0";
	
	private static BWA1Board bwa1board = new BWA1Board();
	private static boolean bwa1boardInitialized = false;
	
	
	private static boolean inWordChallenge = false;
	private static String wcRequiredLetters = "";
	private static int wcRacksLeft = 10;
	private static int wcMaxPoints = 0;
	private static int wcCurrentPoints = 0;
	
	private static final int DEFAULT_WC_RACK_COUNT = 10;
	private static final int BEST_WORDS_DISPLAY_COUNT = 9;
	
	
	
	/**
	 * Utility method that divides an integer by four,
	 * using nice decimals and converting to a String
	 * 
	 * @param qh Input number
	 * @return String version of {@code qh / 4}
	 */
	private static String qhToHearts(int qh) {
		StringBuilder sb = new StringBuilder();
		sb.append(qh / 4);
		
		switch (qh % 4) {
		case 1:
			sb.append(".25");
			break;
		case 2:
			sb.append(".5");
			break;
		case 3:
			sb.append(".75");
			break;
		}
		
		return sb.toString();
	} //end qhToHearts()
	
	
	/**
	 * Lists all words in the supplied list, spearated by commas
	 * 
	 * @param words List of words
	 * @return Comma-separated list
	 */
	private static String listWords(Set<String> words) {
		if (words.size() == 0) {
			return "No words found!";
		}
		
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			sb.append(word);
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		return sb.toString();
	} //end listWords()
	
	
	/**
	 * Lists the best words in the rack
	 * 
	 * @return List of best words
	 */
	private static String listBestWords(List<String> bestWords) {
		StringBuilder sb = new StringBuilder("Best words:\n");
		for (int i = 0; i < Math.min(BEST_WORDS_DISPLAY_COUNT, bestWords.size()); ++i) {
			sb.append(bestWords.get(i));
			sb.append(" - ");
			sb.append(qhToHearts(bwa1board.attackPower(bestWords.get(i)).getBaseQh()));
			sb.append("BH\n");
		}
		sb.append('\n');
		return sb.toString();
	} //end listBestWords()
	
	
	
	/**
	 * Merges the provided strings into one, with no added spacing
	 * 
	 * @param strings Strings
	 * @return Conjoined string
	 */
	private static String conjoinStrings(String... strings) {
		StringBuilder sb = new StringBuilder();
		for (String str : strings) {
			sb.append(str);
		}
		return sb.toString();
	} //end conjoinStrings()
	
	
	
	/**
	 * Finds the appropriate command from the list (if it
	 * exists) and calls it's given method with the supplied params
	 * 
	 * @param prompt Command name
	 * @param params Command parameters
	 * @return Command result
	 */
	public static String callCommand (String prompt, String[] params) {
		for (Command command : COMMANDS) {
			if (prompt.equals(command.name)) {
				return command.call(params);
			}
		}
		return "Command \"" + prompt + "\" not recognized!\nPlease type \"help\" for a list of accepted commands!";
	}
	
	
	/**
	 * Helper class used to store the information
	 * pertaining to a command. {@code helpInfo()} gives
	 * information about a commands' usage, which can be
	 * called through {@code call()}.
	 * 
	 * @author FoxSylv
	 */
	private static abstract class Command {
		private static final int NAME_BUFFER = 15;
		private static final int PARAM_BUFFER = 50;
		
		private String name, params, helpInfo, detailedInfo;
		private boolean isVisible = true;
		
		/**
		 * Initializes data pertaining to the command
		 * 
		 * @param name Command name
		 * @param params Command parameter explanation
		 * @param helpInfo Basic Command Usage
		 * @param isVisible Visibility in "help"
		 * @param detailedInfo Advanced Command Usage
		 */
		public Command(String name, String params, String helpInfo, boolean isVisible, String detailedInfo) {
			this.name = name;
			this.params = params;
			this.helpInfo = helpInfo;
			this.isVisible = isVisible;
			this.detailedInfo = detailedInfo;
		} //end Command()
		
		
		/**
		 * Calls the command with the given params
		 * 
		 * @param params Parameters
		 * @return Command visual result
		 */
		public abstract String call(String[] params);
		
		/**
		 * Returns a one-line simple informer about the command
		 * 
		 * @return Command usage guide
		 */
		public String helpInfo() {
			StringBuilder sb = new StringBuilder(name);
			while (sb.length() < NAME_BUFFER) {
				sb.append(' ');
			}
			sb.append(params);
			while (sb.length() < PARAM_BUFFER) {
				sb.append(' ');
			}
			sb.append("| ");
			sb.append(helpInfo);
			
			return sb.toString();
		} //end helpInfo()
	} //end Command
	
	
	
	
	
	//List of all commands:
	private static final Command[] COMMANDS = {
			new Command("help", "(command1) (command2) ...", "Displays more information about the given commands, or lists all commands if none are given", true,
					"If \"help\" is called with no parameters, it displays a list of all commands.\nIf \"help\" is run with parameters, it displays detailed information about the given commands.") {
				@Override
				public String call(String[] params) {
					StringBuilder sb = new StringBuilder();
					
					if (params.length == 0) {
						for (Command command : COMMANDS) {
							if (command.isVisible) {
								sb.append(command.helpInfo());
								sb.append('\n');
							}
						}
						sb.delete(sb.length() - 1, sb.length());
					}
					else {
						//Display help for found params
						boolean[] foundParam = new boolean[params.length];
						for (Command command : COMMANDS) {
							for (int i = 0; i < params.length; ++i) {
								if (params[i].equals(command.name)) {
									sb.append("\'" + command.name + "\'\n");
									sb.append(command.detailedInfo);
									sb.append("\n\n");
									foundParam[i] = true;
								}
							}
						}

						//Elaborate on missing params if needed
						boolean missingParams = false;
						for (int i = 0; i < foundParam.length; ++i) {
							if (!foundParam[i]) {
								missingParams = true;
								break;
							}
						}
						if (missingParams) {
							sb.append("\nThe following commands were not found: ");
							for (int i = 0; i < params.length; ++i) {
								if (!foundParam[i]) {
									sb.append("\"");
									sb.append(params[i]);
									sb.append("\", ");
								}
							}
							sb.delete(sb.length() - 2, sb.length());
							if (sb.length() != 37) {
								sb.append("\nPlease type \"help\" by itself for a list of accepted commands!");
							}
						}
					}

					return sb.toString();
				} //end call()
			}, //end help Command
			
			new Command("board", "", "Displays the last printed board for convenience", true,
					"Displays the last printed board again. If no such board exists, a notification of such will be shown") {
				@Override
				public String call(String[] params) {
					if (bwa1boardInitialized) {
						return bwa1board.toString();
					}
					else {
						return "There is no board to currently display!";
					}
				} //end call()
			}, //end board Command
			
			
			
			new Command("wordchallenge", "(rack count) (letters)", "Starts a word challenge with the given number of racks, always using any provided letters", true,
					"The word challenge is a game wherein you must try to find the best words in a series of racks, submitting your best guess each board.\nThe best words are shown after each submission, and a final score is shown at the end.\nThe number of racks can be manually supplied to differ from the default (" + Integer.toString(DEFAULT_WC_RACK_COUNT) + "), and letters required to be in each rack can also be supplied.") {
				@Override
				public String call(String[] params) {
					int rackCount;
					try {
						rackCount = Integer.parseInt(params[0]);
					}
					catch (Exception e) {
						rackCount = DEFAULT_WC_RACK_COUNT;
					}
					String requiredLetters = ((params.length > 1) ? conjoinStrings(Arrays.copyOfRange(params, 1, params.length)) : "");
					
					inWordChallenge = true;
					wcRequiredLetters = requiredLetters;
					wcRacksLeft = rackCount;
					wcMaxPoints = 0;
					wcCurrentPoints = 0;
					
					StringBuilder sb = new StringBuilder("Use the \"submit <word>\" command to submit your final words:\n");
					bwa1board.randomizeBoard(requiredLetters);
					sb.append(bwa1board.toString());
					return sb.toString();
				} //end call()
			}, //end wordchallenge Command
			
			new Command("submit", "<word>", "Submits a word for the word challenge", false,
					"Submits a word for the word challenge.\nIf a word challenge is not currently active, a notification will be displayed.") {
				@Override
				public String call(String[] params) {
					if (!inWordChallenge) {
						return "There is no word challenge currently active!";
					}
					if (params.length == 0) {
						return "Correct usage is \"submit <word>\". Please try again";
					}
					String submittedWord = params[0];
					if (!bwa1board.dictionary().containsWord(submittedWord)) {
						return submittedWord + " is not a valid word! Please try again";
					}
					
					//Update scores
					List<String> bestWords = bwa1board.bestWords();
					AttackDmgs bestDmg = bwa1board.attackPower(bestWords.get(0));
					AttackDmgs submittedDmg = bwa1board.attackPower(submittedWord);
					wcMaxPoints += bestDmg.getBaseQh();
					wcCurrentPoints += submittedDmg.getBaseQh();
					
					//Print final score or show next rack
					StringBuilder sb = new StringBuilder(listBestWords(bestWords));
					if (--wcRacksLeft == 0) {
						sb.append("Game over. Well done!\nFinal Score: ");
						sb.append(qhToHearts(wcCurrentPoints));
						sb.append(" out of ");
						sb.append(qhToHearts(wcMaxPoints));
						sb.append(" points. Accuracy: ");
						double accuracy = 100.0d * ((double) wcCurrentPoints) / ((double) wcMaxPoints);
						String accStr = Double.toString(accuracy);
						sb.append(accStr.substring(0, Math.min(accStr.length(), 5)));
						sb.append("%");
					}
					else {
						bwa1board.randomizeBoard(wcRequiredLetters);
						sb.append(bwa1board.toString());
					}
					return sb.toString();
				} //end call()
			}, //end submit Command
			
			
			
			new Command("isword", "<word1> (word2) ...", "Tests whether the given words are accepted", true,
					"Lists the words in the given order, denoting next to each one whether or not the dictionary accepts it.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "No words supplied!";
					}
					
					StringBuilder sb = new StringBuilder();
					for (String param : params) {
						boolean isWord = bwa1board.dictionary().containsWord(param.toLowerCase());
						sb.append(param + " is " + (isWord ? "" : "NOT ") + "accepted\n");
					}
					
					sb.delete(sb.length() - 1, sb.length());
					return sb.toString();
				} //end call()
			}, //end isword Command
			
			new Command("attack", "<word1> (word2) ... (specifiers)", "Gives the attack potential of the given words", true,
					"Finds the Base Heart damage and Final Quarter-heart damage of the given words using the specifications given.\nThe following specifiers are allowed:\n\'bow=\' - Set to 0 for no bow, 1 for Zyx, and 2 (default) for Xyzzy\n\'parrot=\' - Set to 0 if false, 1 if true (default)\n\'hammer=\' - Set to 0 if false, 1 if true (default)\n\'pu=\' - Set to 1 if powered up, -1 if powered down, and 0 for no modifier (default)\n\'attack=\' - Selects the attack power, in tenths of a percent (i.e. 163.3% would be submitted as \'attack=1633\')\n\'gem=\' - Selects the attack power, as a percent (i.e. 20% would be submitted as \'gem=20\')\n\'armour=\' Set to 0 for heavy armour, 1 for light armour, and 0 for no armour (default)") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "No words supplied!";
					}
					
					int bowLvl = 2;
					int usingParrot = 1;
					int usingHammer = 1;
					int isPowered = 0;
					int gemBoost = 0;
					int attackBoost = 1698;
					int armourLvl = 0;
					
					//Differentiating keywords and words
					ArrayList<String> words = new ArrayList<String>();
					for (String param : params) {
						switch (param) {
						case "parrot=0":
						case "parrot=false":
						case "parrot=off":
							usingParrot = 0;
							break;
						case "hammer=0":
						case "hammer=false":
						case "hammer=off":
							usingHammer = 0;
							break;
						case "powered=1":
						case "powered=true":
						case "powered=on":
						case "pu=1":
						case "pd=-1":
							isPowered = 1;
							break;
						case "powered=-1":
						case "pd=1":
						case "pu=-1":
							isPowered = -1;
							break;
						case "bow=1":
							bowLvl = 1;
							break;
						case "bow=0":
						case "bow=false":
						case "bow=off":
							bowLvl = 0;
							break;
						case "armour=2":
						case "armor=2":
						case "armour=heavy":
						case "armor=heavy":
						case "armour=true":
						case "armor=true":
						case "armour=on":
						case "armor=on":
							armourLvl = 2;
							break;
						case "armour=1":
						case "armor=1":
						case "armour=light":
						case "armor=light":
							armourLvl = 1;
						case "armour = 0":
						case "armor=0":
						case "armour=none":
						case "armor=none":
						case "armour=off":
						case "armor=off":
						case "armour=false":
						case "armor=false":
						case "parrot=1":
						case "parrot=on":
						case "parrot=true":
						case "hammer=1":
						case "hammer=on":
						case "hammer=true":
						case "powered=0":
						case "powered=off":
						case "pu=0":
						case "pd=0":
						case "bow=2":
						case "bow=on":
						case "bow=true":
							break;
						default:
							//Gem Boost
							if ((param.substring(0, Math.min(4, param.length())).equals("gem="))) {
								try {
									gemBoost = Integer.parseInt(param.substring(4, param.length()));
								}
								catch (Exception e) {}
							}
							//Attack Boost
							else if ((param.substring(0, Math.min(7, param.length())).equals("attack="))) {
								try {
									attackBoost = Integer.parseInt(param.substring(7, param.length()));
								}
								catch (Exception e) {}
							}
							else {
								words.add(param);
							}
							break;
						}
					}
					
					//Actually calculating attack power for words finally
					StringBuilder sb = new StringBuilder();
					for (String word : words) {
						sb.append(word);
						sb.append(" - ");
						
						AttackDmgs dmg = bwa1board.attackPower(word, bowLvl, usingHammer, usingParrot, gemBoost, attackBoost, isPowered, armourLvl);
						sb.append(qhToHearts(dmg.getBaseQh()));
						sb.append("BH (");
						sb.append(dmg.getFinalDmg());
						sb.append(" Final Qh)");
						
						if (!bwa1board.dictionary().containsWord(word)) {
							sb.append(" *Note! This is NOT an accepted word*");
						}
						sb.append('\n');
					}
					sb.delete(sb.length() - 1, sb.length());
					return sb.toString();
				} //end call()
			}, //end attack Command
			
			new Command("bestwords", "(letters)", "Finds the best words in the current rack", true,
					"Lists the top " + Integer.toString(BEST_WORDS_DISPLAY_COUNT) + " words in a rack.\nIn the event of a tie in BH, alphabetical order is then used.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						if (!bwa1boardInitialized) {
							return "There is no board to find the best words in!";
						}
						
						List<String> bestWords = bwa1board.bestWords();
						return listBestWords(bestWords);
					}
					else {
						String letters = conjoinStrings(params);
						BWABoard newBoard = new BWA1Board(letters.length(), 1);
						newBoard.randomizeBoard(letters);
						
						List<String> bestWords = newBoard.bestWords();
						return listBestWords(bestWords);
					}
				} //end call()
			}, //end bestwords Command
			
			
			
			new Command("setboard", "<rack>", "Sets the stored board to be the provided rack", true,
					"Sets the board to be the provided letters.\nIf too few are supplied, the rest are filled by random letters.\nIf too many are supplied, only the first ones are used.") {
				@Override
				public String call(String[] params) {
					String letters = conjoinStrings(params).toUpperCase();
					bwa1board.randomizeBoard(letters);
					inWordChallenge = false;
					bwa1boardInitialized = true;
					return bwa1board.toString();
				}
			},
			
			new Command("randomboard", "(letters)", "Sends a random rack, including any given letters", true,
					"Randomizes the board, including any provided letters.\nIf too many letters are supplied, only the first ones are used to set the board.") {
				@Override
				public String call(String[] params) {
					String requiredLetters = conjoinStrings(params);
					if (requiredLetters.length() > bwa1board.boardWidth() * bwa1board.boardHeight()) {
						return "Error! Too many provided letters!";
					}
					
					bwa1board.randomizeBoard(requiredLetters.toString().toUpperCase());
					bwa1boardInitialized = true;
					inWordChallenge = false;
					return bwa1board.toString();
				} //end call()
			}, //end randomboard Command
			
			new Command("randomword", "", "Gives a random word from the dictionary", true,
					"Gives a random word from the dictionary.") {
				@Override
				public String call(String[] params) {
					return bwa1board.dictionary().randomWord();
				} //end call()
			}, //end randomword Command
			
			
			
			new Command ("contains", "<letters>", "Lists all words containing the given letters", true,
					"Lists all words containing the given letters at any point in the word.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "No letters provided!";
					}
					
					//Create required regex
					StringBuilder regex = new StringBuilder("(?:");
					String requiredLetters = conjoinStrings(params);
					for (char letter : requiredLetters.toCharArray()) {
						regex.append(letter);
						regex.append("()|");
					}
					regex.append("[a-z]){");
					regex.append(requiredLetters.length());
					regex.append(",}");
					for (int i = 1; i <= requiredLetters.length(); ++i) {
						regex.append('\\');
						regex.append(i);
					}
					
					//Find and display words
					Set<String> matchedWords = bwa1board.dictionary().matchRegex(regex.toString());
					return listWords(matchedWords);
				} //end call()
			}, //end contains Command
			
			new Command("sequence", "<letters>", "Lists all words that contain a given sequence of letters", true,
					"Lists all words that contain a given sequence of letters.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "No letters provided!";
					}
					
					String sequence = conjoinStrings(params);
					Set<String> matchedWords = bwa1board.dictionary().matchRegex(sequence);
					return listWords(matchedWords);
				} //end call()
			}, //end sequence Command
			
			new Command("begins", "<letters>", "Lists all words that start with the given letters", true,
					"Lists all words that start with the given sequence of letters.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "No letters provided!";
					}
					
					String beginning = conjoinStrings(params);
					Set<String> matchedWords = bwa1board.dictionary().matchRegex("^" + beginning);
					return listWords(matchedWords);
				} //end call()
			}, //end begins Command
			
			new Command("ends", "<letters>", "Lists all words that end with the given letters", true,
					"Lists all words that end with the given sequence of letters.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "No letters provided!";
					}
					
					String ending = conjoinStrings(params);
					Set<String> matchedWords = bwa1board.dictionary().matchRegex(ending + "$");
					return listWords(matchedWords);
				} //end call()
			}, //end ends Command
			
			new Command("similarto", "<word> <transpositions>", "Lists all words within the given letter transposition count of the given word", true,
					"Lists all words a given number of character transpositions or less away from the given word.\nA character transposition is the changing of one letter into another, without any adding or subtracting.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "No supplied word!";
					}
					int separation = 0;
					try {
						separation = Integer.parseInt(params[1]);
					}
					catch (Exception e) {
						return "Invalid transposition count!";
					}
					
					Set<String> matchedWords = bwa1board.dictionary().suggestCorrections(params[0], separation);
					return listWords(matchedWords);
				} //end call()
			}, //end similarto Command
			
			new Command("regex", "<regex>", "Searches the dictionary using a given regex pattern", true,
					"Searches the dictionary using a given regex pattern.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "Regex pattern required!";
					}
					Set<String> matchedWords;
					try {
						matchedWords = bwa1board.dictionary().matchRegex(params[0]);
					}
					catch (Exception e) {
						return "Invalid regex pattern!";
					}
					
					return listWords(matchedWords);
				} //end call()
			}, //end regex Command
			
			new Command("credits", "", "Displays the credits for this program", true,
					"Displays the credits for this program.") {
				@Override
				public String call(String[] params) {
					return "This code has been developed and programmed by FoxSylv!\nCatch her on discord at \'FoxSylv#7315\'! ^w^";
				} //end call()
			}, //end credits Command
			
			new Command("version", "", "Displays the version number of this program", true,
					"Displays the version number of this program.") {
				@Override
				public String call(String[] params) {
					return "Version: " + VERSION_NUM;
				} //end call()
			}, //end version Command
			
			new Command("github", "", "Displays the github link for this program", true,
					"Displays the github link for this program.") {
				@Override
				public String call(String[] params) {
					return "github.com/FoxSylv/Offline_Magic_Pen";
				} //end call()
			}, //end version Command
			
			
			
			
			
			
			
			//Debug commands
			new Command("debug.help", "", "Displays ALL commands", false,
					"Displays all commands, including ones not visible with the standard \'help\' command.") {
				@Override
				public String call(String[] params) {
					StringBuilder sb = new StringBuilder();
					for (Command command : COMMANDS) {
						sb.append(command.helpInfo());
						sb.append('\n');
					}
					sb.delete(sb.length() - 1, sb.length());
					return sb.toString();
				} //end call()
			}, //end debug.help Command
			
			new Command("debug.helpall", "", "Displays detailed information for ALL commands", false,
					"Displays the detailed information for all commands.") {
				@Override
				public String call(String[] params) {
					StringBuilder sb = new StringBuilder();
					for (Command command : COMMANDS) {
						sb.append("\'" + command.name + "\'\n");
						sb.append(command.detailedInfo);
						sb.append("\n\n");
					}
					return sb.toString();
				} //end call()
			}, //end debug.helpall Command
			
			
			
			new Command("debug.remove", "<word1> (word2) ...", "Removes the given words from the dictionary", false,
					"Removes the given words from the dictionary.") {
				@Override
				public String call(String[] params) {
					for (String word : params) {
						bwa1board.dictionary().removeWord(word);
					}
					return "Words removed successfully!";
				} //end call()
			}, //end debug.remove Command
			
			new Command("debug.addword", "<word1> (word2) ...", "Adds the given words into the dictionary", false,
					"Adds the given words into the dictionary.") {
				@Override
				public String call(String[] params) {
					for (String word : params) {
						bwa1board.dictionary().addWord(word);
					}
					return "Words added successfully!";
				} //end call()
			}, //end debug.addword Command
			
			new Command("debug.addfile", "<file1> (file2) ...", "Adds words from the provided files into the dictionary", false,
					"Adds words from the provided files into the dictionary.\nThe number of words added will be displayed, along with any error that caused word adding to cease.") {
				@Override
				public String call(String[] params) {
					if (params.length == 0) {
						return "Error: No file name provided!";
					}
					
					int wordsAdded = 0;
					for (String file : params) {
						int addResult = bwa1board.dictionary().addWordsFromFile(file);
						if (addResult == -1) {
							return "An error occurred while trying to read " + file + ". " + Integer.toString(wordsAdded) + " words added successfully!";
						}
					}
					return Integer.toString(wordsAdded) + " words added successfully!";
				} //end call()
			}, //end debug.addfile Command
			
			
			
			new Command("debug.wordcount", "", "Displays the number of words in the dictionary", false,
					"Displays the number of words in the dictionary.") {
				@Override
				public String call(String[] params) {
					return "Word Count: " + bwa1board.dictionary().numWords();
				} //end call()
			}, //end debug.wordcount Command
			
			
			new Command("debug.wcinfo", "", "Displays the current stats for the Word Challenge", false,
					"Displays the current stats for the Word Challenge.") {
				@Override
				public String call(String[] params) {
					if (!inWordChallenge) {
						return "Not in a word challenge!";
					}
					
					StringBuilder sb = new StringBuilder("Current Points: ");
					sb.append(qhToHearts(wcCurrentPoints));
					sb.append("\nMax Points: ");
					sb.append(qhToHearts(wcMaxPoints));
					sb.append("\nRacks Left: ");
					sb.append(wcRacksLeft);
					sb.append("\nRequired Letters: ");
					sb.append((wcRequiredLetters.length() == 0) ? "None" : wcRequiredLetters);
					return sb.toString();
				} //end call()
			} //end debug.wcinfo Command
	}; //end COMMANDS
} //end BWACommandHandler
