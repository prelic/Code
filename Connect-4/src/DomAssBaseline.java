import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class DomAssBaseline {
    Player thePlayer;

    public DomAssBaseline(Player p) {
	thePlayer=p;
    }

    final int numFeatures = 17;

    private static Rules gameRules;

    private Vector<Vector<Player> > board; //indexed by column number then row,
                                          //we number rows from the bottom
                                          //we number columns from the left

    private Vector<Vector<Integer> > features; //indexed by column number then feature,
                                          //we number features from highest priority to lowest priority
                                          //we number columns from the left to right

    private Vector<Double> weights; //indexed by features
                                    // Feature priority is order of weights

    private void setupBoard(){

	//create the board
	board=new Vector<Vector<Player> >(gameRules.numCols);
	for(int c=0; c<gameRules.numCols; c++) {
	    Vector<Player> col_vec=new Vector<Player>(gameRules.numRows);
	    for(int r=0; r<gameRules.numRows; r++) {
		col_vec.add(Player.EMPTY);
	    }
	    board.add(col_vec);
	}
    }

    private void initFeatures(){

	//initialize features to 0
	features=new Vector<Vector<Integer> >(gameRules.numCols);
	for(int c=0; c<gameRules.numCols; c++) {
	    Vector<Integer> col_vec=new Vector<Integer>(numFeatures);
	    for(int r=0; r<numFeatures; r++) {
		col_vec.add(0);
	    }
	    features.add(col_vec);
	}
    }

    private void initWeights(){

	//initialize features to 0
	weights=new Vector<Double>(numFeatures);
	for(int r=0; r<numFeatures; r++) {
            weights.add(0.0);
	}
    }

    private void showBoard() {
	Player p;
	StringBuffer sb=new StringBuffer(gameRules.numRows*(gameRules.numCols+1) + 1);
	for(int r=gameRules.numRows-1; r>=0; r--) {
	    for(int c=0; c<gameRules.numCols; c++) {
		p=board.get(c).get(r);
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

    private int getRowFromColumn(int col) {
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

    private void setFeatures()
    {
        initFeatures();
        for( int i = 0; i < gameRules.numCols; i++)
        {
            int row = getRowFromColumn(i);
            if (row >= gameRules.numRows)
                continue;
            features.get(i).set(0, threeVertical(thePlayer,i,row));
            features.get(i).set(1, threeHorizontal(thePlayer, i, row));
            features.get(i).set(2, threeDiagRight(thePlayer, i, row));
            features.get(i).set(3, threeDiagLeft(thePlayer, i, row));
            features.get(i).set(4, threeVertical(Player.otherPlayer(thePlayer), i, row));
            features.get(i).set(5, threeHorizontal(Player.otherPlayer(thePlayer), i, row));
            features.get(i).set(6, threeDiagRight(Player.otherPlayer(thePlayer), i, row));
            features.get(i).set(7, threeDiagLeft(Player.otherPlayer(thePlayer), i, row));
            features.get(i).set(8, twoVertical(thePlayer, i, row));
            features.get(i).set(9, twoHorizontal(thePlayer, i, row));
            features.get(i).set(10, twoDiagRight(thePlayer, i, row));
            features.get(i).set(11, twoDiagLeft(thePlayer, i, row));
            features.get(i).set(12, twoVertical(Player.otherPlayer(thePlayer), i, row));
            features.get(i).set(13, twoHorizontal(Player.otherPlayer(thePlayer), i, row));
            features.get(i).set(14, twoDiagRight(Player.otherPlayer(thePlayer), i, row));
            features.get(i).set(15, twoDiagLeft(Player.otherPlayer(thePlayer), i, row));
            features.get(i).set(16, doNotPlace(i, row));
        }
    }

    private int threeHorizontal(Player p, int col, int row)
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

    private int threeVertical(Player p, int col, int row)
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

    private int threeDiagRight(Player p, int col, int row)
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

    private int threeDiagLeft(Player p, int col, int row)
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

    private int twoHorizontal(Player p, int col, int row)
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

    private int twoVertical(Player p, int col, int row)
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

    private int twoDiagRight(Player p, int col, int row)
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

    private int twoDiagLeft(Player p, int col, int row)
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

    /*private int randomMove()
    {
        return 1;
    }*/

    private int doNotPlace(int col, int row)
    {
        if (row <= 8)
        {
            if (threeVertical(thePlayer,col,row+1) == 1)
                return 1;
            if (threeVertical(Player.otherPlayer(thePlayer),col,row+1) == 1)
                return 1;
            if (threeHorizontal(thePlayer,col,row+1) == 1)
                return 1;
            if (threeHorizontal(Player.otherPlayer(thePlayer),col,row+1) == 1)
                return 1;
            if (threeDiagRight(thePlayer,col,row+1) == 1)
                return 1;
            if (threeDiagRight(Player.otherPlayer(thePlayer),col,row+1) == 1)
                return 1;
            if (threeDiagLeft(thePlayer,col,row+1) == 1)
                return 1;
            if (threeDiagLeft(Player.otherPlayer(thePlayer),col,row+1) == 1)
                return 1;
        }
        return 0;
    }

    private int getMove()
    {
        int player2vert [] = {0,0,0,0,0,0,0,0,0,0,0};
        int doNotPlace [] = {0,0,0,0,0,0,0,0,0,0,0};
        int player1vert [] = {0,0,0,0,0,0,0,0,0,0,0};
        int twoThreatThem = -1;
        int twoThreatUs = -1;
        int threeThreatThem = -1;
        int diagRight2Them = -1;
        int diagRight2Us = -1;
        int diagRight3Them = -1;
        int diagLeft2Them = -1;
        int diagLeft2Us = -1;
        int diagLeft3Them = -1;
        int threat = -1;
        int rowincrease = -1;

        // Check Vertical Threats

        // Count across the columns
        for (int i = 0; i < 11; i++)
        {
            // Count up the rows
            for (int j = 0; j < 10; j++)
            {
                // if empty space break out of counting up rows loop and you have the number
                //   vertically for each of the players
                if(board.get(i).get(j) == Player.EMPTY)
                    break;
                else
                {
                    // If we find their piece reset our vertical count to 0 and increment
                    //   their vertical count
                    if(board.get(i).get(j) == Player.TWO)
                    {
                        player1vert[i] = 0;
                        player2vert[i] = player2vert[i] + 1;
                    }
                    // If we find our piece reset their vertical count to 0 and increment
                    //   our vertical count
                    if(board.get(i).get(j) == Player.ONE)
                    {
                        player2vert[i] = 0;
                        player1vert[i] = player1vert[i] + 1;
                    }
                }
            }
            if (player1vert[i] == 3)
            {
                if (getRowFromColumn(i) < 10)
                    return i;
            }
        }

        // Check Horizontal Threats

        // Count up the rows
        for (int i = 0; i < 10; i++)
        {
            // Count across the columns
            for (int j = 0; j < 8; j++)
            {
                int empty = 0;
                int us = 0;
                int them = 0;
                // Make the window
                for (int k = 0; k < 4; k++)
                {
                    // Empty Space
                    if (board.get(j+k).get(i) == Player.EMPTY)
                    {
                        empty++;
                        threat = j+k;
                    }
                    // Our Piece
                    else if(board.get(j + k).get(i) == Player.ONE)
                        us++;
                    // Their Piece
                    else
                        them++;
                }
                // Case No Threat - Horizontal
                if ((us > 0 && them > 0) || (empty > 2))
                {
                }
                // Case 3 Threat - Horizontally
                else if(empty == 1)
                {
                    // We Are Domination Association Hear Us ROAR!!!
                    if (us > 0)
                    {
                        if (i > 0)
                            if (board.get(threat).get(i-1) != Player.EMPTY)
                                return threat;
                            else
                            {
                                if (i > 1)
                                    if (board.get(threat).get(i-2) != Player.EMPTY)
                                        doNotPlace[threat] = 1;
                                    else
                                    {
                                    }
                                else
                                    doNotPlace[threat] = 1;
                            }
                        else
                            return threat;
                    }
                    // They have a three threat, KILL THEM!!!
                    if (them > 0 && threeThreatThem == -1)
                    {
                        if (i > 0)
                            if (board.get(threat).get(i-1) != Player.EMPTY)
                                threeThreatThem = threat;
                            else
                            {
                                if (i > 1)
                                    if (board.get(threat).get(i-2) != Player.EMPTY)
                                        doNotPlace[threat] = 1;
                                    else
                                    {
                                    }
                                else
                                    doNotPlace[threat] = 1;
                            }
                        else
                            threeThreatThem = threat;
                    }
                }
                // Case 2 Threat - Horizontals
                else if(empty == 2)
                {
                    // We have a 2 threat horizontally
                    if (us > 0)
                    {
                        if (i > 0)
                            if (board.get(threat).get(i-1) != Player.EMPTY)
                                twoThreatUs = threat;
                            else
                            {
                            }
                        else
                            twoThreatUs = threat;
                    }
                    // They have a 2 threat horizontally
                    if (them > 0 && twoThreatThem == -1)
                    {
                        if (i > 0)
                            if (board.get(threat).get(i-1) != Player.EMPTY)
                                twoThreatThem = threat;
                            else
                            {
                            }
                        else
                             twoThreatThem = threat;
                    }
                }
            }
        }

        // Check Diagonally Positive

        // Counting up the rows
        for (int i = 0; i < 7; i++)
        {
            // Counting across the columns
            for (int j = 0; j < 8; j++)
            {
                int empty = 0, us = 0, them = 0;
                // Make the Window
                for (int k = 0; k < 4; k++)
                {
                    // Empty spaces
                    if (board.get(j+k).get(i+k) == Player.EMPTY)
                    {
                        empty++;
                        threat = j+k;
                        rowincrease = k;
                    }
                    // Our piece
                    else if(board.get(j + k).get(i + k) == Player.ONE)
                        us++;
                    // Their piece
                    else
                        them++;
                }
                 // Case No Threat - Dont do anything
                if ((us > 0 && them > 0) || (empty > 2))
                {
                }
                // Case 3 Threat - We can win or fail the choice is ours
                else if(empty == 1)
                {
                    // Our 3 threat - We win
                    if (us > 0)
                    {
                        if (i+rowincrease > 0)
                            if (board.get(threat).get(i-1+rowincrease) != Player.EMPTY)
                                return threat;
                            else
                            {
                                if (i > 1)
                                    if (board.get(threat).get(i-2+rowincrease) != Player.EMPTY)
                                        doNotPlace[threat] = 1;
                                    else
                                    {
                                    }
                                else
                                    doNotPlace[threat] = 1;
                            }
                        else
                             return threat;
                    }
                    // Their 3 threat - block or we lose
                    if (them > 0 && diagRight3Them == -1)
                    {
                        if (i+rowincrease > 0)
                            if (board.get(threat).get(i-1+rowincrease) != Player.EMPTY)
                                diagRight3Them = threat;
                            else
                            {
                                if (i > 1)
                                    if (board.get(threat).get(i-2+rowincrease) != Player.EMPTY)
                                        doNotPlace[threat] = 1;
                                    else
                                    {
                                    }
                                else
                                    doNotPlace[threat] = 1;
                            }
                        else
                             diagRight3Them = threat;
                    }
                }
                // Case 2 Threat - Us or them that is the question
                else if(empty == 2)
                {
                    // Our 2 threat
                    if (us > 0)
                    {
                        if (i+rowincrease > 0)
                            if (board.get(threat).get(i-1+rowincrease) != Player.EMPTY)
                                diagRight2Us = threat;
                            else
                            {
                            }
                        else
                             diagRight2Us = threat;
                    }
                    // Their 2 threat
                    if (them > 0 && diagRight2Them == -1)
                    {
                        if (i+rowincrease > 0)
                            if (board.get(threat).get(i-1+rowincrease) != Player.EMPTY)
                                diagRight2Them = threat;
                            else
                            {
                            }
                        else
                             diagRight2Them = threat;
                    }
                }
            }
        }

        // Check Diagonally Negative

        // Counting up rows
        for (int i = 0; i < 7; i++)
        {
            // Counting across columns
            for (int j = 3; j < 11; j++)
            {
                int empty = 0, us = 0, them = 0;
                // Make the window
                for (int k = 0; k < 4; k++)
                {
                    // Find the empty spaces
                    if (board.get(j-k).get(i+k) == Player.EMPTY)
                    {
                        empty++;
                        threat = j-k;
                        rowincrease = k;
                    }
                    // Find our pieces
                    else if(board.get(j - k).get(i + k) == Player.ONE)
                        us++;
                    // Find their pieces
                    else
                        them++;
                }
                 // Case No Threat - There is no need for us to block
                if ((us > 0 && them > 0) || (empty > 2))
                {
                }
                // Case 3 Threat - If there's a three threat find it
                else if(empty == 1)
                {
                    // Our three threat return and WIN!!!!
                    if (us > 0)
                    {
                        if (i+rowincrease > 0)
                            if (board.get(threat).get(i-1+rowincrease) != Player.EMPTY)
                                return threat;
                            else
                            {
                                if (i > 1)
                                    if (board.get(threat).get(i-2+rowincrease) != Player.EMPTY)
                                        doNotPlace[threat] = 1;
                                    else
                                    {
                                    }
                                else
                                    doNotPlace[threat] = 1;
                            }
                        else
                             return threat;
                    }
                    // Their three threat shit Shit SHIT!!! Block them!
                    if (them > 0 && diagLeft3Them == -1)
                    {
                        if (i+rowincrease > 0)
                            if (board.get(threat).get(i-1+rowincrease) != Player.EMPTY)
                                diagLeft3Them = threat;
                            else
                            {
                                if (i > 1)
                                    if (board.get(threat).get(i-2+rowincrease) != Player.EMPTY)
                                        doNotPlace[threat] = 1;
                                    else
                                    {
                                    }
                                else
                                    doNotPlace[threat] = 1;
                            }
                        else
                             diagLeft3Them = threat;
                    }
                }
                // Case 2 Threat - Ours gives us a 3 threat, theirs will give them a 3 threat
                else if(empty == 2)
                {
                    // Our 2 threat
                    if (us > 0)
                    {
                        if (i+rowincrease > 0)
                            if (board.get(threat).get(i-1+rowincrease) != Player.EMPTY)
                                diagLeft2Us = threat;
                            else
                            {
                            }
                        else
                             diagLeft2Us = threat;
                    }
                    // Their 2 threat
                    if (them > 0 && diagLeft2Them == -1)
                    {
                        if (i+rowincrease > 0)
                            if (board.get(threat).get(i-1+rowincrease) != Player.EMPTY)
                                diagLeft2Them = threat;
                            else
                            {
                            }
                        else
                             diagLeft2Them = threat;
                    }
                }
            }
        }

        /* Decide What to do Next
         *
         * Psuedo:
         *  Do their three threat, then our 2 threat, then their 2 threat
         */

        /* PRIORITY
         *
         * ANY 3 OF OURS
         * THEIR 3 HORIZONTAL
         * THEIR 3 VERTICAL
         * THEIR 3 DIAGONAL
         * OUR 2 HORIZONTAL
         * THEIR 2 HORIZONTAL
         * OUR 2 HORIZONTAL
         * OUR 2 DIAGONAL
         * THEIR 2 VERTICAL
         * THEIR 2 DIAGONAL
        */

        int returnposition = -1;
        //their 2 diagonal
        if(diagRight2Them != -1)
            returnposition =  diagRight2Them;
        else if(diagLeft2Them != -1)
            returnposition =  diagLeft2Them;

        //their 2 vertical
        for(int i = 0; i < 11; i++)
        {
            if(player2vert[i] == 2)
            {
                returnposition = i;
            }
        }

        //our 2 diagonal
        if(diagRight2Us != -1)
            returnposition =  diagRight2Us;
        else if(diagLeft2Us != -1)
            returnposition =  diagLeft2Us;

        //our 2 vertical
        for(int i = 0; i < 11; i++)
        {
            if(player1vert[i] == 2)
            {
		returnposition =  i;
            }
        }

        //their 2 horizontal
        if(twoThreatThem != -1)
            returnposition =  twoThreatThem;
        //our 2 horizontal
        else if(twoThreatUs != -1)
            returnposition =  twoThreatUs;

        //their 3 diag
        if(diagLeft3Them != -1)
            returnposition =  diagLeft3Them;
        else if(diagRight3Them != -1)
            returnposition =  diagRight3Them;



        //they have a horizontal 3 threat
        if(threeThreatThem != -1)
            returnposition =  threeThreatThem;

         //their 3 vert
        for(int i = 0; i < 11; i++)
        {
            if(player2vert[i] == 3)
		returnposition =  i;
        }

        if(returnposition == -1 || getRowFromColumn(returnposition) == 10)
        {
            //invalid
        }
        else if (doNotPlace[returnposition] != 1)
            return returnposition;


        /* Default Move - Go along a bell curve
         *
         * Pseudo:
         *  Calculate a bell curve, and randomly drop based on those probabilities
         */

         int position = DropOurPiece();
         /*do
         {
             position = DropOurPiece();
         }
         while (doNotPlace[position] == 1);*/
         return position;
    }

    private void readWeights()
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
    }

    /*private int getMove()
    {
        int[] colSums = new int[11];
        setFeatures();
        int max1 = -1, max2 = -1;
        int maxcol = -1;
        for (int col = 0; col < gameRules.numCols; col ++)
        {
            int sum = 0;
            for (int feat = 0; feat < numFeatures; feat++)
            {
                sum += features.get(col).get(feat)*weights.get(feat);
            }
            colSums[col] = sum;
            if (sum >= max1)
            {
                max2 = max1;
                max1 = sum;
                maxcol = col;
            }
        }
        if (max1 - max2 < 1)
        {
            System.out.println("Random Move made");
            return DropOurPiece(colSums, max1);
        }
        return maxcol;
    }*/

    public int DropOurPiece()
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
                if(u2 > Math.pow(Math.E, (x - 0.18 * Math.pow(x-5, 2)-(5-(25/18)))))
                    x = -1;
            }
            while(x < 0 || x > 10);
            rand = (int)Math.round(x);
            if ((getRowFromColumn(rand) > 9 || getRowFromColumn(rand) < 0))
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
                row = getRowFromColumn(mess.move);
                board.get(mess.move).set(row, Player.TWO);
                showBoard();
	    }
	    System.out.println("Your move?");
	    move = getMove();
	    mess.move=move;
            row = getRowFromColumn(mess.move);
            board.get(mess.move).set(row, Player.ONE);
	    out.writeObject(mess);
	    mess = (GameMessage) in.readObject();
            showBoard();
	}
	System.out.printf("Player %s wins.\n", mess.win);
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
	DomAssBaseline me = new DomAssBaseline(p);
        //me.initWeights();
        //me.readWeights();
	try {
	    me.play();
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	} catch(ClassNotFoundException cnfe) {
	    cnfe.printStackTrace();
	}

    }

}
