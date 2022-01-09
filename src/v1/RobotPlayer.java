package v1;

import battlecode.common.*;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random();

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.NORTHWEST,
        Direction.EAST,
        Direction.WEST,
        Direction.SOUTH,
        Direction.SOUTHEAST,
        Direction.SOUTHWEST,
    };

    static final Direction[] modded = {
        Direction.NORTHEAST,
        Direction.NORTHWEST,
        Direction.SOUTHEAST,
        Direction.SOUTHWEST,
    };

    static final Direction[] modded2 = {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.EAST,
        Direction.WEST,
    };

    static int targetx = -1;
    static int targety = -1;
    static int randnum = -1;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!

        // You can also use indicators to save debug notes in replays.

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ARCHON:     runArchon(rc);  break;
                    case LABORATORY: runLab(rc); break;
                    case MINER:      runMiner(rc);   break;
                    case BUILDER: runBuilder(rc); break;
                    case SOLDIER:    runSoldier(rc); break;
                    case WATCHTOWER: runWatch(rc); break; // You might want to give them a try!
                    case SAGE: runSage(rc); break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runArchon(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        int rn = rc.getRoundNum();
        RobotInfo[] robs = rc.senseNearbyRobots();
        int watchs = 0;
        for (int a=0; a<robs.length; a++){
            if (robs[a].type==RobotType.WATCHTOWER){
                watchs++;
            }
        }

        if (rn<=1){
            rc.writeSharedArray(10, rc.getLocation().x);
            rc.writeSharedArray(11, rc.getLocation().y);
        }
        if (rn<100) {
            if (rng.nextBoolean()) {
                // Let's try to build a miner.
                rc.setIndicatorString("Trying to build a miner");
                if (rc.canBuildRobot(RobotType.MINER, dir)) {
                    rc.buildRobot(RobotType.MINER, dir);
                }
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
            }
        } 
        else if (rc.getTeamLeadAmount(rc.getTeam())>2000 && watchs<4){
            rc.setIndicatorString("Trying to build a builder");
            if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                rc.buildRobot(RobotType.BUILDER, dir);
            }
        }
        else if (rn<200){
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        }
        else if (rc.getTeamLeadAmount(rc.getTeam())<5000) {
            // Let's try to build a miner.
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        }
        else {
            if (rng.nextBoolean() && rn<1800) {
                // Let's try to build a miner.
                rc.setIndicatorString("Trying to build a sage");
                if (rc.canBuildRobot(RobotType.SAGE, dir)) {
                    rc.buildRobot(RobotType.SAGE, dir);
                }
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
            }
        }
    }

    /**
     * Run a single turn for a Miner.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runMiner(RobotController rc) throws GameActionException {
        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        int count = rc.senseLead(me);
        double maxs = 0;
        int bestx = 0;
        int besty = 0;
        if (count!=0){
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                    // Notice that the Miner's action cooldown is very low.
                    // You can mine multiple times per turn!
                    // double metric = rc.senseGold(mineLocation) + 0.5*rc.senseLead(mineLocation);
                    // if (metric >= maxs){
                    //     maxs = metric;
                    //     bestx = dx;
                    //     besty = dy;
                    // }
                    while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation)>1) {
                        rc.mineGold(mineLocation);
                    }
                    while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation)>1) {
                        rc.mineLead(mineLocation);
                    }
                }
            }
        }
        else {
            rc.disintegrate();
        }
       
        // Direction dir = directions[0];
        // boolean stay = false;
        
        // if (besty>0){
        //     if (bestx>0){
        //        dir = directions[1];
        //     }
        //     else if (bestx<0){
        //        dir = directions[2];
        //     }
        //     else {
        //         dir = directions[0];
        //     }
            
        // } else if (besty<0){
        //     if (bestx>0){
        //        dir = directions[6];
        //     }
        //     else if (bestx<0){
        //        dir = directions[7];
        //     }
        //     else {
        //         dir = directions[5];
        //     }
        // } else {
        //     if (bestx>0){
        //        dir = directions[3];
        //     }
        //     else if (bestx<0){
        //        dir = directions[4];
        //     }
        //     else {
        //         stay = true;
        //     }
        // }

        // if (!stay && rc.canMove(dir)) {
        //     rc.move(dir);
        // }
    //else if (stay){
        if (count<2){
            Direction dir2 = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir2)) {
                rc.move(dir2);
            }
        }
        
        //}
    }

    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runSoldier(RobotController rc) throws GameActionException {
        // Try to attack someone
        rc.setIndicatorString(""+targetx+" "+targety+" "+randnum);
        if(targetx==-1 || targety==-1 || (rc.canSenseRobotAtLocation(new MapLocation(targetx, targety)) && rc.senseRobotAtLocation(new MapLocation(targetx, targety)).type!=RobotType.ARCHON)){
            int r = 0;
            if (randnum==-1){
                r = rng.nextInt(3);
                randnum = r;
            }
            else {
                r = (randnum+1)%3;
                randnum = r;
            }
            
            if (r==0){
                targetx = rc.getMapWidth()-rc.readSharedArray(10)-1;
                targety = rc.getMapHeight()-rc.readSharedArray(11)-1;
            }
            else if (r==1){
                targetx = rc.getMapWidth()-rc.readSharedArray(10)-1;
                targety = rc.readSharedArray(11);
            }
            else {
                targetx = rc.readSharedArray(10);
                targety = rc.getMapHeight()-rc.readSharedArray(11)-1;
            }
        }

        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
        
        // Also try to move randomly.
        MapLocation temp = new MapLocation(targetx, targety);
        Direction dir = rc.getLocation().directionTo(temp);
        if (rc.senseNearbyRobots(radius, rc.getTeam()).length>7){
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
            else {
                dir = directions[rng.nextInt(directions.length)] ;
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }
        }
    }

    static void runWatch(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
    }

    static void runBuilder(RobotController rc) throws GameActionException {
        // Try to attack someone
        MapLocation me = rc.getLocation();
        boolean cont = false;
        boolean dontmove = false;
        RobotInfo[] li = rc.senseNearbyRobots();
        int count = 0;
        boolean maketower = true;
        for (int a=0; a<li.length; a++){
            if (li[a].type == RobotType.ARCHON){
                cont = true;
            }
            if (li[a].type == RobotType.WATCHTOWER || li[a].type == RobotType.LABORATORY){
                if(li[a].health<li[a].type.health && rc.canRepair(li[a].location)){
                    rc.repair(li[a].location);
                    dontmove = true;
                }
                
                if(li[a].level<3 && rc.canMutate(li[a].location)){
                    rc.mutate(li[a].location);
                    dontmove = true;
                }
            }
            if (rc.getTeam()==li[a].team && !(li[a].type == RobotType.WATCHTOWER || li[a].type == RobotType.LABORATORY)){
                count++;
            }
            if (li[a].type == RobotType.WATCHTOWER && rc.getTeam()==li[a].team){
                maketower = false;
            }
        }

        if (cont){
            for (int i = 0; i<directions.length; i++){
                Direction dir = directions[i];
                MapLocation ml = new MapLocation(me.x+dir.dx, me.y+dir.dy);

                if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                    if ((me.x+dir.dx)%2==(me.y+dir.dy)%2) {
                        rc.buildRobot(RobotType.WATCHTOWER, dir);
                        break;
                    }
                    else if (rc.readSharedArray(0)==0){
                        rc.writeSharedArray(0, 1);
                        rc.buildRobot(RobotType.LABORATORY, dir);
                        break;
                    }
                }
            }
        } else if (count>3 && maketower){
            Direction dirt = directions[rng.nextInt(directions.length)];
            if (rc.canBuildRobot(RobotType.WATCHTOWER, dirt)){
                rc.buildRobot(RobotType.WATCHTOWER, dirt);
            }
        }

        Direction dir2 = directions[rng.nextInt(directions.length)];
        if (!dontmove && rc.canMove(dir2)) {
            rc.move(dir2);
        }
    }

    static void runSage(RobotController rc) throws GameActionException {
        // Try to attack someone
        rc.setIndicatorString(""+targetx+" "+targety+" "+randnum);
        if(targetx==-1 || targety==-1 || (rc.canSenseRobotAtLocation(new MapLocation(targetx, targety)) && rc.senseRobotAtLocation(new MapLocation(targetx, targety)).type!=RobotType.ARCHON)){
            int r = 0;
            if (randnum==-1){
                r = rng.nextInt(3);
                randnum = r;
            }
            else {
                r = (randnum+1)%3;
                randnum = r;
            }
            
            if (r==0){
                targetx = rc.getMapWidth()-rc.readSharedArray(10)-1;
                targety = rc.getMapHeight()-rc.readSharedArray(11)-1;
            }
            else if (r==1){
                targetx = rc.getMapWidth()-rc.readSharedArray(10)-1;
                targety = rc.readSharedArray(11);
            }
            else {
                targetx = rc.readSharedArray(10);
                targety = rc.getMapHeight()-rc.readSharedArray(11)-1;
            }
        }

        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 8 || rc.getHealth()<50) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canEnvision(AnomalyType.CHARGE)) {
                rc.envision(AnomalyType.CHARGE);
            }
        }
        
        // Also try to move randomly.
        MapLocation temp = new MapLocation(targetx, targety);
        Direction dir = rc.getLocation().directionTo(temp);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        else {
            dir = directions[rng.nextInt(directions.length)] ;
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    static void runLab(RobotController rc) throws GameActionException {
        if (rc.getTeamLeadAmount(rc.getTeam())>1000 && rc.isActionReady()){
            rc.transmute();
        }
    }
}
