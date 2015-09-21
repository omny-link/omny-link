package link.omny.custmgmt.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.core.CrudInvoker;
import org.springframework.data.repository.init.RepositoriesPopulatedEvent;
import org.springframework.data.repository.init.RepositoryPopulator;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.data.repository.init.ResourceReaderRepositoryPopulator;
import org.springframework.data.repository.support.Repositories;
import org.springframework.util.Assert;

/**
 */
public class UpdatingResourceReaderRepositoryPopulator extends
        ResourceReaderRepositoryPopulator implements
        RepositoryPopulator,
        ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UpdatingResourceReaderRepositoryPopulator.class);

    private final ResourcePatternResolver resolver;
    private final ResourceReader reader;
    private final ClassLoader classLoader;

    private ApplicationEventPublisher publisher;
    private Collection<Resource> resources;

    /**
     * Creates a new {@link ResourceReaderRepositoryPopulator} using the given
     * {@link ResourceReader}.
     * 
     * @param reader
     *            must not be {@literal null}.
     */
    public UpdatingResourceReaderRepositoryPopulator(ResourceReader reader) {
        this(reader, null);
    }

    /**
     * Creates a a new {@link ResourceReaderRepositoryPopulator} using the given
     * {@link ResourceReader} and {@link ClassLoader}.
     * 
     * @param reader
     *            must not be {@literal null}.
     * @param classLoader
     */
    public UpdatingResourceReaderRepositoryPopulator(ResourceReader reader,
            ClassLoader classLoader) {
        // TODO this is necessary because spring data classes are not extensible
        super(reader, classLoader);
        Assert.notNull(reader);

        this.reader = reader;
        this.classLoader = classLoader;
        this.resolver = classLoader == null ? new PathMatchingResourcePatternResolver()
                : new PathMatchingResourcePatternResolver(classLoader);
    }

    /**
     * Configures the location of the {@link Resource}s to be used to initialize
     * the repositories.
     * 
     * @param location
     *            must not be {@literal null} or empty.
     * @throws IOException
     */
    public void setResourceLocation(String location) throws IOException {
        Assert.hasText(location);
        setResources(resolver.getResources(location));
    }

    /**
     * Configures the {@link Resource}s to be used to initialize the
     * repositories.
     * 
     * @param resources
     */
    public void setResources(Resource... resources) {
        this.resources = Arrays.asList(resources);
    }

    /**
     * @see org.springframework.context.ApplicationEventPublisherAware#
     *      setApplicationEventPublisher
     *      (org.springframework.context.ApplicationEventPublisher)
     */
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * @see org.springframework.data.repository.init.RepositoryPopulator#initialize()
     */
    public void populate(Repositories repositories) {

        for (Resource resource : resources) {

            LOGGER.info(String.format("Reading resource: %s", resource));

            Object result = readObjectFrom(resource);

            if (result instanceof Collection) {
                for (Object element : (Collection<?>) result) {
                    if (element != null) {
                        persist(element, repositories);
                    } else {
                        LOGGER.info("Skipping null element found in unmarshal result!");
                    }
                }
            } else {
                persist(result, repositories);
            }
        }

        if (publisher != null) {
            publisher.publishEvent(new RepositoriesPopulatedEvent(this,
                    repositories));
        }
    }

    /**
     * Reads the given resource into an {@link Object} using the configured
     * {@link ResourceReader}.
     * 
     * @param resource
     *            must not be {@literal null}.
     * @return
     */
    private Object readObjectFrom(Resource resource) {
        try {
            return reader.readFrom(resource, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Persists the given {@link Object} using a suitable repository.
     * 
     * @param object
     *            must not be {@literal null}.
     * @param repositories
     *            must not be {@literal null}.
     */
    @SuppressWarnings({ "unchecked" })
    private void persist(Object object, Repositories repositories) {

        CrudInvoker<Object> invoker = (CrudInvoker<Object>) repositories
                .getCrudInvoker(object.getClass());
        LOGGER.debug(String.format("Persisting %s using repository %s", object,
                invoker));
        try {
            invoker.invokeSave(object);
        } catch (DataIntegrityViolationException e) {
            LOGGER.warn(String.format(
                    "Ignoring failed data initialization: %1$s",
                    e.getMessage()));
        }
    }

}