/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.acctmgmt.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import link.omny.custmgmt.model.CustomField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OL_COHORT_CUSTOM")
@Data
@EqualsAndHashCode(callSuper = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
public class CustomCohortField extends CustomField {

    private static final long serialVersionUID = -7683896817261973079L;

    public CustomCohortField(String key, Object value) {
        super(key, value);
    }

    @ManyToOne(optional = false, targetEntity = CohortPerformance.class)
    private CohortPerformance cohort;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomCohortField other = (CustomCohortField) obj;
        if (cohort == null) {
            if (other.cohort != null)
                return false;
        } else if (cohort.getId() == null) {
            if (other.cohort.getId() != null)
                return false;
        } else if (!cohort.getId().equals(other.cohort.getId()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((cohort == null || cohort.getId() == null) ? 0 : cohort
                        .getId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "CustomContactField [contact=%s, id=%s, name=%s, value=%s]",
                (cohort == null || cohort.getId() == null) ? null : cohort
                        .getId(), getId(), getName(), getValue());
    }

}
