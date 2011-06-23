package com.zford.jobs.config.container;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.zford.jobs.config.JobsConfiguration;


public class RestrictedArea {

    private Location location1;
    private Location location2;
    
    public RestrictedArea(Location location1, Location location2) {
        this.location1 = location1;
        this.location2 = location2;
    }
    
    /**
     * Function check if location is a restricted area
     * @param loc - the location to checked
     * @return true - the location is inside a restricted area
     * @return false - the location is outside a restricted area
     */
    public static boolean isRestricted(Location loc) {
        List<RestrictedArea> restrictedAreas = JobsConfiguration.getInstance().getRestrictedAreas();
        for(RestrictedArea restrictedArea : restrictedAreas) {
            if(restrictedArea.inRestrictedArea(loc)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Function check if entity is a restricted area
     * @param entity - the entity to checked
     * @return true - the location is inside a restricted area
     * @return false - the location is outside a restricted area
     */
    public static boolean isRestricted(Entity entity) {
        return RestrictedArea.isRestricted(entity.getLocation());
    }

    /**
     * Function check if location is in the restricted area
     * @param loc - the location to checked
     * @return true - the location is inside the restricted area
     * @return false - the location is outside the restricted area
     */
    public boolean inRestrictedArea(Location loc) {
        if(RestrictedArea.isBetween(loc.getX(), this.location1.getX(), this.location2.getX()) &&
                RestrictedArea.isBetween(loc.getY(), this.location1.getY(), this.location2.getY()) &&
                RestrictedArea.isBetween(loc.getZ(), this.location1.getZ(), this.location2.getZ()) &&
                this.location1.getWorld().equals(loc.getWorld()) &&
                this.location2.getWorld().equals(loc.getWorld())) {
            return true;
        }
        return false;
    }

    /**
     * Function check if entity is in the restricted area
     * @param entity - the entity to checked
     * @return true - the entity is inside the restricted area
     * @return false - the entity is outside the restricted area
     */
    public boolean inRestrictedArea(Entity entity) {
        return this.inRestrictedArea(entity.getLocation());
    }
    
    /**
     * Function check if number is between bounds
     * @param number - the number to be checked
     * @param bound1 - the first bound
     * @param bound2 - the second bound
     * @return true - number is between bounds
     * @return false - number is out of bounds
     */
    private static boolean isBetween(double number, double bound1, double bound2) {
        if(bound1 < bound2 && number > bound1 && number < bound2) {
            return true;
        } else if (bound1 > bound2 && number < bound1 && number > bound2) {
            return true;
        }
        return false;
    }
}
