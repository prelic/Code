import java.io.*;
import java.net.*;
import java.util.Random;

public class AIPlayer {
    Player thePlayer;

    public AIPlayer(Player p) {
	thePlayer=p;
    }

    public int[] single_counter(int[][] board, int num_rows, int num_cols, 
                       int cur_row, int cur_col, int off_row, int off_col)
    // Input: board setup, the number of rows and columns of the board,
    // the current position to be inspected and the amount of offset of that
    // position to use.
    // Output: Array of 3 integers.
    // The first element is how many chips of the same player are in a row
    // The second element is how many moves can be made now to increase
    // The third element is how many moves can be made in the future to increase
    {
        int[] output = {0, 0, 0};   // Array to be returned
        int temp_row = cur_row;     // Set a temp_row which will be manipulated
        int temp_col  = cur_col;    // Set a temp_col which will be manipulated
        int num_open = 0;           // Holds how many open spots have been found
        int opposite;               // Holds the number of the opponent
        int in_row;                 // Holds the current amount in row
        int max_in_row;             // Holds the max amount in row found

        max_in_row = 0;             // Initalize to 0
        in_row = 0;                 // Initalize to 0

        // If the position to be inspected isn't held by a player return 0's
        if (board[cur_row][cur_col] <= 0)
            return output;

        // Find out who the opponent is
        if (board[cur_row][cur_col] == 1)
            opposite = 2;
        else
            opposite = 1;
        
        output[0] += 1;     // Increment output[0] by 1
        in_row += 1;        // Increment current amount in row by 1
        
        // While the future board position is valid and it is of the same player
        while (temp_row >= 0 && temp_col >= 0 && 
               temp_row < num_rows && temp_col < num_cols &&
               board[temp_row][temp_col] != opposite && num_open < 2)
        {
            // Offset the board position
            temp_row += off_row;
            temp_col += off_col;
            
            // If it is still valid
            if (temp_row >= 0 && temp_col >= 0 &&
                temp_row < num_rows && temp_col < num_cols)
            {
                // Increment the appropriate counters
                if (board[temp_row][temp_col] == board[cur_row][cur_col])
                {
                    output[0] += 1;
                    in_row += 1;
                }
                else if (board[temp_row][temp_col] == -1)
                {
                    if (num_open == 0)
                        output[1] += 1;

                    num_open++;

                    // Find out if max_in_row needs to be updated
                    if (in_row > max_in_row)
                    {
                        max_in_row = in_row;
                    }

                    in_row = 0;     // Reset in_row to 0
                }
                else if (board[temp_row][temp_col] == 0)
                {
                    if (num_open == 0)
                        output[2] += 1;

                    num_open++;

                    // Find out if max_in_row needs to be updated
                    if (in_row > max_in_row)
                    {
                        max_in_row = in_row;
                    }

                    in_row = 0; // Reset in_row to 0
                }
            }
        }
        
        // Check to see if the other side is open
        temp_row = cur_row - off_row;
        temp_col = cur_col - off_col;
        if (temp_row >= 0 && temp_col >= 0 && 
            temp_row < num_rows && temp_col < num_cols)
        {
            if (board[temp_row][temp_col] == 0)
                output[2] += 1;
            else if (board[temp_row][temp_col] == -1)
                output[1] += 1;
        }

        // Check to see if max_in_row needs to be updated
        if (in_row > max_in_row)
        {
            max_in_row = in_row;
        }

        // Determine if we should have output[0] = max_in_row
        if (max_in_row <= 3 && output[0] > 3)
        {
            output[0] = max_in_row;
        }

        // Return output
        return output;     
    }
    
    public int[][][] full_counter(int[][] board, int num_rows, int num_cols)
    // Input: Board setup, number of rows and columns board has
    // Output: 3D array
    // Dimension 1 identifies the player to counter refers to
    // (0 = p1, 1 = p2)
    // Dimension 2 identifies the type the counter refers to
    // (0 = now, 1 = future)
    // Dimension 3 identifies the amount the counter refers to
    // (0 = spot creates 2 in row, 1 = spot creates 3 in row, 
    // 2 = spot creates 4 in row)
    {
        int[][][] output = new int[2][2][3]; // Array to be returned
        int[] single_holder = new int[3]; // Array to hold output from single_counter
        
        // Initialize all elements to 0
        for (int player_i = 0; player_i < 2; player_i++)
        {
            for (int type_i = 0; type_i < 2; type_i++)
            {
                for (int amount_i = 0; amount_i < 3; amount_i++)
                {
                    output[player_i][type_i][amount_i] = 0;
                }
            }
        }
        
        for (int row_i = 0; row_i < num_rows; row_i++)
        {
            for (int col_i = 0; col_i < num_cols; col_i++)
            {
                // If the position is occupied
                if (board[row_i][col_i] > 0)
                {
                    // Determine if vertical should be checked
                    if ((row_i - 1) >= 0 && 
                         board[row_i - 1][col_i] != board[row_i][col_i])
                    {
                        // Check the vertical
                        single_holder = single_counter(board, num_rows, num_cols,
                                row_i, col_i, 1, 0);
                        
                        // If the game is over return array full of negative
                        // of the winning player's number
                        if (single_holder[0] > 3)
                        {
                            for (int player_i = 0; player_i < 2; player_i++)
                            {
                                for (int type_i = 0; type_i < 2; type_i++)
                                {
                                    for (int amount_i = 0; amount_i < 3; amount_i++)
                                    {
                                        output[player_i][type_i][amount_i] = -(board[row_i][col_i]);
                                    }
                                }
                            }

                            return output;

                        }

                        // Update output array
                        output[board[row_i][col_i] - 1][0][single_holder[0] - 1] += single_holder[1];
                        output[board[row_i][col_i] - 1][1][single_holder[0] - 1] += single_holder[2];
                    }
                    
                    // Determine if horizontal should be checked
                    if ((col_i - 1) >= 0 &&
                         board[row_i][col_i - 1] != board[row_i][col_i])
                    {
                        // Check the horizontal
                        single_holder = single_counter(board, num_rows, num_cols,
                                row_i, col_i, 0, 1);

                        // If the game is over return array full of negative
                        // of the winning player's number
                        if (single_holder[0] > 3)
                        {
                            for (int player_i = 0; player_i < 2; player_i++)
                            {
                                for (int type_i = 0; type_i < 2; type_i++)
                                {
                                    for (int amount_i = 0; amount_i < 3; amount_i++)
                                    {
                                        output[player_i][type_i][amount_i] = -(board[row_i][col_i]);
                                    }
                                }
                            }

                            return output;
                        }

                        // Update output array
                        output[board[row_i][col_i] - 1][0][single_holder[0] - 1] += single_holder[1];
                        output[board[row_i][col_i] - 1][1][single_holder[0] - 1] += single_holder[2];
                    }
                    
                    // Determine if the top left to bottom right diagonal should be checked
                    if ((col_i - 1) >= 0 && (row_i - 1) >= 0 && 
                         board[row_i - 1][col_i - 1] != board[row_i][col_i])
                    {
                        single_holder = single_counter(board, num_rows, num_cols,
                                row_i, col_i, 1, 1);

                        // If the game is over return array full of negative
                        // of the winning player's number
                        if (single_holder[0] > 3)
                        {
                            for (int player_i = 0; player_i < 2; player_i++)
                            {
                                for (int type_i = 0; type_i < 2; type_i++)
                                {
                                    for (int amount_i = 0; amount_i < 3; amount_i++)
                                    {
                                        output[player_i][type_i][amount_i] = -(board[row_i][col_i]);
                                    }
                                }
                            }

                            return output;
                        }

                        // Update output array
                        output[board[row_i][col_i] - 1][0][single_holder[0] - 1] += single_holder[1];
                        output[board[row_i][col_i] - 1][1][single_holder[0] - 1] += single_holder[2];
                    }
                    
                    // Determine if the bottom left to top right diagonal should be checked
                    if ((col_i + 1) < num_cols && (row_i - 1) >= 0 &&
                         board[row_i - 1][col_i + 1] != board[row_i][col_i])
                    {
                        single_holder = single_counter(board, num_rows, num_cols,
                                row_i, col_i, 1, -1);

                        // If the game is over return array full of negative
                        // of the winning player's number
                        if (single_holder[0] > 3)
                        {
                            for (int player_i = 0; player_i < 2; player_i++)
                            {
                                for (int type_i = 0; type_i < 2; type_i++)
                                {
                                    for (int amount_i = 0; amount_i < 3; amount_i++)
                                    {
                                        output[player_i][type_i][amount_i] = -(board[row_i][col_i]);
                                    }
                                }
                            }

                            return output;
                        }

                        // Update output array
                        output[board[row_i][col_i] - 1][0][single_holder[0] - 1] += single_holder[1];
                        output[board[row_i][col_i] - 1][1][single_holder[0] - 1] += single_holder[2];
                    }
                }
            }
        }

        // Return output array
        return output;
    }

    public double board_rating(int[][] board, int num_rows, int num_cols, int player)
    {
        int[][][] full_holder = new int[2][2][3];   // Holds the full_counter output

        double rating = 0;      // Holds the rating to be returned
        int win_count = 0;      // Holds how many winning positions are available

        // Fill full_holder
        full_holder = full_counter(board, num_rows, num_cols);

        // Check if the board is a finished game
        if (full_holder[0][0][0] == -(player))
        {
            return 7000;    // This means the player won
        }
        else if (full_holder[0][0][0] < 0 && full_holder[0][0][0] != -(player))
        {
            return -5000;   // This means the player lost
        }

        // Iterate through full_holder
        for (int player_i = 0; player_i < 2; player_i++)
        {
            for (int type_i = 0; type_i < 2; type_i++)
            {
                for (int amount_i = 0; amount_i < 3; amount_i++)
                {
                    // Update rating according to full_holder numbers
                    if ((player_i + 1) == player)
                    {
                        if (type_i == 0 && amount_i == 0)
                            rating += 1 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 0 && amount_i == 1)
                            rating += 5 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 0 && amount_i == 2)
                        {
                            win_count += full_holder[player_i][type_i][amount_i];
                            rating += 20 * full_holder[player_i][type_i][amount_i];
                        }
                        else if (type_i == 1 && amount_i == 0)
                            rating += .5 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 1 && amount_i == 1)
                            rating += 2.5 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 1 && amount_i == 2)
                            rating += 10 * full_holder[player_i][type_i][amount_i];
                    }
                    else
                    {
                        if (type_i == 0 && amount_i == 0)
                            rating -= 1.5 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 0 && amount_i == 1)
                            rating -= 5.5 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 0 && amount_i == 2)
                            rating -= 4000 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 1 && amount_i == 0)
                            rating -= .75 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 1 && amount_i == 1)
                            rating -= 2.75 * full_holder[player_i][type_i][amount_i];
                        else if (type_i == 1 && amount_i == 2)
                            rating -= 10.25 * full_holder[player_i][type_i][amount_i];
                    }
                }
            }
        }

        // If there is two or more winning positions increment the rating by 1000
        if (win_count > 1)
            rating += 1000;

        // Return rating
        return rating;
    }

    private double make_move(int[][] board, int num_rows, int num_cols, int num_moves_made, int player)
    {
        int best_col = 0;                       // Holds oldest best column
        double[] best_cols = new double[num_cols];    // Holds all best columns (1 = best, 0 = not best)
        double best_rating;                     // Holds the current best rating
        double temp_rating;                     // Used to hold temporary ratings
        int[][] temp_board = new int[num_rows][num_cols];   // Used to manipulate board
        double min_rating;                      // Minimum rating to be randomed into
        Random random_gen = new Random();       // Gives random numbers
        int temp_col;                           // Temporary column used when randomizing

        // Set the starting base rating at the appropriate extreme
        if (num_moves_made == 1)
            best_rating = 10000;
        else
            best_rating = -10000;

        // Initialize all best_cols to 0
        for (int a = 0; a < num_cols; a++)
        {
            best_cols[a] = -10000;
        }

        // Copy board into temp_board
        for (int a = 0; a < num_rows; a++)
        {
            for (int b = 0; b < num_cols; b++)
            {
                temp_board[a][b] = board[a][b];
            }
        }

        // Check if this is the move to return column rather than rating
        if (num_moves_made == 0)
        {
            // Copy board into temp_board
            for (int col_i = 0; col_i < num_cols; col_i++)
            {
                for (int a = 0; a < num_rows; a++)
                {
                    for (int b = 0; b < num_cols; b++)
                    {
                        temp_board[a][b] = board[a][b];
                    }
                }
                // Observe all possible moves
                for (int row_i = 0; row_i < num_rows; row_i++)
                {
                    if (board[row_i][col_i] == -1)
                    {
                        temp_board[row_i][col_i] = player;
                        if ((row_i - 1) >= 0)
                            temp_board[row_i - 1][col_i] = -1;

                        // Checks to see if this move is a win and if so just goes there
                        if (board_rating(temp_board, num_rows, num_cols, player) == 7000)
                            return col_i;

                        // Look at next move (this is the first step of looking ahead)
                        if (player == 1)
                            temp_rating = make_move(temp_board, num_rows, num_cols, num_moves_made + 1, 2);
                        else
                            temp_rating = make_move(temp_board, num_rows, num_cols, num_moves_made + 1, 1);

                        best_cols[col_i] = temp_rating;

                        if (temp_rating > best_rating)
                        {
                            best_rating = temp_rating;
                            best_col = col_i;

                        }
                    }
                }
            }

            if (best_rating  > 0)
                min_rating = best_rating * .95;
            else
                min_rating = best_rating * 1.05;
            
            best_col = -1;

            while (best_col == -1)
            {
                temp_col = random_gen.nextInt(num_cols);
                if (best_cols[temp_col] >= min_rating)
                    best_col = temp_col;
            }

            return best_col;
        }
        // Check if this is the last move to look at
        else if (num_moves_made == 2)
        {
            for (int col_i = 0; col_i < num_cols; col_i++)
            {
                // Copy board into temp_board
                for (int a = 0; a < num_rows; a++)
                {
                    for (int b = 0; b < num_cols; b++)
                    {
                        temp_board[a][b] = board[a][b];
                    }
                }
                // Check all possible moves
                for (int row_i = 0; row_i < num_rows; row_i++)
                {
                    if (board[row_i][col_i] == -1)
                    {
                        temp_board[row_i][col_i] = player;
                        if ((row_i - 1) >= 0)
                            temp_board[row_i - 1][col_i] = -1;

                        // Find the rating for that move
                        temp_rating = board_rating(temp_board, num_rows, num_cols, player);

                        best_cols[col_i] = temp_rating;

                        if (temp_rating > best_rating)
                        {
                            best_rating = temp_rating;
                            best_col = col_i;

                        }
                    }
                }
            }

            // Check if you already lost
            if (player == 1)
            {
                if (board_rating(board, num_rows, num_cols, 2) > 3000)
                {
                    return -5000;
                }
            }
            else
            {
                if (board_rating(board, num_rows, num_cols, 1) > 3000)
                {
                    return  -5000;
                }
            }

            // Return rating
            return best_rating;
        }
        // Otherwise this is the opponents move (minization)
        else
        {
            for (int col_i = 0; col_i < num_cols; col_i++)
            {
                // Copy board into temp_board
                for (int a = 0; a < num_rows; a++)
                {
                    for (int b = 0; b < num_cols; b++)
                    {
                        temp_board[a][b] = board[a][b];
                    }
                }
                // Check all moves
                for (int row_i = 0; row_i < num_rows; row_i++)
                {
                    if (board[row_i][col_i] == -1)
                    {
                        temp_board[row_i][col_i] = player;
                        if ((row_i - 1) >= 0)
                            temp_board[row_i - 1][col_i] = -1;

                        // Checks to see if this move is a win and if so just goes there
                        if (board_rating(temp_board, num_rows, num_cols, player) == 7000)
                            return -board_rating(temp_board, num_rows, num_cols, player);

                        // This looks ahead one last move
                        if (player == 1)
                            temp_rating = make_move(temp_board, num_rows, num_cols, num_moves_made + 1, 2);
                        else
                            temp_rating = make_move(temp_board, num_rows, num_cols, num_moves_made + 1, 1);

                        best_cols[col_i] = temp_rating;

                        if (temp_rating < best_rating)
                        {
                            best_col = col_i;
                            best_rating = temp_rating;
                        }
                    }
                }
            }

            return best_rating;
        }
    }

    public void play() throws IOException,ClassNotFoundException {
	//open socket and in/out streams
	Socket sock = new Socket("localhost", Player.getSocketNumber(thePlayer));
	ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
	ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
	BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
	//get the game rules
	Rules gameRules = (Rules) in.readObject();
	System.out.printf("Num Rows: %d, Num Cols: %d, Num Connect: %d\n", gameRules.numRows, gameRules.numCols, gameRules.numConnect);
	//start playing the game
	System.out.println("Waiting...");
	GameMessage mess = (GameMessage) in.readObject(); //wait for initial message
        int player_num;
        int opp_num;

        // Find out what number we are and what number opponent is
        if (Player.otherPlayer(thePlayer).equals(Player.TWO))
        {
            player_num = 1;
            opp_num = 2;
        }
        else
        {
            player_num = 2;
            opp_num = 1;
        }

        int[][] my_board = new int[gameRules.numRows][gameRules.numCols];

        // Initialize my_board
        for (int a = 0; a < gameRules.numRows; a++)
        {
            for (int b = 0; b < gameRules.numCols; b++)
            {
                my_board[a][b] = 0;

                if (a == gameRules.numRows - 1)
                    my_board[a][b] = -1;
            }
        }

	int move;
	while(mess.win == Player.EMPTY) {
	    if(mess.move != -1)
            {
		System.out.printf("Player %s moves %d\n", Player.otherPlayer(thePlayer),mess.move);

                // Update my_board
                for(int a = 0; a < gameRules.numRows; a++)
                {
                    if (my_board[a][mess.move] == -1)
                    {
                        my_board[a][mess.move] = opp_num;
                        if ((a - 1) >= 0)
                            my_board[a - 1][mess.move] = -1;
                    }
                }
	    }
	    System.out.println("Your move?");
            // Find move
            move = (int)make_move(my_board, gameRules.numRows, gameRules.numCols, 0, player_num);
            // Update my_board
            for(int a = 0; a < gameRules.numRows; a++)
            {
                if (my_board[a][move] == -1)
                {
                    my_board[a][move] = player_num;
                    if ((a - 1) >= 0)
                        my_board[a - 1][move] = -1;
                }
            }
	    mess.move=move;
	    out.writeObject(mess);
	    mess = (GameMessage) in.readObject();
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
	AIPlayer me = new AIPlayer(p);
	try {
	    me.play();
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	} catch(ClassNotFoundException cnfe) {
	    cnfe.printStackTrace();
	}

    }

}
