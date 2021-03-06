import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.text.DecimalFormat;

public class DomAssAgent2 {
    Player thePlayer;

    public DomAssAgent2(Player p) {
	thePlayer=p;
    }

    private Random rand = new Random();

    // boolean to differentiate between competition play and training play
    // train = true in MLPlayer and train = false in current agent
    private boolean train = false;

    private double Epsilon = 0.01;

    final int numFeatures = 17;

    private static Rules gameRules;

    private Vector<Vector<Player> > globalboard; //indexed by column number then row,
                                          //we number rows from the bottom
                                          //we number columns from the left

    private Vector<Double> weights; //indexed by features
                                    // Feature priority is order of weights

    private ArrayList<Vector<Integer>> history;

    private int result;

    private int movecounter = 0;

    private int depth = 0;

    private double gamma = 0.01;

    private double epsilon = 0.01;

    private void setupBoard(){

	//create the board
	globalboard=new Vector<Vector<Player> >(gameRules.numCols);
	for(int c=0; c<gameRules.numCols; c++) {
	    Vector<Player> col_vec=new Vector<Player>(gameRules.numRows);
	    for(int r=0; r<gameRules.numRows; r++) {
		col_vec.add(Player.EMPTY);
	    }
	    globalboard.add(col_vec);
	}
    }

    private void initWeights(){

	//initialize features to 0
	weights=new Vector<Double>(numFeatures);
        for(int r=0; r<numFeatures; r++) {
		weights.add(0.0);
	}
	weights.set(0, 10.0);
	weights.set(1, 10.0);
	weights.set(2, 10.0);
	weights.set(3, 10.0);
	weights.set(4, 1.32320477953318);
	weights.set(5, 0.91247443162786);
	weights.set(6, 0.91512864125813);
	weights.set(7, 2.12105093731677);
	weights.set(8, 0.06617662834313);
	weights.set(9, 0.13846976333009);
	weights.set(10, 0.15147480212139);
	weights.set(11, 0.08447566064713);
	weights.set(12, 0.03536124670867);
	weights.set(13, 0.03066561122946);
	weights.set(14, 0.0062433173429);
	weights.set(15, 0.07342481301686);
	weights.set(16, -0.81247443162786);
    }

    private void showBoard() {
	Player p;
	StringBuffer sb=new StringBuffer(gameRules.numRows*(gameRules.numCols+1) + 1);
	for(int r=gameRules.numRows-1; r>=0; r--) {
	    for(int c=0; c<gameRules.numCols; c++) {
		p=globalboard.get(c).get(r);
		switch(p) {
		    case ONE: sb.append(1);
		        break;
		    case TWO: sb.append("2");
		        break;
		    case EMPTY: sb.append("-");
		        break;
		    default: sb.append("-");
		        break;
		}
	    }
	    sb.append("\n");
	}
	sb.append("\n");
	System.out.print(sb);
    }

    private int getRowFromColumn(Vector<Vector<Player> > board, int col) {
	Vector<Player> col_vec=board.get(col);
	int index=gameRules.numRows;
	for(int r=0;r<gameRules.numRows;r++) {
	    if(col_vec.get(r) == Player.EMPTY) {
		index=r;
		break;
	    }
	}
	return index;
    }

    private int threeHorizontal(Vector<Vector<Player> > board,Player p, int col, int row)
    {
        if (col >= 3 && board.get(col-1).get(row) == p && board.get(col-2).get(row) == p && board.get(col-3).get(row) == p)
            return 1;
        if (col <= 7 && board.get(col+1).get(row) == p && board.get(col+2).get(row) == p && board.get(col+3).get(row) == p)
            return 1;
        if (col >= 2 && col <=9 && board.get(col-1).get(row) == p && board.get(col-2).get(row) == p && board.get(col+1).get(row) == p)
            return 1;
        if (col >= 1 && col <=8 && board.get(col-1).get(row) == p && board.get(col+2).get(row) == p && board.get(col+1).get(row) == p)
            return 1;
        return 0;
    }

    private int threeVertical(Vector<Vector<Player> > board, Player p, int col, int row)
    {
        if (row < 3)
            return 0;
        else
        {
            if (board.get(col).get(row-1) == p && board.get(col).get(row-2) == p && board.get(col).get(row-3) == p)
                return 1;
        }
        return 0;
    }

    private int threeDiagRight(Vector<Vector<Player> > board,Player p, int col, int row)
    {
        if (col <= 7 && row <= 6 && board.get(col+1).get(row+1) == p && board.get(col+2).get(row+2) == p && board.get(col+3).get(row+3) == p)
            return 1;
        if (col <= 8 && row <= 7 && col >= 1 && row >= 1 && board.get(col-1).get(row-1) == p && board.get(col+2).get(row+2) == p && board.get(col+1).get(row+1) == p)
            return 1;
        if (col <= 9 && row <= 8 && col >= 2 && row >= 2 && board.get(col-1).get(row-1) == p && board.get(col-2).get(row-2) == p && board.get(col+1).get(row+1) == p)
            return 1;
        if (col >= 3 && row >= 3 && board.get(col-1).get(row-1) == p && board.get(col-2).get(row-2) == p && board.get(col-3).get(row-3) == p)
            return 1;
        return 0;
    }

    private int threeDiagLeft(Vector<Vector<Player> > board,Player p, int col, int row)
    {
        if (col >= 3 && row <= 6 && board.get(col-1).get(row+1) == p && board.get(col-2).get(row+2) == p && board.get(col-3).get(row+3) == p)
            return 1;
        if (col <= 9 && row <= 7 && col >= 2 && row >= 1 && board.get(col+1).get(row-1) == p && board.get(col-1).get(row+1) == p && board.get(col-2).get(row+2) == p)
            return 1;
        if (col <= 8 && row <= 8 && col >= 1 && row >= 2 && board.get(col+2).get(row-2) == p && board.get(col+1).get(row-1) == p && board.get(col-1).get(row+1) == p)
            return 1;
        if (col <= 7 && row >= 3 && board.get(col+1).get(row-1) == p && board.get(col+2).get(row-2) == p && board.get(col+3).get(row-3) == p)
            return 1;
        return 0;
    }

    private int twoHorizontal(Vector<Vector<Player> > board,Player p, int col, int row)
    {
        int numTwos = 0;
        Player a = Player.otherPlayer(p), b = Player.otherPlayer(p), c = Player.otherPlayer(p), d = Player.otherPlayer(p), e = Player.otherPlayer(p), f = Player.otherPlayer(p);
        if (col >= 3)
        {
            a = board.get(col-3).get(row);
        }
        if (col >= 2)
        {
            b = board.get(col-2).get(row);
        }
        if (col >= 1)
        {
            c = board.get(col-1).get(row);
        }
        if (col <= 9)
        {
            d = board.get(col+1).get(row);
        }
        if (col <= 8)
        {
            e = board.get(col+2).get(row);
        }
        if (col <= 7)
        {
            f = board.get(col+3).get(row);
        }

        if (a == p && b == p && c == Player.EMPTY)
            numTwos++;
        if (a == p && b == Player.EMPTY && c == p)
            numTwos++;
        if (a == Player.EMPTY && b == p && c == p)
            numTwos++;
        if (b == p && c == p && d == Player.EMPTY)
            numTwos++;
        if (b == p && c == Player.EMPTY && d == p)
            numTwos++;
        if (b == Player.EMPTY && c == p && d == p)
            numTwos++;
        if (c == p && d == p && e == Player.EMPTY)
            numTwos++;
        if (c == p && d == Player.EMPTY && e == p)
            numTwos++;
        if (c == Player.EMPTY && d == p && e == p)
            numTwos++;
        if (d == p && e == p && f == Player.EMPTY)
            numTwos++;
        if (d == p && e == Player.EMPTY && f == p)
            numTwos++;
        if (d == Player.EMPTY && e == p && f == p)
            numTwos++;

        return numTwos;
    }

    private int twoVertical(Vector<Vector<Player> > board,Player p, int col, int row)
    {
        if (row < 2)
            return 0;
        else
        {
            if (board.get(col).get(row-1) == p && board.get(col).get(row-2) == p)
                return 1;
        }
        return 0;
    }

    private int twoDiagRight(Vector<Vector<Player> > board,Player p, int col, int row)
    {
        int numTwos = 0;
        Player a = Player.otherPlayer(p), b = Player.otherPlayer(p), c = Player.otherPlayer(p), d = Player.otherPlayer(p), e = Player.otherPlayer(p), f = Player.otherPlayer(p);
        if (col >= 3 && row >= 3)
        {
            a = board.get(col-3).get(row-3);
        }
        if (col >= 2 && row >= 2)
        {
            b = board.get(col-2).get(row-2);
        }
        if (col >= 1 && row >= 1)
        {
            c = board.get(col-1).get(row-1);
        }
        if (col <= 9 && row <= 8)
        {
            d = board.get(col+1).get(row+1);
        }
        if (col <= 8 && row <= 7)
        {
            e = board.get(col+2).get(row+2);
        }
        if (col <= 7 && row <= 6)
        {
            f = board.get(col+3).get(row+3);
        }

        if (a == p && b == p && c == Player.EMPTY)
            numTwos++;
        if (a == p && b == Player.EMPTY && c == p)
            numTwos++;
        if (a == Player.EMPTY && b == p && c == p)
            numTwos++;
        if (b == p && c == p && d == Player.EMPTY)
            numTwos++;
        if (b == p && c == Player.EMPTY && d == p)
            numTwos++;
        if (b == Player.EMPTY && c == p && d == p)
            numTwos++;
        if (c == p && d == p && e == Player.EMPTY)
            numTwos++;
        if (c == p && d == Player.EMPTY && e == p)
            numTwos++;
        if (c == Player.EMPTY && d == p && e == p)
            numTwos++;
        if (d == p && e == p && f == Player.EMPTY)
            numTwos++;
        if (d == p && e == Player.EMPTY && f == p)
            numTwos++;
        if (d == Player.EMPTY && e == p && f == p)
            numTwos++;

        return numTwos;
    }

    private int twoDiagLeft(Vector<Vector<Player> > board,Player p, int col, int row)
    {
        int numTwos = 0;
        Player a = Player.otherPlayer(p), b = Player.otherPlayer(p), c = Player.otherPlayer(p), d = Player.otherPlayer(p), e = Player.otherPlayer(p), f = Player.otherPlayer(p);
        if (col >= 3 && row <= 6)
        {
            a = board.get(col-3).get(row+3);
        }
        if (col >= 2 && row <= 7)
        {
            b = board.get(col-2).get(row+2);
        }
        if (col >= 1 && row <= 8)
        {
            c = board.get(col-1).get(row+1);
        }
        if (col <= 9 && row >= 1)
        {
            d = board.get(col+1).get(row-1);
        }
        if (col <= 8 && row >= 2)
        {
            e = board.get(col+2).get(row-2);
        }
        if (col <= 7 && row >= 3)
        {
            f = board.get(col+3).get(row-3);
        }

        if (a == p && b == p && c == Player.EMPTY)
            numTwos++;
        if (a == p && b == Player.EMPTY && c == p)
            numTwos++;
        if (a == Player.EMPTY && b == p && c == p)
            numTwos++;
        if (b == p && c == p && d == Player.EMPTY)
            numTwos++;
        if (b == p && c == Player.EMPTY && d == p)
            numTwos++;
        if (b == Player.EMPTY && c == p && d == p)
            numTwos++;
        if (c == p && d == p && e == Player.EMPTY)
            numTwos++;
        if (c == p && d == Player.EMPTY && e == p)
            numTwos++;
        if (c == Player.EMPTY && d == p && e == p)
            numTwos++;
        if (d == p && e == p && f == Player.EMPTY)
            numTwos++;
        if (d == p && e == Player.EMPTY && f == p)
            numTwos++;
        if (d == Player.EMPTY && e == p && f == p)
            numTwos++;

        return numTwos;
    }

    /*private void weightUpdate()
    {
        try {
            FileWriter buf = new FileWriter("P:\\NetBeans Projects\\game\\src\\Weights.txt",false);
            int count = history.size()-1;
            DecimalFormat f = new DecimalFormat("##.##############");
            Vector<Integer> temp;
            double poscap = 10;
            while(count >= 0)
            {
                temp = history.get(count);
                for (int j = 0; j < temp.size(); j++)
                {
                    if (temp.get(j) > 0)
                        temp.set(j, 1);
                }
                for (int i = 0; i < temp.size(); i++)
                {
                    double tmp = weights.get(i)+(Math.pow((.9), history.size()-count)*(result*(temp.get(i)*Math.abs(weights.get(i))/100)));
                    if (i != 16)
                    {
                        if (tmp < poscap)
                            weights.set(i, tmp);
                        else
                            weights.set(i, poscap);
                    }
                    else
                    {
                        double min = weights.get(0);
                        for(int k = 1; k < 8; k++)
                        {
                            if (weights.get(k) < min)
                            {
                                min = weights.get(k);
                            }
                        }
                        if (min-.1 < Math.abs(tmp))
                            weights.set(i, (min*-1) + .1);
                        else
                            weights.set(i, tmp);
                    }

                }
                count--;
            }
            if (thePlayer == Player.ONE)
            {
                for (int j = 0; j < weights.size(); j++)
                {
                    if (j < weights.size()-1)
                        buf.write("" + f.format(weights.get(j)) +"\n");
                    else
                        buf.write("" + f.format(weights.get(j)));
                }
            }
            buf.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }*/

    private int doNotPlace(Vector<Vector<Player> > board,int col, int row)
    {
        if (row <= 8)
        {
            if (threeVertical(board,thePlayer,col,row+1) == 1)
                return 1;
            if (threeVertical(board,Player.otherPlayer(thePlayer),col,row+1) == 1)
                return 1;
            if (threeHorizontal(board,thePlayer,col,row+1) == 1)
                return 1;
            if (threeHorizontal(board,Player.otherPlayer(thePlayer),col,row+1) == 1)
                return 1;
            if (threeDiagRight(board,thePlayer,col,row+1) == 1)
                return 1;
            if (threeDiagRight(board,Player.otherPlayer(thePlayer),col,row+1) == 1)
                return 1;
            if (threeDiagLeft(board,thePlayer,col,row+1) == 1)
                return 1;
            if (threeDiagLeft(board,Player.otherPlayer(thePlayer),col,row+1) == 1)
                return 1;
        }
        return 0;
    }



    /*private void readWeights()
    {
        String line = "";
        int counter = 0;
        try
        {
            BufferedReader input = new BufferedReader(new FileReader("P:\\NetBeans Projects\\game\\src\\Weights.txt"));
            while ((line = input.readLine()) != null)
            {
                weights.set(counter, Double.parseDouble(line));
                counter++;
            }
            input.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }*/

    private int getOpponentMove(Vector<Vector<Player> > board)
    {

        Vector<Vector<Integer> > oppfeatures = new Vector<Vector<Integer> >(gameRules.numCols); //indexed by column number then feature,
                                          //we number features from highest priority to lowest priority
                                          //we number columns from the left to right

        //initialize oppfeatures to 0
	for(int c=0; c<gameRules.numCols; c++) {
	    Vector<Integer> col_vec=new Vector<Integer>(numFeatures);
	    for(int r=0; r<numFeatures; r++) {
		col_vec.add(0);
	    }
	    oppfeatures.add(col_vec);
        }

        for( int i = 0; i < gameRules.numCols; i++)
        {
            int row = getRowFromColumn(board,i);
            if (row >= gameRules.numRows)
            {
                oppfeatures.get(i).set(0, -1);
                oppfeatures.get(i).set(1, -1);
                oppfeatures.get(i).set(2, -1);
                oppfeatures.get(i).set(3, -1);
                oppfeatures.get(i).set(4, -1);
                oppfeatures.get(i).set(5, -1);
                oppfeatures.get(i).set(6, -1);
                oppfeatures.get(i).set(7, -1);
                oppfeatures.get(i).set(8, -1);
                oppfeatures.get(i).set(9, -1);
                oppfeatures.get(i).set(10, -1);
                oppfeatures.get(i).set(11, -1);
                oppfeatures.get(i).set(12, -1);
                oppfeatures.get(i).set(13, -1);
                oppfeatures.get(i).set(14, -1);
                oppfeatures.get(i).set(15, -1);
                oppfeatures.get(i).set(16, 1);
            }
            else
            {
                oppfeatures.get(i).set(0, threeVertical(board,thePlayer,i,row));
                oppfeatures.get(i).set(1, threeHorizontal(board,thePlayer, i, row));
                oppfeatures.get(i).set(2, threeDiagRight(board,thePlayer, i, row));
                oppfeatures.get(i).set(3, threeDiagLeft(board,thePlayer, i, row));
                oppfeatures.get(i).set(4, threeVertical(board,Player.otherPlayer(thePlayer), i, row));
                oppfeatures.get(i).set(5, threeHorizontal(board,Player.otherPlayer(thePlayer), i, row));
                oppfeatures.get(i).set(6, threeDiagRight(board,Player.otherPlayer(thePlayer), i, row));
                oppfeatures.get(i).set(7, threeDiagLeft(board,Player.otherPlayer(thePlayer), i, row));
                oppfeatures.get(i).set(8, twoVertical(board,thePlayer, i, row));
                oppfeatures.get(i).set(9, twoHorizontal(board,thePlayer, i, row));
                oppfeatures.get(i).set(10, twoDiagRight(board,thePlayer, i, row));
                oppfeatures.get(i).set(11, twoDiagLeft(board,thePlayer, i, row));
                oppfeatures.get(i).set(12, twoVertical(board,Player.otherPlayer(thePlayer), i, row));
                oppfeatures.get(i).set(13, twoHorizontal(board,Player.otherPlayer(thePlayer), i, row));
                oppfeatures.get(i).set(14, twoDiagRight(board,Player.otherPlayer(thePlayer), i, row));
                oppfeatures.get(i).set(15, twoDiagLeft(board,Player.otherPlayer(thePlayer), i, row));
                oppfeatures.get(i).set(16, doNotPlace(board,i, row));
            }
        }

        double[] colSums = new double[11];
        double max1 = -1000, max2 = -1000;
        int maxcol = -1;
        for (int col = 0; col < gameRules.numCols; col ++)
        {
            double sum = 0;
            for (int feat = 0; feat < numFeatures; feat++)
            {
                sum += oppfeatures.get(col).get(feat)*weights.get(feat);
            }
            colSums[col] = sum;
            if (sum >= max1)
            {
                max2 = max1;
                max1 = sum;
                maxcol = col;
            }
        }
        if (max1 - max2 < epsilon)
        {
            //System.out.println("Random Move made");
            maxcol = DropOurPiece(board,colSums, max1);
        }
        for (int i = 0; i < oppfeatures.get(maxcol).size(); i++)
        {
            //System.out.println(features.get(maxcol).get(i));
        }
        //history.add(features.get(maxcol));
        return maxcol;
    }

    private double[] getMove( Vector<Vector<Player> > board)
    {
        depth++;
        double[] colSums = new double[11];
        double[] retarray = new double[] {0.0,0.0};

        if (depth == 4)
        {
            depth--;
            return retarray;
        }

        if (movecounter > 52)
        {
            depth--;
            return retarray;
        }

        Vector<Vector<Integer> > features = new Vector<Vector<Integer> >(gameRules.numCols); //indexed by column number then feature,
                                          //we number features from highest priority to lowest priority
                                          //we number columns from the left to right

        //initialize oppfeatures to 0
	for(int c=0; c<gameRules.numCols; c++) {
	    Vector<Integer> col_vec=new Vector<Integer>(numFeatures);
	    for(int r=0; r<numFeatures; r++) {
		col_vec.add(0);
	    }
	    features.add(col_vec);
        }

        for( int i = 0; i < gameRules.numCols; i++)
        {
            int row = getRowFromColumn(board,i);
            if (row >= gameRules.numRows)
            {
                features.get(i).set(0, -1);
                features.get(i).set(1, -1);
                features.get(i).set(2, -1);
                features.get(i).set(3, -1);
                features.get(i).set(4, -1);
                features.get(i).set(5, -1);
                features.get(i).set(6, -1);
                features.get(i).set(7, -1);
                features.get(i).set(8, -1);
                features.get(i).set(9, -1);
                features.get(i).set(10, -1);
                features.get(i).set(11, -1);
                features.get(i).set(12, -1);
                features.get(i).set(13, -1);
                features.get(i).set(14, -1);
                features.get(i).set(15, -1);
                features.get(i).set(16, 1);
            }
            else
            {
                features.get(i).set(0, threeVertical(board,thePlayer,i,row));
                features.get(i).set(1, threeHorizontal(board,thePlayer, i, row));
                features.get(i).set(2, threeDiagRight(board,thePlayer, i, row));
                features.get(i).set(3, threeDiagLeft(board,thePlayer, i, row));
                features.get(i).set(4, threeVertical(board,Player.otherPlayer(thePlayer), i, row));
                features.get(i).set(5, threeHorizontal(board,Player.otherPlayer(thePlayer), i, row));
                features.get(i).set(6, threeDiagRight(board,Player.otherPlayer(thePlayer), i, row));
                features.get(i).set(7, threeDiagLeft(board,Player.otherPlayer(thePlayer), i, row));
                features.get(i).set(8, twoVertical(board,thePlayer, i, row));
                features.get(i).set(9, twoHorizontal(board,thePlayer, i, row));
                features.get(i).set(10, twoDiagRight(board,thePlayer, i, row));
                features.get(i).set(11, twoDiagLeft(board,thePlayer, i, row));
                features.get(i).set(12, twoVertical(board,Player.otherPlayer(thePlayer), i, row));
                features.get(i).set(13, twoHorizontal(board,Player.otherPlayer(thePlayer), i, row));
                features.get(i).set(14, twoDiagRight(board,Player.otherPlayer(thePlayer), i, row));
                features.get(i).set(15, twoDiagLeft(board,Player.otherPlayer(thePlayer), i, row));
                features.get(i).set(16, doNotPlace(board,i, row));
            }
        }

        //setFeatures(board);
        double max1 = -1000, max2 = -1000;
        int maxcol = -1;
        double base = 0.0;
        for (int col = 0; col < gameRules.numCols; col ++)
        {
            Vector<Vector<Player> > currentBoard = new Vector<Vector<Player> >(gameRules.numCols);
            for(int c=0; c<gameRules.numCols; c++) {
                Vector<Player> col_vec=new Vector<Player>(gameRules.numRows);
                for(int r=0; r<gameRules.numRows; r++) {
                    col_vec.add(board.get(col).get(r));
                }
                currentBoard.add(col_vec);
	    }
            if (getRowFromColumn(currentBoard,col) > 9)
                continue;
            currentBoard.get(col).set(getRowFromColumn(currentBoard,col), thePlayer);
            int opponentMove = getOpponentMove(currentBoard);
            currentBoard.get(opponentMove).set(getRowFromColumn(currentBoard,opponentMove), Player.otherPlayer(thePlayer));
            double sum = 0;
            for (int feat = 0; feat < numFeatures; feat++)
            {
                    sum += features.get(col).get(feat)*weights.get(feat);
            }
            sum += (gamma*getMove(currentBoard)[1]);
            colSums[col] = sum;
            if (sum >= max1)
            {
                max2 = max1;
                max1 = sum;
                maxcol = col;
            }
        }
        if (max1 - max2 < epsilon)
        {
            if (depth == 1)
                System.out.println("Random Move made");
            maxcol = DropOurPiece(board,colSums, max1);
        }
        history.add(features.get(maxcol));
        retarray[0] = (double)maxcol;
        retarray[1] = (double)colSums[maxcol];
        if (depth == 1)
        {
            for (int feat = 0; feat < numFeatures; feat++)
            {
                    base += features.get(maxcol).get(feat)*weights.get(feat);
            }
            double dif = retarray[1] - base;
            System.out.println(dif);
        }
        depth--;
        return retarray;
    }

    public int DropOurPiece(Vector<Vector<Player> > board, double[] colSums, double max)
    {
        double x;
        boolean invalid = true;
        int rand = -1;
        // Check if invalid row
        while (invalid == true)
        {
            // This function will calcuate the column where we will
            // drop our piece within the proper size of the board
            do
            {
                double u1 = Math.random();
                double u2 = Math.random();
                x =  -1 * Math.log(u1);
                if(u2 > Math.pow(Math.E, (x - 0.18 * Math.pow(x-5, 2)-(5-(25/9)))))
                    x = -1;
            }
            while(x < 0 || x > 10);
            rand = (int)Math.round(x);
            if ((getRowFromColumn(board,rand) > 9 || getRowFromColumn(board,rand) < 0) || colSums[rand] < max-epsilon)
                invalid = true;
            else
                invalid = false;
        }
        return rand;
    }

    public void play() throws IOException,ClassNotFoundException {
	//open socket and in/out streams
        int row;
	Socket sock = new Socket("localhost", Player.getSocketNumber(thePlayer));
	ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
	ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
	BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
	//get the game rules
	gameRules = (Rules) in.readObject();
        //create the board
	setupBoard();
	System.out.printf("Num Rows: %d, Num Cols: %d, Num Connect: %d\n", gameRules.numRows, gameRules.numCols, gameRules.numConnect);
	//start playing the game
	System.out.println("Waiting...");
	GameMessage mess = (GameMessage) in.readObject(); //wait for initial message
	int move;
	while(mess.win == Player.EMPTY) {
	    if(mess.move != -1) {
		System.out.printf("Player %s moves %d\n", Player.otherPlayer(thePlayer),mess.move);
                row = getRowFromColumn(globalboard,mess.move);
                globalboard.get(mess.move).set(row, Player.TWO);
                //showBoard();
	    }
	    System.out.println("Your move?");
            if (rand.nextDouble() < Epsilon && train == true)
            {
                double[] dumb = new double[] {0,0,0,0,0,0,0,0,0,0,0};
                move = DropOurPiece(globalboard,dumb,0);
                System.out.println("Exploration strategy e-greedy random move made");
            }
            else
            {
                move = (int)getMove(globalboard)[0];
            }
            movecounter++;
	    mess.move=move;
            row = getRowFromColumn(globalboard,mess.move);
            globalboard.get(mess.move).set(row, Player.ONE);
	    out.writeObject(mess);
	    mess = (GameMessage) in.readObject();
            //showBoard();
	}
	System.out.printf("Player %s wins.\n", mess.win);
        if(mess.win == thePlayer)
            result = 1;
        else
            result = -1;
        //weightUpdate();
        for(int i = 0; i < weights.size(); i++)
        {
            System.out.println(weights.get(i));
        }
	sock.close();
    }

    public static void main(String[] args) {
	if(args.length != 1) {
	    System.out.println("Usage:\n java MPLayer [1|2]");
	    System.exit(-1);
	}
	int which_player = Integer.parseInt(args[0]);
	Player p=null;
	if(which_player == 1) {
	    p = Player.ONE;
	}
	else if ( which_player == 2) {
	    p = Player.TWO;
	}
	else {
	    System.out.println("Usage:\n java MPLayer [1|2]");
	    System.exit(-1);
	}
            DomAssAgent2 me = new DomAssAgent2(p);
            me.initWeights();
            //me.readWeights();
            me.history = new ArrayList<Vector<Integer>>();
            try {
                me.play();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            } catch(ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
    }

}
