package com.hospital.report.mapper;

import com.hospital.report.entity.SqlTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class SqlTemplateMapperTest {

    @Autowired
    private SqlTemplateMapper sqlTemplateMapper;

    @Test
    public void testInsertTemplate() {
        // 准备测试数据
        SqlTemplate template = new SqlTemplate();
        template.setTemplateName("测试模板");
        template.setTemplateDescription("这是一个测试模板");
        template.setTemplateContent("SELECT * FROM test_table WHERE id = #{id}");
        template.setTemplateCategory("测试分类");
        template.setTemplateVersion("1.0");
        template.setBusinessType("门诊");
        template.setUsageType("日报");
        template.setDepartmentCode("内科");
        template.setTags("测试,示例");
        template.setDatabaseType("MySQL");
        template.setTemplateHash("test_hash_123");
        template.setIsActive(true);
        template.setIsPublic(false);
        template.setApprovalStatus("PENDING");
        template.setCreatedBy(1L);
        template.setCreatedTime(LocalDateTime.now());
        template.setUpdatedBy(1L);
        template.setUpdatedTime(LocalDateTime.now());
        template.setUsageCount(0);
        template.setLastUsedTime(null);
        template.setExecutionTimeout(30);
        template.setMaxRows(1000);

        // 执行插入
        int result = sqlTemplateMapper.insertTemplate(template);

        // 验证结果
        assertEquals(1, result, "插入应该返回1");
        assertNotNull(template.getTemplateId(), "模板ID应该被自动生成");
        assertTrue(template.getTemplateId() > 0, "模板ID应该大于0");

        System.out.println("✅ 测试通过：模板插入成功，生成的ID=" + template.getTemplateId());

        // 验证数据是否真的插入到数据库
        SqlTemplate insertedTemplate = sqlTemplateMapper.selectById(template.getTemplateId());
        assertNotNull(insertedTemplate, "应该能从数据库中查询到插入的模板");
        assertEquals("测试模板", insertedTemplate.getTemplateName());
        assertEquals("这是一个测试模板", insertedTemplate.getTemplateDescription());
        assertEquals("SELECT * FROM test_table WHERE id = #{id}", insertedTemplate.getTemplateContent());

        System.out.println("✅ 验证通过：数据已正确插入数据库");
    }

    @Test
    public void testInsertTemplateMinimalFields() {
        // 测试只填写必填字段的情况
        SqlTemplate template = new SqlTemplate();
        template.setTemplateName("最小字段测试");
        template.setTemplateDescription("最小字段测试描述");
        template.setTemplateContent("SELECT 1");
        template.setBusinessType("门诊");
        template.setUsageType("日报");
        template.setDepartmentCode("内科");
        template.setDatabaseType("MySQL");
        template.setIsActive(true);
        template.setIsPublic(false);
        template.setApprovalStatus("PENDING");
        template.setCreatedBy(1L);
        template.setCreatedTime(LocalDateTime.now());
        template.setUpdatedBy(1L);
        template.setUpdatedTime(LocalDateTime.now());
        template.setUsageCount(0);

        // 执行插入
        int result = sqlTemplateMapper.insertTemplate(template);

        // 验证结果
        assertEquals(1, result, "插入应该返回1");
        assertNotNull(template.getTemplateId(), "模板ID应该被自动生成");

        System.out.println("✅ 最小字段测试通过：ID=" + template.getTemplateId());
    }

    @Test
    public void testMultipleInserts() {
        // 测试多次插入，验证ID是否正确递增
        SqlTemplate template1 = createTestTemplate("模板1");
        SqlTemplate template2 = createTestTemplate("模板2");

        int result1 = sqlTemplateMapper.insertTemplate(template1);
        int result2 = sqlTemplateMapper.insertTemplate(template2);

        assertEquals(1, result1);
        assertEquals(1, result2);
        assertNotNull(template1.getTemplateId());
        assertNotNull(template2.getTemplateId());
        assertNotEquals(template1.getTemplateId(), template2.getTemplateId(), "两个模板的ID应该不同");

        System.out.println("✅ 多次插入测试通过：");
        System.out.println("模板1 ID: " + template1.getTemplateId());
        System.out.println("模板2 ID: " + template2.getTemplateId());
    }

    private SqlTemplate createTestTemplate(String name) {
        SqlTemplate template = new SqlTemplate();
        template.setTemplateName(name);
        template.setTemplateDescription("测试模板：" + name);
        template.setTemplateContent("SELECT * FROM test WHERE name = '" + name + "'");
        template.setBusinessType("门诊");
        template.setUsageType("日报");
        template.setDepartmentCode("内科");
        template.setDatabaseType("MySQL");
        template.setIsActive(true);
        template.setIsPublic(false);
        template.setApprovalStatus("PENDING");
        template.setCreatedBy(1L);
        template.setCreatedTime(LocalDateTime.now());
        template.setUpdatedBy(1L);
        template.setUpdatedTime(LocalDateTime.now());
        template.setUsageCount(0);
        return template;
    }
}
