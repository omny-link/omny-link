package link.omny.catalog.internal;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("tstephen");
        // Can use Spring Security to return currently logged in user
        // return ((User)
        // SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()
    }
}
