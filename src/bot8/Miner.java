package bot8;

import battlecode.common.*;

public class Miner extends Robot {

    int leadCount;
    MapLocation searchTarget;

    public Miner(RobotController rc) {
        super(rc);
        leadCount = 0;
        searchTarget = null;
    }

    // TODO: as you get further from the parentLoc, explore less aggressively
    void playTurn() throws GameActionException {
        super.playTurn();

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam);
        for (RobotInfo info : nearbyEnemies) {
            if (util.attackerType(info.type)) {
                nav.retreatFromEnemies(nearbyEnemies);
                rc.setIndicatorString("RETREAT");
                break;
            }
        }

        // TODO: possibly research for best tile each time we mine, instead of mining multiple times at the same loc
        MapLocation neighboringDepositLoc = largestNeighboringDeposit(true);
        if (neighboringDepositLoc != null) {
            while (rc.senseGold(neighboringDepositLoc) > 0 && rc.canMineGold(neighboringDepositLoc)) {
                rc.mineGold(neighboringDepositLoc);
            }
            while (rc.senseLead(neighboringDepositLoc) > 1 && rc.canMineLead(neighboringDepositLoc)) {
                rc.mineLead(neighboringDepositLoc);
            }
        }

        // TODO: in move towards, favor directions that have better neighboring deposits
        MapLocation depositLoc = largestDeposit(true);
        if (depositLoc != null) {
            nav.moveTowards(depositLoc);
            rc.setIndicatorString("1: " + depositLoc);
        } else if (searchTarget == null || myLoc.distanceSquaredTo(searchTarget) < myType.visionRadiusSquared) {
            searchTarget = randomSearchTarget();
            rc.setIndicatorString("2: " + searchTarget);
        }

        if (searchTarget != null) {
            rc.setIndicatorString("3: " + searchTarget);
            nav.moveTowards(searchTarget);
        }
    }

    // true: consider gold deposits as well (gold weighted at highestLead * 5); false: only looks for lead deposits
    MapLocation largestDeposit(boolean searchForGold) throws GameActionException {
        MapLocation depositLoc = null;
        int highestGold = 0;
        int highestLead = -1000;

        for (MapLocation loc : rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared)) {
            if (rc.senseLead(loc) >= 2) {
                int value = rc.senseLead(loc) - myLoc.distanceSquaredTo(loc);
                if (value > highestLead) {
                    highestLead = value;
                    depositLoc = loc;
                }
            }
        }

        if (searchForGold) {
            highestGold = highestLead / 2; // TODO: once we get better lab code, find a better constant than 2
            for (MapLocation loc : rc.senseNearbyLocationsWithGold(RobotType.MINER.visionRadiusSquared)) {
                int value = rc.senseGold(loc) - myLoc.distanceSquaredTo(loc);
                if (value > highestGold) {
                    highestGold = value;
                    depositLoc = loc;
                }
            }
        }

        return depositLoc;
    }

    // true: if gold nearby, mine that first; false: ignore gold, only mine lead
    MapLocation largestNeighboringDeposit(boolean searchForGold) throws GameActionException {
        MapLocation depositLoc = null;
        int highestGold = 0;
        int highestLead = 1;

        if (searchForGold) {
            for (MapLocation loc : rc.senseNearbyLocationsWithGold(myType.actionRadiusSquared)) {
                int value = rc.senseLead(loc);
                if (value > highestGold) {
                    highestGold = value;
                    depositLoc = loc;
                }
            }
        }

        if (depositLoc != null) {
            return depositLoc;
        }

        for (MapLocation loc : rc.senseNearbyLocationsWithLead(myType.actionRadiusSquared)) {
            int value = rc.senseLead(loc);
            if (value > highestLead) {
                highestLead = value;
                depositLoc = loc;
            }
        }

        return depositLoc;
    }

    // TODO: get better miner exploration code
    MapLocation randomSearchTarget() throws GameActionException {
        Direction dir = randomSearchDirection();
        switch (dir) {
            case NORTH:
                return new MapLocation(myLoc.x, rc.getMapHeight() - 1);
            case EAST:
                return new MapLocation(rc.getMapWidth() - 1, myLoc.y);
            case SOUTH:
                return new MapLocation(myLoc.x, 0);
            case WEST:
                return new MapLocation(0, myLoc.y);
            case NORTHEAST:
                if (rc.getMapWidth() - myLoc.x < rc.getMapHeight() - myLoc.y) {
                    return new MapLocation(rc.getMapWidth() - 1, myLoc.y + rc.getMapWidth() - myLoc.x - 1);
                } else {
                    return new MapLocation(myLoc.x + rc.getMapHeight() - myLoc.y - 1, rc.getMapHeight() - 1);
                }
            case SOUTHEAST:
                if (rc.getMapWidth() - myLoc.x - 1 < myLoc.y) {
                    return new MapLocation(rc.getMapWidth() - 1, myLoc.y - (rc.getMapWidth() - myLoc.x - 1));
                } else {
                    return new MapLocation(rc.getMapWidth() - myLoc.y - 1, 0);
                }
            case SOUTHWEST:
                if (myLoc.x < myLoc.y) {
                    return new MapLocation(0, myLoc.y - myLoc.x);
                } else {
                    return new MapLocation(myLoc.x - myLoc.y, 0);
                }
            case NORTHWEST:
                if (myLoc.x < rc.getMapHeight() - myLoc.y - 1) {
                    return new MapLocation(0, rc.getMapHeight() - myLoc.x - 1);
                } else {
                    return new MapLocation(myLoc.x - (rc.getMapHeight() - myLoc.y - 1), rc.getMapHeight() - 1);
                }
            default:
                return null;
        }
    }

    Direction randomSearchDirection() throws GameActionException {
        // northeast
        if (rc.getMapWidth() - parentLoc.x - 1 <= 5 && rc.getMapHeight() - parentLoc.y - 1 <= 5) {
            Direction[] directions = {
                    Direction.SOUTHEAST,
                    Direction.SOUTH,
                    Direction.SOUTHWEST,
                    Direction.WEST,
                    Direction.NORTHWEST,
            };
            return directions[(Clock.getBytecodeNum() * rc.getID()) % 5];
        }

        // southeast
        if (rc.getMapWidth() - parentLoc.x - 1 <= 5 && parentLoc.y <= 5) {
            Direction[] directions = {
                    Direction.NORTH,
                    Direction.NORTHEAST,
                    Direction.SOUTHWEST,
                    Direction.WEST,
                    Direction.NORTHWEST,
            };
            return directions[(Clock.getBytecodeNum() * rc.getID()) % 5];
        }

        // southwest
        if (parentLoc.x <= 5 && parentLoc.y <= 5) {
            Direction[] directions = {
                    Direction.NORTH,
                    Direction.NORTHEAST,
                    Direction.EAST,
                    Direction.SOUTHEAST,
                    Direction.NORTHWEST,
            };
            return directions[(Clock.getBytecodeNum() * rc.getID()) % 5];
        }

        // northwest
        if (parentLoc.x <= 5 && rc.getMapHeight() - parentLoc.y - 1 <= 5) {
            Direction[] directions = {
                    Direction.NORTHEAST,
                    Direction.EAST,
                    Direction.SOUTHEAST,
                    Direction.SOUTH,
                    Direction.SOUTHWEST,
            };
            return directions[(Clock.getBytecodeNum() * rc.getID()) % 5];
        }

        // north
        if (rc.getMapHeight() - parentLoc.y - 1 <= 5) {
            Direction[] directions = {
                    Direction.EAST,
                    Direction.SOUTHEAST,
                    Direction.SOUTH,
                    Direction.SOUTHWEST,
                    Direction.WEST,
            };
            return directions[(Clock.getBytecodeNum() * rc.getID()) % 5];
        }

        // east
        if (rc.getMapWidth() - parentLoc.x - 1 <= 5) {
            Direction[] directions = {
                    Direction.NORTH,
                    Direction.SOUTH,
                    Direction.SOUTHWEST,
                    Direction.WEST,
                    Direction.NORTHWEST,
            };
            return directions[(Clock.getBytecodeNum() * rc.getID()) % 5];
        }

        // south
        if (parentLoc.y <= 5) {
            Direction[] directions = {
                    Direction.NORTH,
                    Direction.NORTHEAST,
                    Direction.EAST,
                    Direction.WEST,
                    Direction.NORTHWEST,
            };
            return directions[(Clock.getBytecodeNum() * rc.getID()) % 5];
        }

        // west
        if (parentLoc.x <= 5) {
            Direction[] directions = {
                    Direction.NORTH,
                    Direction.NORTHEAST,
                    Direction.EAST,
                    Direction.SOUTHEAST,
                    Direction.SOUTH,
            };
            return directions[(Clock.getBytecodeNum() * rc.getID()) % 5];
        }

        return Navigation.directions[(Clock.getBytecodesLeft() * rng.nextInt(1337) + Clock.getBytecodeNum() * rc.getID()) % 8];
    }
}
