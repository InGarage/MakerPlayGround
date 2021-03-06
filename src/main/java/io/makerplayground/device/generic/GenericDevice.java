/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device.generic;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.makerplayground.device.GenericDeviceType;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Value;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
public class GenericDevice {
    private final String name;
    @ToString.Exclude private final String description;
    @ToString.Exclude private final List<Action> action;
    @ToString.Exclude private final List<Condition> condition;
    @ToString.Exclude private final List<Value> value;
    @JacksonInject
    @ToString.Exclude private final GenericDeviceType type;

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
    public GenericDevice(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("type") GenericDeviceType type
            , @JsonProperty("action") List<Action> action, @JsonProperty("condition") List<Condition> condition, @JsonProperty("value") List<Value> value) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.action = Objects.nonNull(action) ? Collections.unmodifiableList(action) : Collections.emptyList();
        this.condition = Objects.nonNull(condition) ? Collections.unmodifiableList(condition) : Collections.emptyList();
        this.value = Objects.nonNull(value) ? Collections.unmodifiableList(value) : Collections.emptyList();
        if (this.action.isEmpty() && this.condition.isEmpty() && this.value.isEmpty()) {
            throw new IllegalStateException("The generic device needs to have one of action, condition, or value.");
        }
    }

    public Optional<Action> getAction(String name) {
        return action.stream().filter(action1 -> action1.getName().equals(name)).findFirst();
    }

    public Optional<Condition> getCondition(String name) {
        return condition.stream().filter(condition1 -> condition1.getName().equals(name)).findFirst();
    }

    public Optional<Value> getValue(String name) {
        return value.stream().filter(value1 -> value1.getName().equals(name)).findFirst();
    }

    public boolean hasValue() {
        return !value.isEmpty();
    }

    public boolean hasAction() {
        return !action.isEmpty();
    }

    public boolean hasCondition() {
        return !condition.isEmpty();
    }
}
