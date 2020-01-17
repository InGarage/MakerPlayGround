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

package io.makerplayground.util;

public class AzureSubscription {
    private final String name;
    private final String id;
    private final String tenantId;
    private final String userName;

    public AzureSubscription(String name, String id, String tenantId, String userName) {
        this.name = name;
        this.id = id;
        this.tenantId = tenantId;
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserName() {
        return userName;
    }
}
