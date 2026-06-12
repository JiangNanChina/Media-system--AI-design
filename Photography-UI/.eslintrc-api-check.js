/**
 * ESLint 自定义规则：检查API调用中的重复前缀
 * 
 * 使用方法：
 * 1. 将此文件复制到项目根目录
 * 2. 在 .eslintrc.js 中添加此规则
 * 3. 运行 eslint 检查
 */

module.exports = {
  rules: {
    'no-duplicate-api-prefix': {
      meta: {
        type: 'problem',
        docs: {
          description: '禁止在API调用中使用重复的/api前缀',
          category: 'Best Practices',
          recommended: true
        },
        fixable: 'code',
        schema: []
      },
      create(context) {
        return {
          CallExpression(node) {
            // 检查 request.get('/api/...'), request.post('/api/...') 等
            if (
              node.callee &&
              node.callee.type === 'MemberExpression' &&
              node.callee.object &&
              node.callee.object.name === 'request' &&
              node.callee.property &&
              ['get', 'post', 'put', 'delete', 'patch'].includes(node.callee.property.name)
            ) {
              const firstArg = node.arguments[0];
              if (
                firstArg &&
                firstArg.type === 'Literal' &&
                typeof firstArg.value === 'string' &&
                firstArg.value.startsWith('/api/')
              ) {
                context.report({
                  node: firstArg,
                  message: `API调用不应包含 '/api' 前缀，因为 baseURL 已包含。当前: '{{path}}'，建议: '{{suggested}}'`,
                  data: {
                    path: firstArg.value,
                    suggested: firstArg.value.replace('/api', '')
                  },
                  fix(fixer) {
                    const newValue = firstArg.value.replace('/api', '');
                    return fixer.replaceText(firstArg, `'${newValue}'`);
                  }
                });
              }
            }

            // 检查模板字符串中的重复前缀
            if (
              node.callee &&
              node.callee.type === 'MemberExpression' &&
              node.callee.object &&
              node.callee.object.name === 'request' &&
              node.callee.property &&
              ['get', 'post', 'put', 'delete', 'patch'].includes(node.callee.property.name)
            ) {
              const firstArg = node.arguments[0];
              if (
                firstArg &&
                firstArg.type === 'TemplateLiteral' &&
                firstArg.quasis.length > 0 &&
                firstArg.quasis[0].value.raw.startsWith('/api/')
              ) {
                context.report({
                  node: firstArg,
                  message: `API调用不应包含 '/api' 前缀，因为 baseURL 已包含。请检查模板字符串。`,
                });
              }
            }
          }
        };
      }
    }
  }
};
