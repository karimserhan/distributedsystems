/**
 * Created by egantoun on 11/20/15.
 */

import java.util.*;
public class PredicateControl {

    public ArrayList<Integer> getShortestSequence (ArrayList<Integer> initialEvents, ArrayList<Integer> finalEvents, boolean [][] adjacencyMatrix)
    {
        int numberOfEvents = adjacencyMatrix.length;
        int[][] pairwiseShortestPath = new int[numberOfEvents][numberOfEvents];
        int[][] nextHopEvents =  new int[numberOfEvents][numberOfEvents];
        FloydWarshall.getPairwiseShortestPath(adjacencyMatrix,pairwiseShortestPath,nextHopEvents);

        //Start with initializing shortest path from inital to final
        ArrayList<Integer> resultShortestPath = null;
        int minDistanceSoFar = Integer.MAX_VALUE;

        for(Integer initialEvent: initialEvents)
        {
            for(Integer finalEvent: finalEvents)
            {
                if(pairwiseShortestPath[initialEvent][finalEvent] < minDistanceSoFar)
                {
                    minDistanceSoFar = pairwiseShortestPath[initialEvent][finalEvent];
                    resultShortestPath = FloydWarshall.getPath(nextHopEvents,initialEvent, finalEvent);
                }
            }
        }

        return resultShortestPath;
    }
}

