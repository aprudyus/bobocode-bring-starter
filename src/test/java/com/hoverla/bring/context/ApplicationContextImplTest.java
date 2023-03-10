package com.hoverla.bring.context;

import com.hoverla.bring.context.fixtures.autowired.success.AutowiredService;
import com.hoverla.bring.context.fixtures.autowired.success.TestService;
import com.hoverla.bring.context.fixtures.bean.ChildService;
import com.hoverla.bring.context.fixtures.bean.NotABean;
import com.hoverla.bring.context.fixtures.bean.ParentService;
import com.hoverla.bring.context.fixtures.bean.success.ChildServiceBeanOne;
import com.hoverla.bring.context.fixtures.bean.success.ChildServiceBeanTwo;
import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithName;
import com.hoverla.bring.context.fixtures.bean.success.TestBeanWithoutName;
import com.hoverla.bring.exception.NoSuchBeanException;
import com.hoverla.bring.exception.NoUniqueBeanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationContextImplTest {

    private static final String CHILD_SERVICE_BEAN_ONE_NAME = "childServiceBeanOne";
    private static final String CHILD_SERVICE_BEAN_TWO_NAME = "childServiceBean";

    private ApplicationContext applicationContext;

    @BeforeEach
    void init() {
        applicationContext = new ApplicationContextImpl("com.hoverla.bring.context.fixtures.bean");
    }

    @Test
    @Order(1)
    @DisplayName("Bean is successfully retrieved from the context by it's type")
    public void getBeanByClassReturnsCorrectBean() {
        TestBeanWithName beanWithName = applicationContext.getBean(TestBeanWithName.class);
        assertNotNull(beanWithName);

        TestBeanWithoutName beanWithoutName = applicationContext.getBean(TestBeanWithoutName.class);
        assertNotNull(beanWithoutName);
    }

    @Test
    @Order(2)
    @DisplayName("NoSuchBeanException is thrown if there is no such bean")
    void getBeanByTypeWhenIfThereIsNoSuchBean() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean(NotABean.class));
    }

    @Test
    @Order(3)
    @DisplayName("NoUniqueBeanException is thrown if there is no such bean")
    void getBeanByTypeIfThereIsADuplicateBean() {
        assertThrows(NoUniqueBeanException.class, () -> applicationContext.getBean(ParentService.class));
    }

    @Test
    @Order(4)
    @DisplayName("Bean is successfully retrieved from the context by it's name")
    public void getBeanByNameReturnsCorrectBean() {
        TestBeanWithName beanWithName = applicationContext.getBean("BeanName", TestBeanWithName.class);
        assertNotNull(beanWithName);

        TestBeanWithoutName beanWithoutName = applicationContext.getBean("testBeanWithoutName", TestBeanWithoutName.class);
        assertNotNull(beanWithoutName);
    }

    @Test
    @Order(5)
    @DisplayName("NoSuchBeanException is thrown if there is no such bean with a name")
    void getBeanByNameWhenIfThereIsNoSuchBean() {
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("Ho", TestBeanWithName.class));
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("ver", TestBeanWithoutName.class));
        assertThrows(NoSuchBeanException.class, () -> applicationContext.getBean("la", NotABean.class));
    }

    @Test
    @Order(6)
    @DisplayName("Bean is successfully retrieved by it's name and superclass")
    void getBeanByNameAndSuperClassReturnsCorrectBean() {
        ChildServiceBeanOne childServiceBeanOne = (ChildServiceBeanOne) applicationContext
            .getBean(CHILD_SERVICE_BEAN_ONE_NAME, ParentService.class);
        assertNotNull(childServiceBeanOne);

        ChildServiceBeanTwo childServiceBeanTwo = (ChildServiceBeanTwo) applicationContext
            .getBean(CHILD_SERVICE_BEAN_TWO_NAME, ParentService.class);
        assertNotNull(childServiceBeanTwo);
    }

    @Test
    @Order(7)
    @DisplayName("Get all beans by type returns the matching objects")
    void getAllBeansReturnsCorrectMap() {
        Map<String, ParentService> services = applicationContext.getAllBeans(ParentService.class);

        assertEquals(2, services.size());

        assertTrue(services.containsKey(CHILD_SERVICE_BEAN_ONE_NAME));
        assertTrue(services.containsKey(CHILD_SERVICE_BEAN_TWO_NAME));

        ChildServiceBeanOne autowiredService = (ChildServiceBeanOne) services.get(CHILD_SERVICE_BEAN_ONE_NAME);
        ChildServiceBeanTwo testService = (ChildServiceBeanTwo) services.get(CHILD_SERVICE_BEAN_TWO_NAME);
        assertNotNull(autowiredService);
        assertNotNull(testService);

        //ChildService is a subclass of ParentService, but it is not a bean
        assertTrue(ParentService.class.isAssignableFrom(ChildService.class));
        assertFalse(services.containsKey("childService"));
    }

    @Test
    @Order(8)
    @DisplayName("Autowiring has been successful")
    void autowiringFieldIsSetCorrectly() {
        ApplicationContext autowiringContext =
            new ApplicationContextImpl("com.hoverla.bring.context.fixtures.autowired.success");
        AutowiredService autowiredService = autowiringContext.getBean(AutowiredService.class);
        TestService testService = autowiringContext.getBean(TestService.class);
        assertEquals("A,B,C", testService.getLetters());
    }

    @Test
    @Order(9)
    @DisplayName("NoSuchBeanException is thrown if there is no bean which can be autowired")
    void autowiringFieldIfThereIsNoSuchBean() {
        assertThrows(NoSuchBeanException.class, () ->
            new ApplicationContextImpl("com.hoverla.bring.context.fixtures.autowired.nosuchbean"));
    }

    @Test
    @Order(10)
    @DisplayName("NoUniqueBeanException is thrown if there are > 1 bean of the same type for autowiring")
    void autowiringFieldIThereIsNoUniqueBean() {
        assertThrows(NoUniqueBeanException.class, () ->
            new ApplicationContextImpl("com.hoverla.bring.context.fixtures.autowired.nouniquebean"));
    }
}
