#!/bin/bash

# VNC_SERVER 환경 변수가 없으면 기본값으로 localhost:5900을 사용합니다.
# 예: -e VNC_SERVER=172.17.0.2:5901
VNC_SERVER=${VNC_SERVER:-"localhost:5900"}

# LISTEN_PORT 환경 변수가 없으면 기본값으로 80 포트를 사용합니다.
# 예: -e LISTEN_PORT=8080
LISTEN_PORT=${LISTEN_PORT:-"80"}

echo "Starting WebSockify server..."
echo " - Listening on port: ${LISTEN_PORT}"
echo " - Proxying to VNC server: ${VNC_SERVER}"

# websockify를 실행합니다.
# --web 옵션으로 /opt/novnc 폴더의 웹 파일(index.html 등)을 서비스합니다.
exec websockify --web /opt/novnc ${LISTEN_PORT} ${VNC_SERVER}