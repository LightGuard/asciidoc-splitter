package com.redhat.documentation.asciidoc.extraction.model;

import java.util.Objects;

/**
 * Task to handle multiple location and pushable location types.
 */
public class Task {
    private final Location location;
    private final PushableLocation pushableLocation;

    public Task(Location location, PushableLocation pushableLocation) {
        this.location = location;
        this.pushableLocation = pushableLocation;
    }

    /**
     * Gets location
     * @return location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets pushable location
     * @return pushable location
     */
    public PushableLocation getPushableLocation() {
        return pushableLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return Objects.equals(location, task.location) &&
                Objects.equals(pushableLocation, task.pushableLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, pushableLocation);
    }

    @Override
    public String toString() {
        return "Task{" +
                "location=" + location +
                ", pushableLocation=" + pushableLocation +
                '}';
    }
}
