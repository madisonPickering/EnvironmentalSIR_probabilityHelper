/**Generates some interactions; used to perform Pearson's Chi^2 test
 * @author Madison Pickering
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

import java.util.Random;
import java.util.TreeSet;

/** Generates some interactions
 */
public class InteractionGenerator {
	
	private static final int MAX_INTER = 10; //number of interactions to generate
	
	/**Generates InteractionPairs for n humans using generateInteraction
	 * NOTE: DOES NOT GENERATE THE NUMBER OF INTERACTIONS OF THIS TYPE.
	 * @param n the number of InteractionPairs to generate
	 * @return fakeData the generated dataSet
	 */
	public static TreeSet<InteractionPair> generateDataSet(int n)
	{
		TreeSet<InteractionPair> fakeData = new TreeSet<>();
		for (int i = 1; i <= 1; i++)
		{
			InteractionPair thisInter = new InteractionPair(i);
			
			//generate some interactions. Start by generating a duration
			Random generator = new Random();
			while (thisInter.getInteractions().size() < MAX_INTER)
			{
				//*2 since approx 64% of data is within 2 of the mean, +1 since shouldnt generate a 0
				int duration = (int) Math.round(( (Math.abs(generator.nextGaussian()) * 2 ) + 1));
				System.out.println("Duration: " + duration);
				//generate the probability of that duration using the equation y = 0.06 * (x ^ -1.7)
				double probability = 0.06 * Math.pow(duration, -1.7);
				//if underflow or overflow occurs, set probability to 0.0001
				if (probability < 0 || probability > 1)
					probability = 0.001;
				
				Interaction inter = new Interaction(duration);
				inter.setProbability(probability);
				thisInter.addInteraction(inter);
			}
			
			fakeData.add(thisInter);	
		}
		return fakeData;
	}
}
