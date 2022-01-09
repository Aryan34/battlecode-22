package bot4;

import battlecode.common.*;

public strictfp class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        Robot robot;
        switch (rc.getType()) {
            case ARCHON:
                robot = new Archon(rc);
                break;
            case LABORATORY:
                robot = new Laboratory(rc);
                break;
            case WATCHTOWER:
                robot = new Watchtower(rc);
                break;
            case MINER:
                robot = new Miner(rc);
                break;
            case BUILDER:
                robot = new Builder(rc);
                break;
            case SOLDIER:
                robot = new Soldier(rc);
                break;
            case SAGE:
                robot = new Sage(rc);
                break;
            default:
                robot = new Robot(rc);
                break;
        }

        while (true) {
            try {
                robot.playTurn();
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }
}
