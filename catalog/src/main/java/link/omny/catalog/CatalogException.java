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
package link.omny.catalog;

public class CatalogException extends RuntimeException {

    private static final long serialVersionUID = 8596935185538284707L;

    public CatalogException() {
        super();
    }

    public CatalogException(String message, Throwable cause) {
        super(message, cause);
    }

    public CatalogException(String message) {
        super(message);
    }

    public CatalogException(Throwable cause) {
        super(cause);
    }

}
