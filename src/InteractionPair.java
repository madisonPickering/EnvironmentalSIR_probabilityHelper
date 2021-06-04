/**Utility class used to perform pearson's chi^2 test
 * @author Madison Pickering
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class InteractionPair implements Comparable<InteractionPair>
{
	private static final int OBSERVATION_THRESHOLD = 3; //must have at least this num of observations
														//to be added to the observed[] array
	private int id; //the id of this individual
	private TreeSet<Interaction> interactions;
	private Interaction[] observed;
	private Interaction[] ideal;
	
	public InteractionPair(int id)
	{
		this.id = id;
		interactions = new TreeSet<>();
	}
	
	public int getID()
	{
		return id;
	}
	
	public TreeSet<Interaction> getInteractions()
	{
		return interactions;
	}
	
	/**Performs Pearson's Chi Squared test for goodness of fit
	 * Does the following:
	 * 1. migrates interactions to observed, using OBSERVATION_THRESHOLD to do so
	 * 2. estimates the parameter P for the observed values
	 * 3. uses the observed parameter to generate ideal counts according to a geometric
	 *    probability distribution
	 * 4. calculates the chi squared value from the ideal and observed counts
	 * 5. calculates the degrees of freedom
	 * 6. Uses the calculated chi squared value and degrees of freedom to find the p-value
	 * 	  (if any) to the finest degree of granularity at which we can fail to reject the null 
	 *    hypothesis. Updates chiSquared, pValue, and the appropriate counter (notRejected_x)
	 *    in Runner.
	 */
	public void runChiSquaredTest()
	{
		//step 1
		generateObserved();
		
		//check to make sure that theres over 250 CPIs recorded
		int sampleSize = 0;
		for (int i = 0; i < observed.length; i++)
		{
			Interaction inter = observed[i];
			sampleSize += inter.getInteractions();
//			System.out.println(inter.toString());
		}
//		System.out.println("sample size: " + sampleSize);
		//if there aren't, return
		if (sampleSize < 250)
			return;

		//else
		Runner.numberOfCPIs++;
		
		//step 2
		double p = estimateP();
		
		//test
//		System.out.println("p: " + p);
		
		//step 3
		generateIdeal(sampleSize, p);
		
		//test
//		System.out.println("Printing ideal: ");
//		for (int i = 0; i < ideal.length; i++)
//			System.out.println(ideal[i].toString());
		
		//step 4	
		double chiSq = calculateChiSq();
		
	
		//step 5
		int degreesOfFreedom = observed.length - 2; //using the formula k - p - 1
		
		//step 6 - determine p val
		double[] pValues = Runner.pValues[degreesOfFreedom - 1]; //minus one bc index 0 has degree of freedom 1
		double chiThous = pValues[2]; //0.001
		double chiHund = pValues[1];  //0.01
		double chiFive = pValues[0];  //0.05
		
		System.out.println("id " + id + " Chi sq: " + chiSq + " degrees of freedom: " + degreesOfFreedom);
		
		if (chiSq < chiThous)
			Runner.notRejected_thous++;
		else if (chiSq < chiHund)
			Runner.notRejected_hund++;
		else if (chiSq < chiFive)
			Runner.notRejected_fiveHund++;
		else
			Runner.rejected++;
		
	}
	
	/**Loops through interactions and adds an interaction with no associated probability
	 * to observed. Adds an interaction if the interaction has been observed at least
	 * OBSERVATION_THRESHOLD number of times.
	 * NOTE: we say that for an interacton of duration x, the x + 1th trial was
	 *  successful. Thus we increment duration by 1, since it is analogous to the number of
	 *  bernoulli experiments performed
	 */
	private void generateObserved()
	{
		//copy all interactions observed > OBSERVATION_THRESHOLD times to observed
		ArrayList<Interaction> inter = new ArrayList<>();
		Iterator<Interaction> iter = interactions.iterator();
		while (iter.hasNext())
		{
			Interaction thisInter = iter.next();
			if (thisInter.getInteractions() > OBSERVATION_THRESHOLD)
				inter.add(thisInter);
		}
		
		//make a deep copy
		observed = new Interaction[inter.size()];
		for (int i = 0; i < inter.size(); i++)
		{
			Interaction thisInter = inter.get(i);
			int duration = thisInter.getDuration() + 1;
			double numInteractions = thisInter.getInteractions();
			Interaction newInter = new Interaction(duration);
			newInter.setNumInteractions(numInteractions);
			observed[i] = newInter;
		}
		
	}
	
	/** Estimates the probability p, the probability of a positive outcome
	 *  for a bernoulli trial given a series of observed vales
	 * 
	 * estimate = <num data pts> / numTrails for all data points
	 * 
	 * @return an MLE estimate of the probability p of Geometric(p)
	 */
	private double estimateP()
	{
		double numDataPts = 0;
		double numTrials = 0;
		for (int i = 0; i < observed.length; i++)
		{
			Interaction inter = observed[i];
			int interTrials = inter.getDuration();
			double numThisInter = inter.getInteractions();
			numDataPts += numThisInter;
			numTrials += (numThisInter * interTrials);
		}
		
		double p = numDataPts / numTrials;
		return p;
	}
	
	/** Generates ideal frequencies based on the results of observed
	 * Ideal frequency = P(Geom) * sample size
	 */
	private void generateIdeal(double sampleSize, double p)
	{
		ideal = new Interaction[observed.length];
		for (int i = 0; i < observed.length; i++)
		{
			Interaction inter = observed[i];
			int k = inter.getDuration(); //since its observed it will be an int
			double prob = generateGeomPoint(k, p);
			double frequency = prob * sampleSize;
			
			Interaction idealInter = new Interaction(k);
			idealInter.setNumInteractions(frequency);
			ideal[i] = idealInter;
		}
	}
	
	/** Generates a point on a geometric distribution according
	 * to its PDF
	 * 
	 * @param k the number of bernoulli trials
	 * @param p the probability of success for each bernoulli trial
	 * @return Geom(p), the probability of a point on the geometric distribution
	 */
	private double generateGeomPoint(double k, double p)
	{
		double oneMinusP = 1.0 - p;
		double oneMinusToTheK = Math.pow(oneMinusP, k - 1);
		return (oneMinusToTheK * p);
	}
	
	/** Uses the frequency arrays observed[] and ideal[] to generate the
	 * chi squared value for the data
	 * @return chiSq the chi squared value for the data
	 */
	private double calculateChiSq()
	{
		double chiSq = 0;
		for (int i = 0; i < observed.length; i++)
		{
			Interaction obsInter = observed[i];
			double obsFreq = obsInter.getInteractions();
			
			Interaction idealInter = ideal[i];
			double idealFreq = idealInter.getInteractions();
			
			double sqdDiff = Math.pow((obsFreq - idealFreq), 2);
			double thisChi = sqdDiff / idealFreq;
			chiSq += thisChi;
		}
		
		return chiSq;
	}
	
	/**Calls Interaction.setProbabilities to set the probabilities of 
	 * this set of interactions
	 */
	public void setInteractionsProbabilities()
	{
		TreeSet<Interaction> prob = Interaction.setProbabilites(interactions);
		interactions = prob;
	}
	
	/** Adds an interaction of this duration to this individuals
	 * list of interactions if it does not yet exist, otherwise,
	 * updates the number of interactions of this duration if it
	 * does exist.
	 * @param interactionDuration the duration of this interaction
	 */
	public void addInteraction(int interactionDuration)
	{
		//check to see if an interaction of this duration exists
		Interaction inter = new Interaction(interactionDuration);
		
		boolean contains = interactions.contains(inter);
		if (contains)
		{
			//find the interaction and update the number of interactions of that duration
			Interaction exclude = new Interaction(interactionDuration + 1);
			SortedSet<Interaction> thisInter = interactions.subSet(inter, exclude);
			if (thisInter.size() != 1)
				System.out.println("Error in InteractionPair addInteraction() - more than one interaction");
			Interaction exists = thisInter.first();
			
			exists.incrementInteractions();
			
			//replace the object with the old value with the obj with the new value
			interactions.remove(inter);
			interactions.add(exists);
		}
		else //else, add the interaction
			interactions.add(inter);
		
	}
	
	/** Adds an interaction of this duration to this individuals
	 * list of interactions, making no attempt to update counts
	 * NOTE: this is to be used by InteractionGenerator only
	 */
	public void addInteraction(Interaction inter)
	{
		interactions.add(inter);
	}
	

	/** Returns -1 if self < other, 0 if self == other, 1 if self > other
	 * Two interactions are equal IF their IDS are equal;
	 * interactions are compared based on id value
	 */
	@Override
	public int compareTo(InteractionPair other)
	{
		int otherId = other.getID();
		
		if (id < otherId)
			return -1;
		if (id > otherId)
			return 1;
		//else, ids are equal
		return 0;
	}
}
