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

package io.makerplayground.project.term;

import io.makerplayground.device.shared.AnimatedValue;
import io.makerplayground.device.shared.StringCategoricalAnimatedValue;

import java.util.stream.Collectors;

public class StringAnimationTerm extends Term {
    public StringAnimationTerm(AnimatedValue value) {
        super(Type.STRING_ANIMATED, value);
    }

    @Override
    public AnimatedValue getValue() {
        return (AnimatedValue) value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public String toString() {
        return ((StringCategoricalAnimatedValue) getValue()).getKeyValues().stream()
                .map((kv) -> kv.getValue().getTerms().stream().map(Term::toString).collect(Collectors.joining(" ")))
                .collect(Collectors.joining(", "));
    }
}