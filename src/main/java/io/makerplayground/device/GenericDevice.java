/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Represent a generic device ex. led, motor, temp sensor, etc.
 */
public class GenericDevice {
    private final String name;
    private final String description;
    private final List<Action> action;
    private final List<Action> condition;
    private final List<Value> value;

    /**
     * Construct a new generic device. The constructor should only be invoked by the DeviceLibrary
     * in order to rebuild the library from file.
     * @param name name of this device ex. led, motor, etc.
     * @param action list of {@link Action} that this device can performed or can be preformed to this device
     *              ex. on, off, pressed, blink
     * @param value list of {@link Value} that can be generated by this device ex. the accelerometer have accel_x,
     *              accel_y and accel_z as it's values
     */
    @JsonCreator
    GenericDevice(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("action") List<Action> action
            , @JsonProperty("condition") List<Action> condition, @JsonProperty("value") List<Value> value) {
        this.name = name;
        this.description = description;
        this.action = Collections.unmodifiableList(action);
        this.condition = Collections.unmodifiableList(condition);
        this.value = Collections.unmodifiableList(value);
    }

    /**
     * Get the name of this device ex. led, motor, etc.
     * @return name of this device
     */
    public String getName() {
        return name;
    }

    /**
     * Get a list of {@link Action} that can be performed by or to this device
     * @return list of {@link Action} ex. on, off, press, blink, etc.
     */
    public List<Action> getAction() {
        return action;
    }

    public Action getAction(String name) {
        for (Action a : action) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public List<Action> getCondition() {
        return condition;
    }

    public Action getCondition(String name) {
        for (Action a : condition) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get a list of {@link Value} that can be generated by this device
     * @return list of {@link Value} ex. accel_x, accel_y, accel_z, temp, etc.
     */
    public List<Value> getValue() {
        return value;
    }

    public Value getValue(String name) {
        for (Value v : value) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "GenericDevice{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", action=" + action +
                ", condition=" + condition +
                ", value=" + value +
                '}';
    }
}
