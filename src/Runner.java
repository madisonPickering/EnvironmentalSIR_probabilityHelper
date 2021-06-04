/**Program reads in an input file specified in the format of paper
 * "A high-resolution human contact network for infectious disease transmission"
 * then calculates the probability, for every individual in the data set, of an
 * interaction with that duration occurring
 * 
 * Has been augmented to perform chi squared tests as well.
 * 
 * @author Madison Pickering
 * Last modified 10/2/2020
 * 
 * (Copyright 2020 Madison Pickering)
 * This file is part of EnvironmentalSIR_probhelper.
    EnvironmentalSIR_probhelper is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    EnvironmentalSIR_probhelper is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with EnvironmentalSIR_probhelper.  If not, see <https://www.gnu.org/licenses/>.
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
	
	//info for calculating P values
	private static final String TABLE_PATH = "table/ChiSq_PVals.txt";
	public static double[][] pValues; //contains info in ChiSq_PVals. Rows = degrees of freedom, col = chi sqd
	public static int numberOfCPIs;
	public static int notRejected_thous; //number of individuals who did not reject when P = 0.001
	public static int notRejected_hund; //....P = 0.01
	public static int notRejected_fiveHund; //...P = 0.05
	public static int rejected;
	
	/** Throws IOException since we do reading and writing of files **/
	public static void main(String[] args) throws IOException
	{
		numberOfCPIs = 0;
		notRejected_thous = 0;
		notRejected_hund = 0;
		notRejected_fiveHund = 0;
		rejected = 0;
		readChiSqTable();
		
		File inputFile = getInputFile();
		TreeSet<InteractionPair> input = readInput(inputFile);
		
		TreeSet<InteractionPair> generated = InteractionGenerator.generateDataSet(10);
		
		logOutput(input);
		
		logOutput(generated);
		
		//print chi squared stuff
		System.out.println("Number of individuals with > 250 CPIs: " + numberOfCPIs);
		System.out.println("Number of individuals who didn't reject H0:");
		System.out.println("P = 0.001: " + notRejected_thous);
		System.out.println("P = 0.01: " + notRejected_hund);
		System.out.println("P = 0.05: " + notRejected_fiveHund);
		System.out.println("Number of individuals who rejected H0: " + rejected);
		

	}
	
	public static void readChiSqTable() throws FileNotFoundException
	{
		pValues = new double[100][3]; //rows index df, cols index p value
		File table = new File(TABLE_PATH);
		
		int i = 0;
		Scanner lineScanner = new Scanner(table);
		lineScanner.nextLine(); //throw out first line, contains metainfo
		while (lineScanner.hasNextLine())
		{
			String line = lineScanner.nextLine();
			Scanner tokenScanner = new Scanner(line);
			tokenScanner.next(); //throw out df val
			double first = tokenScanner.nextDouble();
			double second = tokenScanner.nextDouble();
			double last = tokenScanner.nextDouble();
			tokenScanner.close();
			
			double[] row = {first, second, last};
			pValues[i] = row;
			i++;
		}
		lineScanner.close();
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
	
	
	/** Reads the chi squared table in folder table, and returns it
	 * 
	 * @return a table giving ideal chi squared values for degrees of 
	 * freedom [1-->100], 
	 */
	public static int[][] readChiSqVals()
	{
		return null;
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
			int id = tokens.nextInt(); 
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
		
		//set the probabilities & run Chi Squared for each interactionPair
		Iterator<InteractionPair> iter = input.iterator();
		while (iter.hasNext())
		{
			InteractionPair thisPair = iter.next();
			thisPair.setInteractionsProbabilities();
//			thisPair.runChiSquaredTest();
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
