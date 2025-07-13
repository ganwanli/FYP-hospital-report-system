// Test script to verify SQL Template API functionality
const fetch = require('node-fetch');

const API_BASE_URL = 'http://localhost:8081';

async function testSqlTemplateAPI() {
  console.log('Testing SQL Template API...');
  
  try {
    // Test 1: Get all templates
    console.log('\n1. Testing GET /api/sql-templates');
    const response1 = await fetch(`${API_BASE_URL}/api/sql-templates?page=1&size=10&isActive=true`);
    const data1 = await response1.json();
    console.log('Response status:', response1.status);
    console.log('Response data:', JSON.stringify(data1, null, 2));
    
    // Test 2: Get templates by category
    console.log('\n2. Testing GET /api/sql-templates with category filter');
    const response2 = await fetch(`${API_BASE_URL}/api/sql-templates?page=1&size=10&isActive=true&templateCategory=门诊`);
    const data2 = await response2.json();
    console.log('Response status:', response2.status);
    console.log('Response data:', JSON.stringify(data2, null, 2));
    
    // Test 3: Get all categories
    console.log('\n3. Testing GET /api/sql-templates/categories');
    const response3 = await fetch(`${API_BASE_URL}/api/sql-templates/categories`);
    const data3 = await response3.json();
    console.log('Response status:', response3.status);
    console.log('Response data:', JSON.stringify(data3, null, 2));
    
  } catch (error) {
    console.error('Error testing API:', error);
  }
}

// Run the test
testSqlTemplateAPI();
