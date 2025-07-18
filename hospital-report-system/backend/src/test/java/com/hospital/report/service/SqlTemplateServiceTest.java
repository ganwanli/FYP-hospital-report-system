package com.hospital.report.service;

import com.hospital.report.entity.SqlTemplate;
import com.hospital.report.entity.SqlTemplateParameter;
import com.hospital.report.entity.SqlTemplateVersion;
import com.hospital.report.mapper.SqlTemplateMapper;
import com.hospital.report.mapper.SqlTemplateVersionMapper;
import com.hospital.report.service.impl.SqlTemplateServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class SqlTemplateServiceTest {

    @Autowired
    private SqlTemplateService sqlTemplateService;

    @Autowired
    private SqlTemplateMapper sqlTemplateMapper;

    @Autowired
    private SqlTemplateVersionMapper versionMapper;

    @Test
    public void testCreateTemplate() {
        // 准备测试数据
        SqlTemplate template = new SqlTemplate();
        template.setTemplateName("测试模板");
        template.setTemplateDescription("这是一个测试模板");
        template.setTemplateContent("SELECT * FROM test_table WHERE id = #{id}");
        template.setBusinessType("门诊");
        template.setUsageType("日报");
        template.setDepartmentCode("内科");
        template.setTags("测试,示例");
        template.setDatabaseType("MySQL");
        template.setCreatedBy(1L);

        // 添加参数
        List<SqlTemplateParameter> parameters = new ArrayList<>();
        SqlTemplateParameter param = new SqlTemplateParameter();
        param.setParameterName("id");
        param.setParameterType("INTEGER");
        param.setParameterDescription("记录ID");
        param.setIsRequired(true);
        param.setDefaultValue("1");
        parameters.add(param);
        template.setParameters(parameters);

        // 执行创建
        SqlTemplate createdTemplate = sqlTemplateService.createTemplate(template);

        // 验证结果
        assertNotNull(createdTemplate, "创建的模板不应为null");
        assertNotNull(createdTemplate.getTemplateId(), "模板ID应该被正确生成");
        assertTrue(createdTemplate.getTemplateId() > 0, "模板ID应该大于0");

        // 验证数据库中的数据
        SqlTemplate dbTemplate = sqlTemplateMapper.selectById(createdTemplate.getTemplateId());
        assertNotNull(dbTemplate, "数据库中应该存在该模板");
        assertEquals("测试模板", dbTemplate.getTemplateName());
        assertEquals("这是一个测试模板", dbTemplate.getTemplateDescription());

        // 验证版本是否创建
        SqlTemplateVersion currentVersion = versionMapper.selectCurrentVersion(createdTemplate.getTemplateId());
        assertNotNull(currentVersion, "应该创建初始版本");
        assertEquals("v1.0", currentVersion.getVersionNumber());
        assertTrue(currentVersion.getIsCurrent());

        System.out.println("✅ 测试通过：模板创建成功，ID=" + createdTemplate.getTemplateId());
    }

    @Test
    public void testCreateTemplateWithoutParameters() {
        // 测试不带参数的模板创建
        SqlTemplate template = new SqlTemplate();
        template.setTemplateName("简单测试模板");
        template.setTemplateDescription("不带参数的测试模板");
        template.setTemplateContent("SELECT COUNT(*) FROM users");
        template.setBusinessType("住院");
        template.setUsageType("月报");
        template.setDepartmentCode("外科");
        template.setDatabaseType("MySQL");
        template.setCreatedBy(1L);

        // 执行创建
        SqlTemplate createdTemplate = sqlTemplateService.createTemplate(template);

        // 验证结果
        assertNotNull(createdTemplate.getTemplateId(), "模板ID应该被正确生成");
        
        // 验证版本创建
        SqlTemplateVersion currentVersion = versionMapper.selectCurrentVersion(createdTemplate.getTemplateId());
        assertNotNull(currentVersion, "应该创建初始版本");

        System.out.println("✅ 测试通过：简单模板创建成功，ID=" + createdTemplate.getTemplateId());
    }

    @Test
    public void testTemplateIdGeneration() {
        // 测试ID生成机制
        SqlTemplate template1 = createTestTemplate("模板1");
        SqlTemplate template2 = createTestTemplate("模板2");

        assertNotNull(template1.getTemplateId());
        assertNotNull(template2.getTemplateId());
        assertNotEquals(template1.getTemplateId(), template2.getTemplateId(), "两个模板的ID应该不同");

        System.out.println("✅ 测试通过：ID生成机制正常");
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
        template.setCreatedBy(1L);

        return sqlTemplateService.createTemplate(template);
    }
}
