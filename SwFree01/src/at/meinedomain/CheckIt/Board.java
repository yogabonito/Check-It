package at.meinedomain.CheckIt;

import java.util.ArrayList;

import android.util.Log;
import at.meinedomain.CheckIt.Pieces.*;

public class Board {
	public enum MatchState{
		RUNNING,
		
		CHECK_MATE_WON,
		TIME_UP_WON,
		
		OPPONENT_GONE,
		
		CHECK_MATE_LOST,
		TIME_UP_LOST,

		STALE_MATE_DRAW,
		LITTLE_MATERIAL_DRAW // TODO Test for this...
	}
	
	private SendMoveListener sendMoveListener;
	private Color myColor;
	private Point myKing;
	private Point opponentKing;
	private MatchState matchState;
	private int width;
	private int height;
	private AbstractPiece[][] board;
	private Color turn;
	private Point markedPoint;
	private Point markedPointOpponent;
	private Point enPassant;
	
	// Constructors=============================================================
	public Board(SendMoveListener sml, Color player){
		this.sendMoveListener = sml;
		this.myColor = player;
		myKing       = myColor==Color.WHITE ? new Point(4,0) : new Point(4,7);
		opponentKing = myColor==Color.WHITE ? new Point(4,7) : new Point(4,0);
		matchState = MatchState.RUNNING;
		width = 8;
		height = 8;
		turn = Color.WHITE;
		markedPoint = null;
		markedPointOpponent = null;
		enPassant = null;
		board = new AbstractPiece[width][height];
//		whitePieces = new AbstractPiece[width][height];
//		blackPieces = new AbstractPiece[width][height];
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				board[i][j] = null;
			}
		}

		// init pawns
		for(int i=0; i<width; i++){
			board[i][1] = new Pawn(this, Color.WHITE, new Point(i,1));
			board[i][6] = new Pawn(this, Color.BLACK, new Point(i,6));
		}
		// init rooks
		board[0][0] = new Rook(this, Color.WHITE, new Point(0,0));
		board[7][0] = new Rook(this, Color.WHITE, new Point(7,0));
		board[0][7] = new Rook(this, Color.BLACK, new Point(0,7));
		board[7][7] = new Rook(this, Color.BLACK, new Point(7,7));
		//init knights
		board[1][0] = new Knight(this, Color.WHITE, new Point(1,0));
		board[6][0] = new Knight(this, Color.WHITE, new Point(6,0));
		board[1][7] = new Knight(this, Color.BLACK, new Point(1,7));
		board[6][7] = new Knight(this, Color.BLACK, new Point(6,7));
		//init bishops
		board[2][0] = new Bishop(this, Color.WHITE, new Point(2,0));
		board[5][0] = new Bishop(this, Color.WHITE, new Point(5,0));
		board[2][7] = new Bishop(this, Color.BLACK, new Point(2,7));
		board[5][7] = new Bishop(this, Color.BLACK, new Point(5,7));
		//init queen and king
		board[3][0] = new Queen(this, Color.WHITE, new Point(3,0));
		board[4][0] = new King(this, Color.WHITE, new Point(4,0));
		board[3][7] = new Queen(this, Color.BLACK, new Point(3,7));
		board[4][7] = new King(this, Color.BLACK, new Point(4,7));
		
//		// init Piece-ArrayLists
//		for(int i=0; i<width; i++){
//			for(int j=0; j<height; j++){
//				if(board[i][j] != null){
//					if(board[i][j].getColor() == Color.WHITE){
//						whitePieces[i][j] = board[i][j];
//					}
//					else{
//						blackPieces[i][j] = board[i][j];
//					}
//				}
//			}
//		}
	}
	
	//--------------------------------------------------------------------------
	// this constructor is used for testing the canMove()-method of pieces.
	public Board(SendMoveListener sml, Color player, Color turn){
		this(sml, player);
		// redefine the board
		this.turn = turn;
	}
	
	// Getters/Setters/move-methods=============================================
	public AbstractPiece[][] getBoard(){
		return board;
	}
	@Deprecated
	public void setBoard(AbstractPiece[][] board){ // USED FOR TESTING ONLY!
		this.board = board;
		
		// set the myKing and opponentKing locations:
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				if(board[i][j]!=null && board[i][j] instanceof King){
					if(pieceAt(i,j).getColor() == myColor){
						myKing = new Point(i,j);
						Log.d("Board", "My king is at "+i+","+j);
					}
					else{
						opponentKing = new Point(i,j);
						Log.d("Board", "Opponent's king is at "+i+","+j);
					}
				}
			}
		}
	}
	
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}
	
	public AbstractPiece pieceAt(Point pt){
		return pieceAt(pt.getX(), pt.getY());
	}
	public AbstractPiece pieceAt(int i, int j){
		return board[i][j];
	}
	
	// for rook-placing (castling) and piece-placing (pawn reaches last rank)
	public void placePiece(Point from, Point to){
		AbstractPiece movingPiece = pieceAt(from); 
		
		movingPiece.setLocation(to);
		
		if(movingPiece instanceof King){
			if(movingPiece.getColor() == myColor){
				myKing = to;
			}
			else{
				opponentKing = to;
			}
		}
		
		board[  to.getX()][  to.getY()] = movingPiece; 
		board[from.getX()][from.getY()] = null;
	}
	
	// Currently used for en-passant-capturing only.
	private void killPiece(int x, int y){
		board[x][y] = null;
	}
	
//	// move without testing for correctness of the move.
//	public void move(Point from, Point to, MoveType mt){
//		move(from, to, null, mt);
//	}
	// move without testing for correctness of the move.
	public void move(Point from, Point to, MoveType mt){
//		enPassant = ep;
		if(turn.equals(myColor)){
			sendMoveListener.sendMove(new Move(from, to, mt));
			markedPoint = null;
			markedPointOpponent = null;
		}
		else{
			markedPointOpponent = to;
		}
		Log.d("Board", "now placePiece() with from.x="+from.getX()+", from.y="+from.getY());
		playSound(mt);
		placePiece(from, to);
		if(mt==MoveType.CASTLE_KINGSIDE || mt==MoveType.CASTLE_QUEENSIDE){
			placeCastlingRook(mt);
		}
		
		if(mt==MoveType.EN_PASSANT){
			killPiece(to.getX(), from.getY());
		}
		
		if(mt==MoveType.DOUBLE_STEP){
			enPassant = new Point(to.getX(), (from.getY()+to.getY())/2);
		}
		else{
			enPassant = null;
		}
		
		if(mt==MoveType.PAWN_TO_QUEEN){
			killPiece(to.getX(), to.getY());
			board[to.getX()][to.getY()] = new Queen(this, turn, to);
		}
		else if(mt==MoveType.PAWN_TO_ROOK){
			killPiece(to.getX(), to.getY());
			board[to.getX()][to.getY()] = new Rook(this, turn, to);
		}
		else if(mt==MoveType.PAWN_TO_KNIGHT){
			killPiece(to.getX(), to.getY());
			board[to.getX()][to.getY()] = new Knight(this, turn, to);
		}
		else if(mt==MoveType.PAWN_TO_BISHOP){
			killPiece(to.getX(), to.getY());
			board[to.getX()][to.getY()] = new Bishop(this, turn, to);
		}
		
		// check if game is over------------------------------------------------
		Color nextCol = (turn.equals(Color.WHITE)) ? Color.BLACK : Color.WHITE;
		if(isInCheckMate(nextCol)){
			matchState = nextCol==myColor ? MatchState.CHECK_MATE_LOST : 
											MatchState.CHECK_MATE_WON;
			return;
		}
		if(isInStaleMate(nextCol)){
			matchState = MatchState.STALE_MATE_DRAW;
			return;
		}
		
		// ok, let's continue---------------------------------------------------
		turn = (turn.equals(Color.WHITE)) ? Color.BLACK : Color.WHITE;
	}
	
	public void tryToMove(Point from, Point to){
		AbstractPiece tempPiece = pieceAt(from);
		if(tempPiece == null){
			Log.wtf("Board", "Trying to move null!");
			return;
		}
		else if(to.getX() >= width  ||  to.getY() >= height){
			Log.wtf("Board", "Trying to move outside the board");
			return;
		}
		else{
			tempPiece.tryToMove(to);
		}
	}
	
	public Point getEnPassant(){
		return enPassant;
	}
	
	public Point getMarkedPoint(){
		return markedPoint;
	}
	
	public Point getMarkedPointOpponent(){
		return markedPointOpponent;
	}
	
	public MatchState getMatchState(){
		return matchState;
	}
	
	public Color getTurn(){
		return turn;
	}
	
	public void setMarkedPoint(Point P){
		markedPoint = P;
	}
	
	public void setMatchState(MatchState ms){
		matchState = ms;
	}
	public void toggleTurn(){
		turn = (turn==Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	public void playSound(MoveType mt){
		if(!Settings.soundEnabled){
			return;
		}
		else if(mt == MoveType.CAPTURE)
			Assets.capture.play(1);
		else if(mt==MoveType.CASTLE_KINGSIDE || mt==MoveType.CASTLE_QUEENSIDE)
			Assets.castle.play(1);
		else
			Assets.move.play(1);
	}
	
	// Utility methods =========================================================
	public boolean isEmpty(Point pt){
		return pieceAt(pt)==null ? true : false;
	}
	
	public boolean isEmpty(int x, int y){
		return pieceAt(x,y)==null ? true : false;
	}
	
	public boolean emptyAfterOppMove(Point pt, Point oppFrom, Point oppTo){
		if(oppFrom == null){
			// assuming oppTo==null too.
			return isEmpty(pt);
		}
		
		else if(isEmpty(pt)){
			return !pt.equals(oppTo);
		}
		else{ // was not empty
			return pt.equals(oppFrom);
		}
	}
	
	public boolean isOccupiedByTurn(Point pt){
		if(!isEmpty(pt) && pieceAt(pt).getColor()==turn){
			return true;
		}
		return false;
	}
	
	public boolean isOccupiedByTurnOpponent(Point pt){
		if(!isEmpty(pt) && pieceAt(pt).getColor()!=turn){
			return true;
		}
		return false;
	}
	
	public boolean isInCheck(Color c){
		return leavesInCheck(c, null, null);
	}
	
	public boolean isInCheckMate(Color c){
		return isInCheck(c) && !canMove(c)  ?  true  :  false;
	}
	
	public boolean isInStaleMate(Color c){
		return !isInCheck(c) && !canMove(c) ?  true  : false;
	}
	
	private boolean canMove(Color c){
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				if(!isEmpty(i,j) && pieceAt(i,j).getColor()==c &&
				   pieceAt(i,j).canMoveSomewhere()){
					return true;
				}
			}
		}
		return false;
	}
	
	// test if we are left in check if we move ignore to consider
	// if ignore==consider==null then test for check in current position.
	public boolean leavesInCheck(Color c, Point ignore, Point consider){
		Point kingPt = myColor==c ? myKing : opponentKing;
		// but if the king is moving right now, we need to reassign.
		// color-test to because a king could move and cause an "Abzugsschach"
		if(kingPt.equals(ignore) && pieceAt(ignore).getColor()==c)
			kingPt = consider;
		
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				if(!isEmpty(i,j) && pieceAt(i,j).getColor()!=c 		 // if opp there
					&& !pieceAt(i,j).getLocation().equals(consider)){// and we did't just capture the attacker
					if(pieceAt(i,j).attacks(kingPt, ignore, consider)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void placeCastlingRook(MoveType mt){
		if(mt==MoveType.CASTLE_KINGSIDE  &&  turn == Color.WHITE){
			placePiece(new Point(7,0), new Point(5,0));
		}
		else if(mt==MoveType.CASTLE_QUEENSIDE  &&  turn == Color.WHITE){
			placePiece(new Point(0,0), new Point(3,0));
		}
		else if(mt==MoveType.CASTLE_KINGSIDE  &&  turn == Color.BLACK){
			placePiece(new Point(7,7), new Point(5,7));
		}
		else if(mt==MoveType.CASTLE_QUEENSIDE  &&  turn == Color.BLACK){
			placePiece(new Point(0,7), new Point(3,7));
		}
	}
}
