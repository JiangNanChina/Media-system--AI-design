#!/usr/bin/env node

/**
 * API调用检查脚本
 * 
 * 功能：
 * 1. 扫描所有Vue文件中的API调用
 * 2. 检查是否有重复的/api前缀
 * 3. 生成API调用统计报告
 * 4. 提供修复建议
 * 
 * 使用方法：
 * node scripts/check-api-calls.js
 */

try {
  var fs = require('fs');
  var path = require('path');
} catch (e) {
  console.error('需要在Node.js环境中运行此脚本');
  process.exit(1);
}

// 颜色输出工具
const colors = {
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m',
  white: '\x1b[37m',
  reset: '\x1b[0m'
};

function colorize(text, color) {
  return `${colors[color]}${text}${colors.reset}`;
}

// 扫描目录
function scanDirectory(dir, fileList = []) {
  const files = fs.readdirSync(dir);
  
  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    
    if (stat.isDirectory()) {
      if (!file.startsWith('.') && file !== 'node_modules') {
        scanDirectory(filePath, fileList);
      }
    } else if (file.endsWith('.vue') || file.endsWith('.js') || file.endsWith('.ts')) {
      fileList.push(filePath);
    }
  });
  
  return fileList;
}

// 检查API调用
function checkApiCalls(content, filePath) {
  const lines = content.split('\n');
  const issues = [];
  const apiCalls = [];
  
  // 正则模式
  const patterns = [
    // request.get('/api/...') 等
    /request\.(get|post|put|delete|patch)\s*\(\s*['`"]([^'`"]+)['`"]/g,
    // await request.get(`/api/...`) 等
    /await\s+request\.(get|post|put|delete|patch)\s*\(\s*['`"]([^'`"]+)['`"]/g
  ];
  
  lines.forEach((line, index) => {
    patterns.forEach(pattern => {
      let match;
      while ((match = pattern.exec(line)) !== null) {
        const method = match[1];
        const url = match[2];
        
        apiCalls.push({
          method: method.toUpperCase(),
          url,
          line: index + 1,
          content: line.trim()
        });
        
        // 检查重复前缀
        if (url.startsWith('/api/')) {
          issues.push({
            type: 'duplicate-prefix',
            method: method.toUpperCase(),
            url,
            line: index + 1,
            content: line.trim(),
            suggestion: url.replace('/api', '')
          });
        }
        
        // 检查URL格式
        if (!url.startsWith('/')) {
          issues.push({
            type: 'invalid-url',
            method: method.toUpperCase(),
            url,
            line: index + 1,
            content: line.trim(),
            suggestion: `URL应该以 '/' 开头`
          });
        }
      }
    });
  });
  
  return { issues, apiCalls };
}

// 生成报告
function generateReport(results) {
  console.log(colorize('\n📋 API调用检查报告', 'cyan'));
  console.log(colorize('='.repeat(50), 'cyan'));
  
  let totalFiles = 0;
  let totalApiCalls = 0;
  let totalIssues = 0;
  const issuesByType = {};
  const apiCallsByMethod = {};
  
  Object.keys(results).forEach(filePath => {
    const { issues, apiCalls } = results[filePath];
    
    totalFiles++;
    totalApiCalls += apiCalls.length;
    totalIssues += issues.length;
    
    // 统计API调用方法
    apiCalls.forEach(call => {
      apiCallsByMethod[call.method] = (apiCallsByMethod[call.method] || 0) + 1;
    });
    
    // 统计问题类型
    issues.forEach(issue => {
      issuesByType[issue.type] = (issuesByType[issue.type] || 0) + 1;
    });
    
    if (issues.length > 0) {
      console.log(colorize(`\n🔍 ${filePath}`, 'yellow'));
      issues.forEach(issue => {
        const icon = issue.type === 'duplicate-prefix' ? '❌' : '⚠️';
        console.log(`  ${icon} 第${issue.line}行: ${colorize(issue.method, 'blue')} ${colorize(issue.url, 'red')}`);
        console.log(`     建议: ${colorize(issue.suggestion, 'green')}`);
        console.log(`     代码: ${issue.content}`);
      });
    }
  });
  
  // 总结统计
  console.log(colorize('\n📊 统计信息', 'magenta'));
  console.log(`总文件数: ${totalFiles}`);
  console.log(`总API调用数: ${totalApiCalls}`);
  console.log(`问题数量: ${colorize(totalIssues, totalIssues > 0 ? 'red' : 'green')}`);
  
  if (Object.keys(apiCallsByMethod).length > 0) {
    console.log(colorize('\n📈 API方法统计:', 'blue'));
    Object.entries(apiCallsByMethod).forEach(([method, count]) => {
      console.log(`  ${method}: ${count}`);
    });
  }
  
  if (Object.keys(issuesByType).length > 0) {
    console.log(colorize('\n⚠️ 问题类型统计:', 'red'));
    Object.entries(issuesByType).forEach(([type, count]) => {
      const typeNames = {
        'duplicate-prefix': '重复API前缀',
        'invalid-url': '无效URL格式'
      };
      console.log(`  ${typeNames[type] || type}: ${count}`);
    });
  }
  
  // 建议
  console.log(colorize('\n💡 修复建议:', 'green'));
  if (issuesByType['duplicate-prefix'] > 0) {
    console.log('  1. 移除所有API调用中的 "/api" 前缀');
    console.log('  2. 确保 axios baseURL 配置为 "/api"');
  }
  if (totalIssues === 0) {
    console.log('  🎉 所有API调用都符合规范！');
  }
}

// 主函数
function main() {
  console.log(colorize('🔍 开始扫描API调用...', 'cyan'));
  
  const srcDir = path.join(__dirname, '../src');
  const files = scanDirectory(srcDir);
  
  console.log(`找到 ${files.length} 个文件需要检查`);
  
  const results = {};
  
  files.forEach(filePath => {
    try {
      const content = fs.readFileSync(filePath, 'utf-8');
      const result = checkApiCalls(content, filePath);
      
      if (result.issues.length > 0 || result.apiCalls.length > 0) {
        results[filePath.replace(__dirname + '/../', '')] = result;
      }
    } catch (error) {
      console.warn(`⚠️ 无法读取文件: ${filePath}`);
    }
  });
  
  generateReport(results);
}

// 运行脚本
if (require.main === module) {
  main();
}

module.exports = { checkApiCalls, scanDirectory, generateReport };
