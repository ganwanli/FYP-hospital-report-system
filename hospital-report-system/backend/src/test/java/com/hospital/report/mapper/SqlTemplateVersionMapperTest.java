package com.hospital.report.mapper;

import com.hospital.report.entity.SqlTemplateVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class SqlTemplateVersionMapperTest {

    @Autowired
    private SqlTemplateVersionMapper versionMapper;

    @Test
    public void testInsertVersion() {
        // 准备测试数据
        SqlTemplateVersion version = new SqlTemplateVersion();
        version.setTemplateId(1L); // 假设模板ID为1
        version.setVersionNumber("v1.0");
        version.setVersionDescription("初始版本");
        version.setTemplateContent("SELECT * FROM test_table WHERE id = #{id}");
        version.setChangeLog("创建初始版本");
        version.setIsCurrent(true);
        version.setCreatedBy(1L);
        version.setCreatedTime(LocalDateTime.now());
        version.setTemplateHash(DigestUtils.md5DigestAsHex("SELECT * FROM test_table WHERE id = #{id}".getBytes()));
        version.setParentVersionId(null);
        version.setValidationStatus("PENDING");
        version.setValidationMessage(null);
        version.setApprovalStatus("PENDING");
        version.setApprovedBy(null);
        version.setApprovedTime(null);

        // 执行插入
        int result = versionMapper.insertVersion(version);

        // 验证结果
        assertEquals(1, result, "插入应该返回1");
        assertNotNull(version.getVersionId(), "版本ID应该被自动生成");
        assertTrue(version.getVersionId() > 0, "版本ID应该大于0");

        System.out.println("✅ 测试通过：版本插入成功，生成的ID=" + version.getVersionId());

        // 验证数据是否真的插入到数据库
        SqlTemplateVersion insertedVersion = versionMapper.selectById(version.getVersionId());
        assertNotNull(insertedVersion, "应该能从数据库中查询到插入的版本");
        assertEquals("v1.0", insertedVersion.getVersionNumber());
        assertEquals("初始版本", insertedVersion.getVersionDescription());
        assertEquals("SELECT * FROM test_table WHERE id = #{id}", insertedVersion.getTemplateContent());
        assertTrue(insertedVersion.getIsCurrent());

        System.out.println("✅ 验证通过：版本数据已正确插入数据库");
    }

    @Test
    public void testInsertVersionMinimalFields() {
        // 测试只填写必填字段的情况
        SqlTemplateVersion version = new SqlTemplateVersion();
        version.setTemplateId(1L);
        version.setVersionNumber("v1.1");
        version.setTemplateContent("SELECT COUNT(*) FROM test_table");
        version.setIsCurrent(false);
        version.setCreatedBy(1L);
        version.setCreatedTime(LocalDateTime.now());
        version.setValidationStatus("PENDING");
        version.setApprovalStatus("PENDING");

        // 执行插入
        int result = versionMapper.insertVersion(version);

        // 验证结果
        assertEquals(1, result, "插入应该返回1");
        assertNotNull(version.getVersionId(), "版本ID应该被自动生成");

        System.out.println("✅ 最小字段测试通过：ID=" + version.getVersionId());
    }

    @Test
    public void testInsertMultipleVersions() {
        // 测试同一模板的多个版本
        Long templateId = 1L;
        
        SqlTemplateVersion version1 = createTestVersion(templateId, "v1.0", "初始版本", true);
        SqlTemplateVersion version2 = createTestVersion(templateId, "v1.1", "更新版本", false);

        int result1 = versionMapper.insertVersion(version1);
        int result2 = versionMapper.insertVersion(version2);

        assertEquals(1, result1);
        assertEquals(1, result2);
        assertNotNull(version1.getVersionId());
        assertNotNull(version2.getVersionId());
        assertNotEquals(version1.getVersionId(), version2.getVersionId(), "两个版本的ID应该不同");

        System.out.println("✅ 多版本插入测试通过：");
        System.out.println("版本1 ID: " + version1.getVersionId() + " (当前版本: " + version1.getIsCurrent() + ")");
        System.out.println("版本2 ID: " + version2.getVersionId() + " (当前版本: " + version2.getIsCurrent() + ")");
    }

    @Test
    public void testInsertVersionWithParent() {
        // 测试带父版本的版本插入
        Long templateId = 1L;
        
        // 先插入父版本
        SqlTemplateVersion parentVersion = createTestVersion(templateId, "v1.0", "父版本", false);
        int parentResult = versionMapper.insertVersion(parentVersion);
        assertEquals(1, parentResult);
        assertNotNull(parentVersion.getVersionId());

        // 再插入子版本
        SqlTemplateVersion childVersion = createTestVersion(templateId, "v1.1", "子版本", true);
        childVersion.setParentVersionId(parentVersion.getVersionId());
        
        int childResult = versionMapper.insertVersion(childVersion);
        assertEquals(1, childResult);
        assertNotNull(childVersion.getVersionId());

        // 验证父子关系
        SqlTemplateVersion insertedChild = versionMapper.selectById(childVersion.getVersionId());
        assertEquals(parentVersion.getVersionId(), insertedChild.getParentVersionId());

        System.out.println("✅ 父子版本测试通过：");
        System.out.println("父版本 ID: " + parentVersion.getVersionId());
        System.out.println("子版本 ID: " + childVersion.getVersionId() + " (父版本: " + insertedChild.getParentVersionId() + ")");
    }

    private SqlTemplateVersion createTestVersion(Long templateId, String versionNumber, String description, boolean isCurrent) {
        SqlTemplateVersion version = new SqlTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersionNumber(versionNumber);
        version.setVersionDescription(description);
        version.setTemplateContent("SELECT * FROM test WHERE version = '" + versionNumber + "'");
        version.setChangeLog("创建版本：" + versionNumber);
        version.setIsCurrent(isCurrent);
        version.setCreatedBy(1L);
        version.setCreatedTime(LocalDateTime.now());
        version.setTemplateHash(DigestUtils.md5DigestAsHex(("content_" + versionNumber).getBytes()));
        version.setValidationStatus("PENDING");
        version.setApprovalStatus("PENDING");
        return version;
    }
}
