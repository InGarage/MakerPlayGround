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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.Unit;

import java.util.List;

/**
 * An interface for a constraint of some values ex. parameters of an action or possible values of an input device
 */
@JsonDeserialize(using = ConstraintDeserializer.class)
public interface Constraint {

    /**
     * Test a given number
     * @param d the value to be tested
     * @param unit the unit of the value to be tested
     * @throws IllegalArgumentException if there isn't any constraint specify for this unit
     * @return true if the value specify is valid otherwise return false
     */
    boolean test(double d, Unit unit);

    /**
     * Test a given string
     * @param s a string to be tested
     * @return true if the valid is valid otherwise return false
     */
    boolean test(String s);

    /**
     * A special value indicating no constraint (returns true for every tests)
     */
    Constraint NONE = new Constraint() {
        @Override
        public boolean test(double d, Unit unit) {
            return true;
        }

        @Override
        public boolean test(String s) {
            return true;
        }
    };

    /**
     * Create a constraint for a numeric value
     * @param min the minimum valid value (inclusive)
     * @param max the maximum valid value (inclusive)
     * @param type type of the value as an instance of {@link DataType}
     * @param unit unit of the value as an instance of {@link Unit}
     * @return an instance of {@link NumericConstraint}
     */
    static Constraint createNumericConstraint(double min, double max, DataType type, Unit unit) {
        return new NumericConstraint(min, max, type, unit);
    }

    /**
     * Create a constraint for a numeric value with multiple unit
     * @param constraintValues list of {@link NumericConstraint.Value} to be used to initialize the constraint object
     * @return an instance of {@link NumericConstraint}
     */
    static Constraint createNumericConstraint(List<NumericConstraint.Value> constraintValues) {
        return new NumericConstraint(constraintValues);
    }

    /**
     * Create a constrint that match only the specify string
     * @param s the string to be matched
     * @return an instance of {@link CategoricalConstraint}
     */
    static Constraint createCategoricalConstraint(String s) {
        return new CategoricalConstraint(s);
    }

    /**
     * Create a constrint to match a list of strings given
     * @param value list of string to be matched
     * @return an instance of {@link CategoricalConstraint}
     */
    static Constraint createCategoricalConstraint(List<String> value) {
        return new CategoricalConstraint(value);
    }
}
