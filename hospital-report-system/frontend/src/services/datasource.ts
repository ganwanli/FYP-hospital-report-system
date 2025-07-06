// 数据源相关API
export async function getDatasourceList() {
  // 临时返回空数组
  return [];
}

export async function createDatasource(data: any) {
  // 临时实现
  return { success: true };
}

export async function updateDatasource(id: string, data: any) {
  // 临时实现
  return { success: true };
}

export async function deleteDatasource(id: string) {
  // 临时实现
  return { success: true };
}

export async function testConnection(config: any) {
  // 临时实现
  return { success: true, message: '连接测试成功' };
}