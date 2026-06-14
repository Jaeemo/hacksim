FROM ubuntu:22.04
ENV DEBIAN_FRONTEND=noninteractive

# --- 패키지 설치 ---
# APT 소스를 한국 미러로 변경하여 다운로드 속도 향상
RUN sed -i 's|http://archive.ubuntu.com/ubuntu|http://kr.archive.ubuntu.com/ubuntu|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com/ubuntu|http://kr.archive.ubuntu.com/ubuntu|g' /etc/apt/sources.list

# [수정] websockify만 설치합니다. (novnc는 직접 복사하므로)
# [수정] Python 관련 패키지는 현재 Dockerfile의 목적에 불필요하므로 제거하여 이미지 크기를 줄입니다.
RUN apt-get update && apt-get install -y \
    websockify \
    && rm -rf /var/lib/apt/lists/*

# --- noVNC 파일 복사 및 설정 ---
WORKDIR /opt/novnc

# [설명] 현재 폴더의 모든 noVNC 관련 파일 (index.html, app 폴더 등)을 이미지 안으로 복사합니다.
COPY ./ /opt/novnc/

# [개선] echo로 스크립트를 생성하는 대신, 별도의 launch.sh 파일을 복사하여 가독성과 관리 용이성을 높입니다.
COPY launch.sh /opt/novnc/

# 실행 권한 부여
RUN chmod +x /opt/novnc/launch.sh

# --- 컨테이너 실행 ---
# 컨테이너가 80번 포트를 외부에 노출시킵니다.
EXPOSE 80

# 컨테이너 시작 시 launch.sh 스크립트를 실행합니다.
CMD ["/opt/novnc/launch.sh"]