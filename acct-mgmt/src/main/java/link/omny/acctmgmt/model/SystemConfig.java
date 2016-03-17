package link.omny.acctmgmt.model;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;

@Data
public class SystemConfig {

    @Value("${omny.client.context:}")
    protected String clientContext;
}
