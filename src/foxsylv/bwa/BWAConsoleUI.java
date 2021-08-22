package foxsylv.bwa;

import java.util.Arrays;
import java.util.Scanner;

/**
 * An implementation of the Bookworm Magic Pen commands
 * using only the Windows console
 * 
 * @author FoxSylv
 */
public class BWAConsoleUI {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Welcome to FoxSylv's offline Magic Pen trainer! ^w^");
		System.out.println("Type \"help\" for a list of commands if you're confused at any point!\n");
		
		while (true) {
			String rawInput = scanner.nextLine();
			String[] keywords = rawInput.split(" ");
			
			System.out.println();
			String result = BWACommandHandler.callCommand(keywords[0], Arrays.copyOfRange(keywords, 1, keywords.length));
			System.out.println(result);
			System.out.println();
		}
	} //end main()
} //end BWAConsoleUI
