package no.kristiania.shop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDaoTest {

    private ProductDao productDao;

    @Test
    void shouldRetrieveSavedProducts(){
        Product product = sampleProduct();
        int id = productDao.insert(product);
        assertThat(ProductDao.retrieve(id));
                .hasNoNullFieldsOrProperties()
                .isEqualToComparingFieldByField(product);
    }
}
