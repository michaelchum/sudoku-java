import java.util.*;
import java.io.*;


class Sudoku
{
    /* SIZE is the size parameter of the Sudoku puzzle, and N is the square of the size.  For 
     * a standard Sudoku puzzle, SIZE is 3 and N is 9. */
    static int SIZE, N; // Making these variables static gives runs about 3ms faster in veryHard5x5.txt

    /* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
     * not yet been revealed are stored as 0. */
    int Grid[][];

    // Grid of the same size as Grid, true if cell is solved, false if cell is not solved
	boolean[][] gridCellSolved;

	// Track the number of cells solved
	int numberCellSolved = 0;

	// Used for Random Guess algorithm
	Random generator;

	// Grid of the same size as Grid, constains domains of all the possible valid numbers for each cell
	Domain[][] domains;

	// Check if a value k is valid in the cell (row, col) returns true if valid
	boolean checkValid(int row, int col, int k){
		// Check if k is present in the column
		for (int c=0; c<N; c++)
			if (Grid[row][c] == k) return false;
		// Check if k is present in the row
		for (int r=0; r<N; r++)
			if (Grid[r][col] == k) return false;
		// Check if k is present in the box
		int rBox = row/SIZE;
		int cBox = col/SIZE;
		for (int r=SIZE*rBox; r < SIZE*rBox+SIZE; r++)
		for (int c=SIZE*cBox; c < SIZE*cBox+SIZE; c++)
		if (Grid[r][c] == k) return false;
		return true;
	}

	// A domain object contains all the possible valid values for a cell
	private class Domain {
		public int[] ass = new int[N];
		public int numberAss = 0;
	}

	// Get all the possible values for a cell and insert in domain
	private Domain getDomain(int r, int c) {
		Domain d = new Domain();
		for (int k=1; k<=N; k++)
			if (checkValid(r,c,k)) {
				d.ass[d.numberAss] = k;
				d.numberAss++;
			}
		return d;
  	}

  	// Smart algorithm to systematically solve the grid according to domain availability and unique possibility
  	// It uses a set of rules in order
  	// 1. Find the domain of each cell
  	// 2. Find all domains with a single value and insert in grid
  	// 3. Scan domains of each row and find unique valid numbers for each row and insert in grid
  	// 4. Scan domains of each column and find unique valid numbers for reach column and insert in grid
  	// 5. Scan domains of each box and find unique valid numbers for each box and insert in grid
	private boolean SmartSolve(){

		// A grid containing the domains of every cell
		domains = new Domain[N][N];

		// Find the domain of each cell and insert into domains grid
		for (int r=0; r<N; r++)
			for (int c=0; c<N; c++) { 
				domains[r][c] = new Domain();
				// Fill domain with the only possible value if cell initially solved
				if (Grid[r][c]>0) {
					domains[r][c].numberAss = 1;
					domains[r][c].ass[0]=Grid[r][c];
				}
				// Get domain if cell empty
				else {
					domains[r][c] = getDomain(r,c);
				}
			}

		// For all domains with a single possible value, fill the Grid with the value and assign true in gridCellSolved
		for (int r=0; r<N; r++)
			for (int c=0; c<N; c++)
				if (Grid[r][c]==0 && domains[r][c].numberAss==1) {
					Grid[r][c]=domains[r][c].ass[0];
					gridCellSolved[r][c]=true;
					return true;
				}         

		int count;

		// Scan the domains of every cell in row and find a unique valid number for the same row
		for (int k=1; k<=N; k++)
			for (int r=0; r<N; r++) {
				count=0;
				int col=0;
				for (int c=0; c<N; c++)
					// Scan through every possible number in the domain
					for (int i=0; i<domains[r][c].numberAss; i++)
						if (domains[r][c].ass[i]==k) {
							col=c; 
							count++;
						}
				// If valid number is unique in the column and grid is empty at this cell
				if (count==1 && Grid[r][col]==0) {
					Grid[r][col]=k;
					gridCellSolved[r][col]=true;
					return true;
				}
	  		}

		// Scan the domains of every cell in column and find a unique valid number for the same column
		for (int k=1; k<=N; k++)
			for (int c=0; c<N; c++) {
				int row=0;
				count=0;
				for (int r=0; r<N; r++)
					// Scan through every possible number in the domain
					for (int i=0; i<domains[r][c].numberAss; i++)
						if (domains[r][c].ass[i]==k) {
							row=r; 
							count++;
						}
				// If valid number is unique in the row and grid is empty at this cell
				if (count==1 && Grid[row][c]==0) {
					Grid[row][c]=k;
					gridCellSolved[row][c]=true;
					return true;
				}
			}

		// Scan the domains of every cell in a box and find a unique valid number for the same box
		for (int k=1; k<=N; k++)
			for (int b1=0; b1<SIZE; b1++)
  				for (int b2=0; b2<SIZE; b2++) {
					int col=0;
					int row=0;
					count=0;
					for (int r=b1*SIZE; r<b1*SIZE+SIZE; r++)
						for (int c=b2*SIZE; c<b2*SIZE+SIZE; c++) 
							for (int i=0; i<domains[r][c].numberAss; i++)
								if (domains[r][c].ass[i]==k) {
									col=c; 
									row=r; 
									count++;
								}
		  			if (count==1 && Grid[row][col]==0) {
						Grid[row][col]=k;
						gridCellSolved[row][col]=true;
						return true;
		  			}
	      		}
		return false;
	}

	// Simple backtrack algorithm to systematically solve the grid row by row with recursion
	// 1. Find a valid number for a cell and insert it
	// 2. Find a valid number for next cell, if non-existent, backtrack and try another valid number for previous cell
	// 3. Do steps 1 and 2 recursively until entire grid is solved
	public void backtrack(int row, int col)throws Exception{

		// If row number exceeds the max row, the grid is solved
        if (row > N-1)throw new Exception( "Success!" ) ;

	    // If cell is not 0 (or x), continue with the next cell
	    if (Grid[row][col]!=0)backtrackNext(row, col);
	    else {
	        // Guess a valid number for current cell
	        for ( int number = 1; number <= N; number++){
	            if (checkValid(row,col,number)){
	                Grid[row][col] = number;
	                numberCellSolved++;
	                // Continue with next cell
	                backtrackNext(row, col);
	            }
	        }
	        Grid[row][col]=0;
	    }
	}

	// Calls backtrack for the next cell
	public void backtrackNext(int row, int col)throws Exception{
		if(numberCellSolved==N*N) return;
	    if (col < (N-1)){
	        backtrack(row, col+1);
	    } else {
	        backtrack(row+1, 0);
	    }
	}

	// Random guess algorithm for large grids
	// 1. Update all domains
	// 2. Find cell with the smallest domain
	// 3. Insert a random number from the corresponding domain to cell
	// 4. If a random assignment caused an 
	public void randomGuess(){
		int row=0, col=0;
		// Solve the rest of the grid with random guess and backtrack if wrong
		while (numberCellSolved < N*N) {	    
			Domain current = null;
			Domain best = new Domain();
			best.numberAss = N+1;
		  
			// Find the cell with the smallest domain size 
			for (int r=0; r<N; r++)
				for (int c=0; c<N; c++)
					if (Grid[r][c]==0) {
					    current = getDomain(r,c);
					    // Check if current domain is smallest, copy it to the best domain variable
					    if (current.numberAss < best.numberAss) {
							best.numberAss = current.numberAss;
							for (int i=0; i<best.numberAss; i++)
							    best.ass[i] = current.ass[i];
							row = r; col = c;
					    }
					}

			// Random backtrack if smallest domain is zero, thus a wrong value has been previously assigned, unassign a random value and backtrack
			if (best.numberAss==0) {
			    for (int r=0; r<N; r++)
					for (int c=0; c<N; c++)
					    if (!gridCellSolved[r][c] && Grid[r][c]>0) {
					    	// At this point, I feel that using a random probability for unassignment yields a faster algorithm than systematical unassignment (deduction by testing)
							double probability = 0.1;
							if (generator.nextFloat() < probability) {
							    numberCellSolved--;
							    Grid[r][c] = 0;
							}
					    }
			}
			// Assign random value from domain
			// Again random assignment from the domain feels faster that systematical assignment (deduction from testing)
			else {
				int i = generator.nextInt(best.numberAss);
			    Grid[row][col] = best.ass[i];
			    numberCellSolved++;
			}
		}
	}
    
    // Apply the 3 algorithms above to solve the grid
	public boolean solveSudoku() {
		generator = new Random();
		gridCellSolved = new boolean[N][N];

		// Fill up the gridCellSolved matrix
		for (int r=0; r<N; r++)
			for (int c=0; c<N; c++){
				if (Grid[r][c] > 0) {
					gridCellSolved[r][c] = true;
					numberCellSolved++;
				}
				else gridCellSolved[r][c] = false;
      		}
		
		// 1. Fill the grid intuitively using smart solve algorithm
		while (SmartSolve()) {
			numberCellSolved++;
		};

		// 2. Use simple backtrack algorithm to solve 3x3 bigger as it seems to be faster than random guesses
		if (SIZE==3){
			try {backtrack(0,0);}catch(Exception e){}
		}
		// 2. Or use random guesses to solve bigger grids
		else{
			randomGuess();
		}
    	return true;
	}

    /* The solve() method should remove all the unknown characters ('x') in the Grid
     * and replace them with the numbers from 1-9 that satisfy the Sudoku puzzle. */
    public void solve(){
        // Save the time in order to calculate runtime
        long startTime = System.nanoTime();
        solveSudoku();
        // Print out the running time in MILLISECONDS of this solve() method
        System.out.println("Runtime: " + (System.nanoTime() - startTime)/1e6);
    }


    /*****************************************************************************/
    /* NOTE: YOU SHOULD NOT HAVE TO MODIFY ANY OF THE FUNCTIONS BELOW THIS LINE. */
    /*****************************************************************************/
 
    /* Default constructor.  This will initialize all positions to the default 0
     * value.  Use the read() function to load the Sudoku puzzle from a file or
     * the standard input. */
    public Sudoku( int size )
    {
        SIZE = size;
        N = size*size;

        Grid = new int[N][N];
        for( int i = 0; i < N; i++ ) 
            for( int j = 0; j < N; j++ ) 
                Grid[i][j] = 0;
    }


    /* readInteger is a helper function for the reading of the input file.  It reads
     * words until it finds one that represents an integer. For convenience, it will also
     * recognize the string "x" as equivalent to "0". */
    static int readInteger( InputStream in ) throws Exception
    {
        int result = 0;
        boolean success = false;

        while( !success ) {
            String word = readWord( in );

            try {
                result = Integer.parseInt( word );
                success = true;
            } catch( Exception e ) {
                // Convert 'x' words into 0's
                if( word.compareTo("x") == 0 ) {
                    result = 0;
                    success = true;
                }
                // Ignore all other words that are not integers
            }
        }

        return result;
    }


    /* readWord is a helper function that reads a word separated by white space. */
    static String readWord( InputStream in ) throws Exception
    {
        StringBuffer result = new StringBuffer();
        int currentChar = in.read();
    String whiteSpace = " \t\r\n";
        // Ignore any leading white space
        while( whiteSpace.indexOf(currentChar) > -1 ) {
            currentChar = in.read();
        }

        // Read all characters until you reach white space
        while( whiteSpace.indexOf(currentChar) == -1 ) {
            result.append( (char) currentChar );
            currentChar = in.read();
        }
        return result.toString();
    }


    /* This function reads a Sudoku puzzle from the input stream in.  The Sudoku
     * grid is filled in one row at at time, from left to right.  All non-valid
     * characters are ignored by this function and may be used in the Sudoku file
     * to increase its legibility. */
    public void read( InputStream in ) throws Exception
    {
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                Grid[i][j] = readInteger( in );
            }
        }
    }


    /* Helper function for the printing of Sudoku puzzle.  This function will print
     * out text, preceded by enough ' ' characters to make sure that the printint out
     * takes at least width characters.  */
    void printFixedWidth( String text, int width )
    {
        for( int i = 0; i < width - text.length(); i++ )
            System.out.print( " " );
        System.out.print( text );
    }


    /* The print() function outputs the Sudoku grid to the standard output, using
     * a bit of extra formatting to make the result clearly readable. */
    public void print()
    {
        // Compute the number of digits necessary to print out each number in the Sudoku puzzle
        int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

        // Create a dashed line to separate the boxes 
        int lineLength = (digits + 1) * N + 2 * SIZE - 3;
        StringBuffer line = new StringBuffer();
        for( int lineInit = 0; lineInit < lineLength; lineInit++ )
            line.append('-');

        // Go through the Grid, printing out its values separated by spaces
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                printFixedWidth( String.valueOf( Grid[i][j] ), digits );
                // Print the vertical lines between boxes 
                if( (j < N-1) && ((j+1) % SIZE == 0) )
                    System.out.print( " |" );
                System.out.print( " " );
            }
            System.out.println();

            // Print the horizontal line between boxes
            if( (i < N-1) && ((i+1) % SIZE == 0) )
                System.out.println( line.toString() );
        }
    }


    /* The main function reads in a Sudoku puzzle from the standard input, 
     * unless a file name is provided as a run-time argument, in which case the
     * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
     * outputs the completed puzzle to the standard output. */
    public static void main( String args[] ) throws Exception
    {
        InputStream in;
        if( args.length > 0 ) 
            in = new FileInputStream( args[0] );
        else
            in = System.in;

        // The first number in all Sudoku files must represent the size of the puzzle.  See
        // the example files for the file format.
        int puzzleSize = readInteger( in );
        if( puzzleSize > 100 || puzzleSize < 1 ) {
            System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
            System.exit(-1);
        }

        Sudoku s = new Sudoku( puzzleSize );

        // read the rest of the Sudoku puzzle
        s.read( in );

        // Solve the puzzle.  We don't currently check to verify that the puzzle can be
        // successfully completed.  You may add that check if you want to, but it is not
        // necessary.

        s.solve();

        // Print out the (hopefully completed!) puzzle
        s.print();
    }
}

