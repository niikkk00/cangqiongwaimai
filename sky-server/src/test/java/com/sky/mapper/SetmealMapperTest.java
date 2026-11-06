package com.sky.mapper;

import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class SetmealMapperTest {
    private Configuration configuration() throws Exception {
        Configuration config = new Configuration();
        config.setMapUnderscoreToCamelCase(true);
        for (String resource : new String[]{"mapper/SetmealMapper.xml", "mapper/SetmealDishMapper.xml"}) {
            try (InputStream input = Resources.getResourceAsStream(resource)) {
                new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
            }
        }
        return config;
    }

    @Test
    void pageSqlIncludesAllFiltersAndCategoryJoin() throws Exception {
        Configuration config = configuration();
        SetmealPageQueryDTO query = new SetmealPageQueryDTO();
        query.setName("套餐");
        query.setCategoryId(2147483648L);
        query.setStatus(0);
        BoundSql sql = config.getMappedStatement(SetmealMapper.class.getName() + ".pageQuery")
                .getBoundSql(query);
        assertTrue(sql.getSql().contains("left join category"));
        assertTrue(sql.getSql().contains("s.category_id = ?"));
        assertTrue(sql.getSql().contains("s.status = ?"));
        assertEquals(3, sql.getParameterMappings().size());
    }

    @Test
    void statusUpdateDoesNotOverwriteOtherFieldsAndBatchBindsRelationList() throws Exception {
        Configuration config = configuration();
        String update = config.getMappedStatement(SetmealMapper.class.getName() + ".update")
                .getBoundSql(Setmeal.builder().id(1L).status(0).build()).getSql();
        assertTrue(update.contains("status = ?"));
        assertFalse(update.contains("name ="));
        BoundSql batch = config.getMappedStatement(SetmealDishMapper.class.getName() + ".insertBatch")
                .getBoundSql(Collections.singletonMap("setmealDishes",
                        Collections.singletonList(SetmealDish.builder().dishId(10L).copies(1).build())));
        assertEquals(5, batch.getParameterMappings().size());
    }
}
