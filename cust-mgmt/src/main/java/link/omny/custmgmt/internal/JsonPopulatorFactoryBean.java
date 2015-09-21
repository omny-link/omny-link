package link.omny.custmgmt.internal;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.init.Jackson2ResourceReader;
import org.springframework.data.repository.init.RepositoryPopulator;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.data.repository.init.ResourceReaderRepositoryPopulator;
import org.springframework.data.repository.support.Repositories;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPopulatorFactoryBean extends
        AbstractFactoryBean<ResourceReaderRepositoryPopulator> implements
        ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private ObjectMapper mapper;

    private Resource[] resources;
    private RepositoryPopulator populator;
    private ApplicationContext context;

    /**
     * Configures the {@link Resource}s to be used to load objects from and
     * initialize the repositories eventually.
     * 
     * @param resources
     *            must not be {@literal null}.
     */
    public void setResources(Resource[] resources) {
        Assert.notNull(resources, "Resources must not be null!");
        this.resources = resources.clone();
    }

    /**
     * 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext
     *      (org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    /**
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType
     *      ()
     */
    @Override
    public Class<?> getObjectType() {
        return ResourceReaderRepositoryPopulator.class;
    }

    /**
     * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance
     *      ()
     */
    @Override
    protected ResourceReaderRepositoryPopulator createInstance() {

        ResourceReaderRepositoryPopulator initializer = new UpdatingResourceReaderRepositoryPopulator(
                getResourceReader());
        initializer.setResources(resources);
        initializer.setApplicationEventPublisher(context);

        this.populator = initializer;

        return initializer;
    }

    /**
     * 
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org
     *      .springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext().equals(context)) {
            Repositories repositories = new Repositories(
                    event.getApplicationContext());
            populator.populate(repositories);
        }
    }

    /**
     * Configures the {@link ObjectMapper} to be used.
     * 
     * @param mapper
     */
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 
     * @see org.springframework.data.repository.init.
     *      AbstractRepositoryPopulatorFactoryBean#getResourceReader()
     */
    protected ResourceReader getResourceReader() {
        return new Jackson2ResourceReader(mapper);
    }
}
