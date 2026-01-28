[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
./java.exe --% -Denv.file.location=./.env -Dspring.config.location=./application.yml -jar ./comfy-pilot-app.jar