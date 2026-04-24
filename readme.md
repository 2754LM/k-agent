## 介绍
基于spring ai+mybatis plus+ pgvector的agent智能体项目

代码库主要是后端部分，前端是简陋的demo测试功能。

## 现有功能：
1. react模式循环
2. function tool与外部mcp工具调用
3. rag检索增强，基于pgvector的向量数据库检索增强，目前只支持markwodn解析，采用多层embedding
4. sse流式传输
5. 会话总结压缩

## 改进方向
rag做混合检索和重排。目前是把rag封装成了tool，所以可以实现混合rag（具体可以百度两步rag与agent rag)

此外代码向量数据库、记忆存储等都是手动实现，之后可以试试advisor 

目前是单agent操作，压缩功能可以调其他小模型来做，可以试试subagent实现

增加skill支持，然后加点外部脚本的工具调用，然后封装到docker里。

## 杂谈
下面是学习过程中遇到的一些坑

sse无法识别空格，前端只能get。解决方案，空格换成占位符，然后分开请求，一个get单独建sse，用户发信息单独走post直接返回，异步sse再给结果。

bing网络查询结果很差，用了tavily这种针对ai优化的搜索引擎，直接返回与 Query 相关的网页正文片段，无需二次抓取

md解析的表格，直接解析语义不全，比如表格ab的对比，直接查询ab有什么区别，无法理解。于是拆成a的特性有哪些，b的特性有哪些。

