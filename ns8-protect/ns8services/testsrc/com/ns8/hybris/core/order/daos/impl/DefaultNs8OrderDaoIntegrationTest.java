package com.ns8.hybris.core.order.daos.impl;

import com.ns8.hybris.core.order.daos.Ns8OrderDao;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.interceptors.DefaultAbstractOrderEntryPreparer;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;

@IntegrationTest
public class DefaultNs8OrderDaoIntegrationTest extends ServicelayerTransactionalTest {

    private static final String NEW_ORDER_CODE = "newOrder";
    private static final String WRONG_ORDER_CODE = "wrongOrder";

    @Resource
    private Ns8OrderDao orderDao;

    @Resource
    private UserService userService;
    @Resource
    private ModelService modelService;
    @Resource
    private TypeService typeService;
    @Resource
    private ConfigurationService configurationService;

    private CurrencyModel currency;
    private UserModel user;

    @Before
    public void setUp() {
        getOrCreateLanguage("de");

        setUpOrderEntryPrepare();
        currency = setUpCurrency();
        user = userService.getAnonymousUser();
        setUpProduct();
        setUpOrder(NEW_ORDER_CODE);
        setUpOrder(WRONG_ORDER_CODE);
    }

    @Test
    public void findOrderForCode_ShouldReturnJustTheCorrectOrder() {
        final Optional<OrderModel> result = orderDao.findOrderForCode(NEW_ORDER_CODE);

        assertTrue(result.isPresent());
        final OrderModel order = result.get();
        assertEquals(NEW_ORDER_CODE, order.getCode());
        assertNull(order.getOriginalVersion());
    }

    private OrderModel createOrder(final String code) {
        final OrderModel order = modelService.create(OrderModel.class);
        order.setCode(code);
        order.setDate(new Date());
        order.setCurrency(currency);
        order.setNet(Boolean.TRUE);
        order.setUser(user);
        return order;
    }

    private void setUpOrder(final String orderCode) {
        final OrderModel order = createOrder(orderCode);
        modelService.save(order);
        final OrderModel clonedOrder = modelService.clone(order);
        clonedOrder.setOriginalVersion(order);
        clonedOrder.setVersionID("v1");
        modelService.save(clonedOrder);
    }

    private void setUpProduct() {
        final UnitModel unit = setUpUnitModel();
        final CatalogVersionModel catalogVersion = setUpCatalogVersion();

        final ProductModel prod = modelService.create(ProductModel.class);
        prod.setCode("product");
        prod.setUnit(unit);
        prod.setCatalogVersion(catalogVersion);
        prod.setApprovalStatus(ArticleApprovalStatus.APPROVED);
    }

    private CatalogVersionModel setUpCatalogVersion() {
        final CatalogModel cat = modelService.create(CatalogModel.class);
        cat.setId("catalog");
        final CatalogVersionModel cv = modelService.create(CatalogVersionModel.class);
        cv.setCatalog(cat);
        cv.setVersion("online");
        cv.setActive(Boolean.TRUE);
        modelService.saveAll(cat, cv);
        return cv;
    }

    private CurrencyModel setUpCurrency() {
        currency = modelService.create(CurrencyModel.class);
        currency.setIsocode("XYZ");
        currency.setActive(Boolean.TRUE);
        currency.setConversion(1.0d);
        currency.setDigits(2);
        currency.setSymbol("CCC");
        modelService.save(currency);
        return currency;
    }

    private UnitModel setUpUnitModel() {
        final UnitModel unit = modelService.create(UnitModel.class);
        unit.setCode("unit");
        unit.setConversion(1.0d);
        unit.setUnitType("type");
        modelService.save(unit);
        return unit;
    }

    private void setUpOrderEntryPrepare() {
        final DefaultAbstractOrderEntryPreparer preparer = new DefaultAbstractOrderEntryPreparer();
        preparer.setTypeService(typeService);
        preparer.setConfigurationService(configurationService);
    }
}
