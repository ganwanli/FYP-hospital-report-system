# 数据字典分类管理API文档

## 概述

数据字典分类管理模块提供了完整的分类树形结构管理功能，支持分类的增删改查、树形结构构建、排序调整、状态管理等功能。

## 基础信息

- **基础路径**: `/api/dict/category`
- **认证方式**: Bearer Token（可选）
- **响应格式**: JSON
- **字符编码**: UTF-8

## 统一响应格式

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {},
    "timestamp": "2025-01-15T10:30:00"
}
```

## API接口列表

### 1. 获取分类树

**接口地址**: `GET /api/dict/category/tree`

**接口描述**: 获取完整的分类树形结构

**请求参数**: 无

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "categoryCode": "REPORT",
            "categoryName": "上报类",
            "parentId": 0,
            "categoryLevel": 1,
            "sortOrder": 1,
            "icon": "upload",
            "description": "用于政府部门上报的数据分类",
            "status": 1,
            "hasChildren": true,
            "fieldCount": 5,
            "createTime": "2025-01-15 10:30:00",
            "children": [
                {
                    "id": 6,
                    "categoryCode": "REPORT_MONTHLY",
                    "categoryName": "月报数据",
                    "parentId": 1,
                    "categoryLevel": 2,
                    "sortOrder": 1,
                    "icon": "calendar",
                    "description": "每月定期上报的数据",
                    "status": 1,
                    "hasChildren": false,
                    "fieldCount": 2,
                    "createTime": "2025-01-15 10:30:00",
                    "children": []
                }
            ]
        }
    ]
}
```

### 2. 获取启用的分类树

**接口地址**: `GET /api/dict/category/tree/enabled`

**接口描述**: 获取只包含启用状态的分类树形结构

**请求参数**: 无

**响应格式**: 同获取分类树接口

### 3. 获取分类列表（分页）

**接口地址**: `GET /api/dict/category/list`

**接口描述**: 分页获取分类列表，支持关键词搜索

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Long | 否 | 1 | 当前页码 |
| size | Long | 否 | 10 | 页大小 |
| keyword | String | 否 | - | 搜索关键词 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "categoryCode": "REPORT",
                "categoryName": "上报类",
                "parentId": 0,
                "parentName": null,
                "categoryLevel": 1,
                "sortOrder": 1,
                "icon": "upload",
                "description": "用于政府部门上报的数据分类",
                "status": 1,
                "statusText": "正常",
                "hasChildren": true,
                "categoryPath": "上报类",
                "fieldCount": 5,
                "createTime": "2025-01-15 10:30:00",
                "updateTime": "2025-01-15 10:30:00",
                "createBy": "system",
                "updateBy": "system"
            }
        ],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

### 4. 获取分类详情

**接口地址**: `GET /api/dict/category/{id}`

**接口描述**: 根据ID获取分类详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "categoryCode": "REPORT",
        "categoryName": "上报类",
        "parentId": 0,
        "parentName": null,
        "categoryLevel": 1,
        "sortOrder": 1,
        "icon": "upload",
        "description": "用于政府部门上报的数据分类",
        "status": 1,
        "statusText": "正常",
        "hasChildren": true,
        "categoryPath": "上报类",
        "fieldCount": 5,
        "createTime": "2025-01-15 10:30:00",
        "updateTime": "2025-01-15 10:30:00",
        "createBy": "system",
        "updateBy": "system"
    }
}
```

### 5. 新增分类

**接口地址**: `POST /api/dict/category`

**接口描述**: 创建新的分类

**请求体**:
```json
{
    "categoryCode": "NEW_CATEGORY",
    "categoryName": "新分类",
    "parentId": 0,
    "sortOrder": 1,
    "icon": "folder",
    "description": "新分类描述",
    "status": 1
}
```

**请求参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryCode | String | 是 | 分类编码，最大50字符 |
| categoryName | String | 是 | 分类名称，最大100字符 |
| parentId | Long | 是 | 父级分类ID，0表示顶级 |
| sortOrder | Integer | 否 | 排序序号 |
| icon | String | 否 | 图标，最大50字符 |
| description | String | 否 | 分类描述 |
| status | Integer | 否 | 状态：1-正常，0-禁用 |

**响应格式**: 同获取分类详情接口

### 6. 修改分类

**接口地址**: `PUT /api/dict/category/{id}`

**接口描述**: 根据ID修改分类信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类ID |

**请求体**: 同新增分类接口

**响应格式**: 同获取分类详情接口

### 7. 删除分类

**接口地址**: `DELETE /api/dict/category/{id}`

**接口描述**: 根据ID删除分类

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": true
}
```

**删除限制**:
- 有子分类时不允许删除
- 有关联字段时不允许删除

### 8. 批量删除分类

**接口地址**: `DELETE /api/dict/category/batch`

**接口描述**: 根据ID列表批量删除分类

**请求体**:
```json
[1, 2, 3]
```

**响应格式**: 同删除分类接口

### 9. 调整排序

**接口地址**: `POST /api/dict/category/sort`

**接口描述**: 批量调整分类的排序顺序

**请求体**:
```json
{
    "sortItems": [
        {
            "id": 1,
            "sortOrder": 2
        },
        {
            "id": 2,
            "sortOrder": 1
        }
    ]
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": true
}
```

### 10. 批量修改状态

**接口地址**: `PUT /api/dict/category/status`

**接口描述**: 批量启用或禁用分类

**请求体**:
```json
{
    "ids": [1, 2, 3],
    "status": 0
}
```

**请求参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | List<Long> | 是 | 分类ID列表 |
| status | Integer | 是 | 状态：1-正常，0-禁用 |

**响应格式**: 同删除分类接口

### 11. 获取子分类

**接口地址**: `GET /api/dict/category/children/{id}`

**接口描述**: 获取指定分类的直接子分类列表

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 父级分类ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 6,
            "categoryCode": "REPORT_MONTHLY",
            "categoryName": "月报数据",
            "parentId": 1,
            "parentName": "上报类",
            "categoryLevel": 2,
            "sortOrder": 1,
            "icon": "calendar",
            "description": "每月定期上报的数据",
            "status": 1,
            "statusText": "正常",
            "hasChildren": false,
            "categoryPath": "上报类 / 月报数据",
            "fieldCount": 2,
            "createTime": "2025-01-15 10:30:00",
            "updateTime": "2025-01-15 10:30:00",
            "createBy": "system",
            "updateBy": "system"
        }
    ]
}
```

### 12. 获取分类路径

**接口地址**: `GET /api/dict/category/path/{id}`

**接口描述**: 获取从根节点到指定分类的完整路径

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "categoryCode": "REPORT",
            "categoryName": "上报类",
            "categoryLevel": 1
        },
        {
            "id": 6,
            "categoryCode": "REPORT_MONTHLY",
            "categoryName": "月报数据",
            "categoryLevel": 2
        }
    ]
}
```

### 13. 检查编码是否存在

**接口地址**: `GET /api/dict/category/check-code`

**接口描述**: 检查分类编码是否已存在

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryCode | String | 是 | 分类编码 |
| excludeId | Long | 否 | 排除的分类ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": true
}
```

### 14. 根据层级获取分类

**接口地址**: `GET /api/dict/category/level/{level}`

**接口描述**: 获取指定层级的所有分类

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| level | Integer | 是 | 分类层级 |

**响应格式**: 同获取子分类接口

### 15. 获取顶级分类

**接口地址**: `GET /api/dict/category/top-level`

**接口描述**: 获取所有顶级分类（层级为1的分类）

**请求参数**: 无

**响应格式**: 同获取子分类接口

### 16. 根据编码获取分类

**接口地址**: `GET /api/dict/category/code/{categoryCode}`

**接口描述**: 根据分类编码获取分类信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryCode | String | 是 | 分类编码 |

**响应格式**: 同获取分类详情接口

### 17. 搜索分类

**接口地址**: `GET /api/dict/category/search`

**接口描述**: 根据关键词搜索分类

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |

**响应格式**: 同获取子分类接口

### 18. 刷新缓存

**接口地址**: `POST /api/dict/category/refresh-cache`

**接口描述**: 刷新分类树缓存

**请求参数**: 无

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": true
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 常见错误信息

| 错误信息 | 说明 | 解决方案 |
|----------|------|----------|
| 分类编码已存在 | 创建或修改时分类编码重复 | 使用唯一的分类编码 |
| 分类不存在 | 操作的分类ID不存在 | 检查分类ID是否正确 |
| 该分类下存在子分类，不能删除 | 删除有子分类的分类 | 先删除子分类或使用级联删除 |
| 该分类下存在关联字段，不能删除 | 删除有关联字段的分类 | 先删除关联字段 |
| 不能将自己设置为父级分类 | 修改时将自己设为父级 | 选择其他分类作为父级 |
| 不能将子分类设置为父级分类 | 修改时形成循环引用 | 选择正确的父级分类 |

## 使用示例

### JavaScript示例

```javascript
// 获取分类树
fetch('/api/dict/category/tree')
  .then(response => response.json())
  .then(data => {
    console.log('分类树:', data.data);
  });

// 新增分类
fetch('/api/dict/category', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    categoryCode: 'NEW_CATEGORY',
    categoryName: '新分类',
    parentId: 0,
    sortOrder: 1,
    status: 1
  })
})
.then(response => response.json())
.then(data => {
  console.log('新增结果:', data);
});
```

### Java示例

```java
// 使用RestTemplate调用API
RestTemplate restTemplate = new RestTemplate();

// 获取分类树
String url = "http://localhost:8080/api/dict/category/tree";
Result<List<CategoryTreeVO>> result = restTemplate.getForObject(url, Result.class);

// 新增分类
DictCategoryDTO dto = new DictCategoryDTO();
dto.setCategoryCode("NEW_CATEGORY");
dto.setCategoryName("新分类");
dto.setParentId(0L);
dto.setStatus(1);

Result<DictCategoryVO> createResult = restTemplate.postForObject(
    "http://localhost:8080/api/dict/category", 
    dto, 
    Result.class
);
```

## 注意事项

1. **性能考虑**: 分类树查询使用了缓存，修改操作会自动清除相关缓存
2. **并发安全**: 排序更新时需要考虑并发修改问题
3. **数据一致性**: 删除操作会检查关联数据，确保数据一致性
4. **参数校验**: 所有输入参数都会进行校验，请确保参数格式正确
5. **权限控制**: 根据需要可以添加权限检查
6. **国际化**: 错误信息支持国际化，可根据Accept-Language头返回对应语言的错误信息
