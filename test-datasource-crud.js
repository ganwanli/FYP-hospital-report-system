#!/usr/bin/env node

/**
 * 数据源CRUD功能测试脚本
 * Test script for DataSource CRUD operations
 */

const BASE_URL = 'http://localhost:8080/api/datasource';

// 测试数据
const testDataSource = {
  datasourceName: "测试数据源CRUD",
  datasourceCode: "test_crud_datasource",
  databaseType: "MySQL",
  jdbcUrl: "jdbc:mysql://localhost:3306/hospital_report_system",
  username: "root",
  password: "ganwanli",
  driverClassName: "com.mysql.cj.jdbc.Driver",
  description: "CRUD测试数据源",
  initialSize: 5,
  minIdle: 5,
  maxActive: 20,
  connectionTimeout: 30000,
  status: 1
};

async function makeRequest(url, options = {}) {
  try {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    });
    
    const data = await response.text();
    let jsonData;
    try {
      jsonData = JSON.parse(data);
    } catch {
      jsonData = data;
    }
    
    return {
      status: response.status,
      data: jsonData
    };
  } catch (error) {
    console.error('Request failed:', error);
    return { status: 0, data: null, error: error.message };
  }
}

async function testCRUD() {
  console.log('🚀 开始测试数据源CRUD功能...\n');
  
  let createdId = null;
  
  try {
    // 1. 测试创建 (CREATE)
    console.log('1️⃣ 测试创建数据源...');
    const createResult = await makeRequest(BASE_URL, {
      method: 'POST',
      body: JSON.stringify(testDataSource)
    });
    
    if (createResult.status === 200 && createResult.data.code === 200) {
      console.log('✅ 创建成功:', createResult.data.message);
    } else {
      console.log('❌ 创建失败:', createResult);
      return;
    }
    
    // 2. 测试查询列表 (READ)
    console.log('\n2️⃣ 测试查询数据源列表...');
    const listResult = await makeRequest(`${BASE_URL}/list`);
    
    if (listResult.status === 200 && listResult.data.code === 200) {
      console.log('✅ 查询成功, 数据源数量:', listResult.data.data.length);
      
      // 找到刚创建的数据源
      const createdDataSource = listResult.data.data.find(ds => ds.datasourceCode === testDataSource.datasourceCode);
      if (createdDataSource) {
        createdId = createdDataSource.id;
        console.log('✅ 找到创建的数据源, ID:', createdId);
      } else {
        console.log('❌ 未找到创建的数据源');
        return;
      }
    } else {
      console.log('❌ 查询失败:', listResult);
      return;
    }
    
    // 3. 测试更新 (UPDATE)
    console.log('\n3️⃣ 测试更新数据源...');
    const updateData = {
      ...testDataSource,
      datasourceName: "测试数据源CRUD - 已更新",
      description: "CRUD测试数据源 - 已更新"
    };
    
    const updateResult = await makeRequest(`${BASE_URL}/${createdId}`, {
      method: 'PUT',
      body: JSON.stringify(updateData)
    });
    
    if (updateResult.status === 200 && updateResult.data.code === 200) {
      console.log('✅ 更新成功:', updateResult.data.message);
    } else {
      console.log('❌ 更新失败:', updateResult);
    }
    
    // 4. 验证更新结果
    console.log('\n4️⃣ 验证更新结果...');
    const verifyResult = await makeRequest(`${BASE_URL}/list`);
    
    if (verifyResult.status === 200) {
      const updatedDataSource = verifyResult.data.data.find(ds => ds.id === createdId);
      if (updatedDataSource && updatedDataSource.datasourceName === updateData.datasourceName) {
        console.log('✅ 更新验证成功');
      } else {
        console.log('❌ 更新验证失败');
      }
    }
    
    // 5. 测试删除 (DELETE)
    console.log('\n5️⃣ 测试删除数据源...');
    const deleteResult = await makeRequest(`${BASE_URL}/${createdId}`, {
      method: 'DELETE'
    });
    
    if (deleteResult.status === 200 && deleteResult.data.code === 200) {
      console.log('✅ 删除成功:', deleteResult.data.message);
    } else {
      console.log('❌ 删除失败:', deleteResult);
    }
    
    // 6. 验证删除结果
    console.log('\n6️⃣ 验证删除结果...');
    const finalResult = await makeRequest(`${BASE_URL}/list`);
    
    if (finalResult.status === 200) {
      const deletedDataSource = finalResult.data.data.find(ds => ds.id === createdId);
      if (!deletedDataSource) {
        console.log('✅ 删除验证成功 - 数据源已从列表中移除');
      } else {
        console.log('❌ 删除验证失败 - 数据源仍在列表中');
      }
    }
    
    console.log('\n🎉 CRUD测试完成！');
    
  } catch (error) {
    console.error('❌ 测试过程中发生错误:', error);
  }
}

// 运行测试
testCRUD();
