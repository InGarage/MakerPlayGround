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

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class Pin {
    private final String refTo;
    private final String codingName;
    private final VoltageLevel voltageLevel;
    private final double minVoltage;
    private final double maxVoltage;
    private final List<PinFunction> function;
    private final boolean hasHwSerial;
    @ToString.Exclude private final double x;
    @ToString.Exclude private final double y;

    public boolean hasHwSerial() {
        return hasHwSerial;
    }
}
