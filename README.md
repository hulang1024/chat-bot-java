# chat-bot

## 构建
1. `Project Structure` > `Artifacts`，点击 增加，选择 `Jar` > `From modules with dependencies...`。
2. 填写表单：
    ```
    Module: chat-bot.main
    Jar files from libraries: copy to the ...（第二项）
    Directory from META-INF/MANIFEST.MF: ...\src\main\resources
    ```
3. `Build` > `Build Artifacts`
4. 可能需要手动将 META-INF 放入主jar中。
5. 将config.properties 放入jar的同级目录。