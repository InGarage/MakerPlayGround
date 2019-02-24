/*
 * Copyright (c) 2018. The Maker Playground Authors.
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

package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.actual.*;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {
    private final Project project;
    private final ObjectProperty<Map<ProjectDevice, List<ActualDevice>>> compatibleDeviceList;
    private final ObjectProperty<Map<ProjectDevice, List<ProjectDevice>>> compatibleShareDeviceList;
    private final ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortList;
    private Runnable platformChangedCallback;
    private Runnable controllerChangedCallback;
    private Runnable deviceConfigChangedCallback;
    private Runnable configChangedCallback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = new SimpleObjectProperty<>();
        this.compatiblePortList = new SimpleObjectProperty<>();
        this.compatibleShareDeviceList = new SimpleObjectProperty<>();
        applyDeviceMapping();
    }

    private void applyDeviceMapping() {
        compatibleDeviceList.set(DeviceMapper.getSupportedDeviceList(project));
        compatibleShareDeviceList.set(DeviceMapper.getShareableDeviceList(project));
        compatiblePortList.set(DeviceMapper.getDeviceCompatiblePort(project));
    }

    public void setPlatformChangedCallback(Runnable callback) {
        platformChangedCallback = callback;
    }

    public void setControllerChangedCallback(Runnable callback) {
        controllerChangedCallback = callback;
    }

    public void setDeviceConfigChangedCallback(Runnable callback) {
        deviceConfigChangedCallback = callback;
    }

    public void clearDeviceConfigChangedCallback() {
        deviceConfigChangedCallback = null;
    }

    public void setConfigChangedCallback(Runnable callback) {
        configChangedCallback = callback;
    }

    List<CompatibleDevice> getCompatibleDevice(ProjectDevice projectDevice) {
        List<CompatibleDevice> compatibleDevices = new ArrayList<>();
        compatibleDevices.addAll(compatibleShareDeviceList.get().get(projectDevice).stream().map(CompatibleDevice::new).collect(Collectors.toList()));
        compatibleDevices.addAll(compatibleDeviceList.get().get(projectDevice).stream().map(CompatibleDevice::new).collect(Collectors.toList()));
        return compatibleDevices;
    }

    Map<Peripheral, List<List<DevicePort>>> getCompatiblePort(ProjectDevice projectDevice) {
        return compatiblePortList.get().get(projectDevice);
    }

    List<ActualDevice> getCompatibleControllerDevice() {
        return DeviceMapper.getSupportedController(project);
    }

    void setPlatform(Platform platform) {
        project.setPlatform(platform);
        applyDeviceMapping();
        if (platformChangedCallback != null) {
            platformChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    Platform getSelectedPlatform() {
        return project.getPlatform();
    }

    ActualDevice getController() {
        return project.getController();
    }

    void setController(ActualDevice device) {
        project.setController(device);
        applyDeviceMapping();
        if (controllerChangedCallback != null) {
            controllerChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    ActualDevice getSelectedController() {
        return project.getController();
    }

//    ObjectProperty<Map<ProjectDevice, List<ActualDevice>>> compatibleDeviceListProperty() {
//        return compatibleDeviceList;
//    }
//
//    ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortListProperty() {
//        return compatiblePortList;
//    }

    void setDevice(ProjectDevice projectDevice, CompatibleDevice device) {
        if (projectDevice.isActualDeviceSelected()) {
            projectDevice.removeAllDeviceConnection();
        }
        if (device.getActualDevice() != null) {
            projectDevice.setActualDevice(device.getActualDevice());
        } else {
            projectDevice.setParentDevice(device.getProjectDevice());
        }
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    void setPeripheral(ProjectDevice projectDevice, Peripheral peripheral, List<DevicePort> port) {
        // TODO: assume a device only has 1 peripheral
        projectDevice.setDeviceConnection(peripheral, port);
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    void clearPeripheral(ProjectDevice projectDevice, Peripheral peripheral) {
        projectDevice.removeDeviceConnection(peripheral);
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    Object getPropertyValue(ProjectDevice projectDevice, Property p) {
        return projectDevice.getPropertyValue(p);
    }

    void setPropertyValue(ProjectDevice projectDevice, Property p, Object value) {
        projectDevice.setPropertyValue(p, value);
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    Set<CloudPlatform> getCloudPlatformUsed() {
        return project.getCloudPlatformUsed();
    }

    String getCloudPlatfromParameterValue(CloudPlatform cloudPlatform, String name) {
        return project.getCloudPlatformParameter(cloudPlatform, name);
    }

    void setCloudPlatformParameter(CloudPlatform cloudPlatform, String parameterName, String value) {
        project.setCloudPlatformParameter(cloudPlatform, parameterName, value);
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    Set<ProjectDevice> getUsedDevice() {
        return project.getAllDeviceUsed();
    }

    Set<ProjectDevice> getUnusedDevice() {
        return  project.getAllDeviceUnused();
    }

    DeviceMapperResult autoAssignDevice() {
        DeviceMapperResult result = DeviceMapper.autoAssignDevices(project);
        System.out.println(result);
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
        return result;
    }
}
