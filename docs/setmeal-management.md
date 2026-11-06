# 套餐管理

依据 day04 的《项目实战-套餐管理.md》及同目录参考答案业务规则实现；接口路径、参数和字段对照 day01/项目接口/苍穹外卖-管理端接口.json。

## 接口

所有接口沿用 `Result`：`code=1` 成功，`code=0` 为业务失败，`msg` 返回原因。管理端请求需要登录取得的 `token` 请求头。

| 方法 | 路径 | 参数及结果 |
| --- | --- | --- |
| POST | /admin/setmeal | JSON SetmealDTO，新增套餐及菜品关联 |
| GET | /admin/setmeal/page | page、pageSize 必传；name、categoryId、status 可选；返回 total、records |
| GET | /admin/setmeal/{id} | 返回套餐、categoryName、setmealDishes，供编辑回显 |
| PUT | /admin/setmeal | JSON SetmealDTO，需包含 id；替换整份套餐菜品列表 |
| DELETE | /admin/setmeal | ids=1,2，单条和批量均支持 |
| POST | /admin/setmeal/status/{status} | 查询参数 id，status 为 0 停售或 1 起售 |
| GET | /admin/dish/list | categoryId 必传，仅返回该分类下起售的菜品 |

## 业务规则和表关系

- `category` 的 type=2 为套餐分类，通过 `setmeal.category_id` 关联；type=1 为菜品分类。
- `setmeal` 与 `dish` 通过 `setmeal_dish` 构成多对多关系，关系表保存菜品份数和名称、价格快照。
- `dish_flavor` 是菜品口味，通过 dish_id 关联菜品。套餐操作不更改菜品和口味数据。
- 套餐名称唯一；名称、分类、价格、图片必填，价格非负；必须至少包含一道存在的菜品，份数为正且菜品不重复。
- 冗余的菜品名称和价格从数据库读取，套餐 id 使用数据库生成值，忽略客户端传入的关系主键和套餐 id。
- 新增默认停售。接口 JSON 中虽列出 status 字段，新增仍按产品业务规则强制置为 0。
- 起售前检查所有关联菜品均存在且起售；编辑后仍为起售状态时也执行此检查。
- 起售套餐禁止删除；批量删除先校验全部目标。新增、修改、删除和状态变更使用 Spring 事务。
- 套餐及关联增删改保存在同一事务中；沿用 AutoFill 填充创建/修改时间和操作人。

## 调试步骤

启动 MySQL、后端和前端 Nginx，在 `http://localhost:8080/doc.html` 查看“套餐相关接口”。先调用员工登录，复制 token，在管理接口的请求头填写 token。前端经 Nginx 使用 `/api/setmeal`，对应后端 `/admin/setmeal`。

新增请求示例（请将分类和菜品 id 替换为数据库现有记录）：

```json
{
  "categoryId": 2,
  "name": "双人套餐",
  "price": 38.00,
  "image": "https://example.com/setmeal.png",
  "description": "两道菜组合",
  "status": 0,
  "setmealDishes": [{"dishId": 10, "copies": 2, "name": "菜品名称", "price": 20.00}]
}
```

1. 新增后分页查询，确认默认停售、分类名称、价格和更新时间；按名称、分类、状态筛选。
2. 查询详情，修改价格、描述和菜品份数后保存；再次查询，确认关联已替换而非追加。
3. 套餐关联菜品全部起售时，执行套餐起售并尝试删除，预期提示“起售中的套餐不能删除”。
4. 将任一关联菜品在数据库或菜品模块停售，再起售套餐，预期提示“套餐内包含未启售菜品，无法启售”。
5. 测试重复名称、空菜品列表、非法状态、无效分类、负数价格、零份数；预期业务失败且数据不发生部分写入。
6. 测试同时批量删除起售、停售套餐，应整体失败；全部停售后删除，确认套餐和关联表都删除。
7. 在前端套餐管理页执行新增、分页筛选、修改回显、起售停售、单条及批量删除，检查浏览器 Network 返回与提示。

## 自动验证与限制

执行 `mvn test`，覆盖服务业务规则、MockMvc 的 7 个接口参数/响应，以及 MyBatis XML 加载和动态 SQL 绑定。测试使用模拟 Mapper，不需要数据库，也不更改业务数据。

当前未启动本地 MySQL、后端和 Nginx，尚未完成真实 MySQL 的事务回滚验证、Swagger 页面调试及浏览器前后端联调；以上步骤供环境启动后执行。
