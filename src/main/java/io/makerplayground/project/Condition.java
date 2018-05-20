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

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.project.expression.Expression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 */
@JsonSerialize(using = ConditionSerializer.class)
public class Condition extends NodeElement {
    private final StringProperty name;
    private final ObservableList<UserSetting> setting;
    private ObjectProperty<TimeCondition> timeCondition;

    private final ObservableList<UserSetting> unmodifiableSetting;

    Condition() {
        super(20,250,185, 115);

        this.name = new SimpleStringProperty();
        this.setting = FXCollections.observableArrayList();
        this.timeCondition = new SimpleObjectProperty<>();

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(setting);
    }

    public Condition(double top, double left, double width, double height
            , String name, List<UserSetting> setting, TimeCondition timeCondition) {
        super(top, left, width, height);

        this.name = new SimpleStringProperty(name);
        this.setting = FXCollections.observableList(setting);
        this.timeCondition = new SimpleObjectProperty<>(timeCondition);

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(this.setting);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void addDevice(ProjectDevice device) {
        setting.add(new UserSetting(device, false));
    }

    public void removeDevice(ProjectDevice device) {
        for (int i = setting.size() - 1; i >= 0; i--) {
            if (setting.get(i).getDevice() == device) {
                setting.remove(i);
            }
        }
    }

    public ObservableList<UserSetting> getSetting() {
        return unmodifiableSetting;
    }

    public boolean isError() {
        return (setting.isEmpty() && timeCondition.get() == null)
                // When the action has some parameters (it is not 'compare'), it's value must not be null
                // otherwise we assume that it is compare
                || (setting.stream().map(UserSetting::getValueMap)
                .filter(valueMap -> !valueMap.isEmpty())
                .flatMap(valueMap -> valueMap.values().stream())
                .anyMatch(Objects::isNull))
                // every expression must be valid
                || !(setting.stream().flatMap(userSetting -> userSetting.getExpression().values().stream())
                .allMatch(Expression::isValid))
                // at least one expression must be enable
                || !(setting.stream().filter(userSetting -> userSetting.getValueMap().isEmpty())
                .map(userSetting -> userSetting.getExpression().values())
                .filter(expressions -> !expressions.isEmpty())
                .allMatch(expressions -> expressions.stream().anyMatch(Expression::isEnable)));
    }

    public Optional<TimeCondition> getTimeCondition() {
        return Optional.ofNullable(timeCondition.get());
    }

    public ObjectProperty<TimeCondition> timeConditionProperty() {
        return timeCondition;
    }

    public void setTimeCondition(TimeCondition condition) {
        if (timeCondition.get() != null) {
            throw new IllegalStateException("Time condition is already existed!!!");
        }
        timeCondition.set(condition);
    }

    public void removeTimeCondition() {
        timeCondition.set(null);
    }
}
