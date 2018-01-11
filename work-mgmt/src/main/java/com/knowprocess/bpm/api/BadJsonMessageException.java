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
package com.knowprocess.bpm.api;

/**
 * An exception to return minimal information to help with debugging API calls
 * but not reveal any internal information.
 *
 * @author Tim Stephenson
 */
public class BadJsonMessageException extends RuntimeException {

    private static final long serialVersionUID = 7459492641770180894L;

    public BadJsonMessageException(String msg) {
        super(msg);
    }

}
