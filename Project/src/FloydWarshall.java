import com.sun.deploy.panel.AbstractRadioPropertyGroup;

import java.util.ArrayList;

public class FloydWarshall {

    public static void getPairwiseShortestPath(boolean[][] adjacencyMatrix, int [][] minimumPairwiseDistances, int [][] nextHopEvents)
    {
        int numberOfEvents = adjacencyMatrix.length; //Number of events in the computation
        //Initialize return 2-D array to Integer.MAX_VALUE
        //Initialize next hop event to -1

        for(int i=0; i<numberOfEvents; i++)
        {
            for (int j = 0; j < numberOfEvents; j++)
            {
                minimumPairwiseDistances[i][j] = Integer.MAX_VALUE;
                nextHopEvents[i][j] = -1;
            }
        }

        //Set distance to self to be equal to 0
        for(int i=0; i<numberOfEvents; i++)
        {
            minimumPairwiseDistances[i][i] = 0;
            nextHopEvents[i][i] = i;
        }

        //For each edge (u,v) set distance to 1
        for(int i=0; i<numberOfEvents; i++)
        {
            for(int j=0; j<numberOfEvents; j++)
            {
                if(adjacencyMatrix[i][j])
                {
                    minimumPairwiseDistances[i][j] =1;
                    nextHopEvents[i][j] = j;
                }
            }
        }

        //Get minimum path between pairwise events
        for(int k=0; k<numberOfEvents; k++)
        {
            for(int i=0; i<numberOfEvents; i++)
            {
                for(int j=0; j<numberOfEvents; j++)
                {
                    if(minimumPairwiseDistances[i][j] > minimumPairwiseDistances[i][k] + minimumPairwiseDistances[k][j]) {
                        minimumPairwiseDistances[i][j] = minimumPairwiseDistances[i][k] + minimumPairwiseDistances[k][j];
                        nextHopEvents[i][j] = nextHopEvents[i][k];
                    }
                }
            }
        }

    }


    public  static ArrayList<Integer> getPath(int[][] nextHopEvents, int from, int to)
    {
        ArrayList<Integer> resultPath = new ArrayList<>();
        if(nextHopEvents[from][to] == -1)
        {
            return resultPath;
        }

        resultPath.add(from);
        while(from != to)
        {
            from = nextHopEvents[from][to];
            resultPath.add(from);
        }

        return resultPath;

    }

}
