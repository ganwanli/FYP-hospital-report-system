// 测试SQL资产管理过滤逻辑
console.log('=== SQL资产管理过滤功能测试 ===');

// 模拟分类数据
const categories = {
  business: [
    { value: "outpatient", label: "门诊" },
    { value: "inpatient", label: "住院" },
    { value: "emergency", label: "急诊" },
    { value: "performance", label: "绩效" },
    { value: "internal_medicine", label: "内科" },
    { value: "surgery", label: "外科" }
  ],
  department: [
    { value: "info_dept", label: "信息科" },
    { value: "finance_dept", label: "财务科" },
    { value: "hr_dept", label: "人事科" },
    { value: "emergency_dept", label: "急诊科" },
    { value: "internal_medicine_dept", label: "内科" },
    { value: "surgery_dept", label: "外科" }
  ],
  usage: [
    { value: "reporting", label: "上报类" },
    { value: "internal_ops", label: "内部运营" },
    { value: "performance_eval", label: "绩效考核" },
    { value: "quality_control", label: "质量监控" }
  ]
};

// 模拟SQL资产数据
const mockAssets = [
  {
    templateId: 1,
    templateName: "门诊患者统计",
    templateDescription: "统计每日门诊患者数量和科室分布情况",
    templateCategory: "门诊",
    templateContent: "SELECT department, COUNT(*) as patient_count FROM outpatient_records WHERE visit_date = CURDATE() GROUP BY department;",
    databaseType: "MySQL",
    isActive: true,
    isPublic: true,
    createdByName: "张医生",
    createdTime: "2024-01-15 10:30",
    updatedTime: "2024-01-20 14:20",
    usageCount: 0
  },
  {
    templateId: 2,
    templateName: "住院收入分析",
    templateDescription: "分析各科室住院患者收入情况",
    templateCategory: "住院",
    templateContent: "SELECT d.name as department, SUM(i.amount) as total_revenue FROM inpatient_bills i JOIN departments d ON i.department_id = d.id WHERE i.bill_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY d.name;",
    databaseType: "MySQL",
    isActive: true,
    isPublic: true,
    createdByName: "李会计",
    createdTime: "2024-01-10 09:15",
    updatedTime: "2024-01-18 16:45",
    usageCount: 0
  },
  {
    templateId: 3,
    templateName: "医生绩效考核",
    templateDescription: "医生月度绩效考核指标统计",
    templateCategory: "绩效",
    templateContent: "SELECT doctor_name, patient_count, surgery_count, satisfaction_score FROM doctor_performance WHERE month = MONTH(CURDATE()) AND year = YEAR(CURDATE());",
    databaseType: "MySQL",
    isActive: true,
    isPublic: true,
    createdByName: "王主任",
    createdTime: "2024-01-05 11:20",
    updatedTime: "2024-01-22 13:10",
    usageCount: 0
  },
  {
    templateId: 4,
    templateName: "急诊科日报表",
    templateDescription: "急诊科每日患者统计",
    templateCategory: "急诊",
    templateContent: "SELECT COUNT(*) as total_patients, AVG(waiting_time) as avg_waiting_time FROM emergency_records WHERE visit_date = CURDATE();",
    databaseType: "MySQL",
    isActive: true,
    isPublic: true,
    createdByName: "急诊科医生",
    createdTime: "2024-01-08 14:30",
    updatedTime: "2024-01-25 09:15",
    usageCount: 0
  },
  {
    templateId: 5,
    templateName: "内科门诊统计",
    templateDescription: "内科门诊患者统计",
    templateCategory: "内科",
    templateContent: "SELECT COUNT(*) as patient_count FROM outpatient_records WHERE department = '内科' AND visit_date = CURDATE();",
    databaseType: "MySQL",
    isActive: true,
    isPublic: true,
    createdByName: "内科医生",
    createdTime: "2024-01-12 16:20",
    updatedTime: "2024-01-28 11:45",
    usageCount: 0
  },
  {
    templateId: 6,
    templateName: "外科手术统计",
    templateDescription: "外科手术数量统计",
    templateCategory: "外科",
    templateContent: "SELECT COUNT(*) as surgery_count FROM surgery_records WHERE department = '外科' AND surgery_date = CURDATE();",
    databaseType: "MySQL",
    isActive: true,
    isPublic: true,
    createdByName: "外科医生",
    createdTime: "2024-01-15 08:30",
    updatedTime: "2024-01-30 15:20",
    usageCount: 0
  },
  {
    templateId: 7,
    templateName: "上报类数据统计",
    templateDescription: "用于上报的数据统计",
    templateCategory: "上报类",
    templateContent: "SELECT * FROM report_data WHERE report_type = 'GOVERNMENT' AND report_date = CURDATE();",
    databaseType: "MySQL",
    isActive: true,
    isPublic: true,
    createdByName: "统计员",
    createdTime: "2024-01-18 10:15",
    updatedTime: "2024-02-01 14:30",
    usageCount: 0
  },
  {
    templateId: 8,
    templateName: "内部运营分析",
    templateDescription: "内部运营数据分析",
    templateCategory: "内部运营",
    templateContent: "SELECT department, revenue, cost, profit FROM operational_data WHERE data_date = CURDATE();",
    databaseType: "MySQL",
    isActive: true,
    isPublic: true,
    createdByName: "运营分析师",
    createdTime: "2024-01-20 13:45",
    updatedTime: "2024-02-03 16:10",
    usageCount: 0
  }
];

// 获取分类标签函数
function getCategoryLabel(type, value) {
  if (value === "all" || !value) return null;
  const category = categories[type]?.find(cat => cat.value === value);
  return category ? category.label : value;
}

// 根据分类生成mock数据的函数
function generateMockDataByCategory(category, type) {
  console.log(`\n--- 测试过滤: ${type} 分类 = ${category} ---`);
  
  // 如果没有选择分类或选择了"all"，返回所有数据
  if (!category || category === "all" || category === "") {
    console.log('返回所有数据 (未选择分类或选择了"全部")');
    return mockAssets;
  }

  // 根据分类类型和选择的分类过滤数据
  const categoryLabel = getCategoryLabel(type, category);
  console.log(`分类标签: ${category} -> ${categoryLabel}`);
  
  const filteredData = mockAssets.filter(item => item.templateCategory === categoryLabel);
  console.log(`过滤结果: ${filteredData.length} 项`);
  filteredData.forEach(item => {
    console.log(`  - ${item.templateName} (${item.templateCategory})`);
  });
  
  return filteredData;
}

// 测试不同的过滤场景
console.log('\n=== 开始测试过滤功能 ===');

// 测试1: 业务分类 - 门诊
console.log('\n【测试1】业务分类 - 门诊');
const test1 = generateMockDataByCategory("outpatient", "business");

// 测试2: 业务分类 - 住院
console.log('\n【测试2】业务分类 - 住院');
const test2 = generateMockDataByCategory("inpatient", "business");

// 测试3: 业务分类 - 绩效
console.log('\n【测试3】业务分类 - 绩效');
const test3 = generateMockDataByCategory("performance", "business");

// 测试4: 业务分类 - 内科
console.log('\n【测试4】业务分类 - 内科');
const test4 = generateMockDataByCategory("internal_medicine", "business");

// 测试5: 全部分类
console.log('\n【测试5】全部分类');
const test5 = generateMockDataByCategory("all", "business");

// 测试6: 空分类
console.log('\n【测试6】空分类');
const test6 = generateMockDataByCategory("", "business");

console.log('\n=== 测试完成 ===');
console.log(`总共有 ${mockAssets.length} 个SQL模板`);
console.log('分类分布:');
const categoryCount = {};
mockAssets.forEach(asset => {
  categoryCount[asset.templateCategory] = (categoryCount[asset.templateCategory] || 0) + 1;
});
Object.entries(categoryCount).forEach(([category, count]) => {
  console.log(`  ${category}: ${count} 个`);
});
