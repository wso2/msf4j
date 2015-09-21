/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.mss.internal.router;

import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface used to get the status code and content from calling another handler internally.
 */
public interface InternalHttpResponse {

    int getStatusCode();

    InputSupplier<? extends InputStream> getInputSupplier() throws IOException;
}
