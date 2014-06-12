package at.meinedomain.CheckIt.Pieces;

import java.util.ArrayList;

import at.meinedomain.CheckIt.Assets;
import at.meinedomain.CheckIt.Board;
import at.meinedomain.CheckIt.Color;
import at.meinedomain.CheckIt.MoveType;
import at.meinedomain.CheckIt.Point;

public class Bishop extends AbstractPiece {

	private ArrayList<SlideType> slideTypes;
	
	public Bishop(Board b, Color c, Point pt){
		super(b, c, pt, "BISHOP");
		if(c == Color.WHITE){
			pixmap = Assets.wb;
		} else{
			pixmap = Assets.bb;
		}
		slideTypes = new ArrayList<>();
		slideTypes.add(SlideType.UPWARD);
		slideTypes.add(SlideType.DOWNWARD);
	}
	
	@Override
	protected MoveType canMove(Point to) {
		return canSlide(location, to, slideTypes) ? MoveType.NORMAL : MoveType.ILLEGAL;
	}
//
//	@Override
//	public void tryToMove(Point pt) {
//		// TODO Auto-generated method stub
//
//	}

	public ArrayList<SlideType> getSlideTypes(){
		return slideTypes;
	}
}
