package cn.mingbai.ScreenInMC.Screen;

import org.bukkit.Location;

public class Screen {
    public Location location;
    private Facing facing;
    private int height;
    private int width;
    private boolean placed = false;
    private Location[][] screenPieces;
    public enum Facing{
        UP,
        DOWN,
        EAST,
        SOUTH,
        WEST,
        NORTH
    }
    public Screen(Location location,Facing facing,int width,int height){
        this.location=location;
        this.facing=facing;
        this.height=height;
        this.width=width;
    }
    public void putScreen(){
        if(!placed) {
            screenPieces = new Location[width][height];
            switch (facing) {
                case UP:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = location.clone().add(x,0,y);
                        }
                    }
                    break;
                case DOWN:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = location.clone().add(x,0,-y);
                        }
                    }
                    break;
                case EAST:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = location.clone().add(0,-y,x);
                        }
                    }
                    break;
                case SOUTH:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = location.clone().add(-x,-y,0);
                        }
                    }
                    break;
                case WEST:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = location.clone().add(0,-y,-x);
                        }
                    }
                    break;
                case NORTH:
                    for(int x=0;x<width;x++){
                        for(int y=0;y<width;y++) {
                            screenPieces[x][y] = location.clone().add(x,-y,0);
                        }
                    }
                    break;
            }
            for(int x=0;x<width;x++) {
                for (int y = 0; y < width; y++) {

                }
            }
            placed=true;
        }else{
            throw new RuntimeException("This Screen has been placed.");
        }
    }
}
