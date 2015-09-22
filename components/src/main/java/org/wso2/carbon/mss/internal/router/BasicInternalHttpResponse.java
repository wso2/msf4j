/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mss.internal.router;

import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class that stores the response body in memory or as a file.
 */
public class BasicInternalHttpResponse implements InternalHttpResponse {
    private final int statusCode;
    private final InputSupplier<? extends InputStream> inputSupplier;

    public BasicInternalHttpResponse(int statusCode, InputSupplier<? extends InputStream> inputSupplier) {
        this.statusCode = statusCode;
        this.inputSupplier = inputSupplier;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public InputSupplier<? extends InputStream> getInputSupplier() throws IOException {
        return inputSupplier;
    }
}
