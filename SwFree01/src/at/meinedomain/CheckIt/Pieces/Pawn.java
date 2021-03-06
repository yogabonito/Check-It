package at.meinedomain.CheckIt.Pieces;

import android.util.Log;
import at.meinedomain.CheckIt.Assets;
import at.meinedomain.CheckIt.Board;
import at.meinedomain.CheckIt.Color;
import at.meinedomain.CheckIt.MoveType;
import at.meinedomain.CheckIt.Point;

public class Pawn extends AbstractPiece {
	
	protected int direction;

	public Pawn(Board b, Color c, Point pt){
		super(b, c, pt, "PAWN");
		if(c == Color.WHITE){
			pixmap = Assets.wp;
			direction = 1;
		} else{
			pixmap = Assets.bp;
			direction = -1;
		}
	}
	
	@Override
	protected MoveType canMove(Point to) {
		
		if(board.leavesInCheck(color, location, to)){
			return MoveType.ILLEGAL;
		}
		// - go 1 step ahead and reach the last rank
		if(legallyReachedLastRank(to)){
			return MoveType.PAWN_TO;
		}
		
		// - go 1 step ahead
		if(isEmpty(to) && isOnSameFile(to) && verticalDiff(to)==direction){
			return MoveType.NORMAL;
			// TODO let Pawn turn into a queen or something other, when last rank is reached
		}
		
		// - capture piece
		if(horizontalDist(to) == 1 && verticalDiff(to) == direction){
			if(isOccupiedByOpponent(to)){
				return MoveType.CAPTURE;
			}
			if(isEnPassant(to)){
				return MoveType.EN_PASSANT;
			}
		}

		// - go 2 steps ahead
		if(isEmpty(to) && 
		   isOnSameFile(to) && 
		   verticalDiff(to)==2*direction &&
		   isOnBaseline() && 
		   isEmpty(location.getX(), location.getY()+direction)){
			
			return MoveType.DOUBLE_STEP;
		}

		return MoveType.ILLEGAL;
		
	}

	@Override
	public MoveType tryToMove(Point to) {
		// TODO the normal checks + check for en passant (see tryToMove() in Piece in the DD)
		MoveType mt = super.tryToMove(to);
		if(mt == MoveType.ILLEGAL ||  
		   mt == MoveType.NORMAL  ||
		   mt == MoveType.CAPTURE ){
			return mt;
		}
		else if(mt == MoveType.DOUBLE_STEP){
			board.move(location, to, mt);
		}
		else if(mt == MoveType.EN_PASSANT){
			board.move(location, to, mt);
		}
		else if(mt == MoveType.PAWN_TO){
			board.move(location, to, MoveType.PAWN_TO_QUEEN);
		}
		return mt;
	}
	
	@Override
	public boolean attacks(Point tile, Point not, Point instead){
		if(horizontalDist(tile) == 1 && verticalDiff(tile) == direction){
			return true;
		}
		return false;
	}
	
	
	public boolean isEnPassant(Point to){
		return board.getEnPassant() != null && to.equals(board.getEnPassant());
	}
	
	private boolean isOnBaseline(){
		if(color == Color.WHITE){
			return (location.getY() == 1);
		}
		else{
			return (location.getY() == board.getHeight()-2);
		}
	}
	
	private boolean legallyReachedLastRank(Point to){
		if(!isEmpty(to) || !isOnSameFile(to) || verticalDiff(to)!=direction){
			return false;
		}
		
		int lastRank = color==Color.WHITE ? 7 : 0;
		return to.getY()==lastRank ? true : false;
	}

}
