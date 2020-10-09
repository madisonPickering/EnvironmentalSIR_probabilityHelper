/**Program reads in an input file specified in the format of paper
 * "A high-resolution human contact network for infectious disease transmission"
 * then calculates the probability, for every individual in the data set, of an
 * interaction with that duration occurring
 * 
 * @author Madison Pickering
 * Last modified 10/2/2020
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

public class Runner {

	private static final String INPUT_ROOT = "input";
	private static final String OUTPUT_ROOT = "output";
	
	/** Throws IOException since we do reading and writing of files **/
	public static void main(String[] args) throws IOException
	{
		File inputFile = getInputFile();
		TreeSet<InteractionPair> input = readInput(inputFile);
		
		//print contents, for testing
//		Iterator<InteractionPair> pairIter = input.iterator();
//		while (pairIter.hasNext())
//		{
//			InteractionPair thisHuman = pairIter.next();
//			System.out.println("Interactions for human " + thisHuman.getID());
//			TreeSet<Interaction> interactions = thisHuman.getInteractions();
//			Iterator<Interaction> iter = interactions.iterator();
//			
//			while (iter.hasNext())
//				System.out.println(iter.next().toString());
//			
//		}
		
		logOutput(input);

	}

	/** Returns the input file to process. Lets the user choose from
	 * whatever input files are in the folder "input"
	 * @return file the file to process for this run
	 */
	public static File getInputFile()
	{
		File root = new File(INPUT_ROOT);
		File[] contents = root.listFiles();
		System.out.println("Process which of these? Enter numerically (eg, 0 for file 0)");
		for (int i = 0; i < contents.length; i++)
			System.out.println(i + ": " + contents[i].getName());
		
		Scanner userInput = new Scanner(System.in);
		int index = userInput.nextInt();
		userInput.close();
		File file = contents[index];
		return file;		
	}
	
	/** Parses the input file and returns the information as an treeset
	 *  PRECONDITION: the input file is in the format specified by paper
	 *  "A high-resolution human contact network for infectious disease transmission"
	 * @param inputFile the file to parse
	 * @return input the parsed input, with statistics calculated
	 * @throws FileNotFoundException in case the input file does not exist
	 */
	public static TreeSet<InteractionPair> readInput(File inputFile) throws FileNotFoundException
	{
		TreeSet<InteractionPair> input = new TreeSet<>();
		Scanner lineScanner = new Scanner(inputFile);
		
		//each line specifies an interaction of the form "human_id1 human_id2 interaction_duration"
		Scanner tokens = new Scanner(System.in);
		while (lineScanner.hasNext())
		{
			String line = lineScanner.nextLine();
			tokens = new Scanner(line);
			int id = /*1;*/tokens.nextInt(); /*****(un)CHANGED THIS SO THAT WE CALCULATE AVG PROBS ***/
			//tokens.next(); /********* REMOVEd THIS WHEN DONE ****/
			//we dont care about who they had the interaction with, so just throw that info away
			tokens.next();
			int duration = tokens.nextInt();
			
			//check to see if we already have an interaction pair for this guy
			InteractionPair maybeExists = new InteractionPair(id);
			boolean contains = input.contains(maybeExists);
			if (contains)
			{
				//get the interactionPair
				InteractionPair excludes = new InteractionPair(id + 1);
				SortedSet<InteractionPair> thisInteractionSet = input.subSet(maybeExists, excludes);
				if (thisInteractionSet.size() != 1)
					System.out.println("Error when specifying comparator; fix");
				
				//log this interaction
				InteractionPair thisInteraction = thisInteractionSet.first();
				thisInteraction.addInteraction(duration);
				
				//stick it back into input
				input.remove(thisInteraction);
				input.add(thisInteraction);
			}
			
			else //else, an interactionPair for this person doesn't yet exist. Make one
			{
				maybeExists.addInteraction(duration);
				input.add(maybeExists);
			}
		}
		tokens.close();
		lineScanner.close();
		
		//set the probabilities for each interactionPair
		Iterator<InteractionPair> iter = input.iterator();
		while (iter.hasNext())
		{
			InteractionPair thisPair = iter.next();
			thisPair.setInteractionsProbabilities();
		}
		
		return input;
	}
	
	/** Log (duration, probability) pairs as a .csv 
	 * @throws IOException, since we are logging to a file
	 **/
	public static void logOutput(TreeSet<InteractionPair> input) throws IOException
	{
		File outputFile = new File(OUTPUT_ROOT + "/output.csv");
		outputFile.createNewFile();
		FileWriter inner = new FileWriter(outputFile);
		BufferedWriter writer = new BufferedWriter(inner);
		
		//print contents, for testing
		Iterator<InteractionPair> pairIter = input.iterator();
		while (pairIter.hasNext())
		{
			InteractionPair thisHuman = pairIter.next();
			writer.write("Interactions for human " + thisHuman.getID() + "\n");
			TreeSet<Interaction> interactions = thisHuman.getInteractions();
			Iterator<Interaction> iter = interactions.iterator();
			
			while (iter.hasNext())
			{
				Interaction thisInteraction = iter.next();
				int duration = thisInteraction.getDuration();
				double prob = thisInteraction.getProbability();
				//writer.write(duration + ", " + prob + "\n");
				writer.write(thisInteraction.toString() + "\n");
			}
			
		}
		
		writer.close();
		inner.close();
	}
}
