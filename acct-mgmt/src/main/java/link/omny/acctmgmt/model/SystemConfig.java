package link.omny.acctmgmt.model;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class SystemConfig {

    @Value("${omny.client.context:}")
    protected String clientContext;
}
