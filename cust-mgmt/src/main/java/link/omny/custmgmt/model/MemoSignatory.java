/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
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
package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import link.omny.supportservices.model.Auditable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OL_MEMO_SIG")
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "memo" })
@NoArgsConstructor
public class MemoSignatory extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = -6720919821444374916L;

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "memoSigIdSeq", sequenceName = "ol_memo_sig_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memoSigIdSeq")
    @JsonProperty
    private Long id;

    @JsonProperty
    @Size(max = 150)
    @Column(name = "email")
    private String email;

    @JsonProperty
    @Column(name = "name")
    private String name;
    
    @Size(max = 1000)
    @Column(name = "tabs")
    private String tabs;

    @ManyToOne(optional = false)
    private Memo memo;

    public MemoSignatory(String name, String email, int x, int y, int page) {
        setName(name);
        setEmail(email);
        setSignHereTab(new SignHereTab(x, y, page));
    }
    
    @JsonProperty
    @Transient
    protected List<SignHereTab> getSignHereTabs() {
        List<SignHereTab> signHereTabs = new ArrayList<SignHereTab>();
        if (tabs != null) {
            String[] tmp = tabs.split(";");
            for (String tab : tmp) {
                signHereTabs.add(new SignHereTab(tab));
            }
        }
        return signHereTabs;
    }

    protected void setSignHereTabs(List<SignHereTab> signHereTabs) {
        tabs = getSignHereTabs().stream()
                .map(SignHereTab::toString)
                .collect(Collectors.joining(";"));
    }
    
    @JsonProperty
    @Transient
    public SignHereTab getSignHereTab() {
        try {
            return getSignHereTabs().get(0);
        } catch (NullPointerException e) {
            return new SignHereTab(0,0,1);
        }
    }
    
    public void setSignHereTab(SignHereTab tab) {
        tabs = tab.toString();   
    }
    
    public String formatForDocuSign() {
        if (getSignHereTabs().size() > 1) {
            throw new IllegalStateException("Only one sign here tab is currently supported");
        }
        return String.format("{" 
        + "\"name\": \"%1$s\","  
        + "\"email\": \"%2$s\","  
        + "\"recipientId\": \"%3$s\"," 
        + "\"tabs\": { \"signHereTabs\": [{ \"xPosition\": \"%4$d\", \"yPosition\": \"%5$d\", \"documentId\": \"1\", \"pageNumber\": \"%6$d\" }]}}",
        name, email, "", getSignHereTabs().get(0).getX(), getSignHereTabs().get(0).getY(), getSignHereTabs().get(0).getPage());
    }

    @Data
    @NoArgsConstructor
    public class SignHereTab {
        @JsonProperty
        private int x;
        @JsonProperty
        private int y;
        @JsonProperty
        private int page;


        public SignHereTab(String tab) {
            String[] args = tab.split(",");
            try {
                x = Integer.parseInt(args[0]);
            } catch (NumberFormatException| ArrayIndexOutOfBoundsException  e) {
                x = 0;
            }
            try {
                y = Integer.parseInt(args[1]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                y = 0;
            }
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException| ArrayIndexOutOfBoundsException  e) {
                page = 1;
            }
        }
        
        public SignHereTab(int x, int y, int page) {
            this.x = x;
            this.y = y;
            this.page = page;
        }

        public SignHereTab(Object toCopy) {
            if (!(toCopy instanceof SignHereTab)) {
                throw new IllegalArgumentException();
            }
            this.x = ((SignHereTab) toCopy).getX();
            this.y = ((SignHereTab) toCopy).getY();
            this.page = ((SignHereTab) toCopy).getPage();
        }
        
        public String toString() {
            return String.format("%1$d,%2$d,%3$d", x, y, page);
        }

    }
    
}
