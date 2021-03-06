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

package io.makerplayground.project;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class Begin extends NodeElement{
    public Begin(Project project) {
        super(200,20,85, 70, project);
    }

    public Begin(String name, double top, double left, Project project) {
        super(top, left, 85, 70, project);
        this.name = name;
    }

    protected Begin(double top, double left, double width, double height, Project project) {
        super(top, left, width, height, project);
    }

    public Project getProject() {
        return project;
    }

    @Override
    protected DiagramError checkError() {
        // name should contain only english alphanumeric characters, underscores and spaces and it should not be empty
        if (!name.matches("\\w[\\w| ]*")) {
            return DiagramError.BEGIN_INVALID_NAME;
        }
        return DiagramError.NONE;
    }

    public IntegerBinding getBeginCountBinding() {
        return Bindings.size(project.getBegin());
    }
}
