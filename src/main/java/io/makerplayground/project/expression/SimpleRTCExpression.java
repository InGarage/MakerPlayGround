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

package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.shared.RealTimeClock;
import io.makerplayground.project.term.RTCTerm;

public class SimpleRTCExpression extends Expression {

    public SimpleRTCExpression(RealTimeClock rtc) {
        super(Type.DATETIME);
        terms.add(new RTCTerm(rtc));
    }

    @JsonIgnore
    public RealTimeClock getRealTimeClock() {
        return (RealTimeClock) getTerms().get(0).getValue();
    }

    public SimpleRTCExpression(SimpleRTCExpression rtcExpression) {
        super(rtcExpression);
    }

    @Override
    public SimpleRTCExpression deepCopy() {
        return new SimpleRTCExpression(this);
    }


}
