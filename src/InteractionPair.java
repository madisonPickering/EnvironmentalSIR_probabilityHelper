import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class InteractionPair implements Comparable<InteractionPair>
{
	private int id; //the id of this individual
	private TreeSet<Interaction> interactions;
	
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
			if (thisInter.size() > 1)
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
