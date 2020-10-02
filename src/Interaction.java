import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class Interaction implements Comparable<Interaction>
{
	private int duration; //the duration of this interaction
	private double probability; //the probability that an individual has an interaction of this duration
	private int numInteractions; //the number of times an interaction of this duration has occured
	
	/**Makes an interaction - DOES NOT SPECIFY THE PROBABILITY OF THIS INTERACTION
	 * OCCURING; setProbabilities() must be called after all an individuals interactions
	 * have been recorded
	 * @param duration the duration of this interaction
	 */
	public Interaction(int duration)
	{
		this.duration = duration;
		numInteractions = 1;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public void incrementInteractions()
	{
		numInteractions++;
	}
	
	public int getInteractions()
	{
		return numInteractions;
	}
	
	public void setProbability(double probability)
	{
		this.probability = probability;
	}
	
	/** sets the probability of an interaction of x duration occuring, given all
	 * interactions taken by an individual. Probability = <#interactions of this length> /
	 * <number of total interactions>
	 * 
	 * @param set the set of all interactions taken by an individual
	 * @return probSet, the set of all interactions taken by an individual with probabilities
	 * set for each interaction
	 */
	public static TreeSet<Interaction> setProbabilites(TreeSet<Interaction> set)
	{
		
		//check to see if we can set interactions first
		if (set.size() == 0)
		{
			System.out.println("Poorly specified interaction; size == 0");
			return set;
		}
		
		//first, find out how many interactions there are total
		double totalInteractions = 0;
		Iterator<Interaction> iter = set.iterator();
		while (iter.hasNext())
		{
			Interaction interaction = iter.next();
			totalInteractions += interaction.getDuration();
		}
		
		//now, set the probabilities
		iter = set.iterator();
		while (iter.hasNext())
		{
			Interaction interaction = iter.next();
			double numInteractions = interaction.getInteractions();
			double prob = numInteractions / totalInteractions;
			interaction.setProbability(prob);
		}
		
		return set;
	}
	
	/** Formats an interaction's class data into that of a string **/
	@Override
	public String toString()
	{
		return "Duration: " + duration + " , #Interactions: " + numInteractions 
				+ ", Probability: " + probability;
	}
	
	
	/** Returns -1 if self < other, 0 if self == other, 1 if self > other
	 * Two interactions are equal IF their DURATIONS are equal;
	 * interactions are compared based on duration
	 */
	@Override
	public int compareTo(Interaction other)
	{
		int otherDuration = other.getDuration();
		
		if (duration < otherDuration)
			return -1;
		if (duration > otherDuration)
			return 1;
		//else, durations are equal
		return 0;
	}
}
