/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.analytics.factories;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import link.omny.analytics.api.AnalyticsNotFoundException;
import link.omny.analytics.api.ReportFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlReportFactory implements ReportFactory {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UrlReportFactory.class);

    public InputStream getReportStream(String reportUrl) {
        try {
            return new URL(reportUrl).openStream();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AnalyticsNotFoundException(e.getMessage());
        }
    }
}
