// Fig. 24.15: TicTacToeClient.java
// Client that let a user play Tic-Tac-Toe with another across a network.
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import javax.swing.*;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class TicTacToeClient extends JApplet implements Runnable{
    private JTextField idField; // textfield to display player's mark
    private JTextArea displayArea; // JTextArea to display output
    private JPanel boardPanel; // panel for tic-tac-toe board
    private JPanel panel2; // panel to hold board
    private Square board[][]; // tic-tac-toe board
    private Square currentSquare; // current square
    private Socket connection; // connection to server
    private Scanner input; // input from server
    private Formatter output; // output to server
    private String ticTacToeHost; // host name for server
    private String myMark; // this client's mark
    private boolean myTurn; // determines which client's turn it is
    private final String X_MARK = "X"; // mark for first client
    private final String O_MARK = "O"; // mark for second client
    private JFrame frame = new JFrame("Tic Tac Toe");

    // set up user-interface and board
    public void init()
    {
        displayArea = new JTextArea( 4, 30 ); // set up JTextArea
        displayArea.setEditable( false );
        add( new JScrollPane( displayArea ), BorderLayout.SOUTH );

        boardPanel = new JPanel(); // set up panel for squares in board
        boardPanel.setLayout( new GridLayout( 3, 3, 0, 0 ) );
        panel2 = new JPanel(); // set up panel to contain boardPanel
        panel2.add( boardPanel, BorderLayout.CENTER ); // add board panel
        setupSquares();
        /*board = new Square[ 3 ][ 3 ]; // create board

        // loop over the rows in the board
        for ( int row = 0; row < board.length; row++ )
        {
            // loop over the columns in the board
            for ( int column = 0; column < board[ row ].length; column++ )
            {
                // create square
                board[ row ][ column ] = new Square( " ", row * 3 + column );
                boardPanel.add( board[ row ][ column ] ); // add square
            } // end inner for
        } // end outer for*/

        idField = new JTextField(); // set up textfield
        idField.setEditable( false );
        add( idField, BorderLayout.NORTH );

        //panel2 = new JPanel(); // set up panel to contain boardPanel
        //panel2.add( boardPanel, BorderLayout.CENTER ); // add board panel
        add( panel2, BorderLayout.CENTER ); // add container panel

        setSize( 300, 225 ); // set size of window
        setVisible( true ); // show window

        startClient();
    } // end TicTacToeClient constructor



    private void setupSquares() {
        System.out.println("Reached here");
        boardPanel.removeAll();
        panel2.remove(boardPanel);
        boardPanel = new JPanel(); // set up panel for squares in board
        boardPanel.setLayout(new GridLayout(3, 3, 0, 0));

        board = new Square[3][3];
        // When creating a Square, the location argument to the constructor
// is a value from 0 to 8 indicating the position of the Square on
// the board. Values 0, 1, and 2 are the first row, values 3, 4,
// and 5 are the second row. Values 6, 7, and 8 are the third row.
        for ( int row = 0; row < board.length; row++ ) {

            for ( int column = 0; column < board[ row ].length; column++ ) {

// create Square
                board[ row ][ column ] = new Square( " ", row * 3 + column );
                boardPanel.add( board[ row ][ column ] );
            }
        }
        panel2.add( boardPanel, BorderLayout.CENTER ); // add board panel
    }

    // start the client thread
    public void startClient()
    {
        try // connect to server, get streams and start outputThread
        {
            // make connection to server
            connection = new Socket(
                    InetAddress.getByName(getCodeBase().getHost()), 12345 );

            // get streams for input and output
            input = new Scanner( connection.getInputStream() );
            output = new Formatter( connection.getOutputStream() );
        } // end try
        catch ( IOException ioException )
        {
            ioException.printStackTrace();
        } // end catch

        // create and start worker thread for this client
        ExecutorService worker = Executors.newFixedThreadPool( 1 );
        worker.execute( this ); // execute client
    } // end method startClient

    // control thread that allows continuous update of displayArea
    public void run()
    {
        myMark = input.nextLine(); // get player's mark (X or O)

        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        // display player's mark
                        idField.setText( "You are player \"" + myMark + "\"" );
                    } // end method run
                } // end anonymous inner class
        ); // end call to SwingUtilities.invokeLater

        myTurn = ( myMark.equals( X_MARK ) ); // determine if client's turn

        // receive messages sent to client and output them
        while ( true )
        {
            if ( input.hasNextLine() )
                processMessage( input.nextLine() );
        } // end while
    } // end method run

    // process messages received by client
    private void processMessage( String message )
    {
        // valid move occurred
        if ( message.equals( "Valid move." ) )
        {
            displayMessage( "Valid move, please wait.\n" );
            setMark(currentSquare, myMark); // set mark in square
        } // end if
        else if ( message.equals( "Invalid move, try again" ) )
        {
            displayMessage( message + "\n" ); // display invalid move
            myTurn = true; // still this client's turn
        } // end else if
        else if ( message.equals( "Opponent moved" ) )
        {
            int location = input.nextInt(); // get move location
            input.nextLine(); // skip newline after int location
            int row = location / 3; // calculate row
            int column = location % 3; // calculate column

            setMark(  board[ row ][ column ],
                    ( myMark.equals( X_MARK ) ? O_MARK : X_MARK ) ); // mark move
            displayMessage( "Opponent moved. Your turn.\n" );
            myTurn = true; // now this client's turn
        } else if ( message.equals( "Over." ) ) {
            for(int i = 0; i<1; i++) {
                displayMessage("Game Over\n");
                wantsToPlayAgain();
                myTurn = true;
            }
        }else if ( message.startsWith("VICTORY" ) )
        {
            displayMessage( "You Win!.\n" );
            wantsToPlayAgain();
            myTurn = true; // now this client's turn
            //displayMessage( "Other Player Won!.\n" );
            //wantsToPlayAgain();
        }else if ( message.equals( "DEFEAT" ) )
        {
            displayMessage( "You lose!.\n" );
            wantsToPlayAgain();
        }else if ( message.equals( "TIE" ) )
        {
            for(int i = 0; i<1; i++) {
                displayMessage("You Tied.\n");
                wantsToPlayAgain();
                myTurn = true; // now this client's turn
            }
        }
        // end else if
        else
            displayMessage( message + "\n" ); // display the message
    } // end method processMessage

    // manipulate outputArea in event-dispatch thread
    private void displayMessage( final String messageToDisplay )
    {
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        displayArea.append( messageToDisplay ); // updates output
                    } // end method run
                }  // end inner class
        ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
                "Want to play again?",
                "Tic Tac Toe is Fun Fun Fun",
                JOptionPane.YES_NO_OPTION);
        System.out.println(response);
        if (response == 0) {
            displayMessage("Resetting squres");
            setupSquares();
        }
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }


    // utility method to set mark on board in event-dispatch thread
    private void setMark( final Square squareToMark, final String mark )
    {
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        squareToMark.setMark( mark ); // set mark in square
                    } // end method run
                } // end anonymous inner class
        ); // end call to SwingUtilities.invokeLater
    } // end method setMark

    // send message to server indicating clicked square
    public void sendClickedSquare( int location )
    {
        // if it is my turn
        if ( myTurn )
        {
            output.format( "%d\n", location ); // send location to server
            output.flush();
            myTurn = false; // not my turn anymore
        } // end if
    } // end method sendClickedSquare

    // set current Square
    public void setCurrentSquare( Square square )
    {
        currentSquare = square; // set current square to argument
    } // end method setCurrentSquare

    // private inner class for the squares on the board
    private class Square extends JPanel
    {
        private String mark; // mark to be drawn in this square
        private int location; // location of square

        public Square( String squareMark, int squareLocation )
        {
            mark = squareMark; // set mark for this square
            location = squareLocation; // set location of this square

            addMouseListener(
                    new MouseAdapter() {
                        public void mouseReleased( MouseEvent e )
                        {
                            setCurrentSquare( Square.this ); // set current square

                            // send location of this square
                            sendClickedSquare( getSquareLocation() );
                        } // end method mouseReleased
                    } // end anonymous inner class
            ); // end call to addMouseListener
        } // end Square constructor

        // return preferred size of Square
        public Dimension getPreferredSize() {
            return new Dimension(30, 30); // return preferred size
        } // end method getPreferredSize

        // return minimum size of Square
        public Dimension getMinimumSize() {
            return getPreferredSize(); // return preferred size
        } // end method getMinimumSize

        // set mark for Square
        public void setMark( String newMark )
        {
            mark = newMark; // set mark of square
            repaint(); // repaint square
        } // end method setMark

        // return Square location
        public int getSquareLocation()
        {
            return location; // return location of square
        } // end method getSquareLocation

        // draw Square
        public void paintComponent( Graphics g )
        {
            super.paintComponent( g );

            g.drawRect( 0, 0, 29, 29 ); // draw square
            g.drawString( mark, 11, 20 ); // draw mark
        } // end method paintComponent
    } // end inner-class Square
} // end class TicTacToeClient

/**************************************************************************
 * (C) Copyright 1992-2005 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 *************************************************************************/