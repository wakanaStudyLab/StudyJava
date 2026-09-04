# Java サンプルのコンパイル & 実行一括スクリプト
$ErrorActionPreference = "Stop"

# UTF-8 出力を設定
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "===> 1. Compiling Java Source Files (Java 26 + Preview Enabled)..." -ForegroundColor Cyan
if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

javac -encoding UTF-8 --enable-preview --release 26 -d bin src/sample/*.java

Write-Host "`n===> 2. Running Modern Java Sample (sample.Main)..." -ForegroundColor Cyan
java --enable-preview -cp bin sample.Main
