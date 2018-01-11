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
package link.omny.custmgmt.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import link.omny.custmgmt.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
public class MemoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCsvController() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/omny/memos")
                .contentType("text/csv")
                .accept(MediaType.TEXT_PLAIN_VALUE)
                .content(getMemoListInCsv());
        this.mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("size: 3"))
                .andDo(MockMvcResultHandlers.print());
        ;
    }

    public String getMemoListInCsv() {
        return "id, name, phoneNumber\n1,Joe,123-212-3233\n2,Sara,132,232,3111\n"
                + "3,Mike,111-222-3333\n";
    }
}
