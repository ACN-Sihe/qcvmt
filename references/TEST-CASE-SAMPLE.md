| Module | Scenario | Title | Priority | Test Type | Preconditions | Test Steps | Expected Result | Platform |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 用户管理 | 普通用户登录 | 普通用户使用正确用户名和密码登录 | P0 | Functional | 已注册普通用户账号：{{username}} / {{password}}；账号未锁定；网络正常 | 1. 打开登录页面{{login\_url}}；2. 输入用户名{{username}}，密码{{password}}；3. 点击登录按钮 | 1. 正常显示登录页面；2. 输入框正常接收输入；3. 登录成功，跳转到{{homepage\_url}}或用户中心，显示用户已登录状态 | Web |
