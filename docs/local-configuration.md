# 本地凭据配置

数据库密码和阿里云 OSS 密钥不提交到 Git。`application-dev.yml` 默认读取 `DB_PASSWORD`、`ALIYUN_ACCESS_KEY_ID`、`ALIYUN_ACCESS_KEY_SECRET` 环境变量。

当前电脑的原配置已保存在项目根目录 `.local/application-secrets.yml`，该目录被 Git 忽略。应用从项目根目录或 sky-server 目录运行时自动加载该文件，因此原有本地连接配置继续生效。

换电脑时可设置上述环境变量，或者在项目根目录自行创建 `.local/application-secrets.yml`：

```yaml
sky:
  datasource:
    password: 填写本机数据库密码
  alioss:
    access-key-id: 填写自己的AccessKey
    access-key-secret: 填写自己的Secret
```

本地文件会覆盖 application-dev.yml 中的对应值；如果选择环境变量方式，请移除本地文件中的对应字段。

如密钥曾出现在提交或日志中，应到阿里云控制台禁用旧密钥并生成新密钥，然后更新本地配置。

`codex/backup-before-secret-cleanup-20261106` 是修复前历史的本地备份分支，包含旧凭据，仅供恢复使用，请勿推送此分支或使用 `git push --all`。
