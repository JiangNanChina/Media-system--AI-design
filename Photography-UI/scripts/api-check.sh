#!/bin/bash

# API调用检查脚本 (简化版)
# 
# 功能：检查项目中可能的重复API前缀问题
#
# 使用方法：
# bash scripts/api-check.sh

echo "🔍 检查API调用规范..."
echo "================================"

# 检查重复的/api前缀
echo "📋 检查重复的/api前缀:"
duplicate_count=$(grep -r "'/api/" src/ 2>/dev/null | wc -l)
if [ $duplicate_count -gt 0 ]; then
    echo "❌ 发现 $duplicate_count 个可能的重复前缀:"
    grep -r "'/api/" src/ --include="*.vue" --include="*.js" --include="*.ts" -n
else
    echo "✅ 未发现重复的/api前缀"
fi

echo ""

# 统计API调用
echo "📊 API调用统计:"
get_count=$(grep -r "request\.get" src/ 2>/dev/null | wc -l)
post_count=$(grep -r "request\.post" src/ 2>/dev/null | wc -l)
put_count=$(grep -r "request\.put" src/ 2>/dev/null | wc -l)
delete_count=$(grep -r "request\.delete" src/ 2>/dev/null | wc -l)

echo "GET请求: $get_count"
echo "POST请求: $post_count"
echo "PUT请求: $put_count"
echo "DELETE请求: $delete_count"

total=$((get_count + post_count + put_count + delete_count))
echo "总计: $total 个API调用"

echo ""
echo "💡 如发现问题，请参考: API_STANDARDS.md"
echo "✅ 检查完成"
