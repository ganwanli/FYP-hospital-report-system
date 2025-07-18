# SqlTemplateVersionMapper Insert方法添加文档

## 🎯 问题背景

用户发现在版本管理相关的Service中调用了 `versionMapper.insert(version)`，但在 `SqlTemplateVersionMapper` 接口中没有找到对应的insert语句。

为了确保版本ID正确回填和更好的控制版本创建过程，我们添加了自定义的insert方法。

## 🔧 解决方案

### 1. 添加自定义Insert方法

在 `SqlTemplateVersionMapper.java` 中添加了自定义的 `insertVersion` 方法：

```java
@Insert("INSERT INTO sql_template_version (" +
        "template_id, version_number, version_description, template_content, " +
        "change_log, is_current, created_by, created_time, " +
        "template_hash, parent_version_id, validation_status, validation_message, " +
        "approval_status, approved_by, approved_time" +
        ") VALUES (" +
        "#{templateId}, #{versionNumber}, #{versionDescription}, #{templateContent}, " +
        "#{changeLog}, #{isCurrent}, #{createdBy}, #{createdTime}, " +
        "#{templateHash}, #{parentVersionId}, #{validationStatus}, #{validationMessage}, " +
        "#{approvalStatus}, #{approvedBy}, #{approvedTime}" +
        ")")
@Options(useGeneratedKeys = true, keyProperty = "versionId", keyColumn = "version_id")
int insertVersion(SqlTemplateVersion version);
```

### 2. 关键特性

#### ✅ **自动ID回填**
- `@Options(useGeneratedKeys = true, keyProperty = "versionId", keyColumn = "version_id")`
- 确保数据库自增ID正确回填到实体对象

#### ✅ **完整字段映射**
- 包含所有SqlTemplateVersion实体类中的数据库字段
- 支持版本关系（父版本ID）
- 支持审批流程字段

#### ✅ **版本控制特性**
- 支持版本号管理
- 支持当前版本标记
- 支持版本间的父子关系

### 3. 更新Service实现

#### SqlTemplateVersionServiceImpl.java
```java
// 原来使用MyBatis-Plus默认方法
save(version);

// 现在使用自定义方法
int insertResult = baseMapper.insertVersion(version);
if (insertResult <= 0) {
    throw new RuntimeException("Failed to insert version");
}
```

#### SqlTemplateServiceImpl.java
```java
// createInitialVersion方法中
int insertResult = versionMapper.insertVersion(version);
if (insertResult <= 0) {
    throw new RuntimeException("Failed to create initial version");
}

// createNewVersion方法中
int insertResult = versionMapper.insertVersion(version);
if (insertResult <= 0) {
    throw new RuntimeException("Failed to create new version");
}
```

## 🧪 测试验证

### 创建的测试类

`SqlTemplateVersionMapperTest.java` 包含以下测试用例：

1. **testInsertVersion()** - 测试完整字段版本插入
2. **testInsertVersionMinimalFields()** - 测试最小必填字段插入
3. **testInsertMultipleVersions()** - 测试同一模板的多个版本
4. **testInsertVersionWithParent()** - 测试父子版本关系

### 运行测试

```bash
cd hospital-report-system/backend
mvn test -Dtest=SqlTemplateVersionMapperTest
```

### 预期结果

```
✅ 测试通过：版本插入成功，生成的ID=1
✅ 验证通过：版本数据已正确插入数据库
✅ 最小字段测试通过：ID=2
✅ 多版本插入测试通过：
版本1 ID: 3 (当前版本: true)
版本2 ID: 4 (当前版本: false)
✅ 父子版本测试通过：
父版本 ID: 5
子版本 ID: 6 (父版本: 5)
```

## 📋 字段映射表

| 实体字段 | 数据库字段 | 类型 | 必填 | 说明 |
|---------|-----------|------|------|------|
| `templateId` | `template_id` | BIGINT | ✅ | 关联的模板ID |
| `versionNumber` | `version_number` | VARCHAR | ✅ | 版本号(如v1.0) |
| `versionDescription` | `version_description` | VARCHAR | ❌ | 版本描述 |
| `templateContent` | `template_content` | LONGTEXT | ✅ | SQL内容 |
| `changeLog` | `change_log` | TEXT | ❌ | 变更日志 |
| `isCurrent` | `is_current` | BOOLEAN | ✅ | 是否当前版本 |
| `createdBy` | `created_by` | BIGINT | ✅ | 创建人ID |
| `createdTime` | `created_time` | DATETIME | ✅ | 创建时间 |
| `templateHash` | `template_hash` | VARCHAR | ❌ | 内容哈希值 |
| `parentVersionId` | `parent_version_id` | BIGINT | ❌ | 父版本ID |
| `validationStatus` | `validation_status` | VARCHAR | ✅ | 验证状态 |
| `validationMessage` | `validation_message` | TEXT | ❌ | 验证消息 |
| `approvalStatus` | `approval_status` | VARCHAR | ✅ | 审批状态 |
| `approvedBy` | `approved_by` | BIGINT | ❌ | 审批人ID |
| `approvedTime` | `approved_time` | DATETIME | ❌ | 审批时间 |

## 🔍 使用场景

### 1. 创建初始版本
```java
private void createInitialVersion(SqlTemplate template) {
    SqlTemplateVersion version = new SqlTemplateVersion();
    version.setTemplateId(template.getTemplateId());
    version.setVersionNumber("v1.0");
    version.setVersionDescription("Initial version");
    version.setTemplateContent(template.getTemplateContent());
    version.setChangeLog("Initial template creation");
    version.setIsCurrent(true);
    version.setCreatedBy(template.getCreatedBy());
    version.setCreatedTime(LocalDateTime.now());
    version.setTemplateHash(template.getTemplateHash());
    version.setValidationStatus("PENDING");
    version.setApprovalStatus("PENDING");
    
    int result = versionMapper.insertVersion(version);
    if (result > 0 && version.getVersionId() != null) {
        log.info("Created initial version {} for template {}", 
                version.getVersionNumber(), template.getTemplateId());
    }
}
```

### 2. 创建新版本
```java
private void createNewVersion(SqlTemplate template, SqlTemplate existingTemplate) {
    // 生成新版本号
    Integer maxVersion = versionMapper.selectMaxVersionNumber(template.getTemplateId());
    String newVersionNumber = "v" + (maxVersion != null ? maxVersion + 1 : 1) + ".0";
    
    // 清除当前版本标记
    versionMapper.clearCurrentVersion(template.getTemplateId());
    
    // 创建新版本
    SqlTemplateVersion version = new SqlTemplateVersion();
    version.setTemplateId(template.getTemplateId());
    version.setVersionNumber(newVersionNumber);
    version.setVersionDescription("Template updated");
    version.setTemplateContent(template.getTemplateContent());
    version.setChangeLog("Template content updated");
    version.setIsCurrent(true);
    version.setCreatedBy(template.getUpdatedBy());
    version.setCreatedTime(LocalDateTime.now());
    version.setTemplateHash(template.getTemplateHash());
    version.setValidationStatus("PENDING");
    version.setApprovalStatus("PENDING");
    
    // 设置父版本关系
    SqlTemplateVersion currentVersion = versionMapper.selectCurrentVersion(template.getTemplateId());
    if (currentVersion != null) {
        version.setParentVersionId(currentVersion.getVersionId());
    }
    
    int result = versionMapper.insertVersion(version);
    if (result > 0) {
        log.info("Created new version {} for template {}", 
                version.getVersionNumber(), template.getTemplateId());
    }
}
```

### 3. 版本服务中使用
```java
@Override
@Transactional
public SqlTemplateVersion createVersion(SqlTemplateVersion version) {
    try {
        // 生成内容哈希
        if (version.getTemplateContent() != null) {
            String hash = DigestUtils.md5DigestAsHex(version.getTemplateContent().getBytes());
            version.setTemplateHash(hash);
        }

        // 设置创建时间
        version.setCreatedTime(LocalDateTime.now());

        // 如果没有指定版本号，自动生成
        if (version.getVersionNumber() == null || version.getVersionNumber().isEmpty()) {
            version.setVersionNumber(generateNextVersionNumber(version.getTemplateId()));
        }

        // 如果设置为当前版本，先清除其他当前版本
        if (Boolean.TRUE.equals(version.getIsCurrent())) {
            baseMapper.clearCurrentVersion(version.getTemplateId());
        }

        // 保存版本
        int insertResult = baseMapper.insertVersion(version);
        if (insertResult <= 0) {
            throw new RuntimeException("Failed to insert version");
        }
        
        log.info("Created new version {} for template {}", 
                version.getVersionNumber(), version.getTemplateId());
        return version;
    } catch (Exception e) {
        log.error("Failed to create version for template {}: {}", 
                version.getTemplateId(), e.getMessage());
        throw new RuntimeException("创建版本失败", e);
    }
}
```

## ⚠️ 注意事项

1. **版本唯一性**：确保同一模板下版本号唯一
2. **当前版本管理**：同一模板只能有一个当前版本
3. **父版本关系**：正确设置版本间的父子关系
4. **事务管理**：版本创建应在事务中进行
5. **ID回填验证**：插入后检查versionId是否不为null

## 🔗 相关文件

- `src/main/java/com/hospital/report/mapper/SqlTemplateVersionMapper.java` - Mapper接口
- `src/main/java/com/hospital/report/entity/SqlTemplateVersion.java` - 实体类
- `src/main/java/com/hospital/report/service/impl/SqlTemplateVersionServiceImpl.java` - 版本服务实现
- `src/main/java/com/hospital/report/service/impl/SqlTemplateServiceImpl.java` - 模板服务实现
- `src/test/java/com/hospital/report/mapper/SqlTemplateVersionMapperTest.java` - 测试类

## 🎉 总结

通过添加自定义的 `insertVersion` 方法，我们解决了：

1. ✅ **版本ID回填问题** - 确保自增ID正确回填
2. ✅ **版本关系管理** - 支持父子版本关系
3. ✅ **审批流程支持** - 完整的审批状态管理
4. ✅ **版本控制功能** - 完整的版本生命周期管理
5. ✅ **测试覆盖** - 全面的测试用例验证

现在 `versionMapper.insertVersion(version)` 方法可以正常工作，并且能够正确回填自增ID，支持完整的版本管理功能！
