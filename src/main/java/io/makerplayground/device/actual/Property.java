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

package io.makerplayground.device.actual;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import lombok.Data;
import lombok.ToString;

@Data
@JsonDeserialize(using = PropertyDeserializer.class)
public class Property {
    private final String name;
    private final DataType dataType;
    private final boolean optional;
    @ToString.Exclude private final Object defaultValue;
    @ToString.Exclude private final Constraint constraint;
    @ToString.Exclude private final ControlType controlType;

    public double getMinimumValue() {
        return ((NumericConstraint) constraint).getMin();
    }

    public double getMaximumValue() {
        return ((NumericConstraint) constraint).getMax();
    }

    public Unit getUnit() {
        return ((NumericConstraint) constraint).getUnit();
    }
}
